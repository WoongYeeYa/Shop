(() => {
  'use strict';
  const count = document.querySelector('#bag-count');
  const purchaseForm = document.querySelector('.purchase-form');
  const toast = document.querySelector('.toast');
  if (!purchaseForm) return;
  if (purchaseForm.dataset.auth === 'true') {
    fetch(purchaseForm.dataset.api).then(response => response.json()).then(result => {
      if (result.success && count) count.textContent = String(result.count);
    }).catch(() => {});
  }
  purchaseForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (purchaseForm.dataset.auth !== 'true') {
      window.location.href = purchaseForm.dataset.login;
      return;
    }
    const quantity = new FormData(purchaseForm).get('quantity');
    const params = new URLSearchParams({action: 'add', productId: purchaseForm.dataset.productId, quantity});
    try {
      const response = await fetch(purchaseForm.dataset.api, {method: 'POST', headers: {'Content-Type': 'application/x-www-form-urlencoded', 'X-CSRF-Token': purchaseForm.dataset.csrf}, body: params});
      const result = await response.json();
      if (!result.success) throw new Error(result.message);
      if (count) count.textContent = String(result.count);
      show(`${quantity}개를 장바구니에 담았습니다.`);
    } catch (error) { show(error.message || '장바구니 처리에 실패했습니다.'); }
  });
  function show(message) { if (!toast) return; toast.textContent = message; toast.hidden = false; window.setTimeout(() => { toast.hidden = true; }, 2400); }
})();
