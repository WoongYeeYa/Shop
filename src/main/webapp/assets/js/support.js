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
  try { element.textContent = JSON.stringify(JSON.parse(element.textContent), null, 2); } catch (_) {}
});

(() => {
  const editor = document.querySelector('[data-reply-editor]');
  if (!editor || !window.SupportEditor) return;
  const rules = window.SupportEditor;
  const textarea = editor.querySelector('textarea[name="answer"]');
  const version = Number(editor.dataset.version);
  const key = 'myshop:reply:' + editor.dataset.user + ':' + editor.dataset.inquiry;
  const notice = document.getElementById('reply-save-status');
  const restore = document.getElementById('restore-reply');
  const discard = document.getElementById('discard-reply');
  const previous = document.getElementById('previous-reply');
  const previousText = document.getElementById('previous-reply-text');
  let storage;
  try { storage = window.sessionStorage; } catch (_) { storage = null; }
  let initialText = textarea.value, pendingText = null, stored = false, submitting = false;
  let saved = rules.readDraft(storage, key, version);
  if (editor.dataset.published === 'true' && saved.kind === 'stale' && saved.version < version) {
    rules.clearDraft(storage, key); saved = { kind: 'none' };
  }
  if (saved.kind === 'saved') {
    pendingText = saved.text; restore.hidden = false; discard.hidden = false;
    notice.textContent = '이 탭에 보관한 답변이 있습니다. 복원하거나 삭제할 수 있어요.';
  } else if (saved.kind === 'stale') {
    previous.hidden = false; previousText.textContent = saved.text; discard.hidden = false;
    notice.textContent = '담당자 답변이 변경되어 이전 임시 답변은 자동 복원하지 않습니다. 최신 답변과 비교해 주세요.';
  } else if (saved.kind === 'unavailable') {
    notice.textContent = '이 브라우저에서는 임시 보관이 불가능합니다. 이동 전에 내용을 복사해 주세요.';
  }
  function persist() {
    stored = rules.saveDraft(storage, key, version, textarea.value);
    notice.textContent = stored ? '이 탭에 임시 보관했습니다. 고객에게 공개된 답변이 아닙니다.' :
      '임시 보관하지 못했습니다. 이동 전에 내용을 복사해 주세요.';
    discard.hidden = !stored;
  }
  textarea.addEventListener('input', () => {
    pendingText = null; restore.hidden = true; persist();
  });
  restore.addEventListener('click', () => {
    if (pendingText !== null && (textarea.value === initialText || window.confirm('작성 중인 내용을 보관된 답변으로 바꿀까요?'))) {
      textarea.value = pendingText; pendingText = null; restore.hidden = true; persist(); textarea.focus();
    }
  });
  discard.addEventListener('click', () => {
    if (!rules.clearDraft(storage, key)) { notice.textContent = '임시 보관 내용을 삭제하지 못했습니다.'; return; }
    pendingText = null; restore.hidden = true; previous.hidden = true; discard.hidden = true; stored = false;
    notice.textContent = '임시 보관본을 삭제했습니다. 편집 중인 내용은 그대로입니다.';
  });
  editor.addEventListener('submit', () => { persist(); submitting = true; });
  window.addEventListener('beforeunload', event => {
    if (!submitting && textarea.value !== initialText && !stored) { event.preventDefault(); event.returnValue = ''; }
  });

  const statusBox = document.querySelector('[data-ai-status-url]');
  if (!statusBox) return;
  const statusText = document.getElementById('ai-status-text');
  const checkButton = document.getElementById('check-ai-status');
  const draftBox = document.getElementById('live-ai-draft');
  const draftText = document.getElementById('live-ai-draft-text');
  const applyDraft = document.getElementById('apply-ai-draft');
  const review = document.getElementById('live-ai-reason');
  let aiDraft = '', timer = null, attempts = 0, errors = 0, waiting = false, inFlight = false;
  let activeController = null;
  function setStatus(text) { if (statusText.textContent !== text) statusText.textContent = text; }
  function schedule() {
    clearTimeout(timer);
    if (waiting && !document.hidden && attempts < 60) timer = setTimeout(check, 5000);
    else if (waiting && attempts >= 60) { waiting = false; setStatus('자동 확인을 잠시 멈췄습니다. 상태 확인 버튼으로 다시 확인할 수 있어요.'); }
  }
  async function check() {
    if (inFlight || document.hidden) return;
    inFlight = true; checkButton.disabled = true; attempts += 1;
    activeController = new AbortController();
    const timeout = setTimeout(() => activeController?.abort(), 10000);
    try {
      const response = await fetch(statusBox.dataset.aiStatusUrl, {
        credentials: 'same-origin', cache: 'no-store', redirect: 'error',
        headers: { Accept: 'application/json' }, signal: activeController.signal
      });
      if (!response.ok) throw new Error('status unavailable');
      const payload = await response.json();
      if (!payload.success) throw new Error('status unavailable');
      const data = payload.data, decision = rules.statusDecision(data, version);
      if (decision === 'invalid') throw new Error('invalid status');
      errors = 0; waiting = decision === 'waiting';
      if (decision === 'conflict') {
        setStatus('다른 화면에서 답변이 변경되었습니다. 작성 내용을 보관한 뒤 최신 답변을 새로 불러오세요.');
        editor.querySelectorAll('button[type="submit"]').forEach(button => { button.disabled = true; });
      } else if (decision === 'waiting') {
        setStatus('AI 초안을 준비 중입니다. 페이지 이동 없이 자동으로 확인합니다.');
      } else if (decision === 'ready') {
        setStatus('AI 초안이 준비됐습니다. 작성 중인 답변은 그대로 두었습니다. 아래에서 검토 후 적용하세요.');
        aiDraft = typeof data.draft === 'string' ? data.draft : '';
        draftText.textContent = aiDraft; draftBox.hidden = !aiDraft;
        review.textContent = data.reason || (data.needsReview ? '추가 확인이 필요한 답변입니다.' : '근거와 내용을 확인한 뒤 적용해 주세요.');
      } else if (decision === 'failed' || decision === 'expired') {
        setStatus(decision === 'failed' ? 'AI 생성에 실패했습니다. 재시도하거나 직접 답변할 수 있어요.' :
          'AI 작업 응답이 지연되고 있습니다. 재시도하거나 직접 답변할 수 있어요.');
      } else if (decision === 'answered') {
        setStatus('이미 공개된 답변입니다. 수정 후 다시 승인할 수 있어요.');
      } else {
        setStatus('새로 생성된 AI 초안이 없습니다. 직접 답변하거나 AI 생성을 요청하세요.');
      }
    } catch (_) {
      if (!document.hidden) {
        errors += 1; waiting = errors < 3;
        setStatus(errors < 3 ? '상태 연결을 다시 확인하고 있습니다. 작성 중인 답변은 유지됩니다.' :
          '자동 확인을 중지했습니다. 로그인·연결 상태를 확인한 뒤 다시 시도해 주세요.');
      }
    } finally {
      clearTimeout(timeout); activeController = null; inFlight = false; checkButton.disabled = false; schedule();
    }
  }
  applyDraft.addEventListener('click', () => {
    if (!aiDraft) return;
    if (textarea.value.trim() && textarea.value !== aiDraft && !window.confirm('작성 중인 답변을 AI 초안으로 바꿀까요?')) return;
    textarea.value = aiDraft; pendingText = null; restore.hidden = true; persist(); textarea.focus();
  });
  checkButton.addEventListener('click', () => { attempts = 0; errors = 0; waiting = true; clearTimeout(timer); check(); });
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) { clearTimeout(timer); activeController?.abort(); } else schedule();
  });
  window.addEventListener('pagehide', () => { waiting = false; clearTimeout(timer); activeController?.abort(); });
  if (['RUNNING', 'PENDING'].includes(statusBox.dataset.state)) { waiting = true; schedule(); }
})();
