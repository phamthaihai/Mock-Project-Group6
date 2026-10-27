/* ===== CONFIG: set USE_MOCK=false when the Spring Boot backend is ready ===== */
const CONFIG = { USE_MOCK: localStorage.getItem('learnpath-mode') !== 'live', API_BASE: '/api' };
const ICON = { Programming: '💻', Data: '📊', Design: '🎨', Business: '💼', Languages: '🌍', Marketing: '📣' };
const CATS = Object.keys(ICON); // seed categories only; the real list comes from GET /categories
const $ = s => document.querySelector(s), $$ = s => document.querySelectorAll(s);
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const fmt = d => new Date(d).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
const formData = f => Object.fromEntries(new FormData(f));
const state = { user: JSON.parse(localStorage.getItem('user') || 'null'), token: localStorage.getItem('token') };

/* ===== popups ===== */
const TOAST = { success: ['check-circle-fill', 'Success'], danger: ['x-circle-fill', 'Error'], warning: ['exclamation-triangle-fill', 'Heads up'], info: ['info-circle-fill', 'Notice'] };
function toast(msg, type = 'success') {
  const [icon, title] = TOAST[type] || TOAST.info, el = document.createElement('div');
  el.className = 'lh-toast lh-' + type;
  el.innerHTML = `<i class="bi bi-${icon}"></i><div class="lh-body"><b>${title}</b><span>${esc(msg)}</span></div><button class="lh-x" aria-label="Close">&times;</button><i class="lh-bar"></i>`;
  const close = () => { el.classList.add('out'); setTimeout(() => el.remove(), 300); };
  el.querySelector('.lh-x').onclick = close; $('#toasts').append(el); setTimeout(close, 1800);
}
// Styled replacement for confirm(): if (!await confirmBox('Delete?', { title: 'Delete', confirmText: 'Delete' })) return;
function confirmBox(message, { title = 'Are you sure?', confirmText = 'Confirm', danger = true } = {}) {
  return new Promise(resolve => {
    const el = $('#confirmModal'), m = bootstrap.Modal.getOrCreateInstance(el), ok = $('#cfOk'); let result = false;
    $('#cfTitle').textContent = title; $('#cfMsg').textContent = message; ok.textContent = confirmText; ok.className = 'btn ' + (danger ? 'btn-danger' : 'btn-primary');
    $('#cfIcon').className = 'cf-icon ' + (danger ? 'danger' : 'primary'); $('#cfIcon i').className = 'bi ' + (danger ? 'bi-exclamation-triangle-fill' : 'bi-question-circle-fill');
    ok.onclick = () => { result = true; m.hide(); };
    el.addEventListener('hidden.bs.modal', () => resolve(result), { once: true }); m.show();
  });
}
const flash = (msg, type = 'success') => sessionStorage.setItem('flash', JSON.stringify([msg, type])); // shown on the next page

/* ===== session ===== */
function setSession(user, token) {
  state.user = user; state.token = token;
  localStorage.setItem('user', JSON.stringify(user)); localStorage.setItem('token', token);
}
function logout() { localStorage.removeItem('user'); localStorage.removeItem('token'); flash('Logged out', 'info'); location.href = 'index.html'; }
function guard(...roles) {
  if (!state.user) { location.href = 'login.html?next=' + encodeURIComponent(location.pathname.split('/').pop() + location.search); return false; }
  if (roles.length && !roles.includes(state.user.role)) { flash('You do not have access to that page', 'danger'); location.href = 'index.html'; return false; }
  return true;
}

/* ===== API LAYER ===== */
async function api(path, method = 'GET', body) {
  if (CONFIG.USE_MOCK) { await new Promise(r => setTimeout(r, 100)); return mock(method, path, body); }
  const res = await fetch(CONFIG.API_BASE + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(state.token && { Authorization: 'Bearer ' + state.token }) },
    body: body ? JSON.stringify(body) : undefined
  });
  if (!res.ok) { const e = await res.json().catch(() => ({})); throw { status: res.status, message: e.message || res.statusText }; }
  return res.status === 204 ? null : res.json();
}

/* ===== helpers ===== */
const money = n => n > 0 ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n) : 'Free';
const isoLocal = d => { const p = x => String(x).padStart(2, '0'); return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:00`; };
const stars = r => '★'.repeat(Math.round(r)) + '☆'.repeat(5 - Math.round(r));
const catClass = c => 'c-' + String(c).replace(/[^A-Za-z]/g, '');
const fallbackImg = c => 'https://picsum.photos/seed/' + encodeURIComponent(c.category + c.courseId) + '/1200/700';
// Last-resort artwork (inline, so it can never fail to load)
const FALLBACK_SVG = 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 720 420'><defs><linearGradient id='g' x1='0' y1='0' x2='1' y2='1'><stop stop-color='#24235d'/><stop offset='1' stop-color='#087e8b'/></linearGradient></defs><rect width='720' height='420' fill='url(#g)'/><g fill='none' stroke='#fff' stroke-opacity='.3' stroke-width='3'><path d='M0 70H720M0 140H720M0 210H720M0 280H720M0 350H720M80 0V420M160 0V420M240 0V420M320 0V420M400 0V420M480 0V420M560 0V420M640 0V420'/></g><g transform='translate(360 165)' fill='none' stroke='#fff' stroke-width='9' stroke-linecap='round' stroke-linejoin='round'><path d='M-90 5 0-40 90 5 0 50Z'/><path d='M-58 30v48q58 34 116 0V30M90 5v78'/><circle cx='90' cy='98' r='8' fill='#fff' stroke='none'/></g><text x='360' y='330' text-anchor='middle' fill='#fff' font-family='Arial,sans-serif' font-size='26' font-weight='700'>SKILLTRAIL</text></svg>");
// onerror handler: 1) course image -> 2) seeded stock photo -> 3) inline artwork
function imgFallback(img) {
  if (img.dataset.step === '2') { img.onerror = null; return; }
  if (img.dataset.step !== '1' && img.dataset.fallback) { img.dataset.step = '1'; img.src = img.dataset.fallback; return; }
  img.dataset.step = '2'; img.src = FALLBACK_SVG;
}
const imgTag = (c, cls = '', alt = '') => `<img ${cls ? `class="${cls}" ` : ''}loading="lazy" src="${esc(c.imageUrl || fallbackImg(c))}" data-fallback="${esc(fallbackImg(c))}" alt="${esc(alt)}" onerror="imgFallback(this)">`;
const LICON = { VIDEO: 'play-circle', TEXT: 'file-text', FILE: 'file-earmark-arrow-down' };
const SBADGE = { DRAFT: 'secondary', PENDING: 'warning text-dark', PUBLISHED: 'success', REJECTED: 'danger' };
const statusBadge = s => `<span class="badge bg-${SBADGE[s]}">${s}</span>`;
const safeUrl = u => /^https?:\/\//i.test(u || '') ? u : '#';
const textHtml = t => String(t || '').split(/\n\n+/).filter(Boolean).map(p => p.startsWith('## ') ? '<h6 class="mt-3">' + esc(p.slice(3)) + '</h6>'
  : p.startsWith('- ') ? '<ul class="checks">' + p.split('\n').map(x => '<li><i class="bi bi-dot"></i> ' + esc(x.replace(/^- /, '')) + '</li>').join('') + '</ul>' : '<p>' + esc(p).replace(/\n/g, '<br>') + '</p>').join('');
function lessonHtml(l) {
  const yt = (l.contentUrl || '').match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([\w-]{11})/); let media = '';
  if (l.type === 'VIDEO') media = yt ? `<div class="ratio ratio-16x9 mb-3"><iframe src="https://www.youtube.com/embed/${yt[1]}" allowfullscreen></iframe></div>`
    : `<a class="player mb-3 text-decoration-none" href="${esc(safeUrl(l.contentUrl))}" target="_blank" rel="noopener"><i class="bi bi-play-circle-fill"></i><span>Open video: ${esc(l.title)}</span></a>`;
  if (l.type === 'FILE') media = `<a class="btn btn-outline-light mb-3" href="${esc(safeUrl(l.contentUrl))}" target="_blank" rel="noopener"><i class="bi bi-file-earmark-arrow-down"></i> Download file</a>`;
  return media + textHtml(l.text);
}

const UI_CSS = `
.trophy { width: 96px; height: 96px; margin: 0 auto 1rem; display: grid; place-items: center; border-radius: 50%; font-size: 2.6rem; color: #fff; background: linear-gradient(135deg, #f59e0b, #fbbf24); box-shadow: 0 0 0 10px rgba(245, 158, 11, 0.15); }
.confetti { position: fixed; inset: 0; pointer-events: none; overflow: hidden; z-index: 2000; }
.confetti i { position: absolute; top: -20px; width: 9px; height: 14px; border-radius: 2px; animation: confFall linear forwards; }
@keyframes confFall { to { top: 105vh; margin-left: 60px; } }
.lh-toast {
    position: relative;
    display: flex;
    align-items: flex-start;
    gap: 0.8rem;
    min-width: 300px;
    max-width: 380px;
    margin-bottom: 0.75rem;
    padding: 0.9rem 2.2rem 0.9rem 1rem;
    border-radius: 14px;
    background: rgba(20, 22, 48, 0.96);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.45);
    overflow: hidden;
    animation: lhIn 0.35s ease;
    transition: opacity 0.3s, transform 0.3s;
}
.lh-toast.out { opacity: 0; transform: translateX(30px); }
.lh-toast > i:first-child { font-size: 1.5rem; margin-top: 2px; }
.lh-body { display: flex; flex-direction: column; line-height: 1.35; }
.lh-body b { font-size: 0.9rem; }
.lh-body span { font-size: 0.85rem; color: #cbd5ff; }
.lh-x { position: absolute; top: 0.35rem; right: 0.7rem; border: 0; background: none; color: #8b93b8; font-size: 1.3rem; line-height: 1; }
.lh-bar { position: absolute; left: 0; bottom: 0; display: block; width: 100%; height: 3px; transform-origin: left; animation: lhBar 4s linear forwards; }
.lh-success > i:first-child { color: #22c55e; } .lh-success .lh-bar { background: #22c55e; }
.lh-danger > i:first-child { color: #ef4444; } .lh-danger .lh-bar { background: #ef4444; }
.lh-warning > i:first-child { color: #f59e0b; } .lh-warning .lh-bar { background: #f59e0b; }
.lh-info > i:first-child { color: #22d3ee; } .lh-info .lh-bar { background: #22d3ee; }
@keyframes lhIn { from { opacity: 0; transform: translateX(40px); } to { opacity: 1; transform: none; } }
@keyframes lhBar { to { transform: scaleX(0); } }
@media (max-width: 576px) { .lh-toast { min-width: 0; max-width: none; width: calc(100vw - 2rem); } }
#confirmModal .modal-content { border-radius: 20px; background: rgba(20, 22, 48, 0.97); border: 1px solid rgba(255, 255, 255, 0.1); }
.cf-icon { width: 64px; height: 64px; margin: 0 auto 1rem; display: grid; place-items: center; border-radius: 50%; font-size: 1.8rem; }
.cf-icon.danger { background: rgba(239, 68, 68, 0.15); color: #ef4444; }
.cf-icon.primary { background: rgba(109, 93, 252, 0.15); color: #a5b4fc; }
.admin-pill, .instr-pill { display: inline-block; margin-left: 0.4rem; padding: 0.15rem 0.6rem; border-radius: 50rem; font-size: 0.7rem; font-weight: 800; letter-spacing: 0.05em; color: #1a1040; }
.admin-pill { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.instr-pill { background: linear-gradient(135deg, #22d3ee, #67e8f9); }
.admin-nav { border-bottom: 2px solid #f59e0b !important; }
.instr-nav { border-bottom: 2px solid #22d3ee !important; }
.player { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 0.5rem; aspect-ratio: 16 / 9; border-radius: 14px; background: linear-gradient(135deg, #1a1040, #2a3358); color: #cbd5ff; }
.player i { font-size: 3.5rem; color: var(--brand2); }
.lesson.active { background: rgba(109, 93, 252, 0.22); color: #fff; }
.qopt { cursor: pointer; }
.qopt.ok { color: #22c55e; font-weight: 600; }
.qopt.bad { color: #ef4444; }
.starpick i { cursor: pointer; font-size: 1.5rem; color: #fbbf24; }
.notif { padding: 0.7rem 1rem; border-bottom: 1px solid rgba(255, 255, 255, 0.08); font-size: 0.85rem; }
.notif.unread { background: rgba(109, 93, 252, 0.15); }
`;

/* ===== course completion helpers (congrats.html, quiz.html) ===== */
const isDone = e => !!e && (Number(e.percent ?? e.progressPercent ?? 0) >= 100 || e.completed === true || e.status === 'COMPLETED');
// Up to n courses the learner is not enrolled in; same category as finished courses first, then best rated.
function suggestNext(courses, mine, n = 3) {
  const taken = new Set((mine || []).map(e => Number(e.courseId))), cats = new Set((mine || []).filter(isDone).map(e => e.category).filter(Boolean));
  return (courses || []).filter(c => !taken.has(Number(c.courseId)) && c.status !== 'DRAFT')
    .map(c => ({ c, same: cats.has(c.category) })).sort((a, b) => (b.same - a.same) || ((b.c.rating || 0) - (a.c.rating || 0))).slice(0, n)
    .map(x => ({ c: x.c, why: x.same ? 'More in ' + x.c.category : 'Top rated' }));
}
const sugCard = (c, why) => `<div class="col-sm-6 col-lg-4"><a href="course.html?id=${c.courseId}" class="text-reset text-decoration-none"><div class="card ev h-100 glass">
  <div class="ev-img ${catClass(c.category)}">${imgTag(c)}<span class="ev-cat">${ICON[c.category] || '📚'} ${esc(c.category)}</span></div>
  <div class="card-body"><h5>${esc(c.title)}</h5><p class="small text-secondary mb-2"><i class="bi bi-person-video3"></i> ${esc(c.instructor?.fullName || c.instructor || '')}</p>
  <span class="badge text-bg-dark">${esc(why)}</span> <span class="float-end fw-bold">${money(c.price)}</span></div></div></a></div>`;
const certDate = e => new Date(e.completedAt || Date.now()).toLocaleDateString([], { year: 'numeric', month: 'long', day: 'numeric' });
const certId = e => 'ST-' + String(e.courseId).padStart(3, '0') + '-' + String(state.user?.userId ?? 0).slice(-4).padStart(4, '0');
const certificateHtml = e => `<div class="cert"><div class="cert-in">
  <div class="cert-brand"><i class="bi bi-mortarboard-fill"></i> Skill<span>Trail</span></div>
  <div class="cert-small">CERTIFICATE OF COMPLETION</div><div class="cert-line">This certifies that</div>
  <div class="cert-name">${esc(e.studentName || state.user?.fullName || 'Learner')}</div>
  <div class="cert-line">has successfully completed the course</div><div class="cert-course">${esc(e.title)}</div>
  <div class="cert-foot"><div><b>${certDate(e)}</b><small>Date</small></div><div><b>${certId(e)}</b><small>Certificate ID</small></div></div></div></div>`;
function printCertificate(e) {
  const w = open('', '_blank'); if (!w) { toast('Allow pop-ups to print your certificate', 'warning'); return; }
  w.document.write('<!doctype html><title>Certificate - ' + esc(e.title) + '</title><style>' + CERT_CSS + 'body{margin:0;padding:24px;background:#fff;font-family:Poppins,Arial,sans-serif}@page{size:landscape;margin:12mm}</style>' + certificateHtml(e) + '<script>onload=()=>setTimeout(()=>print(),300)<\/script>');
  w.document.close();
}
function confetti() {
  if (matchMedia('(prefers-reduced-motion: reduce)').matches) return;
  const box = document.createElement('div'); box.className = 'confetti'; const colors = ['#6d5dfc', '#22d3ee', '#f59e0b', '#22c55e', '#ec4899'];
  for (let i = 0; i < 80; i++) { const b = document.createElement('i'); b.style.cssText = `left:${Math.random() * 100}%;background:${colors[i % 5]};animation-delay:${Math.random() * 1.2}s;animation-duration:${2.4 + Math.random() * 2}s;transform:rotate(${Math.random() * 360}deg)`; box.append(b); }
  document.body.append(box); setTimeout(() => box.remove(), 5500);
}
const CERT_CSS = `.cert{border:3px solid #6d5dfc;border-radius:18px;padding:10px;background:#fff;color:#1a1040}.cert-in{border:1px solid #c7c2ff;border-radius:12px;padding:2.2rem 1.5rem;text-align:center}
.cert-brand{font-weight:800;font-size:1.3rem}.cert-brand span{color:#6d5dfc}.cert-small{letter-spacing:.3em;font-size:.8rem;font-weight:700;color:#6d5dfc;margin:1.2rem 0 1rem}.cert-line{color:#555}
.cert-name{font-size:2.3rem;font-weight:800;margin:.4rem 0;border-bottom:2px solid #6d5dfc;display:inline-block;padding:0 1.5rem}.cert-course{font-size:1.5rem;font-weight:700;margin:.5rem 0 1.5rem}
.cert-foot{display:flex;justify-content:space-around;margin-top:1.5rem}.cert-foot b{display:block}.cert-foot small{color:#777}`;

const NAMES = ['Minh Anh Pham', 'Daniel Kim', 'Hoa Nguyen', 'Sofia Alvarez', 'Tuan Le', 'Aisha Khan', 'Lucas Meyer', 'Thao Vo', 'Priyank Shah', 'Linh Dang'];
const TPL = [
  { r: 5, t: c => 'Great course! ' + c.instructor + ' explains everything step by step and the examples are easy to follow.' },
  { r: 5, t: c => 'Exactly what I needed for my job. I applied the lessons to a real project within a week.' },
  { r: 5, t: c => 'The curriculum is well structured and the practice tasks really help the ideas stick.' },
  { r: 4, t: c => 'Good value for the price. I would love a few more advanced exercises, but overall very satisfied.' },
  { r: 5, t: c => 'I started as a complete beginner and finished with real confidence. Highly recommended!' },
  { r: 4, t: c => 'Clear, practical and well paced. The slides and resources are a nice bonus.' },
  { r: 3, t: c => 'Solid content. A few sections moved quickly for me, but replaying the lessons helped.' },
  { r: 5, t: c => 'Best online course I have taken this year. The instructor really knows the subject.' },
  { r: 5, t: c => 'The ' + c.title + ' lessons are organised logically, so I always knew what to learn next.' },
  { r: 4, t: c => 'Friendly teaching style and useful practice tasks. I finished the whole course in two weeks.' }
];
/* ===== MOCK BACKEND (localStorage). Delete when the real API is live. Response shapes = API.md ===== */
function seed(oldUsers) {
  let seedN = 11; const rnd = () => (seedN = seedN * 16807 % 2147483647) / 2147483647;
  const shuffle = a => a.map(v => [rnd(), v]).sort((x, y) => x[0] - y[0]).map(x => x[1]);
  const users = (oldUsers || []).map(u => ({ ...u, role: u.role === 'USER' ? 'STUDENT' : u.role, locked: !!u.locked }));
  let maxId = Math.max(100, ...users.map(u => u.userId));
  const account = (email, fullName, role, pw = '123456') => { let u = users.find(x => x.email === email); if (!u) users.push(u = { userId: ++maxId, fullName, email, password: pw, role, locked: false }); return u; };
  const admin = account('admin@learnpath.com', 'Admin', 'ADMIN', 'admin123'), student = account('student@learnpath.com', 'Demo Student', 'STUDENT');
  const slug = s => s.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
  const raw = [
    {
      title: 'Java Full-Stack Bootcamp', category: 'Programming', level: 'Intermediate', instructor: 'Nguyen Van An', hours: 48, lang: 'English', price: 1290000, rating: 4.8, students: 120, kw: 'coding,programming',
      description: 'Go from Java basics to a deployed full-stack app: Spring Boot REST APIs, SQL Server, JWT security and a responsive front-end.', tagline: 'Build and ship a real full-stack application.',
      learn: ['Design REST APIs with Spring Boot', 'Model data with JPA and SQL Server', 'Secure apps with JWT and roles', 'Build a responsive front-end with fetch'], req: ['Basic programming knowledge', 'A laptop with JDK 21 installed'],
      skills: ['Spring Boot', 'REST', 'JPA', 'SQL Server', 'JWT', 'Git'], bio: 'Senior backend engineer with 10 years of experience building enterprise Java systems and mentoring junior developers.',
      cur: ['Java refresher::OOP essentials|18;Collections and streams|22;Exceptions and testing|15', 'Spring Boot REST::Controllers and services|25;Validation and error handling|20;Swagger documentation|12', 'Data and security::JPA with SQL Server|28;Authentication with JWT|30;Role-based access|18', 'Capstone::Connect the front-end|24;Deploy the app|16']
    },
    {
      title: 'Intro to Web Development', category: 'Programming', level: 'Beginner', instructor: 'Le Thu Ha', hours: 12, lang: 'English', price: 0, rating: 4.6, students: 300, kw: 'laptop,webdesign',
      description: 'Learn HTML, CSS and JavaScript from zero and publish your first responsive website.', tagline: 'Your first website in a weekend. Free.',
      learn: ['Structure pages with semantic HTML', 'Style layouts with CSS and Bootstrap', 'Add interactivity with JavaScript', 'Publish a site to the web'], req: ['No experience needed', 'Any computer and a browser'],
      skills: ['HTML', 'CSS', 'Bootstrap', 'JavaScript'], bio: 'Front-end developer and teacher who has helped over 5,000 beginners write their first line of code.',
      cur: ['HTML basics::Page structure|10;Forms and tables|14', 'CSS and Bootstrap::Selectors and layout|16;Responsive design|18', 'JavaScript::DOM and events|20;Fetching data|15']
    },
    {
      title: 'Docker & DevOps Essentials', category: 'Programming', level: 'Intermediate', instructor: 'Marcus Reyes', hours: 20, lang: 'English', price: 790000, rating: 4.7, students: 90, kw: 'server,datacenter',
      description: 'Containerize applications, automate builds and deploy with confidence using Docker, CI/CD and cloud basics.', tagline: 'Ship faster with containers and pipelines.',
      learn: ['Write Dockerfiles and compose files', 'Automate tests with CI/CD', 'Deploy containers to the cloud', 'Monitor and troubleshoot services'], req: ['Comfort with the command line', 'One programming language'],
      skills: ['Docker', 'CI/CD', 'Linux', 'Cloud'], bio: 'Cloud engineer who runs production platforms serving millions of requests per day.',
      cur: ['Containers::Images and containers|20;Dockerfile deep dive|25;Docker Compose|18', 'Pipelines::Git workflows|15;Build and test automation|28', 'Deploy::Cloud deployment|30;Monitoring basics|16']
    },
    {
      title: 'Machine Learning Foundations', category: 'Data', level: 'Intermediate', instructor: 'Dr. Linh Tran', hours: 36, lang: 'English', price: 1490000, rating: 4.9, students: 150, kw: 'artificialintelligence,technology',
      description: 'Understand the core ideas behind machine learning and build, evaluate and deploy your first models in Python.', tagline: 'From data to models that actually work.',
      learn: ['Prepare and explore real datasets', 'Train regression and classification models', 'Evaluate models and avoid overfitting', 'Serve a model behind a simple API'], req: ['Basic Python', 'High-school level maths'],
      skills: ['Python', 'scikit-learn', 'Pandas', 'Model evaluation'], bio: 'Machine learning researcher turned industry practitioner, with a PhD in applied statistics.',
      cur: ['Foundations::What is machine learning|12;Data preparation|26;Exploratory analysis|24', 'Core models::Linear regression|28;Classification|30;Trees and ensembles|32', 'In practice::Evaluation and tuning|26;Deploying a model|22']
    },
    {
      title: 'SQL & Data Analysis', category: 'Data', level: 'Beginner', instructor: 'Priya Nair', hours: 18, lang: 'English', price: 450000, rating: 4.6, students: 210, kw: 'data,analytics',
      description: 'Query, clean and summarise data with SQL, then turn results into clear dashboards and insights.', tagline: 'Ask better questions of your data.',
      learn: ['Write SELECT, JOIN and GROUP BY queries', 'Clean messy data', 'Design simple relational schemas', 'Present findings with charts'], req: ['No prior experience', 'SQL Server or any SQL database'],
      skills: ['SQL', 'T-SQL', 'Data cleaning', 'Dashboards'], bio: 'Data analyst who has built reporting for retail and fintech teams.',
      cur: ['SQL basics::SELECT and filtering|14;Joins|22;Aggregations|20', 'Working with data::Cleaning data|18;Subqueries and CTEs|24', 'Insights::Designing a schema|16;Building a dashboard|26']
    },
    {
      title: 'UI/UX Design Essentials', category: 'Design', level: 'Beginner', instructor: 'Mai Anh', hours: 24, lang: 'English', price: 790000, rating: 4.7, students: 130, kw: 'design,interface',
      description: 'Learn the full design process: research, wireframes, visual design and prototyping, with a portfolio project.', tagline: 'Design products people love to use.',
      learn: ['Run quick user research', 'Create wireframes and flows', 'Apply colour, type and layout', 'Prototype and test your design'], req: ['No experience needed', 'A free Figma account'],
      skills: ['Figma', 'Wireframing', 'Prototyping', 'Usability testing'], bio: 'Product designer with 8 years of experience across fintech and e-commerce apps.',
      cur: ['Research::Understanding users|15;Personas and journeys|18', 'Design::Wireframes|22;Visual design basics|26;Design systems|20', 'Deliver::Prototyping|24;Usability testing|18']
    },
    {
      title: 'Digital Marketing Masterclass', category: 'Marketing', level: 'Beginner', instructor: 'Khanh Vy', hours: 16, lang: 'English', price: 650000, rating: 4.5, students: 180, kw: 'marketing,socialmedia',
      description: 'Plan and run campaigns across search, social and email, and measure what actually drives growth.', tagline: 'Grow an audience and measure the results.',
      learn: ['Build a simple marketing funnel', 'Run search and social campaigns', 'Write emails that convert', 'Read analytics and report ROI'], req: ['No experience needed'],
      skills: ['SEO', 'Social ads', 'Email marketing', 'Analytics'], bio: 'Growth marketer who has scaled several local brands from zero to national reach.',
      cur: ['Strategy::Funnels and goals|14;Audience research|16', 'Channels::Search and SEO|24;Social media ads|26;Email marketing|18', 'Measure::Analytics and reporting|22']
    },
    {
      title: 'Business English for Professionals', category: 'Languages', level: 'Intermediate', instructor: 'Emma Walker', hours: 30, lang: 'English', price: 890000, rating: 4.8, students: 260, kw: 'meeting,office',
      description: 'Communicate clearly in meetings, emails and negotiations, with real workplace scenarios and feedback.', tagline: 'Speak and write with confidence at work.',
      learn: ['Run and join meetings in English', 'Write clear professional emails', 'Handle calls and small talk', 'Negotiate and present ideas'], req: ['Intermediate English (B1 or above)'],
      skills: ['Speaking', 'Email writing', 'Presenting', 'Negotiation'], bio: 'Certified English trainer with 12 years coaching teams at international companies.',
      cur: ['Everyday work::Meetings and discussions|20;Professional emails|18', 'Speaking up::Phone and video calls|22;Small talk and networking|16', 'Advanced::Presentations|26;Negotiation|24']
    },
    {
      title: 'Public Speaking & Presentation Skills', category: 'Business', level: 'Beginner', instructor: 'Samir Oduya', hours: 10, lang: 'English', price: 390000, rating: 4.7, students: 140, kw: 'speaker,presentation',
      description: 'Overcome nerves, structure a message and deliver talks and pitches that keep an audience engaged.', tagline: 'Say it clearly. Be remembered.',
      learn: ['Structure a talk with a clear message', 'Control nerves and body language', 'Design simple, effective slides', 'Handle questions with confidence'], req: ['No experience needed'],
      skills: ['Storytelling', 'Slide design', 'Confidence', 'Q&A handling'], bio: 'Keynote speaker and coach who has trained thousands of professionals.',
      cur: ['Foundations::Know your audience|12;Structuring your message|16', 'Delivery::Voice and body language|18;Managing nerves|14', 'Pitching::Designing slides|20;Handling Q&A|15']
    },
    {
      title: 'React for Modern Web Apps', category: 'Programming', level: 'Intermediate', instructor: 'Hoang Minh', hours: 28, lang: 'English', price: 990000, rating: 4.7, students: 140, kw: 'code,screen',
      description: 'Build fast, component-based interfaces with React, hooks and routing, and connect them to a REST API.', tagline: 'Build interactive UIs the modern way.',
      learn: ['Build UIs from reusable components', 'Manage state with hooks', 'Add routing and forms', 'Fetch and display data from an API'], req: ['Solid HTML, CSS and JavaScript', 'Node.js installed'],
      skills: ['React', 'Hooks', 'React Router', 'REST APIs'], bio: 'Front-end lead who has shipped React apps for startups and banks.',
      cur: ['React basics::JSX and components|18;Props and state|24;Handling events|16', 'Hooks and data::useEffect and data fetching|28;Forms and validation|22', 'Real apps::Routing|20;Build a mini dashboard|30']
    },
    {
      title: 'Data Visualization with Power BI', category: 'Data', level: 'Beginner', instructor: 'Nina Petrova', hours: 16, lang: 'English', price: 520000, rating: 4.6, students: 110, kw: 'dashboard,analytics',
      description: 'Connect data sources, model them and build interactive dashboards that tell a clear story.', tagline: 'Turn spreadsheets into dashboards people use.',
      learn: ['Import and clean data', 'Build relationships and measures', 'Design interactive dashboards', 'Publish and share reports'], req: ['Basic Excel skills'],
      skills: ['Power BI', 'DAX', 'Data modelling', 'Dashboards'], bio: 'BI consultant who has built reporting for logistics and retail teams.',
      cur: ['Getting started::Power BI overview|12;Connecting to data|18', 'Modelling::Relationships and tables|22;Intro to DAX|28', 'Dashboards::Visual best practices|20;Publishing reports|16']
    },
    {
      title: 'Graphic Design with Photoshop & Illustrator', category: 'Design', level: 'Beginner', instructor: 'Carlos Mendes', hours: 26, lang: 'English', price: 750000, rating: 4.7, students: 95, kw: 'illustration,designer',
      description: 'Learn the tools and design principles to create logos, posters and social media graphics.', tagline: 'Create visuals that stand out.',
      learn: ['Master layers, masks and vector shapes', 'Apply colour, type and composition', 'Design logos and brand assets', 'Prepare files for print and web'], req: ['A computer that can run design software'],
      skills: ['Photoshop', 'Illustrator', 'Branding', 'Typography'], bio: 'Brand designer with a decade of experience in agencies and freelance work.',
      cur: ['Design basics::Colour and typography|18;Composition|16', 'Photoshop::Layers and masks|26;Photo retouching|24', 'Illustrator::Vector shapes|22;Designing a logo|28']
    },
    {
      title: 'Project Management Fundamentals', category: 'Business', level: 'Beginner', instructor: 'Olivia Grant', hours: 14, lang: 'English', price: 590000, rating: 4.6, students: 125, kw: 'teamwork,planning',
      description: 'Plan, run and deliver projects on time using proven methods, from scope and schedule to risks and reporting.', tagline: 'Deliver projects on time and on budget.',
      learn: ['Define scope, goals and deliverables', 'Build realistic schedules and budgets', 'Manage risks and stakeholders', 'Run projects with Agile and Kanban'], req: ['No experience needed'],
      skills: ['Planning', 'Agile', 'Risk management', 'Stakeholders'], bio: 'PMP-certified project manager who has led cross-functional teams of up to 40 people.',
      cur: ['Foundations::What a project is|12;Defining scope|18', 'Planning::Schedules and budgets|24;Risks and stakeholders|20', 'Delivery::Agile and Kanban|22;Reporting progress|14']
    },
    {
      title: 'IELTS Preparation (Academic)', category: 'Languages', level: 'Intermediate', instructor: 'Jacob Lee', hours: 32, lang: 'English', price: 1090000, rating: 4.8, students: 220, kw: 'studying,exam',
      description: 'Targeted practice for all four IELTS Academic skills, with strategies, timed tasks and feedback.', tagline: 'Aim for the band score you need.',
      learn: ['Understand each IELTS section and task type', 'Improve reading and listening speed', 'Write clear Task 1 and Task 2 answers', 'Speak fluently in all three parts'], req: ['Intermediate English (B1 or above)'],
      skills: ['Reading', 'Listening', 'Academic writing', 'Speaking'], bio: 'IELTS examiner trainer who has coached hundreds of students to their target score.',
      cur: ['Reading and listening::Reading strategies|28;Listening practice|26', 'Writing::Task 1 reports|30;Task 2 essays|32', 'Speaking::Parts 1 to 3|28;Mock test and feedback|35']
    },
    {
      title: 'SEO & Content Marketing', category: 'Marketing', level: 'Intermediate', instructor: 'Thanh Huong', hours: 14, lang: 'English', price: 560000, rating: 4.5, students: 100, kw: 'laptop,marketing',
      description: 'Rank higher on search engines and build content that attracts and converts the right audience.', tagline: 'Get found, then get chosen.',
      learn: ['Research keywords and search intent', 'Optimise pages for search engines', 'Plan and write content that converts', 'Track rankings and traffic'], req: ['Basic marketing knowledge'],
      skills: ['SEO', 'Keyword research', 'Content strategy', 'Analytics'], bio: 'SEO specialist who has grown organic traffic for e-commerce and education sites.',
      cur: ['SEO basics::How search works|14;Keyword research|22', 'On-page SEO::Page optimisation|24;Technical SEO basics|20', 'Content::Content planning|18;Measuring results|16']
    }
  ];
  const categories = CATS.map((name, i) => ({ categoryId: i + 1, name }));
  let sid = 0, lid = 0, qid = 0, oid = 0, zid = 0, rid = 0;
  const pool = [];
  const LT = ['VIDEO', 'TEXT', 'VIDEO', 'FILE'];
  const lesson = (title, minutes, n, course) => {
    const type = LT[n % 4]; pool.push(title); return {
      lessonId: ++lid, title, type, minutes, preview: false, position: n + 1,
      contentUrl: type === 'VIDEO' ? 'https://example.com/videos/' + slug(title) : type === 'FILE' ? 'https://example.com/files/' + slug(title) + '.pdf' : null,
      text: '## Objectives\n\n- Understand the key ideas behind ' + title.toLowerCase() + '\n- Apply them in a short hands-on exercise\n- Recognise common mistakes and best practices\n\n## Key notes\n\n- ' + title + ' is a core building block of ' + course + '.\n- Work through the examples step by step before moving on.\n\n## Practice\n\nComplete the short exercise for "' + title + '" and compare your result with the sample solution.'
    };
  };
  const quiz = sec => {
    const own = sec.lessons.map(l => l.title), last = own[own.length - 1];
    const wrong = shuffle(pool.filter(t => !own.includes(t))).slice(0, 3); while (wrong.length < 3) wrong.push(['Introduction to cooking', 'History of art', 'Basic gardening'][wrong.length]);
    const q = (text, ok, bad) => ({ questionId: ++qid, text, options: shuffle([ok, ...bad]).map(o => ({ optionId: ++oid, text: o, correct: o === ok })) });
    return {
      quizId: ++zid, title: sec.title + ' quiz', passScore: 70, maxAttempts: 0, questions: [q('Which lesson is part of "' + sec.title + '"?', own[0], wrong),
      q('What is the best way to learn "' + last + '"?', 'Watch, then practise the example yourself', ['Skip the exercises', 'Only read the title', 'Memorise without trying']),
      q('How is your course progress calculated?', 'Completed lessons divided by total lessons', ['By time spent on the site', 'By number of logins', 'By course price'])]
    };
  };
  const sections = (cur, title) => { let n = 0; const s = cur.map((x, i) => { const [st, ls] = x.split('::'); return { sectionId: ++sid, title: st, position: i + 1, quiz: null, lessons: ls.split(';').map(l => { const [t, m] = l.split('|'); return lesson(t, +m, n++, title); }) }; }); s[0].lessons[0].preview = true; return s; };
  const courses = raw.map((r, i) => {
    const inst = account(r.instructor === 'Nguyen Van An' ? 'instructor@learnpath.com' : slug(r.instructor).replace(/-/g, '.') + '@learnpath.com', r.instructor, 'INSTRUCTOR'), secs = sections(r.cur, r.title);
    secs[0].quiz = quiz(secs[0]);
    return {
      courseId: i + 1, title: r.title, description: r.description, categoryId: CATS.indexOf(r.category) + 1, level: r.level, price: r.price, imageUrl: 'https://loremflickr.com/1200/700/' + r.kw + '?lock=' + (i + 21),
      status: 'PUBLISHED', rejectReason: null, instructorId: inst.userId, language: r.lang, durationHours: r.hours, details: { tagline: r.tagline, learn: r.learn, requirements: r.req, skills: r.skills, instructorBio: r.bio }, sections: secs
    };
  });
  const demo = account('instructor@learnpath.com', 'Nguyen Van An', 'INSTRUCTOR');
  [['Spring Security in Practice', 'DRAFT', null, 0], ['REST API Design Patterns', 'PENDING', null, 2], ['Intro to Cloud Computing', 'REJECTED', 'Please add more lessons and a clearer course description.', 2]].forEach((x, k) => courses.push({
    courseId: 16 + k, title: x[0], description: 'Work in progress course created by the demo instructor.', categoryId: 1, level: 'Intermediate', price: k ? 0 : 490000, imageUrl: null,
    status: x[1], rejectReason: x[2], instructorId: demo.userId, language: 'English', details: {}, sections: [{ sectionId: ++sid, title: 'Getting started', position: 1, quiz: null, lessons: x[3] ? [lesson('Welcome', 8, 0, x[0]), lesson('Core concepts', 15, 1, x[0])] : [] }]
  }));
  const enrollments = []; let eid = 0;
  raw.forEach((r, i) => { for (let n = 0; n < r.students; n++) enrollments.push({ enrollmentId: ++eid, courseId: i + 1, userId: 0, pricePaid: r.price, paymentMethod: 'CARD', enrolledAt: isoLocal(new Date()) }); });
  const reviews = []; raw.forEach((r, i) => shuffle(TPL).slice(0, 5).forEach((t, k) => reviews.push({
    reviewId: ++rid, courseId: i + 1, userId: -(reviews.length + 1), userName: NAMES[(i + k) % NAMES.length], rating: t.r,
    comment: t.t({ instructor: r.instructor, title: r.title }), createdAt: isoLocal(new Date(Date.now() - (3 + Math.floor(rnd() * 120)) * 864e5))
  })));
  const at = isoLocal(new Date()), notifications = [
    { notificationId: 1, userId: student.userId, message: 'Welcome to SkillTrail! Browse the catalog and enroll in your first course.', link: 'index.html#courses', read: false, createdAt: at },
    { notificationId: 2, userId: demo.userId, message: 'Your course "Intro to Cloud Computing" was rejected: Please add more lessons and a clearer course description.', link: 'my-courses.html', read: false, createdAt: at },
    { notificationId: 3, userId: admin.userId, message: '"REST API Design Patterns" is waiting for approval', link: 'index.html#courses', read: false, createdAt: at }];
  return { users, categories, courses, enrollments, progress: [], attempts: [], reviews, notifications, eid, aid: 0, rid, nid: 3, sid, lid, qid, oid, zid };
}

function mock(method, path, body) {
  let db = JSON.parse(localStorage.getItem('db6') || 'null');
  if (!db) { db = seed((JSON.parse(localStorage.getItem('db5') || localStorage.getItem('db4') || 'null') || {}).users); localStorage.setItem('db6', JSON.stringify(db)); }
  const err = (status, message) => { throw { status, message }; };
  const need = (ok, message) => { if (!ok) err(400, message); };
  const uid = state.user?.userId, me = uid ? db.users.find(u => u.userId === uid) : null;
  const admin = me?.role === 'ADMIN', instr = me?.role === 'INSTRUCTOR', now = () => isoLocal(new Date());
  let m; const R = (mt, re) => mt === method && (m = path.match(re));
  const course = id => db.courses.find(c => c.courseId === +id) || err(404, 'Course not found');
  const own = c => c.instructorId === uid;
  const manage = c => { if (!(admin || (instr && own(c)))) err(403, 'You can only manage your own courses'); return c; };
  const enrolledIn = id => !!uid && db.enrollments.some(e => e.courseId === id && e.userId === uid);
  const lessonsOf = c => c.sections.flatMap(s => s.lessons);
  const findSection = id => { for (const c of db.courses) { const s = c.sections.find(s => s.sectionId === +id); if (s) return { c, s }; } err(404, 'Section not found'); };
  const findLesson = id => { for (const c of db.courses) for (const s of c.sections) { const l = s.lessons.find(l => l.lessonId === +id); if (l) return { c, s, l }; } err(404, 'Lesson not found'); };
  const findQuiz = id => { for (const c of db.courses) for (const s of c.sections) if (s.quiz && s.quiz.quizId === +id) return { c, s, q: s.quiz }; err(404, 'Quiz not found'); };
  const renum = a => a.forEach((x, i) => { x.position = i + 1; });
  const move = (a, x, d) => { const i = a.indexOf(x), j = i + (d < 0 ? -1 : 1); if (j < 0 || j >= a.length) return;[a[i], a[j]] = [a[j], a[i]]; renum(a); };
  const notify = (userId, message, link) => db.notifications.push({ notificationId: ++db.nid, userId, message, link: link || null, read: false, createdAt: now() });
  const pubUser = ({ password, ...u }) => u;
  const rating = id => { const r = db.reviews.filter(x => x.courseId === id); return { rating: r.length ? Math.round(r.reduce((t, x) => t + x.rating, 0) / r.length * 10) / 10 : 0, ratingCount: r.length }; };
  const view = c => {
    const ls = lessonsOf(c), inst = db.users.find(u => u.userId === c.instructorId);
    return {
      courseId: c.courseId, title: c.title, description: c.description, categoryId: c.categoryId, category: db.categories.find(k => k.categoryId === c.categoryId)?.name || '', level: c.level, price: c.price, imageUrl: c.imageUrl, status: c.status, rejectReason: c.rejectReason,
      instructor: { userId: c.instructorId, fullName: inst?.fullName || 'Instructor' }, language: c.language || 'English', durationHours: c.durationHours ?? Math.max(1, Math.ceil(ls.reduce((t, l) => t + l.minutes, 0) / 60)), details: c.details || {}, ...rating(c.courseId),
      enrolledCount: db.enrollments.filter(e => e.courseId === c.courseId).length, lessonCount: ls.length,
      sections: c.sections.map(s => ({
        sectionId: s.sectionId, title: s.title, position: s.position, lessons: s.lessons.map(l => ({ lessonId: l.lessonId, title: l.title, type: l.type, minutes: l.minutes, preview: l.preview, position: l.position })),
        quiz: s.quiz ? { quizId: s.quiz.quizId, title: s.quiz.title, passScore: s.quiz.passScore, maxAttempts: s.quiz.maxAttempts, questionCount: s.quiz.questions.length } : null
      }))
    };
  };
  const progress = c => {
    const en = enrolledIn(c.courseId), ls = lessonsOf(c), dn = en ? db.progress.filter(p => p.courseId === c.courseId && p.userId === uid && ls.some(l => l.lessonId === p.lessonId)).map(p => p.lessonId) : [], next = ls.find(l => !dn.includes(l.lessonId));
    // quiz status per quiz in this course; a course is 100% only when every lesson is done AND every quiz is passed
    const quizzes = {}; c.sections.forEach(s => { if (!s.quiz) return; const at = en ? db.attempts.filter(a => a.quizId === s.quiz.quizId && a.userId === uid) : []; quizzes[s.quiz.quizId] = { passed: at.some(a => a.passed), best: at.length ? Math.max(...at.map(a => a.score)) : null, attempts: at.length }; });
    const totalQuizzes = Object.keys(quizzes).length, passedQuizzes = Object.values(quizzes).filter(x => x.passed).length, items = ls.length + totalQuizzes;
    return { enrolled: en, completed: dn, quizzes, totalQuizzes, passedQuizzes, percent: items ? Math.round((dn.length + passedQuizzes) / items * 100) : 0, nextLessonId: (next || ls[0])?.lessonId ?? null };
  };
  const courseBody = b => {
    need(b.title && String(b.title).trim(), 'Title is required'); need(b.price !== '' && +b.price >= 0, 'Price must be 0 or more'); need(db.categories.some(k => k.categoryId === +b.categoryId), 'Choose a category'); need(['Beginner', 'Intermediate', 'Advanced'].includes(b.level), 'Invalid level');
    return { title: b.title.trim(), description: b.description || '', categoryId: +b.categoryId, level: b.level, price: +b.price, imageUrl: b.imageUrl || null, details: b.details || {} };
  };
  const lessonBody = b => {
    need(b.title && b.title.trim(), 'Lesson title is required'); need(['VIDEO', 'TEXT', 'FILE'].includes(b.type), 'Invalid lesson type'); need(b.type === 'TEXT' || /^https?:\/\//i.test(b.contentUrl || ''), 'Enter a valid http(s) link for video or file lessons'); need(+b.minutes >= 0, 'Minutes must be 0 or more');
    return { title: b.title.trim(), type: b.type, contentUrl: b.contentUrl || null, text: b.text || '', minutes: +b.minutes || 0, preview: !!b.preview };
  };
  const reviewOk = b => need(Number.isInteger(+b.rating) && +b.rating >= 1 && +b.rating <= 5, 'Rating must be 1 to 5');

  const out = (() => {
    /* ---- public ---- */
    if (R('POST', /^\/auth\/register$/)) {
      const { fullName, email, password } = body || {};
      need(fullName && fullName.trim(), 'Full name is required'); need(/^\S+@\S+\.\S+$/.test(email || ''), 'Enter a valid email'); need((password || '').length >= 6, 'Password must be at least 6 characters');
      if (db.users.some(u => u.email.toLowerCase() === email.toLowerCase())) err(409, 'Email already registered');
      db.users.push({ userId: Date.now(), fullName: fullName.trim(), email, password, role: 'STUDENT', locked: false }); return null;
    }
    if (R('POST', /^\/auth\/login$/)) {
      const u = db.users.find(u => u.email.toLowerCase() === (body.email || '').toLowerCase() && u.password === body.password);
      if (!u) err(401, 'Invalid email or password'); if (u.locked) err(403, 'This account has been locked. Contact an admin.');
      return { token: 'mock-' + u.userId, user: { userId: u.userId, fullName: u.fullName, email: u.email, role: u.role } };
    }
    if (R('GET', /^\/categories$/)) return db.categories;
    if (R('GET', /^\/courses$/)) return db.courses.filter(c => c.status === 'PUBLISHED').map(view);
    if (R('GET', /^\/courses\/(\d+)$/)) { const c = course(m[1]); if (c.status !== 'PUBLISHED' && !(me && (admin || own(c)))) err(404, 'Course not found'); return view(c); }
    if (R('GET', /^\/courses\/(\d+)\/reviews$/)) return db.reviews.filter(r => r.courseId === +m[1]).sort((a, b) => b.createdAt.localeCompare(a.createdAt))
      .map(r => ({ reviewId: r.reviewId, userId: r.userId, userName: db.users.find(u => u.userId === r.userId)?.fullName || r.userName, rating: r.rating, comment: r.comment, createdAt: r.createdAt }));
    if (R('GET', /^\/courses\/(\d+)\/progress$/)) return progress(course(m[1]));
    if (R('GET', /^\/lessons\/(\d+)$/)) {
      const { c, l } = findLesson(m[1]);
      if (c.status !== 'PUBLISHED' && !(me && (admin || own(c)))) err(404, 'Lesson not found');
      if (!l.preview) { if (!me) err(401, 'Please log in first'); if (!(admin || own(c) || enrolledIn(c.courseId))) err(403, 'Enroll in this course to view this lesson'); }
      return { ...l, courseId: c.courseId };
    }

    /* ---- any logged-in user ---- */
    if (!me) err(401, 'Please log in first'); if (me.locked) err(403, 'This account has been locked');
    if (R('GET', /^\/users\/me$/)) return pubUser(me);
    if (R('PUT', /^\/users\/me$/)) { need(body.fullName && body.fullName.trim(), 'Full name is required'); me.fullName = body.fullName.trim(); return pubUser(me); }
    if (R('PUT', /^\/users\/me\/password$/)) { if (me.password !== body.currentPassword) err(400, 'Current password is incorrect'); need((body.newPassword || '').length >= 6, 'Password must be at least 6 characters'); me.password = body.newPassword; return null; }
    if (R('GET', /^\/notifications\/me$/)) return db.notifications.filter(n => n.userId === uid).sort((a, b) => b.notificationId - a.notificationId).slice(0, 20);
    if (R('POST', /^\/notifications\/read$/)) { db.notifications.forEach(n => { if (n.userId === uid) n.read = true; }); return null; }
    if (R('GET', /^\/enrollments\/me$/)) return db.enrollments.filter(e => e.userId === uid).map(e => {
      const c = course(e.courseId), p = progress(c), v = view(c);
      return { enrollmentId: e.enrollmentId, courseId: c.courseId, title: c.title, imageUrl: c.imageUrl, category: v.category, instructor: v.instructor.fullName, studentName: me.fullName, totalLessons: v.lessonCount, completedLessons: p.completed.length, totalQuizzes: p.totalQuizzes, passedQuizzes: p.passedQuizzes, percent: p.percent, nextLessonId: p.nextLessonId, enrolledAt: e.enrolledAt };
    });
    if (R('POST', /^\/courses\/(\d+)\/enroll$/)) {
      const c = course(m[1]); if (c.status !== 'PUBLISHED') err(404, 'Course not found'); if (own(c)) err(400, 'You cannot enroll in your own course');
      if (enrolledIn(c.courseId)) err(409, 'You are already enrolled in this course');
      if (!admin && c.price > 0 && !['CARD', 'EWALLET', 'BANK', 'DEMO'].includes(body?.paymentMethod)) err(400, 'Choose a payment method');
      const en = { enrollmentId: ++db.eid, courseId: c.courseId, userId: uid, pricePaid: admin ? 0 : c.price, paymentMethod: admin ? 'ADMIN' : c.price ? body.paymentMethod : null, enrolledAt: now() };
      db.enrollments.push(en); notify(c.instructorId, me.fullName + ' enrolled in "' + c.title + '"', 'course.html?id=' + c.courseId); return { enrollmentId: en.enrollmentId, courseId: c.courseId, title: c.title };
    }
    if (R('DELETE', /^\/courses\/(\d+)\/enroll$/)) {
      const id = +m[1]; if (!enrolledIn(id)) err(404, 'You are not enrolled in this course');
      db.enrollments = db.enrollments.filter(e => !(e.courseId === id && e.userId === uid)); db.progress = db.progress.filter(p => !(p.courseId === id && p.userId === uid)); return null;
    }
    if (R('PUT', /^\/courses\/(\d+)\/progress$/)) {
      const c = course(m[1]); if (!enrolledIn(c.courseId)) err(403, 'Enroll in this course first');
      const l = lessonsOf(c).find(l => l.lessonId === +body.lessonId) || err(404, 'Lesson not found'), was = progress(c).percent;
      db.progress = db.progress.filter(p => !(p.userId === uid && p.lessonId === l.lessonId)); if (body.done !== false) db.progress.push({ userId: uid, courseId: c.courseId, lessonId: l.lessonId });
      const p = progress(c); if (p.percent === 100 && was < 100) notify(uid, 'Congratulations! You completed "' + c.title + '".', 'congrats.html?id=' + c.courseId); return p;
    }
    if (R('GET', /^\/quizzes\/(\d+)$/)) {
      const { c, q } = findQuiz(m[1]); if (!(admin || own(c) || enrolledIn(c.courseId))) err(403, 'Enroll in this course to take the quiz');
      const at = db.attempts.filter(a => a.quizId === q.quizId && a.userId === uid);
      return {
        quizId: q.quizId, courseId: c.courseId, title: q.title, passScore: q.passScore, maxAttempts: q.maxAttempts, attemptsUsed: at.length, bestScore: at.length ? Math.max(...at.map(a => a.score)) : null, passed: at.some(a => a.passed),
        questions: q.questions.map(x => ({ questionId: x.questionId, text: x.text, options: x.options.map(o => ({ optionId: o.optionId, text: o.text })) }))
      };
    }
    if (R('POST', /^\/quizzes\/(\d+)\/attempts$/)) {
      const { c, q } = findQuiz(m[1]); if (!enrolledIn(c.courseId)) err(403, 'Enroll in this course to take the quiz');
      const used = db.attempts.filter(a => a.quizId === q.quizId && a.userId === uid).length; if (q.maxAttempts > 0 && used >= q.maxAttempts) err(409, 'You have used all attempts for this quiz');
      const ans = body.answers || {}; need(q.questions.every(x => ans[x.questionId] != null), 'Answer every question before submitting');
      const correct = {}; let ok = 0; q.questions.forEach(x => { const co = x.options.find(o => o.correct); correct[x.questionId] = co.optionId; if (+ans[x.questionId] === co.optionId) ok++; });
      const was = progress(c).percent, score = Math.round(ok / q.questions.length * 100), passed = score >= q.passScore; db.attempts.push({ attemptId: ++db.aid, quizId: q.quizId, userId: uid, score, passed, createdAt: now() });
      const after = progress(c).percent; if (after === 100 && was < 100) notify(uid, 'Congratulations! You completed "' + c.title + '".', 'congrats.html?id=' + c.courseId);
      return { score, passed, passScore: q.passScore, correctCount: ok, total: q.questions.length, correct, attemptsUsed: used + 1, courseCompleted: after === 100 };
    }
    if (R('POST', /^\/courses\/(\d+)\/reviews$/)) {
      const c = course(m[1]); if (!enrolledIn(c.courseId)) err(403, 'Only enrolled students can review this course');
      if (db.reviews.some(r => r.courseId === c.courseId && r.userId === uid)) err(409, 'You have already reviewed this course'); reviewOk(body);
      const r = { reviewId: ++db.rid, courseId: c.courseId, userId: uid, rating: +body.rating, comment: (body.comment || '').trim(), createdAt: now() }; db.reviews.push(r); return r;
    }
    if (R('PUT', /^\/reviews\/(\d+)$/)) { const r = db.reviews.find(x => x.reviewId === +m[1]) || err(404, 'Review not found'); if (r.userId !== uid) err(403, 'You can only edit your own review'); reviewOk(body); r.rating = +body.rating; r.comment = (body.comment || '').trim(); return r; }
    if (R('DELETE', /^\/reviews\/(\d+)$/)) { const r = db.reviews.find(x => x.reviewId === +m[1]) || err(404, 'Review not found'); if (r.userId !== uid && !admin) err(403, 'You can only delete your own review'); db.reviews = db.reviews.filter(x => x !== r); return null; }

    /* ---- instructor (owner) or admin ---- */
    if (R('GET', /^\/instructor\/courses$/)) { if (!instr && !admin) err(403, 'Instructor access only'); return db.courses.filter(c => admin || own(c)).map(view); }
    if (R('GET', /^\/instructor\/courses\/(\d+)$/)) { const c = manage(course(m[1])); return { ...view(c), sections: c.sections }; }
    if (R('POST', /^\/courses$/)) { if (!instr) err(403, 'Only instructors can create courses'); const c = { courseId: Date.now(), ...courseBody(body), status: 'DRAFT', rejectReason: null, instructorId: uid, language: 'English', sections: [] }; db.courses.push(c); return view(c); }
    if (R('PUT', /^\/courses\/(\d+)$/)) { const c = manage(course(m[1])); Object.assign(c, courseBody(body)); return view(c); }
    if (R('DELETE', /^\/courses\/(\d+)$/)) {
      const c = manage(course(m[1])); if (db.enrollments.some(e => e.courseId === c.courseId)) err(409, 'Cannot delete a course that has enrolled students');
      db.courses = db.courses.filter(x => x !== c); db.reviews = db.reviews.filter(r => r.courseId !== c.courseId); return null;
    }
    if (R('POST', /^\/courses\/(\d+)\/submit$/)) {
      const c = manage(course(m[1])); if (!own(c)) err(403, 'Only the course owner can submit it'); need(['DRAFT', 'REJECTED'].includes(c.status), 'Only draft or rejected courses can be submitted');
      need(c.sections.some(s => s.lessons.length > 0), 'Add at least one section and one lesson before submitting'); c.status = 'PENDING'; c.rejectReason = null;
      db.users.filter(u => u.role === 'ADMIN').forEach(u => notify(u.userId, '"' + c.title + '" is waiting for approval', 'index.html#courses')); return view(c);
    }
    if (R('GET', /^\/courses\/(\d+)\/students$/)) {
      const c = manage(course(m[1])), total = lessonsOf(c).length;
      return db.enrollments.filter(e => e.courseId === c.courseId && db.users.some(u => u.userId === e.userId)).map(e => {
        const u = db.users.find(x => x.userId === e.userId), dn = db.progress.filter(p => p.courseId === c.courseId && p.userId === e.userId).length;
        return { userId: u.userId, fullName: u.fullName, email: u.email, enrolledAt: e.enrolledAt, completedLessons: dn, totalLessons: total, percent: total ? Math.round(dn / total * 100) : 0 };
      });
    }
    if (R('POST', /^\/courses\/(\d+)\/sections$/)) { const c = manage(course(m[1])); need(body.title && body.title.trim(), 'Section title is required'); const s = { sectionId: ++db.sid, title: body.title.trim(), position: c.sections.length + 1, lessons: [], quiz: null }; c.sections.push(s); return s; }
    if (R('PUT', /^\/sections\/(\d+)$/)) { const { c, s } = findSection(m[1]); manage(c); need(body.title && body.title.trim(), 'Section title is required'); s.title = body.title.trim(); return s; }
    if (R('DELETE', /^\/sections\/(\d+)$/)) { const { c, s } = findSection(m[1]); manage(c); c.sections = c.sections.filter(x => x !== s); renum(c.sections); return null; }
    if (R('POST', /^\/sections\/(\d+)\/move$/)) { const { c, s } = findSection(m[1]); manage(c); move(c.sections, s, +body.direction); return null; }
    if (R('POST', /^\/sections\/(\d+)\/lessons$/)) { const { c, s } = findSection(m[1]); manage(c); const l = { lessonId: ++db.lid, position: s.lessons.length + 1, ...lessonBody(body) }; s.lessons.push(l); return l; }
    if (R('PUT', /^\/lessons\/(\d+)$/)) { const { c, l } = findLesson(m[1]); manage(c); Object.assign(l, lessonBody(body)); return l; }
    if (R('DELETE', /^\/lessons\/(\d+)$/)) { const { c, s, l } = findLesson(m[1]); manage(c); s.lessons = s.lessons.filter(x => x !== l); renum(s.lessons); db.progress = db.progress.filter(p => p.lessonId !== l.lessonId); return null; }
    if (R('POST', /^\/lessons\/(\d+)\/move$/)) { const { c, s, l } = findLesson(m[1]); manage(c); move(s.lessons, l, +body.direction); return null; }
    if (R('PUT', /^\/sections\/(\d+)\/quiz$/)) {
      const { c, s } = findSection(m[1]); manage(c); need(body.title && body.title.trim(), 'Quiz title is required'); const pass = +body.passScore; need(pass >= 1 && pass <= 100, 'Pass score must be 1 to 100');
      need(Array.isArray(body.questions) && body.questions.length, 'Add at least one question');
      body.questions.forEach((x, i) => { need(x.text && x.text.trim(), 'Question ' + (i + 1) + ' needs text'); const os = (x.options || []).filter(o => o.text && o.text.trim()); need(os.length >= 2, 'Question ' + (i + 1) + ' needs at least 2 options'); need(os.filter(o => o.correct).length === 1, 'Question ' + (i + 1) + ' needs exactly one correct option'); });
      s.quiz = {
        quizId: s.quiz?.quizId || ++db.zid, title: body.title.trim(), passScore: pass, maxAttempts: Math.max(0, +body.maxAttempts || 0),
        questions: body.questions.map(x => ({ questionId: ++db.qid, text: x.text.trim(), options: x.options.filter(o => o.text && o.text.trim()).map(o => ({ optionId: ++db.oid, text: o.text.trim(), correct: !!o.correct })) }))
      }; return null;
    }
    if (R('DELETE', /^\/sections\/(\d+)\/quiz$/)) { const { c, s } = findSection(m[1]); manage(c); s.quiz = null; return null; }

    /* ---- admin only ---- */
    if (!admin) err(403, 'Admin only');
    if (R('GET', /^\/admin\/courses$/)) return db.courses.map(view);
    if (R('POST', /^\/admin\/courses\/(\d+)\/approve$/)) { const c = course(m[1]); need(c.status === 'PENDING', 'Only pending courses can be approved'); c.status = 'PUBLISHED'; c.rejectReason = null; notify(c.instructorId, 'Your course "' + c.title + '" was approved and is now published.', 'course.html?id=' + c.courseId); return view(c); }
    if (R('POST', /^\/admin\/courses\/(\d+)\/reject$/)) {
      const c = course(m[1]); need(c.status === 'PENDING', 'Only pending courses can be rejected'); need(body.reason && body.reason.trim(), 'A reason is required'); c.status = 'REJECTED'; c.rejectReason = body.reason.trim();
      notify(c.instructorId, 'Your course "' + c.title + '" was rejected: ' + c.rejectReason, 'course.html?id=' + c.courseId); return view(c);
    }
    if (R('POST', /^\/categories$/)) { need(body.name && body.name.trim(), 'Category name is required'); if (db.categories.some(k => k.name.toLowerCase() === body.name.trim().toLowerCase())) err(409, 'Category already exists'); const k = { categoryId: Date.now(), name: body.name.trim() }; db.categories.push(k); return k; }
    if (R('PUT', /^\/categories\/(\d+)$/)) {
      const k = db.categories.find(x => x.categoryId === +m[1]) || err(404, 'Category not found'); need(body.name && body.name.trim(), 'Category name is required');
      if (db.categories.some(x => x !== k && x.name.toLowerCase() === body.name.trim().toLowerCase())) err(409, 'Category already exists'); k.name = body.name.trim(); return k;
    }
    if (R('DELETE', /^\/categories\/(\d+)$/)) { const k = db.categories.find(x => x.categoryId === +m[1]) || err(404, 'Category not found'); if (db.courses.some(c => c.categoryId === k.categoryId)) err(409, 'Cannot delete a category that has courses'); db.categories = db.categories.filter(x => x !== k); return null; }
    if (R('GET', /^\/admin\/users$/)) return db.users.map(u => ({ ...pubUser(u), enrollments: db.enrollments.filter(e => e.userId === u.userId).length, courses: db.courses.filter(c => c.instructorId === u.userId).length }));
    if (R('PUT', /^\/admin\/users\/(\d+)\/role$/)) { const u = db.users.find(x => x.userId === +m[1]) || err(404, 'User not found'); if (u.userId === uid) err(400, 'You cannot change your own role'); need(['STUDENT', 'INSTRUCTOR', 'ADMIN'].includes(body.role), 'Invalid role'); u.role = body.role; return null; }
    if (R('PUT', /^\/admin\/users\/(\d+)\/lock$/)) { const u = db.users.find(x => x.userId === +m[1]) || err(404, 'User not found'); if (u.userId === uid) err(400, 'You cannot lock your own account'); u.locked = !!body.locked; return null; }
    if (R('GET', /^\/admin\/reviews$/)) return [...db.reviews].sort((a, b) => b.reviewId - a.reviewId).slice(0, 50).map(r => ({ reviewId: r.reviewId, courseTitle: db.courses.find(c => c.courseId === r.courseId)?.title, userName: db.users.find(u => u.userId === r.userId)?.fullName || r.userName, rating: r.rating, comment: r.comment, createdAt: r.createdAt }));
    if (R('GET', /^\/admin\/stats$/)) {
      const pub = db.courses.filter(c => c.status === 'PUBLISHED');
      return {
        users: db.users.length, courses: db.courses.length, published: pub.length, pending: db.courses.filter(c => c.status === 'PENDING').length, enrollments: db.enrollments.length, reviews: db.reviews.length,
        topCourses: pub.map(c => ({ courseId: c.courseId, title: c.title, enrolled: db.enrollments.filter(e => e.courseId === c.courseId).length })).sort((a, b) => b.enrolled - a.enrolled).slice(0, 5)
      };
    }
    err(404, 'Not found');
  })();
  if (method !== 'GET') localStorage.setItem('db6', JSON.stringify(db));
  return out;
}

/* ===== shared course card (course = GET /courses item) ===== */
function card(c, mine) {
  return `<div class="col-sm-6 col-lg-4"><a href="course.html?id=${c.courseId}" class="text-reset text-decoration-none"><div class="card ev h-100 glass">
   <div class="ev-img ${catClass(c.category)}">${imgTag(c)}<span class="ev-cat">${ICON[c.category] || '📚'} ${esc(c.category)}</span>${c.ratingCount ? `<span class="ev-rate">★ ${c.rating}</span>` : ''}</div>
   <div class="card-body"><h5>${esc(c.title)}</h5>
   <p class="small text-secondary mb-2"><i class="bi bi-person-video3"></i> ${esc(c.instructor.fullName)}</p>
   <p class="small text-secondary mb-3"><i class="bi bi-clock"></i> ${c.durationHours}h · <i class="bi bi-collection-play"></i> ${c.lessonCount} lessons · ${esc(c.level)}</p>
   <div class="d-flex justify-content-between align-items-center"><span class="fw-bold fs-5">${money(c.price)}</span>
   <small class="text-secondary">${mine ? '✓ Enrolled' : c.enrolledCount + ' students'}</small></div></div></div></a></div>`;
}

/* ===== shared layout: background, navbar, footer, scroll-to-top, popups ===== */
(function layout() {
  const p = location.pathname.split('/').pop() || 'index.html', on = h => p === h ? ' active' : '', u = state.user, role = u?.role;
  document.body.classList.add('d-flex', 'flex-column', 'min-vh-100');
  document.head.insertAdjacentHTML('beforeend', '<link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800&display=swap" rel="stylesheet"><style>' + UI_CSS + CERT_CSS + '</style>');
  const pill = '';
  const avatar = u?.avatar || '';
  const modeLabel = CONFIG.USE_MOCK ? 'Mock data' : 'Live API';
  document.body.insertAdjacentHTML('afterbegin', `<div class="bg"><i></i><i></i><i></i><i></i></div>
  <nav class="navbar navbar-expand-lg sticky-top glass ${role === 'ADMIN' ? 'admin-nav' : role === 'INSTRUCTOR' ? 'instr-nav' : ''}"><div class="container">
   <a class="navbar-brand fw-bold" href="index.html"><i class="bi bi-mortarboard-fill"></i> Skill<span>Trail</span></a>
   <button class="navbar-toggler" data-bs-toggle="collapse" data-bs-target="#nav"><span class="navbar-toggler-icon"></span></button>
   <div class="collapse navbar-collapse" id="nav"><ul class="navbar-nav me-auto">
    <li class="nav-item"><a class="nav-link${on('index.html')}" href="index.html">Home</a></li>
    <li class="nav-item"><a class="nav-link" href="index.html#courses">Courses</a></li>
    <li class="nav-item"><a class="nav-link" href="index.html#about">About</a></li>
    <li class="nav-item"><a class="nav-link" href="index.html#contact">Contact</a></li>
    ${u ? `<li class="nav-item"><a class="nav-link${on('my-courses.html')}" href="my-courses.html">My Courses</a></li>` : ''}

   </ul>
   ${u ? `<div class="dropdown d-inline-block me-2"><button class="btn btn-outline-light btn-sm position-relative" id="bell" data-bs-toggle="dropdown" aria-label="Notifications"><i class="bi bi-bell"></i><span id="bellN" class="badge bg-danger rounded-pill position-absolute top-0 start-100 translate-middle d-none"></span></button>
      <div class="dropdown-menu dropdown-menu-end p-0 glass" id="bellList" style="width:320px;max-height:380px;overflow:auto"><div class="notif text-secondary">No notifications</div></div></div>
     <a href="profile.html" class="user-chip text-reset text-decoration-none me-2${on('profile.html')}">${avatar ? `<img class="nav-avatar" src="${avatar}" alt="Profile">` : `<span class="nav-avatar nav-avatar-fallback">${esc((u.fullName || 'U').split(' ').map(x => x[0]).slice(0, 2).join(''))}</span>`}<span>Hi, ${esc(u.fullName)}</span></a><button class="btn btn-outline-light btn-sm" onclick="logout()">Logout</button>`
      : `<a href="login.html" class="btn btn-outline-light btn-sm me-2">Login</a><a href="signup.html" class="btn btn-primary btn-sm">Sign up</a>`}
   <button class="btn btn-sm mode-toggle ms-lg-2" id="modeToggle" type="button" title="Switch data source"><i class="bi bi-database"></i> ${modeLabel}</button></div></div></nav>`);
  const modeButton = document.getElementById('modeToggle'); if (modeButton) modeButton.addEventListener('click', () => { const live = localStorage.getItem('learnpath-mode') === 'live'; localStorage.setItem('learnpath-mode', live ? 'mock' : 'live'); toast(live ? 'Mock data mode enabled' : 'Live API mode enabled'); setTimeout(() => location.reload(), 250); });
  document.body.insertAdjacentHTML('beforeend', `<footer class="glass text-center py-4 mt-auto">
   <div class="fs-5 fw-bold mb-2"><i class="bi bi-mortarboard-fill"></i> SkillTrail</div>
   <div class="links mb-2"><a href="index.html" class="mx-2">Home</a><a href="index.html#courses" class="mx-2">Courses</a><a href="index.html#about" class="mx-2">About</a><a href="index.html#contact" class="mx-2">Contact</a></div>
   <div class="mb-2"><a href="#" class="mx-2"><i class="bi bi-facebook"></i></a><a href="#" class="mx-2"><i class="bi bi-instagram"></i></a>
   <a href="#" class="mx-2"><i class="bi bi-twitter-x"></i></a><a href="#" class="mx-2"><i class="bi bi-github"></i></a></div>
   <small class="text-secondary">&copy; 2026 SkillTrail. All rights reserved.</small></footer>
   <button id="toTop" class="btn btn-primary rounded-circle" aria-label="Scroll to top"><i class="bi bi-arrow-up"></i></button>
   <div id="toasts" class="toast-container position-fixed top-0 end-0 p-3"></div>
   <div class="modal fade" id="confirmModal" tabindex="-1"><div class="modal-dialog modal-dialog-centered modal-sm"><div class="modal-content text-center p-3">
    <div class="modal-body"><div id="cfIcon" class="cf-icon danger"><i class="bi bi-exclamation-triangle-fill"></i></div><h5 id="cfTitle" class="mb-2"></h5><p id="cfMsg" class="text-secondary mb-4"></p>
    <div class="d-flex gap-2"><button class="btn btn-outline-light flex-fill" data-bs-dismiss="modal">Cancel</button><button id="cfOk" class="btn btn-danger flex-fill"></button></div></div></div></div></div>`);
  
  // Jump to #about / #contact / #courses when arriving from another page (e.g. My Courses, Profile).
  // The home page keeps growing after load (course grid is fetched and rendered, images load),
  // which pushes the target further down. So we re-align until the layout stops changing,
  // and stop immediately if the visitor scrolls on their own.
  let settle = null;
  const scrollToHashTarget = () => {
    if (settle) settle.stop();
    if (!location.hash || location.hash.length < 2) return;
    let id; try { id = decodeURIComponent(location.hash.slice(1)); } catch { return; }
    const root = document.documentElement;
    let quiet, ro, done = false;
    const align = () => {
      const t = document.getElementById(id); if (!t) return;
      const prev = root.style.scrollBehavior; root.style.scrollBehavior = 'auto'; // instant, never a slow smooth scroll toward a moving target
      t.scrollIntoView({ block: 'start' });
      root.style.scrollBehavior = prev;
    };
    const stop = () => {
      if (done) return; done = true; clearTimeout(quiet); clearTimeout(hard); ro && ro.disconnect();
      ['wheel', 'touchstart', 'mousedown', 'keydown'].forEach(ev => removeEventListener(ev, stop, true));
    };
    const bump = () => { if (done) return; align(); clearTimeout(quiet); quiet = setTimeout(stop, 1200); };
    const hard = setTimeout(stop, 8000);
    ['wheel', 'touchstart', 'mousedown', 'keydown'].forEach(ev => addEventListener(ev, stop, { capture: true, passive: true }));
    if (window.ResizeObserver) { ro = new ResizeObserver(bump); ro.observe(document.body); }
    bump();
    settle = { stop };
  };

  scrollToHashTarget();
  addEventListener('load', scrollToHashTarget);
  addEventListener('pageshow', e => { if (e.persisted) scrollToHashTarget(); });
  // same-page clicks (already on the home page) are handled natively with smooth scrolling

  addEventListener('scroll', () => $('#toTop').classList.toggle('show', scrollY > 300));
  $('#toTop').onclick = () => scrollTo({ top: 0, behavior: 'smooth' });
  const f = JSON.parse(sessionStorage.getItem('flash') || 'null');
  if (f) { sessionStorage.removeItem('flash'); toast(f[0], f[1]); }
  if (u) {
    api('/notifications/me').then(l => {
      const n = l.filter(x => !x.read).length; if (n) { $('#bellN').textContent = n; $('#bellN').classList.remove('d-none'); }
      $('#bellList').innerHTML = l.length ? l.map(x => `<a class="notif d-block text-reset text-decoration-none${x.read ? '' : ' unread'}" href="${esc(x.link || '#')}">${esc(x.message)}<div class="text-secondary small">${fmt(x.createdAt)}</div></a>`).join('') : '<div class="notif text-secondary">No notifications</div>';
    }).catch(() => { });
    $('#bell').addEventListener('show.bs.dropdown', () => { if (!$('#bellN').classList.contains('d-none')) { $('#bellN').classList.add('d-none'); api('/notifications/read', 'POST').catch(() => { }); } });
  }
})();
