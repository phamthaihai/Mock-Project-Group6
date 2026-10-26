/* ===== Runtime configuration =====
 * Override in config.js without changing application logic.
 * Keep USE_MOCK=true until the backend endpoints in database/API_CONTRACT.md are ready.
 */
const CONFIG = Object.freeze({
  USE_MOCK: true,
  API_BASE: '/api',
  REQUEST_TIMEOUT_MS: 15000,
  ...(window.LEARNHUB_CONFIG || {})
});
const CATS = ['Programming', 'Data', 'Design', 'Business', 'Languages', 'Marketing'];
const ICON = { Programming: '💻', Data: '📊', Design: '🎨', Business: '💼', Languages: '🌍', Marketing: '📣' };
const $ = s => document.querySelector(s), $$ = s => document.querySelectorAll(s);
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const fmt = d => new Date(d).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
const formData = f => Object.fromEntries(new FormData(f));
const state = { user: JSON.parse(localStorage.getItem('user') || 'null'), token: localStorage.getItem('token') };

/* ===== messages ===== */
const TOAST = { success: ['check-circle-fill', 'Success'], danger: ['x-circle-fill', 'Error'], warning: ['exclamation-triangle-fill', 'Heads up'], info: ['info-circle-fill', 'Notice'] };
function toast(msg, type = 'success') {
  const [icon, title] = TOAST[type] || TOAST.info, el = document.createElement('div');
  el.className = 'lh-toast lh-' + type;
  el.innerHTML = `<i class="bi bi-${icon}"></i><div class="lh-body"><b>${title}</b><span>${esc(msg)}</span></div><button class="lh-x" aria-label="Close">&times;</button><i class="lh-bar"></i>`;
  const close = () => { el.classList.add('out'); setTimeout(() => el.remove(), 300); };
  el.querySelector('.lh-x').onclick = close; $('#toasts').append(el); setTimeout(close, 4000);
}
// Styled replacement for the browser's confirm(): const ok = await confirmBox('Delete this?', { title: 'Delete', confirmText: 'Delete' })
function confirmBox(message, { title = 'Are you sure?', confirmText = 'Confirm', danger = true } = {}) {
  return new Promise(resolve => {
    const el = $('#confirmModal'), m = bootstrap.Modal.getOrCreateInstance(el), ok = $('#cfOk'); let result = false;
    $('#cfTitle').textContent = title; $('#cfMsg').textContent = message; ok.textContent = confirmText; ok.className = 'btn ' + (danger ? 'btn-danger' : 'btn-primary');
    $('#cfIcon').className = 'cf-icon ' + (danger ? 'danger' : 'primary'); $('#cfIcon i').className = 'bi ' + (danger ? 'bi-exclamation-triangle-fill' : 'bi-question-circle-fill');
    ok.onclick = () => { result = true; m.hide(); };
    el.addEventListener('hidden.bs.modal', () => resolve(result), { once: true }); m.show();
  });
}
const flash = (msg, type = 'success') => sessionStorage.setItem('flash', JSON.stringify([msg, type])); // shown on next page

/* ===== session ===== */
function setSession(user, token) {
  state.user = user; state.token = token;
  localStorage.setItem('user', JSON.stringify(user)); localStorage.setItem('token', token);
}
function logout() { localStorage.removeItem('user'); localStorage.removeItem('token'); flash('Logged out', 'info'); location.href = 'index.html'; }
function guard(role) {
  if (!state.user) { location.href = 'login.html?next=' + encodeURIComponent(location.pathname.split('/').pop() + location.search); return false; }
  if (role && state.user.role !== role) { flash('Access denied', 'danger'); location.href = 'index.html'; return false; }
  return true;
}

/* ===== API LAYER (real REST calls; JSON fields = MSSQL column names in camelCase) ===== */
async function api(path, method = 'GET', body) {
  if (CONFIG.USE_MOCK) {
    await new Promise(r => setTimeout(r, 120));
    return mock(method, path, body);
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), CONFIG.REQUEST_TIMEOUT_MS);
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (state.token) headers.Authorization = 'Bearer ' + state.token;

  try {
    const base = String(CONFIG.API_BASE || '/api').replace(/\/$/, '');
    const endpoint = String(path).startsWith('/') ? String(path) : '/' + path;
    const res = await fetch(base + endpoint, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: controller.signal,
      credentials: 'same-origin'
    });

    if (res.status === 204) return null;
    const contentType = res.headers.get('content-type') || '';
    const payload = contentType.includes('application/json')
      ? await res.json().catch(() => null)
      : await res.text().catch(() => '');

    if (!res.ok) {
      const message = (payload && typeof payload === 'object' && (payload.message || payload.error || payload.detail))
        || (typeof payload === 'string' && payload.trim())
        || res.statusText
        || 'Request failed';
      const error = new Error(message);
      error.status = res.status;
      error.payload = payload;
      throw error;
    }
    return payload;
  } catch (error) {
    if (error.name === 'AbortError') {
      throw new Error('The request timed out. Please try again.');
    }
    if (error instanceof TypeError) {
      throw new Error('Could not reach the LearnHub API. Check that the backend is running and API_BASE is correct.');
    }
    throw error;
  } finally {
    clearTimeout(timeout);
  }
}

/* ===== helpers ===== */
const money = n => n > 0 ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n) : 'Free';
const isoLocal = d => { const p = x => String(x).padStart(2, '0'); return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:00`; };
const dets = c => { let d = c.details; if (typeof d === 'string') { try { d = JSON.parse(d); } catch { d = null; } } return d || {}; };
const lessonCount = c => (dets(c).curriculum || []).reduce((n, s) => n + s.lessons.length, 0);
const quizCount = c => (dets(c).curriculum || []).reduce((n, s) => n + (s.quizzes || []).length, 0);
const catClass = c => 'c-' + String(c).replace(/[^A-Za-z]/g, '');
const fallbackImg = c => 'https://picsum.photos/seed/' + encodeURIComponent(c.category + c.courseId) + '/1200/700';
const img = c => c.imageUrl || fallbackImg(c);
const imgTag = c => `<img loading="lazy" src="${esc(img(c))}" alt="" onerror="this.onerror=null;this.src='${esc(fallbackImg(c))}'">`;

/* ===== MOCK BACKEND (localStorage) - delete when real API is live ===== */
// Curriculum, lesson content and quizzes are built in js/seed-content.js (buildCurriculum)
// Public view of a course: lesson titles only (no lesson content) until the user is enrolled or is an admin
const outline = c => { const d = dets(c); return d.curriculum ? { ...c, details: { ...d, curriculum: d.curriculum.map(s => ({ title: s.title, summary: s.summary,
  lessons: s.lessons.map(l => ({ title: l.title, minutes: l.minutes, type: l.type })),
  quizzes: (s.quizzes || []).map(q => ({ quizId: q.quizId, title: q.title, description: q.description, passPercent: q.passPercent, timeLimitMin: q.timeLimitMin, questionCount: q.questions.length })) })) } } : c; };

function seed(users) {
  const raw = [
    { title: 'Java Full-Stack Bootcamp', category: 'Programming', level: 'Intermediate', instructor: 'Nguyen Van An', hours: 48, lang: 'English', price: 1290000, rating: 4.8, students: 120, kw: 'coding,programming',
      description: 'Go from Java basics to a deployed full-stack app: Spring Boot REST APIs, SQL Server, JWT security and a responsive front-end.', tagline: 'Build and ship a real full-stack application.',
      learn: ['Design REST APIs with Spring Boot', 'Model data with JPA and SQL Server', 'Secure apps with JWT and roles', 'Build a responsive front-end with fetch'], req: ['Basic programming knowledge', 'A laptop with JDK 21 installed'],
      skills: ['Spring Boot', 'REST', 'JPA', 'SQL Server', 'JWT', 'Git'], bio: 'Senior backend engineer with 10 years of experience building enterprise Java systems and mentoring junior developers.',
      cur: ['Java refresher::OOP essentials|18;Collections and streams|22;Exceptions and testing|15', 'Spring Boot REST::Controllers and services|25;Validation and error handling|20;Swagger documentation|12', 'Data and security::JPA with SQL Server|28;Authentication with JWT|30;Role-based access|18', 'Capstone::Connect the front-end|24;Deploy the app|16'] },
    { title: 'Intro to Web Development', category: 'Programming', level: 'Beginner', instructor: 'Le Thu Ha', hours: 12, lang: 'English', price: 0, rating: 4.6, students: 300, kw: 'laptop,webdesign',
      description: 'Learn HTML, CSS and JavaScript from zero and publish your first responsive website.', tagline: 'Your first website in a weekend. Free.',
      learn: ['Structure pages with semantic HTML', 'Style layouts with CSS and Bootstrap', 'Add interactivity with JavaScript', 'Publish a site to the web'], req: ['No experience needed', 'Any computer and a browser'],
      skills: ['HTML', 'CSS', 'Bootstrap', 'JavaScript'], bio: 'Front-end developer and teacher who has helped over 5,000 beginners write their first line of code.',
      cur: ['HTML basics::Page structure|10;Forms and tables|14', 'CSS and Bootstrap::Selectors and layout|16;Responsive design|18', 'JavaScript::DOM and events|20;Fetching data|15'] },
    { title: 'Docker & DevOps Essentials', category: 'Programming', level: 'Intermediate', instructor: 'Marcus Reyes', hours: 20, lang: 'English', price: 790000, rating: 4.7, students: 90, kw: 'server,datacenter',
      description: 'Containerize applications, automate builds and deploy with confidence using Docker, CI/CD and cloud basics.', tagline: 'Ship faster with containers and pipelines.',
      learn: ['Write Dockerfiles and compose files', 'Automate tests with CI/CD', 'Deploy containers to the cloud', 'Monitor and troubleshoot services'], req: ['Comfort with the command line', 'One programming language'],
      skills: ['Docker', 'CI/CD', 'Linux', 'Cloud'], bio: 'Cloud engineer who runs production platforms serving millions of requests per day.',
      cur: ['Containers::Images and containers|20;Dockerfile deep dive|25;Docker Compose|18', 'Pipelines::Git workflows|15;Build and test automation|28', 'Deploy::Cloud deployment|30;Monitoring basics|16'] },
    { title: 'Machine Learning Foundations', category: 'Data', level: 'Intermediate', instructor: 'Dr. Linh Tran', hours: 36, lang: 'English', price: 1490000, rating: 4.9, students: 150, kw: 'artificialintelligence,technology',
      description: 'Understand the core ideas behind machine learning and build, evaluate and deploy your first models in Python.', tagline: 'From data to models that actually work.',
      learn: ['Prepare and explore real datasets', 'Train regression and classification models', 'Evaluate models and avoid overfitting', 'Serve a model behind a simple API'], req: ['Basic Python', 'High-school level maths'],
      skills: ['Python', 'scikit-learn', 'Pandas', 'Model evaluation'], bio: 'Machine learning researcher turned industry practitioner, with a PhD in applied statistics.',
      cur: ['Foundations::What is machine learning|12;Data preparation|26;Exploratory analysis|24', 'Core models::Linear regression|28;Classification|30;Trees and ensembles|32', 'In practice::Evaluation and tuning|26;Deploying a model|22'] },
    { title: 'SQL & Data Analysis', category: 'Data', level: 'Beginner', instructor: 'Priya Nair', hours: 18, lang: 'English', price: 450000, rating: 4.6, students: 210, kw: 'data,analytics',
      description: 'Query, clean and summarise data with SQL, then turn results into clear dashboards and insights.', tagline: 'Ask better questions of your data.',
      learn: ['Write SELECT, JOIN and GROUP BY queries', 'Clean messy data', 'Design simple relational schemas', 'Present findings with charts'], req: ['No prior experience', 'SQL Server or any SQL database'],
      skills: ['SQL', 'T-SQL', 'Data cleaning', 'Dashboards'], bio: 'Data analyst who has built reporting for retail and fintech teams.',
      cur: ['SQL basics::SELECT and filtering|14;Joins|22;Aggregations|20', 'Working with data::Cleaning data|18;Subqueries and CTEs|24', 'Insights::Designing a schema|16;Building a dashboard|26'] },
    { title: 'UI/UX Design Essentials', category: 'Design', level: 'Beginner', instructor: 'Mai Anh', hours: 24, lang: 'English', price: 790000, rating: 4.7, students: 130, kw: 'design,interface',
      description: 'Learn the full design process: research, wireframes, visual design and prototyping, with a portfolio project.', tagline: 'Design products people love to use.',
      learn: ['Run quick user research', 'Create wireframes and flows', 'Apply colour, type and layout', 'Prototype and test your design'], req: ['No experience needed', 'A free Figma account'],
      skills: ['Figma', 'Wireframing', 'Prototyping', 'Usability testing'], bio: 'Product designer with 8 years of experience across fintech and e-commerce apps.',
      cur: ['Research::Understanding users|15;Personas and journeys|18', 'Design::Wireframes|22;Visual design basics|26;Design systems|20', 'Deliver::Prototyping|24;Usability testing|18'] },
    { title: 'Digital Marketing Masterclass', category: 'Marketing', level: 'Beginner', instructor: 'Khanh Vy', hours: 16, lang: 'English', price: 650000, rating: 4.5, students: 180, kw: 'marketing,socialmedia',
      description: 'Plan and run campaigns across search, social and email, and measure what actually drives growth.', tagline: 'Grow an audience and measure the results.',
      learn: ['Build a simple marketing funnel', 'Run search and social campaigns', 'Write emails that convert', 'Read analytics and report ROI'], req: ['No experience needed'],
      skills: ['SEO', 'Social ads', 'Email marketing', 'Analytics'], bio: 'Growth marketer who has scaled several local brands from zero to national reach.',
      cur: ['Strategy::Funnels and goals|14;Audience research|16', 'Channels::Search and SEO|24;Social media ads|26;Email marketing|18', 'Measure::Analytics and reporting|22'] },
    { title: 'Business English for Professionals', category: 'Languages', level: 'Intermediate', instructor: 'Emma Walker', hours: 30, lang: 'English', price: 890000, rating: 4.8, students: 260, kw: 'meeting,office',
      description: 'Communicate clearly in meetings, emails and negotiations, with real workplace scenarios and feedback.', tagline: 'Speak and write with confidence at work.',
      learn: ['Run and join meetings in English', 'Write clear professional emails', 'Handle calls and small talk', 'Negotiate and present ideas'], req: ['Intermediate English (B1 or above)'],
      skills: ['Speaking', 'Email writing', 'Presenting', 'Negotiation'], bio: 'Certified English trainer with 12 years coaching teams at international companies.',
      cur: ['Everyday work::Meetings and discussions|20;Professional emails|18', 'Speaking up::Phone and video calls|22;Small talk and networking|16', 'Advanced::Presentations|26;Negotiation|24'] },
    { title: 'Public Speaking & Presentation Skills', category: 'Business', level: 'Beginner', instructor: 'Samir Oduya', hours: 10, lang: 'English', price: 390000, rating: 4.7, students: 140, kw: 'speaker,presentation',
      description: 'Overcome nerves, structure a message and deliver talks and pitches that keep an audience engaged.', tagline: 'Say it clearly. Be remembered.',
      learn: ['Structure a talk with a clear message', 'Control nerves and body language', 'Design simple, effective slides', 'Handle questions with confidence'], req: ['No experience needed'],
      skills: ['Storytelling', 'Slide design', 'Confidence', 'Q&A handling'], bio: 'Keynote speaker and coach who has trained thousands of professionals.',
      cur: ['Foundations::Know your audience|12;Structuring your message|16', 'Delivery::Voice and body language|18;Managing nerves|14', 'Pitching::Designing slides|20;Handling Q&A|15'] },
    { title: 'React for Modern Web Apps', category: 'Programming', level: 'Intermediate', instructor: 'Hoang Minh', hours: 28, lang: 'English', price: 990000, rating: 4.7, students: 140, kw: 'code,screen',
      description: 'Build fast, component-based interfaces with React, hooks and routing, and connect them to a REST API.', tagline: 'Build interactive UIs the modern way.',
      learn: ['Build UIs from reusable components', 'Manage state with hooks', 'Add routing and forms', 'Fetch and display data from an API'], req: ['Solid HTML, CSS and JavaScript', 'Node.js installed'],
      skills: ['React', 'Hooks', 'React Router', 'REST APIs'], bio: 'Front-end lead who has shipped React apps for startups and banks.',
      cur: ['React basics::JSX and components|18;Props and state|24;Handling events|16', 'Hooks and data::useEffect and data fetching|28;Forms and validation|22', 'Real apps::Routing|20;Build a mini dashboard|30'] },
    { title: 'Data Visualization with Power BI', category: 'Data', level: 'Beginner', instructor: 'Nina Petrova', hours: 16, lang: 'English', price: 520000, rating: 4.6, students: 110, kw: 'dashboard,analytics',
      description: 'Connect data sources, model them and build interactive dashboards that tell a clear story.', tagline: 'Turn spreadsheets into dashboards people use.',
      learn: ['Import and clean data', 'Build relationships and measures', 'Design interactive dashboards', 'Publish and share reports'], req: ['Basic Excel skills'],
      skills: ['Power BI', 'DAX', 'Data modelling', 'Dashboards'], bio: 'BI consultant who has built reporting for logistics and retail teams.',
      cur: ['Getting started::Power BI overview|12;Connecting to data|18', 'Modelling::Relationships and tables|22;Intro to DAX|28', 'Dashboards::Visual best practices|20;Publishing reports|16'] },
    { title: 'Graphic Design with Photoshop & Illustrator', category: 'Design', level: 'Beginner', instructor: 'Carlos Mendes', hours: 26, lang: 'English', price: 750000, rating: 4.7, students: 95, kw: 'illustration,designer',
      description: 'Learn the tools and design principles to create logos, posters and social media graphics.', tagline: 'Create visuals that stand out.',
      learn: ['Master layers, masks and vector shapes', 'Apply colour, type and composition', 'Design logos and brand assets', 'Prepare files for print and web'], req: ['A computer that can run design software'],
      skills: ['Photoshop', 'Illustrator', 'Branding', 'Typography'], bio: 'Brand designer with a decade of experience in agencies and freelance work.',
      cur: ['Design basics::Colour and typography|18;Composition|16', 'Photoshop::Layers and masks|26;Photo retouching|24', 'Illustrator::Vector shapes|22;Designing a logo|28'] },
    { title: 'Project Management Fundamentals', category: 'Business', level: 'Beginner', instructor: 'Olivia Grant', hours: 14, lang: 'English', price: 590000, rating: 4.6, students: 125, kw: 'teamwork,planning',
      description: 'Plan, run and deliver projects on time using proven methods, from scope and schedule to risks and reporting.', tagline: 'Deliver projects on time and on budget.',
      learn: ['Define scope, goals and deliverables', 'Build realistic schedules and budgets', 'Manage risks and stakeholders', 'Run projects with Agile and Kanban'], req: ['No experience needed'],
      skills: ['Planning', 'Agile', 'Risk management', 'Stakeholders'], bio: 'PMP-certified project manager who has led cross-functional teams of up to 40 people.',
      cur: ['Foundations::What a project is|12;Defining scope|18', 'Planning::Schedules and budgets|24;Risks and stakeholders|20', 'Delivery::Agile and Kanban|22;Reporting progress|14'] },
    { title: 'IELTS Preparation (Academic)', category: 'Languages', level: 'Intermediate', instructor: 'Jacob Lee', hours: 32, lang: 'English', price: 1090000, rating: 4.8, students: 220, kw: 'studying,exam',
      description: 'Targeted practice for all four IELTS Academic skills, with strategies, timed tasks and feedback.', tagline: 'Aim for the band score you need.',
      learn: ['Understand each IELTS section and task type', 'Improve reading and listening speed', 'Write clear Task 1 and Task 2 answers', 'Speak fluently in all three parts'], req: ['Intermediate English (B1 or above)'],
      skills: ['Reading', 'Listening', 'Academic writing', 'Speaking'], bio: 'IELTS examiner trainer who has coached hundreds of students to their target score.',
      cur: ['Reading and listening::Reading strategies|28;Listening practice|26', 'Writing::Task 1 reports|30;Task 2 essays|32', 'Speaking::Parts 1 to 3|28;Mock test and feedback|35'] },
    { title: 'SEO & Content Marketing', category: 'Marketing', level: 'Intermediate', instructor: 'Thanh Huong', hours: 14, lang: 'English', price: 560000, rating: 4.5, students: 100, kw: 'laptop,marketing',
      description: 'Rank higher on search engines and build content that attracts and converts the right audience.', tagline: 'Get found, then get chosen.',
      learn: ['Research keywords and search intent', 'Optimise pages for search engines', 'Plan and write content that converts', 'Track rankings and traffic'], req: ['Basic marketing knowledge'],
      skills: ['SEO', 'Keyword research', 'Content strategy', 'Analytics'], bio: 'SEO specialist who has grown organic traffic for e-commerce and education sites.',
      cur: ['SEO basics::How search works|14;Keyword research|22', 'On-page SEO::Page optimisation|24;Technical SEO basics|20', 'Content::Content planning|18;Measuring results|16'] }
  ];
  const courses = raw.map((r, i) => ({ courseId: i + 1, title: r.title, category: r.category, level: r.level, instructor: r.instructor, durationHours: r.hours, language: r.lang, price: r.price, rating: r.rating,
    imageUrl: 'https://loremflickr.com/1200/700/' + r.kw + '?lock=' + (i + 21), description: r.description,
    details: { tagline: r.tagline, learn: r.learn, requirements: r.req, skills: r.skills, instructorBio: r.bio, curriculum: buildCurriculum(r, i + 1, raw) } }));
  let eid = 0; const enrollments = [];
  courses.forEach((c, i) => { for (let n = 0; n < raw[i].students; n++) enrollments.push({ enrollmentId: ++eid, courseId: c.courseId, userId: 0, pricePaid: c.price, paymentMethod: 'CARD', enrolledAt: isoLocal(new Date()) }); });
  users = users || [];
  if (!users.some(u => u.role === 'ADMIN')) users.push({ userId: 1, fullName: 'Admin', email: 'admin@learnhub.com', password: 'admin123', role: 'ADMIN' });
  return { users, courses, enrollments, progress: [], attempts: [], eid };
}

function mock(method, path, body) {
  let db = JSON.parse(localStorage.getItem('db6') || 'null');
  if (!db) { db = seed((JSON.parse(localStorage.getItem('db5') || localStorage.getItem('db4') || localStorage.getItem('db3') || localStorage.getItem('db2') || localStorage.getItem('db') || 'null') || {}).users); localStorage.setItem('db6', JSON.stringify(db)); }
  const save = () => localStorage.setItem('db6', JSON.stringify(db));
  const err = (status, message) => { throw { status, message }; };
  const uid = state.user?.userId, count = id => db.enrollments.filter(e => e.courseId === id).length;
  const admin = state.user?.role === 'ADMIN', wc = c => ({ ...(admin ? c : outline(c)), enrolledCount: count(c.courseId) });
  const done = (id, u) => db.progress.filter(p => p.courseId === id && p.userId === u);
  const passedSet = (id, u) => new Set(db.attempts.filter(a => a.courseId === id && a.userId === u && a.passed).map(a => a.quizId));
  const finished = (id, u) => { const c = db.courses.find(x => x.courseId === id) || {}, tl = lessonCount(c); return tl > 0 && done(id, u).length >= tl && passedSet(id, u).size >= quizCount(c); };
  const findQuiz = qid => { for (const c of db.courses) { const cur = dets(c).curriculum || []; for (let si = 0; si < cur.length; si++) { const q = (cur[si].quizzes || []).find(x => x.quizId === qid); if (q) return { c, s: cur[si], si, q }; } } return null; };
  const ev = en => { const c = db.courses.find(x => x.courseId === en.courseId) || {}, total = lessonCount(c), comp = done(en.courseId, en.userId).length, u = db.users.find(x => x.userId === en.userId) || {};
    return { enrollmentId: en.enrollmentId, courseId: en.courseId, title: c.title, category: c.category, level: c.level, durationHours: c.durationHours, imageUrl: c.imageUrl, instructor: c.instructor, totalLessons: total, completedLessons: comp,
      totalQuizzes: quizCount(c), passedQuizzes: passedSet(en.courseId, en.userId).size, enrolledAt: en.enrolledAt,
      completedAt: finished(en.courseId, en.userId) ? (en.completedAt || en.enrolledAt) : null, studentName: u.fullName || state.user?.fullName || 'Student' }; };
  let m;
  if (path === '/auth/register') {
    if (db.users.some(u => u.email === body.email)) err(409, 'Email already registered');
    db.users.push({ userId: Date.now(), role: 'USER', ...body }); save(); return null;
  }
  if (path === '/auth/login') {
    const u = db.users.find(u => u.email === body.email && u.password === body.password);
    if (!u) err(401, 'Invalid email or password');
    const { password, ...user } = u; return { token: 'mock-' + u.userId, user };
  }
  if (path === '/courses' && method === 'GET') return db.courses.map(wc);
  if ((m = path.match(/^\/courses\/(\d+)$/)) && method === 'GET') { const c = db.courses.find(x => x.courseId === +m[1]); if (!c) err(404, 'Course not found'); return wc(c); }
  if (m = path.match(/^\/courses\/(\d+)\/progress$/)) {
    const id = +m[1], enrolled = !!uid && db.enrollments.some(e => e.courseId === id && e.userId === uid);
    if (method === 'GET') {
      const quizzes = {}; if (enrolled) db.attempts.filter(a => a.courseId === id && a.userId === uid).forEach(a => { const q = quizzes[a.quizId] ||= { attempts: 0, best: 0, passed: false }; q.attempts++; q.best = Math.max(q.best, a.scorePercent); q.passed = q.passed || a.passed; });
      return { enrolled, completed: enrolled ? done(id, uid).map(p => p.lesson) : [], quizzes };
    }
    if (!uid) err(401, 'Please log in first'); if (!enrolled) err(403, 'Enroll in this course first');
    db.progress = db.progress.filter(p => !(p.courseId === id && p.userId === uid && p.lesson === body.lesson));
    if (body.done) db.progress.push({ userId: uid, courseId: id, lesson: body.lesson });
    const en = db.enrollments.find(e => e.courseId === id && e.userId === uid);
    if (en) { if (finished(id, uid)) en.completedAt = en.completedAt || isoLocal(new Date()); else delete en.completedAt; }
    save(); return { courseCompleted: finished(id, uid) };
  }
  if (!uid) err(401, 'Please log in first');
  if (path === '/users/me') {
    const u = db.users.find(x => x.userId === uid);
    if (method === 'PUT') { u.fullName = body.fullName; save(); }
    const { password, ...user } = u; return user;
  }
  if (path === '/users/me/password') {
    const u = db.users.find(x => x.userId === uid);
    if (u.password !== body.currentPassword) err(400, 'Current password is incorrect');
    u.password = body.newPassword; save(); return null;
  }
  if ((m = path.match(/^\/courses\/(\d+)\/lessons\/(\d+)\.(\d+)$/)) && method === 'GET') {
    const id = +m[1], c = db.courses.find(x => x.courseId === id); if (!c) err(404, 'Course not found');
    if (!admin && !db.enrollments.some(e => e.courseId === id && e.userId === uid)) err(403, 'Enroll in this course to view the lesson content');
    const l = dets(c).curriculum?.[+m[2] - 1]?.lessons?.[+m[3] - 1]; if (!l) err(404, 'Lesson not found');
    return { key: m[2] + '.' + m[3], section: dets(c).curriculum[+m[2] - 1].title, ...l };
  }
  if ((m = path.match(/^\/quizzes\/(\d+)$/)) && method === 'GET') {
    const f = findQuiz(+m[1]); if (!f) err(404, 'Quiz not found');
    if (!admin && !db.enrollments.some(e => e.courseId === f.c.courseId && e.userId === uid)) err(403, 'Enroll in this course to take the quiz');
    const have = new Set(done(f.c.courseId, uid).map(p => p.lesson));
    if (!admin && !f.s.lessons.every((_, j) => have.has((f.si + 1) + '.' + (j + 1)))) err(403, 'Finish all lessons in "' + f.s.title + '" to unlock this quiz');
    const mine = db.attempts.filter(a => a.quizId === f.q.quizId && a.userId === uid);
    const { questions, ...meta } = f.q;   // correct answers are NEVER sent before submitting
    return { ...meta, courseId: f.c.courseId, courseTitle: f.c.title, sectionIndex: f.si + 1, sectionTitle: f.s.title, attempts: mine.length, best: Math.max(0, ...mine.map(a => a.scorePercent)), passed: mine.some(a => a.passed),
      questions: questions.map(q => ({ questionId: q.questionId, text: q.text, multi: q.multi, options: q.options.map(o => ({ optionId: o.optionId, text: o.text })) })) };
  }
  if ((m = path.match(/^\/quizzes\/(\d+)\/submit$/)) && method === 'POST') {
    const f = findQuiz(+m[1]); if (!f) err(404, 'Quiz not found');
    if (!admin && !db.enrollments.some(e => e.courseId === f.c.courseId && e.userId === uid)) err(403, 'Enroll in this course to take the quiz');
    const ans = body?.answers || {};
    const review = f.q.questions.map(q => { const right = q.options.filter(o => o.correct).map(o => o.optionId), sel = (ans[q.questionId] || []).map(Number);
      return { questionId: q.questionId, correct: right.length === sel.length && right.every(x => sel.includes(x)), selected: sel, correctOptionIds: right, explanation: q.explanation }; });
    const correctCount = review.filter(r => r.correct).length, totalCount = review.length, scorePercent = Math.round(correctCount / totalCount * 100), passed = scorePercent >= f.q.passPercent;
    let attemptId = null;
    if (!admin || db.enrollments.some(e => e.courseId === f.c.courseId && e.userId === uid)) {
      attemptId = Date.now(); db.attempts.push({ attemptId, quizId: f.q.quizId, courseId: f.c.courseId, userId: uid, scorePercent, correctCount, totalCount, passed, submittedAt: isoLocal(new Date()) });
      const en = db.enrollments.find(e => e.courseId === f.c.courseId && e.userId === uid); if (en && finished(f.c.courseId, uid)) en.completedAt = en.completedAt || isoLocal(new Date());
      save();
    }
    return { attemptId, scorePercent, correctCount, totalCount, passed, passPercent: f.q.passPercent, review, courseCompleted: !!uid && finished(f.c.courseId, uid), tracked: attemptId !== null };
  }
  if (path === '/enrollments/me') return db.enrollments.filter(e => e.userId === uid).map(ev);
  if ((m = path.match(/^\/enrollments\/(\d+)$/)) && method === 'DELETE') {
    const en = db.enrollments.find(e => e.enrollmentId === +m[1] && e.userId === uid); if (!en) err(404, 'Enrollment not found');
    db.enrollments = db.enrollments.filter(e => e !== en); db.progress = db.progress.filter(p => !(p.courseId === en.courseId && p.userId === uid)); db.attempts = db.attempts.filter(a => !(a.courseId === en.courseId && a.userId === uid)); save(); return null;
  }
  if ((m = path.match(/^\/courses\/(\d+)\/enroll$/)) && method === 'POST') {
    const id = +m[1], c = db.courses.find(x => x.courseId === id); if (!c) err(404, 'Course not found');
    if (!admin && c.price > 0 && !['CARD', 'EWALLET', 'BANK', 'DEMO'].includes(body?.paymentMethod)) err(400, 'Choose a payment method');
    if (db.enrollments.some(e => e.courseId === id && e.userId === uid)) err(409, 'You are already enrolled in this course');
    const en = { enrollmentId: ++db.eid, courseId: id, userId: uid, pricePaid: admin ? 0 : c.price, paymentMethod: admin ? 'ADMIN' : c.price ? (body?.paymentMethod || 'CARD') : null, enrolledAt: isoLocal(new Date()) };
    db.enrollments.push(en); save(); return ev(en);
  }
  if (state.user.role !== 'ADMIN') err(403, 'Admin only');
  if (path === '/admin/stats') {
    const nameOf = x => db.users.find(u => u.userId === x)?.fullName || 'Guest student';
    const by = db.courses.map(c => { const es = db.enrollments.filter(e => e.courseId === c.courseId); return { courseId: c.courseId, title: c.title, enrolled: es.length, revenue: es.reduce((t, e) => t + e.pricePaid, 0) }; });
    return { courses: db.courses.length, users: db.users.length, enrollments: db.enrollments.length, revenue: by.reduce((t, c) => t + c.revenue, 0), topCourses: [...by].sort((a, b) => b.enrolled - a.enrolled).slice(0, 5),
      recent: [...db.enrollments].sort((a, b) => b.enrollmentId - a.enrollmentId).slice(0, 8).map(e => ({ student: nameOf(e.userId), courseTitle: db.courses.find(c => c.courseId === e.courseId)?.title, amount: e.pricePaid, method: e.paymentMethod, enrolledAt: e.enrolledAt })) };
  }
  if (path === '/admin/users' && method === 'GET') return db.users.map(({ password, ...u }) => ({ ...u, enrollments: db.enrollments.filter(e => e.userId === u.userId).length }));
  if ((m = path.match(/^\/admin\/users\/(\d+)\/role$/)) && method === 'PUT') {
    const u = db.users.find(x => x.userId === +m[1]); if (!u) err(404, 'User not found');
    if (u.userId === uid) err(400, 'You cannot change your own role'); if (!['USER', 'ADMIN'].includes(body.role)) err(400, 'Invalid role');
    u.role = body.role; save(); return null;
  }
  if ((m = path.match(/^\/admin\/users\/(\d+)$/)) && method === 'DELETE') {
    const id = +m[1]; if (!db.users.some(x => x.userId === id)) err(404, 'User not found'); if (id === uid) err(400, 'You cannot delete your own account');
    db.users = db.users.filter(x => x.userId !== id); db.enrollments = db.enrollments.filter(e => e.userId !== id); db.progress = db.progress.filter(p => p.userId !== id); db.attempts = db.attempts.filter(a => a.userId !== id); save(); return null;
  }
  if (path === '/admin/stats') {
    const paid = db.enrollments.filter(e => e.paymentMethod !== 'DEMO');
    const top = db.courses.map(c => { const es = db.enrollments.filter(e => e.courseId === c.courseId); return { courseId: c.courseId, title: c.title, enrolled: es.length, revenue: es.filter(e => e.paymentMethod !== 'DEMO').reduce((t, e) => t + e.pricePaid, 0) }; }).sort((a, b) => b.enrolled - a.enrolled);
    const recent = db.enrollments.filter(e => e.userId !== 0).slice(-8).reverse().map(e => { const u = db.users.find(x => x.userId === e.userId) || {}, c = db.courses.find(x => x.courseId === e.courseId) || {};
      return { enrollmentId: e.enrollmentId, userName: u.fullName, userEmail: u.email, courseTitle: c.title, pricePaid: e.pricePaid, paymentMethod: e.paymentMethod, enrolledAt: e.enrolledAt }; });
    return { users: db.users.length, courses: db.courses.length, enrollments: db.enrollments.length, revenue: paid.reduce((t, e) => t + e.pricePaid, 0), topCourses: top.slice(0, 6), recent };
  }
  if (path === '/admin/users') return db.users.map(u => ({ userId: u.userId, fullName: u.fullName, email: u.email, role: u.role, enrollments: db.enrollments.filter(e => e.userId === u.userId).length }));
  if ((m = path.match(/^\/admin\/users\/(\d+)\/role$/)) && method === 'PUT') {
    const u = db.users.find(x => x.userId === +m[1]); if (!u) err(404, 'User not found');
    if (u.userId === uid) err(400, 'You cannot change your own role');
    if (!['USER', 'ADMIN'].includes(body.role)) err(400, 'Invalid role'); u.role = body.role; save(); return null;
  }
  if ((m = path.match(/^\/admin\/users\/(\d+)$/)) && method === 'DELETE') {
    const u = db.users.find(x => x.userId === +m[1]); if (!u) err(404, 'User not found');
    if (u.userId === uid) err(400, 'You cannot delete your own account');
    db.users = db.users.filter(x => x !== u); db.enrollments = db.enrollments.filter(e => e.userId !== u.userId); db.progress = db.progress.filter(p => p.userId !== u.userId); save(); return null;
  }
  if (path === '/courses' && method === 'POST') { const c = { ...body, courseId: Date.now() }; db.courses.push(c); save(); return wc(c); }
  if (m = path.match(/^\/courses\/(\d+)$/)) {
    const id = +m[1];
    if (method === 'PUT') db.courses = db.courses.map(c => c.courseId === id ? { ...c, ...body, courseId: id } : c);
    else { db.courses = db.courses.filter(c => c.courseId !== id); db.enrollments = db.enrollments.filter(e => e.courseId !== id); db.progress = db.progress.filter(p => p.courseId !== id); db.attempts = db.attempts.filter(a => a.courseId !== id); }
    save(); return null;
  }
  err(404, 'Not found');
}

/* ===== completion, certificates and suggestions ===== */
const isDone = e => e.totalLessons > 0 && e.completedLessons >= e.totalLessons && (e.passedQuizzes || 0) >= (e.totalQuizzes || 0);
const certId = e => 'LH-' + new Date(e.completedAt || Date.now()).getFullYear() + '-' + String(e.courseId).padStart(3, '0') + '-' + String(e.enrollmentId).padStart(4, '0');
const LV = { Beginner: 1, Intermediate: 2, Advanced: 3 };
// Suggest the next courses: not yet enrolled, ranked by category match, next level up and rating. Returns [{c, why}]
function suggestNext(courses, enr, n = 3) {
  const have = new Set(enr.map(e => e.courseId));
  const done = enr.filter(isDone).sort((a, b) => new Date(b.completedAt) - new Date(a.completedAt));
  const basis = done[0] || [...enr].sort((a, b) => b.completedLessons / (b.totalLessons || 1) - a.completedLessons / (a.totalLessons || 1))[0];
  const cats = new Set(enr.map(e => e.category));
  return courses.filter(c => !have.has(c.courseId)).map(c => {
    let score = c.rating, why = 'Top rated on LearnHub';
    if (cats.has(c.category)) { score += 2; why = 'Popular in ' + c.category; }
    if (basis && c.category === basis.category) {
      score += 3; why = (done[0] ? 'Next step after ' : 'Goes well with ') + basis.title;
      if ((LV[c.level] || 1) >= (LV[basis.level] || 1)) score += (LV[c.level] || 1) > (LV[basis.level] || 1) ? 1.5 : 0.5;
    }
    return { c, why, score: score + c.enrolledCount / 1000 };
  }).sort((a, b) => b.score - a.score).slice(0, n);
}
function sugCard(c, why) {
  return card(c, false).replace('<div class="card-body">', '<div class="card-body"><div class="small text-info mb-2"><i class="bi bi-lightbulb-fill"></i> ' + esc(why) + '</div>');
}
const CERT_CSS = `
.cert { position: relative; padding: 2.2rem 1.5rem; text-align: center; color: #3b2f1a; border-radius: 6px; background: radial-gradient(circle at top, #fffdf5, #f6ecd2); font-family: Georgia, 'Times New Roman', serif; box-shadow: 0 18px 40px rgba(0, 0, 0, 0.4); }
.cert::before { content: ''; position: absolute; inset: 10px; border: 3px double #c9a227; border-radius: 4px; pointer-events: none; }
.cert .c-brand { font: 700 1rem Poppins, sans-serif; letter-spacing: 0.15em; color: #6d5dfc; text-transform: uppercase; }
.cert .c-title { margin: 0.4rem 0; font-size: clamp(1.6rem, 4vw, 2.4rem); letter-spacing: 0.08em; text-transform: uppercase; color: #8a6d0b; }
.cert .c-small { font-style: italic; color: #6b5a33; }
.cert .c-name { margin: 0.4rem auto; padding: 0 1rem 0.3rem; max-width: 80%; border-bottom: 1px solid #c9a227; font-size: clamp(1.5rem, 4vw, 2.2rem); font-style: italic; }
.cert .c-course { margin: 0.5rem 0; font-size: clamp(1.2rem, 3vw, 1.7rem); font-weight: 700; }
.cert .c-meta { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 0.8rem; margin-top: 1.6rem; font-size: 0.8rem; color: #6b5a33; }
.cert .c-meta b { display: block; color: #3b2f1a; font-size: 0.9rem; }
.cert .c-seal { display: inline-grid; place-items: center; width: 54px; height: 54px; margin-top: 0.5rem; border-radius: 50%; background: linear-gradient(135deg, #f59e0b, #fbbf24); color: #fff; font-size: 1.6rem; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.25); }
.cf-piece { position: fixed; top: -20px; width: 10px; height: 16px; opacity: 0.9; z-index: 2000; pointer-events: none; animation: cfFall linear forwards; }
@keyframes cfFall { to { transform: translateY(110vh) rotate(720deg); opacity: 0.8; } }
.trophy { display: grid; place-items: center; width: 96px; height: 96px; margin: 0 auto 1rem; border-radius: 50%; font-size: 3rem; color: #fff; background: linear-gradient(135deg, #f59e0b, #fbbf24); box-shadow: 0 0 0 10px rgba(245, 158, 11, 0.15); animation: pop 0.6s ease; }
@keyframes pop { 0% { transform: scale(0.3); } 70% { transform: scale(1.15); } 100% { transform: scale(1); } }
`;
function certificateHtml(e) {
  const d = e.completedAt ? new Date(e.completedAt).toLocaleDateString([], { dateStyle: 'long' }) : '';
  return `<div class="cert"><div class="c-brand">LearnHub</div><div class="c-title">Certificate of Completion</div><div class="c-small">This is to certify that</div>
    <div class="c-name">${esc(e.studentName)}</div><div class="c-small">has successfully completed the course</div><div class="c-course">${esc(e.title)}</div>
    <div class="c-small">${esc(e.level || '')} level · ${e.durationHours || ''} hours · ${e.totalLessons} lessons</div><div class="c-seal"><i class="bi bi-award-fill"></i></div>
    <div class="c-meta"><div>Date completed<b>${esc(d)}</b></div><div>Instructor<b>${esc(e.instructor)}</b></div><div>Certificate ID<b>${certId(e)}</b></div></div></div>`;
}
function printCertificate(e) {
  const w = window.open('', '_blank'); if (!w) return toast('Allow pop-ups to print your certificate', 'warning');
  w.document.write('<!DOCTYPE html><html><head><title>Certificate - ' + esc(e.title) + '</title><link href="https://fonts.googleapis.com/css2?family=Poppins:wght@600;700&display=swap" rel="stylesheet"><style>body{margin:0;padding:24px;background:#fff;font-size:16px}.cert{max-width:900px;margin:auto;box-shadow:none!important}@page{size:landscape;margin:12mm}' +
    CERT_CSS.replace(/\.cf-piece[\s\S]*$/, '') + '</style><link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"></head><body>' + certificateHtml(e) + '<script>setTimeout(function(){print()},700)<\/script></body></html>');
  w.document.close();
}
function confetti(n = 90) {
  const colors = ['#6d5dfc', '#22d3ee', '#f59e0b', '#22c55e', '#ef4444', '#ec4899'];
  for (let i = 0; i < n; i++) {
    const el = document.createElement('i'); el.className = 'cf-piece';
    el.style.cssText = 'left:' + Math.random() * 100 + 'vw;background:' + colors[i % colors.length] + ';animation-duration:' + (2.5 + Math.random() * 3) + 's;animation-delay:' + Math.random() * 1.5 + 's;transform:rotate(' + Math.random() * 360 + 'deg)';
    document.body.append(el); setTimeout(() => el.remove(), 7500);
  }
}

/* ===== shared course card ===== */
function card(c, mine) {
  return `<div class="col-sm-6 col-lg-4"><a href="course.html?id=${c.courseId}" class="text-reset text-decoration-none"><div class="card ev h-100 glass">
   <div class="ev-img ${catClass(c.category)}">${imgTag(c)}<span class="ev-cat">${ICON[c.category] || '📚'} ${esc(c.category)}</span><span class="ev-rate">★ ${c.rating}</span></div>
   <div class="card-body"><h5>${esc(c.title)}</h5>
   <p class="small text-secondary mb-2"><i class="bi bi-person-video3"></i> ${esc(c.instructor)}</p>
   <p class="small text-secondary mb-3"><i class="bi bi-clock"></i> ${c.durationHours}h · <i class="bi bi-collection-play"></i> ${lessonCount(c)} lessons · ${esc(c.level)}</p>
   <div class="d-flex justify-content-between align-items-center"><span class="fw-bold fs-5">${money(c.price)}</span>
   <small class="text-secondary">${mine ? '✓ Enrolled' : c.enrolledCount + ' students'}</small></div></div></div></a></div>`;
}

const UI_CSS = CERT_CSS + `
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
.lh-x:hover { color: #fff; }
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
.admin-pill { display: inline-block; margin-left: 0.4rem; padding: 0.15rem 0.6rem; border-radius: 50rem; background: linear-gradient(135deg, #f59e0b, #fbbf24); color: #1a1040; font-size: 0.7rem; font-weight: 800; letter-spacing: 0.05em; }
.admin-nav { border-bottom: 2px solid #f59e0b !important; }
`;

/* ===== shared layout: background, navbar, footer, scroll-to-top, toasts ===== */
(function layout() {
  const p = location.pathname.split('/').pop() || 'index.html', on = h => p === h ? ' active' : '';
  document.body.classList.add('d-flex', 'flex-column', 'min-vh-100');
  document.head.insertAdjacentHTML('beforeend', '<style>' + UI_CSS + '</style>');
  document.head.insertAdjacentHTML('beforeend', '<link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800&display=swap" rel="stylesheet">');
  document.body.insertAdjacentHTML('afterbegin', `<div class="bg"><i></i><i></i><i></i><i></i></div>
  <nav class="navbar navbar-expand-lg sticky-top glass${state.user?.role === 'ADMIN' ? ' admin-nav' : ''}"><div class="container">
   <a class="navbar-brand fw-bold" href="index.html"><i class="bi bi-mortarboard-fill"></i> Learn<span>Hub</span></a>
   <button class="navbar-toggler" data-bs-toggle="collapse" data-bs-target="#nav"><span class="navbar-toggler-icon"></span></button>
   <div class="collapse navbar-collapse" id="nav"><ul class="navbar-nav me-auto">
    <li class="nav-item"><a class="nav-link${on('index.html')}" href="index.html">Home</a></li>
    <li class="nav-item"><a class="nav-link" href="index.html#courses">Courses</a></li>
    <li class="nav-item"><a class="nav-link" href="index.html#about">About</a></li>
    <li class="nav-item"><a class="nav-link" href="index.html#contact">Contact</a></li>
    ${state.user ? `<li class="nav-item"><a class="nav-link${on('profile.html')}" href="profile.html">My Learning</a></li>` : ''}
    ${state.user?.role === 'ADMIN' ? `<li class="nav-item"><a class="nav-link${on('dashboard.html')}" href="dashboard.html">Dashboard</a></li><li class="nav-item"><a class="nav-link${on('admin.html')}" href="admin.html"><i class="bi bi-speedometer2"></i> Dashboard</a></li>` : ''}
   </ul>
   ${state.user ? `${state.user.role === 'ADMIN' ? '<span class="badge bg-warning text-dark me-2"><i class="bi bi-shield-check"></i> ADMIN</span>' : ''}<span class="me-3">Hi, ${esc(state.user.fullName)}${state.user.role === 'ADMIN' ? '<span class="admin-pill"><i class="bi bi-shield-fill-check"></i> ADMIN</span>' : ''}</span><button class="btn btn-outline-light btn-sm" onclick="logout()">Logout</button>`
      : `<a href="login.html" class="btn btn-outline-light btn-sm me-2">Login</a><a href="signup.html" class="btn btn-primary btn-sm">Sign up</a>`}
   </div></div></nav>`);
  document.body.insertAdjacentHTML('beforeend', `<footer class="glass text-center py-4 mt-auto">
   <div class="fs-5 fw-bold mb-2"><i class="bi bi-mortarboard-fill"></i> LearnHub</div>
   <div class="links mb-2"><a href="index.html" class="mx-2">Home</a><a href="index.html#courses" class="mx-2">Courses</a><a href="index.html#about" class="mx-2">About</a><a href="index.html#contact" class="mx-2">Contact</a></div>
   <div class="mb-2"><a href="#" class="mx-2"><i class="bi bi-facebook"></i></a><a href="#" class="mx-2"><i class="bi bi-instagram"></i></a>
   <a href="#" class="mx-2"><i class="bi bi-twitter-x"></i></a><a href="#" class="mx-2"><i class="bi bi-github"></i></a></div>
   <small class="text-secondary">&copy; 2026 LearnHub. All rights reserved.</small></footer>
   <button id="toTop" class="btn btn-primary rounded-circle" aria-label="Scroll to top"><i class="bi bi-arrow-up"></i></button>
   <div id="toasts" class="toast-container position-fixed top-0 end-0 p-3"></div>
   <div class="modal fade" id="confirmModal" tabindex="-1"><div class="modal-dialog modal-dialog-centered modal-sm"><div class="modal-content text-center p-3">
    <div class="modal-body"><div id="cfIcon" class="cf-icon danger"><i class="bi bi-exclamation-triangle-fill"></i></div><h5 id="cfTitle" class="mb-2"></h5><p id="cfMsg" class="text-secondary mb-4"></p>
    <div class="d-flex gap-2"><button class="btn btn-outline-light flex-fill" data-bs-dismiss="modal">Cancel</button><button id="cfOk" class="btn btn-danger flex-fill"></button></div></div></div></div></div>`);
  addEventListener('scroll', () => $('#toTop').classList.toggle('show', scrollY > 300));
  $('#toTop').onclick = () => scrollTo({ top: 0, behavior: 'smooth' });
  const f = JSON.parse(sessionStorage.getItem('flash') || 'null');
  if (f) { sessionStorage.removeItem('flash'); toast(f[0], f[1]); }
})();
