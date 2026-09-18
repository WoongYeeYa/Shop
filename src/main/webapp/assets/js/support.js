'use strict';
document.querySelectorAll('[data-submit-lock]').forEach(form => {
  form.addEventListener('submit', () => {
    form.querySelectorAll('button[type="submit"],button:not([type])').forEach(button => {
      button.disabled = true;
      button.textContent = '처리 중…';
    });
  });
});
window.addEventListener('pageshow', event => { if (event.persisted) window.location.reload(); });
document.querySelectorAll('[data-json-pretty]').forEach(element => {
  try { element.textContent = JSON.stringify(JSON.parse(element.textContent), null, 2); } catch (_) { /* Keep escaped original text. */ }
});
