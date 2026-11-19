/* ===== CONFIG: the site always talks to the live Spring Boot API ===== */
const CONFIG = {
  API_BASE: '/api',
  // Google sign-in: paste your OAuth "Web application" Client ID here (Google Cloud Console > APIs & Services > Credentials).
  // A Client ID is public, it is safe to commit; never commit the client SECRET.
  GOOGLE_CLIENT_ID: '387986591955-kkcc9gpidmqiam05l6ash61vfl5sh27v.apps.googleusercontent.com'
};
const ICON = { Programming: '💻', Data: '📊', Design: '🎨', Business: '💼', Languages: '🌍', Marketing: '📣' };
const CATS = Object.keys(ICON); // seed categories only; the real list comes from GET /categories
const $ = s => document.querySelector(s), $$ = s => document.querySelectorAll(s);
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const fmt = d => new Date(d).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
const formData = f => Object.fromEntries(new FormData(f));
// Remember me: a remembered login lives in localStorage (survives closing the browser); otherwise in sessionStorage (cleared when the tab/browser closes)
const stored = k => sessionStorage.getItem(k) ?? localStorage.getItem(k);
const state = { user: JSON.parse(stored('user') || 'null'), token: stored('token') };
if (state.user?.role === 'INSTRUCTOR') { [localStorage, sessionStorage].forEach(s => { s.removeItem('user'); s.removeItem('token'); }); state.user = null; state.token = null; } // the instructor role no longer exists: drop any session saved before it was removed

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
function setSession(user, token, remember) { // remember omitted = keep the session wherever it already lives (new sessions are remembered)
  const keep = remember ?? sessionStorage.getItem('user') === null, [on, off] = keep ? [localStorage, sessionStorage] : [sessionStorage, localStorage];
  state.user = user; state.token = token;
  on.setItem('user', JSON.stringify(user)); on.setItem('token', token); off.removeItem('user'); off.removeItem('token');
}
function updateToken(t) { if (!t) return; state.token = t; [localStorage, sessionStorage].forEach(s => { if (s.getItem('token')) s.setItem('token', t); }); } // after a password change the server issues a new token
function clearSession() { [localStorage, sessionStorage].forEach(s => { s.removeItem('user'); s.removeItem('token'); }); state.user = null; state.token = null; }
async function logout() { try { await api('/auth/logout', 'POST'); } catch (e) { } clearSession(); flash('Logged out', 'info'); location.href = 'index.html'; } // the server ends the token first, then the browser forgets it
/* ===== Google sign-in =====
   Put your OAuth Web Client ID in CONFIG.GOOGLE_CLIENT_ID at the top of this file. Google gives the page a signed ID token (JWT)
   which is POSTed to /auth/google; the server verifies that token with Google. */
const goNext = () => { const n = new URLSearchParams(location.search).get('next'); location.href = n && /^[\w.\-]+\.html(\?[\w=&%.\-]*)?$/.test(n) ? n : 'index.html'; };
async function googleSignIn(credential) {
  try {
    const r = await api('/auth/google', 'POST', { credential });
    setSession(r.user, r.token); flash(r.created ? 'Welcome to SkillTrail! Your account was created.' : 'Signed in with Google'); goNext();
  } catch (x) { toast(x.message || 'Google sign-in failed', 'danger'); }
}
const GOOGLE_G = '<svg width="18" height="18" viewBox="0 0 48 48" aria-hidden="true"><path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/><path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/><path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/><path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/></svg>';
const loadGsi = () => new Promise((ok, no) => {
  if (window.google?.accounts?.id) return ok();
  const s = document.createElement('script'); s.src = 'https://accounts.google.com/gsi/client?hl=en'; s.async = true;
  s.onload = ok; s.onerror = () => no(new Error('Could not load Google sign-in')); document.head.append(s);
});
async function initGoogleButton(box) {
  if (!box) return;
  const id = CONFIG.GOOGLE_CLIENT_ID;
  const plain = () => {
    box.innerHTML = `<button type="button" class="btn btn-outline-light w-100 google-btn">${GOOGLE_G} Continue with Google</button>`;
    box.firstChild.onclick = () => id ? toast('Google sign-in could not be loaded. Check your connection.', 'danger')
      : toast('Google sign-in is not configured. Set CONFIG.GOOGLE_CLIENT_ID in js/common.js.', 'warning');
  };
  if (!id) return plain();
  try {
    await loadGsi();
    const draw = () => { box.innerHTML = ''; google.accounts.id.renderButton(box, { type: 'standard', theme: currentTheme() === 'light' ? 'outline' : 'filled_black', size: 'large', shape: 'pill', text: 'signin_with', width: Math.min(400, box.clientWidth || 360) }); };
    google.accounts.id.initialize({ client_id: id, callback: r => googleSignIn(r.credential) });
    draw(); document.addEventListener('themechange', draw);
  } catch (e) { plain(); }
}

/* ===== Sign-up validation: one rule set used by signup.html (inline messages) and mirrored by the Spring Boot SignupValidator ===== */
const SIGNUP_RULES = {
  fullName: v => { v = String(v || '').trim(); return !v ? 'Full name is required' : v.length < 2 ? 'Full name must be at least 2 characters' : v.length > 100 ? 'Full name must be at most 100 characters' : !/^[\p{L}][\p{L} .'\-]*$/u.test(v) ? 'Full name can only contain letters, spaces, . \' and -' : ''; },
  email: v => { v = String(v || '').trim(); return !v ? 'Email is required' : v.length > 150 ? 'Email must be at most 150 characters' : !/^[A-Za-z0-9._%+\-]+@[A-Za-z0-9\-]+(\.[A-Za-z0-9\-]+)*\.[A-Za-z]{2,}$/.test(v) ? 'Enter a valid email, for example name@gmail.com' : ''; },
  password: v => { v = String(v || ''); return !v ? 'Password is required' : v.length < 6 ? 'Password must be at least 6 characters' : v.length > 64 ? 'Password must be at most 64 characters' : /\s/.test(v) ? 'Password cannot contain spaces' : !/[A-Za-z]/.test(v) || !/\d/.test(v) ? 'Password needs at least one letter and one number' : ''; }
};
function validateSignup(d) { // returns { field: message } for every invalid field; {} = valid
  const out = {}; Object.keys(SIGNUP_RULES).forEach(k => { const m = SIGNUP_RULES[k](d?.[k]); if (m) out[k] = m; }); return out;
}

const currentRole = () => state.user?.role || 'GUEST'; // GUEST = nobody logged in (never stored in the database)
function guard(...roles) {
  if (!state.user) { flash('Guests cannot open that page. Please log in or sign up.', 'warning'); location.href = 'login.html?next=' + encodeURIComponent(location.pathname.split('/').pop() + location.search); return false; }
  if (roles.length && !roles.includes(state.user.role)) { flash('You do not have access to that page', 'danger'); location.href = 'index.html'; return false; }
  return true;
}

/* ===== API LAYER ===== */
async function api(path, method = 'GET', body) {
  const res = await fetch(CONFIG.API_BASE + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(state.token && { Authorization: 'Bearer ' + state.token }) },
    body: body ? JSON.stringify(body) : undefined
  });
  if (!res.ok) {
    const e = await res.json().catch(() => ({})), message = e.message || res.statusText;
    if (res.status === 403 && state.user && /banned/i.test(message)) { // an admin banned this account while it was signed in: end the session
      clearSession(); flash(message, 'danger'); location.href = 'login.html'; await new Promise(() => {});
    }
    throw { status: res.status, message };
  }
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
.admin-pill { display: inline-block; margin-left: 0.4rem; padding: 0.15rem 0.6rem; border-radius: 50rem; font-size: 0.7rem; font-weight: 800; letter-spacing: 0.05em; color: #1a1040; }
.admin-pill { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.admin-nav { border-bottom: 2px solid #f59e0b !important; }
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
const certificateHtml = e => `<div class="cert-wrap"><div class="cert"><div class="cert-in">
  <div class="cert-brand"><i class="bi bi-mortarboard-fill"></i> Skill<span>Trail</span></div>
  <div class="cert-mid"><div class="cert-small">CERTIFICATE OF COMPLETION</div><div class="cert-line">This certifies that</div>
  <div class="cert-name">${esc(e.studentName || state.user?.fullName || 'Learner')}</div>
  <div class="cert-line">has successfully completed the course</div><div class="cert-course">${esc(e.title)}</div></div>
  <div class="cert-foot"><div><b>${certDate(e)}</b><small>Date</small></div><div><b>${certId(e)}</b><small>Certificate ID</small></div></div></div></div></div>`;
// Print / Save as PDF: opens a blank A4-landscape page that the certificate fills edge to edge
function printCertificate(e) {
  const w = open('', '_blank'); if (!w) { toast('Allow pop-ups to print your certificate', 'warning'); return; }
  w.document.write('<!doctype html><title>Certificate - ' + esc(e.title) + '</title><style>' + CERT_CSS + '@page{size:A4 landscape;margin:0}html,body{margin:0;height:100%;background:#fff;font-family:Poppins,Arial,sans-serif}.cert-wrap{width:100vw;height:100vh}.cert{height:100%;aspect-ratio:auto;border-radius:0}.cert-brand i{display:none}</style>' + certificateHtml(e) + '<script>onload=()=>setTimeout(()=>print(),300)<\/script>');
  w.document.close();
}
// Draws the certificate on a canvas (1754x1240 = A4 landscape @150dpi) so it can be downloaded or copied as a PNG
async function certificateCanvas(e) {
  try { await Promise.all([document.fonts.load('800 60px Poppins'), document.fonts.load('600 40px Poppins'), document.fonts.load('400 30px Poppins')]); } catch { }
  const W = 1754, H = 1240, cv = document.createElement('canvas'); cv.width = W; cv.height = H; const g = cv.getContext('2d'), F = 'Poppins, Arial, sans-serif';
  const rr = (x, y, w, h, r) => { g.beginPath(); g.roundRect ? g.roundRect(x, y, w, h, r) : g.rect(x, y, w, h); };
  const fit = (t, weight, size, max) => { do { g.font = weight + ' ' + size + 'px ' + F; size -= 2; } while (g.measureText(t).width > max && size > 20); };
  g.fillStyle = '#fff'; g.fillRect(0, 0, W, H); g.textAlign = 'center'; g.textBaseline = 'alphabetic';
  g.strokeStyle = '#6d5dfc'; g.lineWidth = 12; rr(24, 24, W - 48, H - 48, 40); g.stroke();
  g.strokeStyle = '#c7c2ff'; g.lineWidth = 3; rr(64, 64, W - 128, H - 128, 28); g.stroke();
  g.font = '800 76px ' + F; const a = 'Skill', b = 'Trail', wa = g.measureText(a).width, wb = g.measureText(b).width, x0 = W / 2 - (wa + wb) / 2;
  g.textAlign = 'left'; g.fillStyle = '#1a1040'; g.fillText(a, x0, 230); g.fillStyle = '#6d5dfc'; g.fillText(b, x0 + wa, 230); g.textAlign = 'center';
  g.fillStyle = '#6d5dfc'; g.font = '700 34px ' + F; if ('letterSpacing' in g) g.letterSpacing = '10px'; g.fillText('CERTIFICATE OF COMPLETION', W / 2, 370); if ('letterSpacing' in g) g.letterSpacing = '0px';
  g.fillStyle = '#555'; g.font = '400 38px ' + F; g.fillText('This certifies that', W / 2, 470);
  const name = e.studentName || state.user?.fullName || 'Learner'; g.fillStyle = '#1a1040'; fit(name, 800, 130, 1300); g.fillText(name, W / 2, 620);
  const nw = Math.min(1300, g.measureText(name).width + 120); g.fillStyle = '#6d5dfc'; g.fillRect(W / 2 - nw / 2, 645, nw, 5);
  g.fillStyle = '#555'; g.font = '400 38px ' + F; g.fillText('has successfully completed the course', W / 2, 740);
  g.fillStyle = '#1a1040'; fit(e.title, 700, 84, 1400); g.fillText(e.title, W / 2, 860);
  [[certDate(e), 'Date', 470], [certId(e), 'Certificate ID', W - 470]].forEach(([v, l, x]) => { g.fillStyle = '#1a1040'; g.font = '700 38px ' + F; g.fillText(v, x, 1040); g.fillStyle = '#777'; g.font = '400 30px ' + F; g.fillText(l, x, 1090); });
  return cv;
}
const canvasBlob = cv => new Promise(ok => cv.toBlob(ok, 'image/png'));
async function downloadCertificate(e) {
  try {
    const blob = await canvasBlob(await certificateCanvas(e)), a = document.createElement('a');
    a.href = URL.createObjectURL(blob); a.download = 'SkillTrail-Certificate-' + certId(e) + '.png'; document.body.append(a); a.click(); a.remove(); setTimeout(() => URL.revokeObjectURL(a.href), 2000);
    toast('Certificate downloaded');
  } catch (x) { toast('Could not create the certificate image', 'danger'); }
}
async function copyCertificate(e) {
  try {
    const blob = await canvasBlob(await certificateCanvas(e));
    await navigator.clipboard.write([new ClipboardItem({ 'image/png': blob })]); toast('Certificate image copied to clipboard');
  } catch (x) {
    try { await navigator.clipboard.writeText(`${e.studentName || state.user?.fullName} completed "${e.title}" on ${certDate(e)}. Certificate ID: ${certId(e)}`); toast('Image copy is not supported here, so the certificate details were copied as text', 'info'); }
    catch { toast('Copy is not available in this browser', 'warning'); }
  }
}
function confetti() {
  if (matchMedia('(prefers-reduced-motion: reduce)').matches) return;
  const box = document.createElement('div'); box.className = 'confetti'; const colors = ['#6d5dfc', '#22d3ee', '#f59e0b', '#22c55e', '#ec4899'];
  for (let i = 0; i < 80; i++) { const b = document.createElement('i'); b.style.cssText = `left:${Math.random() * 100}%;background:${colors[i % 5]};animation-delay:${Math.random() * 1.2}s;animation-duration:${2.4 + Math.random() * 2}s;transform:rotate(${Math.random() * 360}deg)`; box.append(b); }
  document.body.append(box); setTimeout(() => box.remove(), 5500);
}
const CERT_CSS = `.cert-wrap{container-type:inline-size;width:100%}
.cert{box-sizing:border-box;width:100%;aspect-ratio:297/210;border:.25em solid #6d5dfc;border-radius:.9em;padding:.5em;background:#fff;color:#1a1040;font-size:1.9cqw;font-family:Poppins,Arial,sans-serif;-webkit-print-color-adjust:exact;print-color-adjust:exact}
.cert-in{box-sizing:border-box;height:100%;border:1px solid #c7c2ff;border-radius:.6em;padding:2em 2.5em;display:flex;flex-direction:column;align-items:center;justify-content:space-between;text-align:center}
.cert-brand{font-weight:800;font-size:2.2em;line-height:1.2}.cert-brand span{color:#6d5dfc}
.cert-mid{display:flex;flex-direction:column;align-items:center;gap:.5em;max-width:100%}
.cert-small{letter-spacing:.3em;font-size:.9em;font-weight:700;color:#6d5dfc}.cert-line{color:#555;font-size:1.1em}
.cert-name{font-size:3.4em;font-weight:800;line-height:1.25;border-bottom:.06em solid #6d5dfc;padding:0 1em;max-width:100%;overflow-wrap:anywhere}
.cert-course{font-size:2.2em;font-weight:700;line-height:1.25;max-width:100%}
.cert-foot{display:flex;justify-content:space-around;width:100%}.cert-foot b{display:block;font-size:1.1em}.cert-foot small{color:#777;font-size:.9em}`;

/* ===== personal courses: private / public switch. Public needs an admin's approval; a rejection arrives as a notification with the reason ===== */
const VIS = { DRAFT: ['secondary', 'lock-fill', 'Private'], PENDING: ['warning text-dark', 'hourglass-split', 'Waiting for admin'], REJECTED: ['danger', 'x-octagon-fill', 'Not approved'], PUBLISHED: ['success', 'globe2', 'Public'] };
const visBadge = s => { const v = VIS[s] || VIS.DRAFT; return `<span class="badge bg-${v[0]}"><i class="bi bi-${v[1]}"></i> ${v[2]}</span>`; };
const visToggle = c => `<div class="form-check form-switch m-0"><input class="form-check-input" type="checkbox" role="switch" id="vis${c.courseId}" data-vis="${c.courseId}" data-title="${esc(c.title)}" data-status="${c.status}" ${c.status === 'PENDING' || c.status === 'PUBLISHED' ? 'checked' : ''}>
  <label class="form-check-label small" for="vis${c.courseId}">${c.status === 'PUBLISHED' ? 'Public' : c.status === 'PENDING' ? 'Public (waiting for approval)' : 'Make public'}</label></div>`;
const visNote = c => c.status === 'REJECTED' ? `<div class="alert alert-danger py-2 small mb-0 mt-2"><b>Not approved:</b> ${esc(c.rejectReason || 'No reason was given.')}<div class="text-secondary">Improve the course, then switch Make public on again.</div></div>`
  : c.status === 'PENDING' ? '<div class="small text-secondary mt-2">An admin is reviewing this course. Switch off to withdraw the request.</div>'
  : c.status === 'PUBLISHED' ? '<div class="small text-secondary mt-2">Listed with the other courses. Only an admin can edit it now.</div>' : '';
/* input = the switch that was clicked; returns the updated course, or null when cancelled or refused (the switch is put back) */
async function changeVisibility(input) {
  const makePublic = input.checked, id = input.dataset.vis, title = input.dataset.title;
  const ok = makePublic
    ? await confirmBox('Ask an admin to make "' + title + '" public? If it is approved it is listed with the other courses and anyone can join it for free. If not, you get a message with the reason.', { title: 'Make course public', confirmText: 'Send request', danger: false })
    : await confirmBox(input.dataset.status === 'PENDING' ? 'Withdraw your request? The course stays private.' : 'Make "' + title + '" private again? It leaves the course list. This only works while nobody else is enrolled.', { title: 'Make course private', confirmText: 'Make private', danger: false });
  if (!ok) { input.checked = !makePublic; return null; }
  try {
    const c = await api('/personal/courses/' + id + (makePublic ? '/publish' : '/unpublish'), 'POST');
    toast(makePublic ? 'Request sent. An admin will review your course.' : 'Course is private again', makePublic ? 'success' : 'info');
    return c;
  } catch (e) { input.checked = !makePublic; toast(e.message || 'Could not change visibility', 'danger'); return null; }
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
  document.head.insertAdjacentHTML('beforeend', '<link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800&display=swap" rel="stylesheet"><style>' + UI_CSS + CERT_CSS + '</style><link href="css/fx.css" rel="stylesheet">');
  const pill = '';
  const avatar = u?.avatar || '';
  document.body.insertAdjacentHTML('afterbegin', `<div class="bg"><i></i><i></i><i></i><i></i></div>
  <nav class="navbar navbar-expand-lg sticky-top glass ${role === 'ADMIN' ? 'admin-nav' : ''}"><div class="container">
   <a class="navbar-brand fw-bold" href="index.html"><b class="brand-mark"><i class="bi bi-mortarboard-fill"></i></b><span class="brand-word">Skill<span>Trail</span></span></a>
   <button class="navbar-toggler" data-bs-toggle="collapse" data-bs-target="#nav"><span class="navbar-toggler-icon"></span></button>
   <div class="collapse navbar-collapse" id="nav"><ul class="navbar-nav me-auto">
    <li class="nav-item"><a class="nav-link${on('index.html')}" href="index.html">Home</a></li>
    <li class="nav-item"><a class="nav-link${on('courses.html')}" href="courses.html">Courses</a></li>
    <li class="nav-item"><a class="nav-link${on('support.html')}" href="support.html">Support</a></li>
    <li class="nav-item"><a class="nav-link${on('about.html')}" href="about.html">About</a></li>
    <li class="nav-item"><a class="nav-link${on('contact.html')}" href="contact.html">Contact</a></li>
    ${u ? `<li class="nav-item"><a class="nav-link${on('my-courses.html')}" href="my-courses.html">My Courses</a></li>` : ''}
    ${role === 'STUDENT' ? `<li class="nav-item"><a class="nav-link${on('personal-courses.html') || on('personal-course.html')}" href="personal-courses.html"><i class="bi bi-journal-plus"></i> Personal</a></li>` : ''}
    ${role === 'ADMIN' ? `<li class="nav-item"><a class="nav-link${on('admin.html')}" href="admin.html"><i class="bi bi-shield-lock"></i> Admin</a></li>` : ''}

   </ul>
   ${u ? `<div class="dropdown d-inline-block me-2"><button class="btn btn-outline-light btn-sm position-relative" id="bell" data-bs-toggle="dropdown" aria-label="Notifications"><i class="bi bi-bell"></i><span id="bellN" class="badge bg-danger rounded-pill position-absolute top-0 start-100 translate-middle d-none"></span></button>
      <div class="dropdown-menu dropdown-menu-end p-0 glass" id="bellList" style="width:320px;max-height:380px;overflow:auto"><div class="notif text-secondary">No notifications</div></div></div>
     <a href="profile.html" class="user-chip text-reset text-decoration-none me-2${on('profile.html')}">${avatar ? `<img class="nav-avatar" src="${avatar}" alt="Profile">` : `<span class="nav-avatar nav-avatar-fallback">${esc((u.fullName || 'U').split(' ').map(x => x[0]).slice(0, 2).join(''))}</span>`}<span>Hi, ${esc(u.fullName)}</span></a><button class="btn btn-outline-light btn-sm" onclick="logout()">Logout</button>`
      : `<span class="badge rounded-pill text-bg-secondary me-2" title="You are browsing as a guest. Log in to enroll, buy and review courses.">Guest</span><a href="login.html" class="btn btn-outline-light btn-sm me-2">Login</a><a href="signup.html" class="btn btn-primary btn-sm">Sign up</a>`}
   <button class="btn btn-sm theme-toggle ms-lg-2" id="themeToggle" type="button"></button></div></div></nav>`);
  const themeButton = document.getElementById('themeToggle'); if (themeButton) { themeButton.addEventListener('click', toggleTheme); paintThemeToggle(); }
  document.body.insertAdjacentHTML('beforeend', `<footer class="footer2 glass mt-auto"><div class="container"><div class="row g-4">
    <div class="col-lg-4"><a class="navbar-brand fw-bold mb-3" href="index.html"><b class="brand-mark"><i class="bi bi-mortarboard-fill"></i></b><span class="brand-word">Skill<span>Trail</span></span></a>
     <p class="text-secondary mb-3" style="max-width:330px">Expert-led online courses you can take anytime, on any device. Track your progress, pass quizzes and earn certificates.</p>
     <div class="soc"><a href="#" aria-label="Facebook"><i class="bi bi-facebook"></i></a><a href="#" aria-label="Instagram"><i class="bi bi-instagram"></i></a><a href="#" aria-label="X"><i class="bi bi-twitter-x"></i></a><a href="#" aria-label="GitHub"><i class="bi bi-github"></i></a></div></div>
    <div class="col-6 col-lg-2"><h6>Explore</h6><div class="fl"><a href="index.html">Home</a><a href="courses.html">Courses</a><a href="my-courses.html">My courses</a><a href="about.html">About us</a></div></div>
    <div class="col-6 col-lg-3"><h6>Support</h6><div class="fl"><a href="support.html#help">Help &amp; FAQ</a><a href="support.html#support">Send a request</a><a href="contact.html">Contact</a><a href="login.html">Login</a></div></div>
    <div class="col-lg-3"><h6>Get in touch</h6><div class="fl"><a href="contact.html"><i class="bi bi-geo-alt me-2"></i>FPT University, Hoa Lac, Hanoi</a><a href="mailto:support@skilltrail.com"><i class="bi bi-envelope me-2"></i>support@skilltrail.com</a><a href="contact.html"><i class="bi bi-clock me-2"></i>Mon - Fri, 8:00 - 17:00</a></div></div>
   </div><div class="bottom d-flex justify-content-between flex-wrap gap-2"><small class="text-secondary">&copy; 2026 SkillTrail. All rights reserved.</small></div></div></footer>
   <button id="toTop" class="btn btn-primary rounded-circle" aria-label="Scroll to top"><i class="bi bi-arrow-up"></i></button>
   <div id="toasts" class="toast-container position-fixed top-0 end-0 p-3"></div>
   <div class="modal fade" id="confirmModal" tabindex="-1"><div class="modal-dialog modal-dialog-centered modal-sm"><div class="modal-content text-center p-3">
    <div class="modal-body"><div id="cfIcon" class="cf-icon danger"><i class="bi bi-exclamation-triangle-fill"></i></div><h5 id="cfTitle" class="mb-2"></h5><p id="cfMsg" class="text-secondary mb-4"></p>
    <div class="d-flex gap-2"><button class="btn btn-outline-light flex-fill" data-bs-dismiss="modal">Cancel</button><button id="cfOk" class="btn btn-danger flex-fill"></button></div></div></div></div></div>`);
  
  const fxScript = document.createElement('script'); fxScript.src = 'js/fx.js'; document.body.appendChild(fxScript);

  /* ---- Smooth navigation to a section (support.html#help / #support / #about / #contact) ------------
     Arriving from another page (footer, notification, ...): the page opens at the top, waits until
     its layout has finished loading (content is fetched and rendered after load), then glides
     down to the section. Waiting matters: scrolling while content is still being inserted would stop
     short. Any wheel / touch / key press by the visitor cancels it. */
  const NAV_OFFSET = 70; // sticky navbar height; must match scroll-padding-top in style.css
  const glideTo = id => {
    const t = id && document.getElementById(id);
    if (t) scrollTo({ top: Math.max(0, t.getBoundingClientRect().top + scrollY - NAV_OFFSET), behavior: 'smooth' });
    else scrollTo({ top: 0, behavior: 'smooth' });
  };
  const instantTop = () => { const r = document.documentElement, p = r.style.scrollBehavior; r.style.scrollBehavior = 'auto'; scrollTo(0, 0); r.style.scrollBehavior = p; };
  let arrival = null;
  const glideOnArrival = () => {
    if (arrival) arrival.cancel();
    if (!location.hash || location.hash.length < 2) return;
    let id; try { id = decodeURIComponent(location.hash.slice(1)); } catch { return; }
    if (!document.getElementById(id)) return;
    if ('scrollRestoration' in history) history.scrollRestoration = 'manual';
    const events = ['wheel', 'touchstart', 'mousedown', 'keydown'];
    let ro, quiet, hard, finished = false;
    const cancel = () => { if (finished) return; finished = true; clearTimeout(quiet); clearTimeout(hard); ro && ro.disconnect(); events.forEach(e => removeEventListener(e, cancel, true)); };
    const go = () => { if (finished) return; cancel(); glideTo(id); };
    const tick = () => { if (finished) return; instantTop(); clearTimeout(quiet); quiet = setTimeout(go, 450); }; // layout still changing: stay at top, restart the wait
    events.forEach(e => addEventListener(e, cancel, { capture: true, passive: true }));
    hard = setTimeout(go, 2500); // never wait longer than this
    if (window.ResizeObserver) { ro = new ResizeObserver(tick); ro.observe(document.body); }
    tick();
    arrival = { cancel };
  };
  glideOnArrival();
  addEventListener('pageshow', e => { if (e.persisted) glideOnArrival(); });

  // Anchors to a section of the page you are already on (support.html#about ...) glide instead of jumping or reloading.
  document.addEventListener('click', e => {
    const a = e.target.closest && e.target.closest('a[href]');
    if (!a || e.defaultPrevented || e.metaKey || e.ctrlKey || e.shiftKey || e.button !== 0 || a.target === '_blank') return;
    let u; try { u = new URL(a.getAttribute('href'), location.href); } catch { return; }
    const here = location.pathname.replace(/\/+$/, '') || '/';
    const there = u.pathname.replace(/\/+$/, '') || '/';
    if (u.origin !== location.origin || there !== here || u.search !== location.search || u.hash.length < 2) return; // only #section links on this same page
    e.preventDefault();
    if (arrival) arrival.cancel();
    const id = u.hash.length > 1 ? decodeURIComponent(u.hash.slice(1)) : '';
    glideTo(id);
    history.pushState(null, '', u.pathname + u.search + u.hash);
    document.querySelector('.navbar-collapse.show')?.classList.remove('show');
  });

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

/* ===== ADD-ON: shopping cart =====
   Appended after the shared layout so nothing above is modified. It (1) adds a cart icon with a count badge to the navbar,
   and (2) adds an "Add to cart" button next to "Buy now" on course.html. */
async function refreshCartBadge() {
  const b = document.getElementById('cartN'), link = document.getElementById('cartLink'); if (!b || !link) return;
  try {
    const c = await api('/cart'); link.classList.remove('d-none');
    b.textContent = c.count; b.classList.toggle('d-none', !c.count);
  } catch (e) { link.classList.add('d-none'); } // live API without /cart: hide the icon instead of showing errors
}
document.addEventListener('cartchange', refreshCartBadge);
(function cartNav() {
  const u = state.user, role = u?.role, here = location.pathname.split('/').pop() || 'index.html';
  if (!u || !document.querySelector('#nav .navbar-nav')) return;
  if (role !== 'ADMIN') { // admins enroll without payment, so no cart
    const anchor = document.getElementById('bell')?.parentElement;
    if (anchor) anchor.insertAdjacentHTML('beforebegin',
      `<a id="cartLink" href="cart.html" class="btn btn-outline-light btn-sm position-relative me-2 d-none${here === 'cart.html' ? ' active' : ''}" aria-label="Shopping cart"><i class="bi bi-cart3"></i><span id="cartN" class="badge bg-danger rounded-pill position-absolute top-0 start-100 translate-middle d-none"></span></a>`);
    refreshCartBadge();
  }
})();
(function addToCartButton() {
  if (!/\/course\.html$/.test(location.pathname) || !state.user || state.user.role === 'ADMIN') return;
  const id = +new URLSearchParams(location.search).get('id'); if (!id) return;
  let busy = false, mark = '';
  const inject = async () => {
    const buy = document.querySelector('#page [data-act="enroll"]'); // only exists for a non-enrolled visitor
    if (!buy || document.getElementById('cartBtn') || busy) return;
    if (/Enroll for free/i.test(buy.textContent)) return; // free courses enroll in one click, no cart needed
    busy = true;
    try {
      const inCart = (await api('/cart')).items.some(c => c.courseId === id);
      if (document.getElementById('cartBtn') || !document.body.contains(buy)) return;
      buy.insertAdjacentHTML('afterend', inCart
        ? '<a id="cartBtn" href="cart.html" class="btn btn-outline-light w-100 mt-2"><i class="bi bi-cart-check"></i> Go to cart</a>'
        : '<button id="cartBtn" type="button" class="btn btn-outline-light w-100 mt-2"><i class="bi bi-cart-plus"></i> Add to cart</button>');
      const btn = document.getElementById('cartBtn');
      if (btn.tagName === 'BUTTON') btn.onclick = async () => {
        btn.disabled = true;
        try { await api('/cart/' + id, 'POST'); toast('Added to your cart'); document.dispatchEvent(new Event('cartchange')); btn.outerHTML = '<a id="cartBtn" href="cart.html" class="btn btn-outline-light w-100 mt-2"><i class="bi bi-cart-check"></i> Go to cart</a>'; }
        catch (x) { toast(x.message || 'Could not add to cart', 'danger'); btn.disabled = false; }
      };
    } catch (e) { /* cart not available: leave the page as it was */ } finally { busy = false; }
  };
  const page = document.getElementById('page'); if (!page) return;
  new MutationObserver(() => { const k = page.innerHTML.length + ':' + !!document.getElementById('cartBtn'); if (k !== mark) { mark = k; inject(); } }).observe(page, { childList: true, subtree: true });
  inject();
})();
