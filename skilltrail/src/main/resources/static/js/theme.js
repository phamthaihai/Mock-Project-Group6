/* Dark / light theme. Loaded in <head> of every page so the saved theme is applied before the first paint.
   The choice is stored in localStorage under "skilltrail-theme" and shared by all pages and tabs. Default: dark. */
(function () {
  var KEY = 'skilltrail-theme', root = document.documentElement;
  function saved() { try { var v = localStorage.getItem(KEY); return v === 'light' || v === 'dark' ? v : null; } catch (e) { return null; } }
  function current() { return root.getAttribute('data-bs-theme') === 'light' ? 'light' : 'dark'; }
  function paint(t) {
    var b = document.getElementById('themeToggle'); if (!b) return;
    var light = t === 'light';
    b.innerHTML = '<i class="bi bi-' + (light ? 'moon-stars-fill' : 'sun-fill') + '"></i>';
    b.setAttribute('aria-label', light ? 'Switch to dark mode' : 'Switch to light mode');
    b.title = light ? 'Switch to dark mode' : 'Switch to light mode';
  }
  function apply(t) { root.setAttribute('data-bs-theme', t); root.style.colorScheme = t; paint(t); }
  window.currentTheme = current;
  window.paintThemeToggle = function () { paint(current()); };
  window.setTheme = function (t) {
    try { localStorage.setItem(KEY, t); } catch (e) { }
    apply(t); document.dispatchEvent(new CustomEvent('themechange', { detail: t }));
  };
  window.toggleTheme = function () { window.setTheme(current() === 'light' ? 'dark' : 'light'); };
  apply(saved() || 'dark');
  window.addEventListener('storage', function (e) { if (e.key === KEY && (e.newValue === 'light' || e.newValue === 'dark')) apply(e.newValue); });
})();
