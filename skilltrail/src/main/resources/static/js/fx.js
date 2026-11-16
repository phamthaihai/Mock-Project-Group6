/* SkillTrail visual effects: ambient shapes, scroll reveal, tilt + spotlight cards, typewriter, count-up, ripple.
   Loaded by layout() in common.js on every page. Everything is skipped for visitors who prefer reduced motion. */
(function () {
  window.__fx = true;
  var d = document, root = d.documentElement;
  var reduce = window.matchMedia && matchMedia('(prefers-reduced-motion: reduce)').matches;
  var fine = window.matchMedia && matchMedia('(pointer: fine)').matches;

  /* ---- 1. ambient shapes behind the page ---- */
  var S = [ // [class, css, depth]
    ['sh-blob', 'width:520px;height:520px;background:linear-gradient(135deg,#6d5dfc,#ff5fa2);top:-12%;left:-9%', 14],
    ['sh-blob', 'width:480px;height:480px;background:linear-gradient(135deg,#22d3ee,#6d5dfc);bottom:-14%;right:-7%', 18],
    ['sh-blob', 'width:340px;height:340px;background:linear-gradient(135deg,#f59e0b,#ff5fa2);top:38%;left:60%;opacity:.2', 10],
    ['sh-ring', 'width:92px;height:92px;top:15%;left:7%', 26],
    ['sh-ring pink', 'width:46px;height:46px;top:80%;left:42%', 34],
    ['sh-ring cyan', 'width:150px;height:150px;top:58%;right:5%', 16],
    ['sh-tri', 'width:64px;height:64px;top:22%;right:11%', 30],
    ['sh-sq', 'width:60px;height:60px;top:64%;left:6%', 22],
    ['sh-hex', 'width:84px;height:84px;top:9%;right:36%', 20],
    ['sh-plus', '', 28, 'top:74%;right:16%'],
    ['sh-dots', 'width:150px;height:150px;top:46%;right:2%', 12],
    ['sh-dots', 'width:110px;height:110px;top:88%;left:14%', 8],
    ['sh-half', 'width:130px;height:65px;top:92%;left:62%', 24],
    ['sh-star', 'width:22px;height:22px;top:32%;left:24%', 38],
    ['sh-star', 'width:16px;height:16px;top:68%;left:56%;animation-delay:-2s', 32],
    ['sh-star', 'width:18px;height:18px;top:12%;left:58%;animation-delay:-1s', 36]
  ];
  var html = '<svg width="0" height="0" style="position:absolute"><defs><linearGradient id="sqg" x1="0" x2="1"><stop offset="0" stop-color="#6d5dfc"/><stop offset="1" stop-color="#ff5fa2"/></linearGradient></defs></svg>';
  S.forEach(function (s) { html += '<div class="fx-l" style="' + (s[3] || '') + ';--d:' + s[2] + '"><i class="sh ' + s[0] + '" style="' + s[1] + '"></i></div>'; });
  html += '<div class="fx-l" style="top:40%;left:3%;--d:20"><svg class="sh sh-squig" viewBox="0 0 160 40"><path d="M2 20 C20 -4 36 44 54 20 S88 -4 106 20 S140 44 158 20"/></svg></div>';
  var fx = d.createElement('div'); fx.className = 'fx'; fx.setAttribute('aria-hidden', 'true'); fx.innerHTML = html;
  var bg = d.querySelector('.bg'); if (bg) bg.insertAdjacentElement('afterend', fx); else d.body.insertBefore(fx, d.body.firstChild);

  if (!reduce && fine) {
    var tx = 0, ty = 0, raf = 0;
    addEventListener('pointermove', function (e) {
      tx = (e.clientX / innerWidth - .5) * 2; ty = (e.clientY / innerHeight - .5) * 2;
      if (!raf) raf = requestAnimationFrame(function () { raf = 0; fx.style.setProperty('--mx0', tx.toFixed(3)); fx.style.setProperty('--my0', ty.toFixed(3)); });
    }, { passive: true });
  }

  /* ---- 2. navbar state, parallax ---- */
  var par = [].slice.call(d.querySelectorAll('[data-parallax]'));
  var tick = 0;
  function onScroll() {
    if (tick) return; tick = requestAnimationFrame(function () {
      tick = 0; var y = scrollY;
      var nav = d.querySelector('.navbar'); if (nav) nav.classList.toggle('nav-scrolled', y > 24);
      if (!reduce) par.forEach(function (el) { el.style.translate = '0 ' + (y * parseFloat(el.dataset.parallax)).toFixed(1) + 'px'; });
    });
  }
  addEventListener('scroll', onScroll, { passive: true }); onScroll();

  /* ---- 3. reveal on scroll (also for cards that are inserted later by page scripts) ---- */
  var io = 'IntersectionObserver' in window ? new IntersectionObserver(function (es) {
    es.forEach(function (e) { if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target); } });
  }, { threshold: .1, rootMargin: '0px 0px -5% 0px' }) : null;
  function arm(scope) {
    if (!scope.querySelectorAll) return;
    var list = scope.matches && scope.matches('[data-reveal], .ev') ? [scope] : [];
    list = list.concat([].slice.call(scope.querySelectorAll('[data-reveal], .ev')));
    list.forEach(function (el) {
      var t = el.classList.contains('ev') ? (el.closest('[class*="col-"]') || el) : el; // cards tilt, so their column animates instead
      if (t.__armed) return; t.__armed = 1;
      if (!t.hasAttribute('data-reveal')) t.setAttribute('data-reveal', '');
      if (t.parentElement && !t.style.getPropertyValue('--delay')) t.style.setProperty('--delay', (([].indexOf.call(t.parentElement.children, t)) % 6) * 90 + 'ms');
      if (io) io.observe(t); else t.classList.add('in');
    });
    [].slice.call(scope.querySelectorAll ? scope.querySelectorAll('[data-count]') : []).forEach(function (el) {
      if (el.__counted) return; el.__counted = 1;
      var go = function () { fxCount(el, parseFloat(el.dataset.count) || 0); };
      if (io) { var o = new IntersectionObserver(function (es) { if (es[0].isIntersecting) { o.disconnect(); go(); } }); o.observe(el); } else go();
    });
  }
  arm(d.body);
  new MutationObserver(function (ms) { ms.forEach(function (m) { m.addedNodes.forEach(function (n) { if (n.nodeType === 1) arm(n); }); }); }).observe(d.body, { childList: true, subtree: true });

  /* ---- 4. count-up, typewriter ---- */
  window.fxCount = function (el, to, dur) {
    dur = dur || 1400; var t0 = performance.now(), dec = String(to).indexOf('.') > -1 ? 1 : 0, suf = el.dataset.suffix || '';
    if (reduce) { el.textContent = to + suf; return; }
    (function step(t) { var p = Math.min(1, (t - t0) / dur), e = 1 - Math.pow(1 - p, 3); el.textContent = (to * e).toFixed(dec).replace(/\B(?=(\d{3})+(?!\d))/g, ',') + suf; if (p < 1) requestAnimationFrame(step); })(t0);
  };
  [].slice.call(d.querySelectorAll('[data-typewriter]')).forEach(function (el) {
    var words; try { words = JSON.parse(el.dataset.typewriter); } catch (e) { return; }
    if (reduce || !words.length) { el.textContent = words[0] || ''; return; }
    var w = 0, c = 0, del = false;
    (function loop() {
      var word = words[w]; el.textContent = word.slice(0, c);
      if (!del && c === word.length) { del = true; return setTimeout(loop, 1500); }
      if (del && c === 0) { del = false; w = (w + 1) % words.length; return setTimeout(loop, 300); }
      c += del ? -1 : 1; setTimeout(loop, del ? 40 : 85);
    })();
  });

  /* ---- 5. tilt + spotlight on cards, ripple on buttons ---- */
  var SEL = '.ev, [data-tilt], .spot';
  d.addEventListener('pointermove', function (e) {
    var t = e.target.closest && e.target.closest(SEL); if (!t) return;
    var r = t.getBoundingClientRect(), x = (e.clientX - r.left) / r.width, y = (e.clientY - r.top) / r.height;
    t.style.setProperty('--mx', (x * 100).toFixed(1) + '%'); t.style.setProperty('--my', (y * 100).toFixed(1) + '%');
    if (!reduce && fine && !t.classList.contains('spot')) { t.style.setProperty('--ry', ((x - .5) * 9).toFixed(2) + 'deg'); t.style.setProperty('--rx', ((.5 - y) * 9).toFixed(2) + 'deg'); }
  }, { passive: true });
  d.addEventListener('pointerout', function (e) {
    var t = e.target.closest && e.target.closest(SEL);
    if (t && !t.contains(e.relatedTarget)) { t.style.setProperty('--rx', '0deg'); t.style.setProperty('--ry', '0deg'); }
  });
  d.addEventListener('pointerdown', function (e) {
    var b = e.target.closest && e.target.closest('.btn'); if (!b || reduce) return;
    var r = b.getBoundingClientRect(), s = Math.max(r.width, r.height), sp = d.createElement('span');
    sp.className = 'ripple'; sp.style.cssText = 'width:' + s + 'px;height:' + s + 'px;left:' + (e.clientX - r.left - s / 2) + 'px;top:' + (e.clientY - r.top - s / 2) + 'px';
    b.appendChild(sp); setTimeout(function () { sp.remove(); }, 650);
  });
})();
