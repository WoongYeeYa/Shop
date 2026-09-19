'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const rules = require('../../main/webapp/assets/js/support-editor.js');
const mainScript = fs.readFileSync(path.join(__dirname, '../../main/webapp/assets/js/support.js'), 'utf8');
function memoryStorage() {
  const values = new Map();
  return { getItem: key => values.get(key) ?? null, setItem: (key, value) => values.set(key, value), removeItem: key => values.delete(key) };
}
test('draft survives refresh but is isolated by administrator and inquiry', () => {
  const storage = memoryStorage();
  assert.equal(rules.saveDraft(storage, 'reply:3:10', 4, '작성 중 답변', 1000), true);
  assert.equal(rules.readDraft(storage, 'reply:3:10', 4, 2000).text, '작성 중 답변');
  assert.equal(rules.readDraft(storage, 'reply:9:10', 4, 2000).kind, 'none');
  assert.equal(rules.readDraft(storage, 'reply:3:11', 4, 2000).kind, 'none');
  assert.equal(rules.readDraft(storage, 'reply:3:10', 5, 2000).kind, 'stale');
});
test('expired or corrupt drafts are never restored', () => {
  const storage = memoryStorage();
  rules.saveDraft(storage, 'x', 0, '답변', 1);
  assert.equal(rules.readDraft(storage, 'x', 0, 86400002).kind, 'expired');
  assert.equal(storage.getItem('x'), null);
  storage.setItem('x', '{"text":123,"version":0,"updatedAt":1000}');
  assert.equal(rules.readDraft(storage, 'x', 0, 2000).kind, 'invalid');
  storage.setItem('x', 'broken');
  assert.equal(rules.readDraft(storage, 'x', 0).kind, 'unavailable');
});
test('blocked browser storage does not throw or claim a successful save', () => {
  const blocked = { getItem() { throw Error(); }, setItem() { throw Error(); }, removeItem() { throw Error(); } };
  assert.equal(rules.saveDraft(blocked, 'x', 0, 'draft'), false);
  assert.equal(rules.readDraft(blocked, 'x', 0).kind, 'unavailable');
  assert.equal(rules.clearDraft(blocked, 'x'), false);
});
test('intentional empty draft is preserved, oversized content is rejected', () => {
  const storage = memoryStorage();
  assert.equal(rules.saveDraft(storage, 'x', 2, ''), true);
  assert.equal(rules.readDraft(storage, 'x', 2).text, '');
  assert.equal(rules.saveDraft(storage, 'x', 2, 'x'.repeat(5001)), false);
});
function element(extra = {}) {
  return { hidden: true, disabled: false, textContent: '', value: '', dataset: {}, listeners: {},
    addEventListener(name, callback) { this.listeners[name] = callback; },
    focus() {}, ...extra };
}
function editorHarness({ payload, text = '담당자가 작성 중인 답변', storage = memoryStorage(), published = false, version = 0, fetchError = false } = {}) {
  const nodes = new Map();
  for (const id of ['reply-save-status', 'restore-reply', 'discard-reply', 'previous-reply', 'previous-reply-text',
    'ai-status-text', 'check-ai-status', 'live-ai-draft', 'live-ai-draft-text', 'apply-ai-draft', 'live-ai-reason']) nodes.set(id, element());
  const textarea = element({ value: text });
  const publish = element({ hidden: false });
  const editor = element({ dataset: { version: String(version), user: '3', inquiry: '1', published: String(published) } });
  editor.querySelector = () => textarea; editor.querySelectorAll = () => [publish];
  const status = element({ dataset: { aiStatusUrl: '/admin/inquiry/status?id=1', state: 'RUNNING' } });
  const document = {
    hidden: false, listeners: {},
    querySelector(selector) { return selector === '[data-reply-editor]' ? editor : status; },
    querySelectorAll() { return []; }, getElementById(id) { return nodes.get(id); },
    addEventListener(name, callback) { this.listeners[name] = callback; }
  };
  const window = { SupportEditor: rules, sessionStorage: storage, listeners: {}, confirm: () => false,
    addEventListener(name, callback) { this.listeners[name] = callback; }, location: { reload() {} } };
  let sequence = 0, calls = 0;
  const timers = new Map();
  const context = vm.createContext({ document, window, AbortController, console,
    setTimeout(fn) { const key = ++sequence; timers.set(key, fn); return key; },
    clearTimeout(key) { timers.delete(key); },
    async fetch() { calls++; if(fetchError) throw new Error('network down'); return { ok: true, async json() { return { success: true, data: payload }; } }; }
  });
  vm.runInContext(mainScript, context);
  return { nodes, textarea, editor, publish, document, window, storage, timers, get calls() { return calls; },
    async click(id) { nodes.get(id).listeners.click(); await new Promise(setImmediate); },
    async tick() { const next = timers.entries().next().value; if (next) { timers.delete(next[0]); await next[1](); await new Promise(setImmediate); } }
  };
}
test('live AI result never replaces typed text; application requires explicit confirmation', async () => {
  const h = editorHarness({ payload: { version: 0, state: 'READY', status: 'OPEN', draft: '<script>bad</script> 새 AI 초안', needsReview: true, reason: '추가 확인' } });
  await h.click('check-ai-status');
  assert.equal(h.textarea.value, '담당자가 작성 중인 답변');
  assert.equal(h.nodes.get('live-ai-draft').hidden, false);
  assert.equal(h.nodes.get('live-ai-draft-text').textContent, '<script>bad</script> 새 AI 초안');
  await h.click('apply-ai-draft');
  assert.equal(h.textarea.value, '담당자가 작성 중인 답변');
  h.window.confirm = () => true;
  await h.click('apply-ai-draft');
  assert.equal(h.textarea.value, '<script>bad</script> 새 AI 초안');
  assert.equal(rules.readDraft(h.storage, 'myshop:reply:3:1', 0).text, h.textarea.value);
});
test('newly published answer disables stale publication but preserves local text', async () => {
  const h = editorHarness({ payload: { version: 1, state: 'READY', status: 'ANSWERED', draft: '늦은 AI 초안' } });
  await h.click('check-ai-status');
  assert.equal(h.publish.disabled, true);
  assert.equal(h.textarea.value, '담당자가 작성 중인 답변');
  assert.match(h.nodes.get('ai-status-text').textContent, /변경/);
});
test('saved text is offered for restoration and cleared only after confirmed newer publication', async () => {
  const storage = memoryStorage();
  rules.saveDraft(storage, 'myshop:reply:3:1', 0, '이전 임시 답변');
  const h = editorHarness({ storage });
  assert.equal(h.nodes.get('restore-reply').hidden, false);
  assert.equal(h.textarea.value, '담당자가 작성 중인 답변');
  await h.click('restore-reply'); assert.equal(h.textarea.value, '이전 임시 답변');
  const changed = editorHarness({ storage, version: 1 });
  assert.equal(changed.nodes.get('previous-reply').hidden, false);
  assert.equal(changed.nodes.get('restore-reply').hidden, true);
  editorHarness({ storage, version: 1, published: true });
  assert.equal(storage.getItem('myshop:reply:3:1'), null);
});
test('network failures stop polling after three attempts and retain typed text', async () => {
  const h = editorHarness({ fetchError: true });
  await h.tick(); await h.tick(); await h.tick();
  assert.equal(h.calls, 3);
  assert.equal(h.timers.size, 0);
  assert.equal(h.textarea.value, '담당자가 작성 중인 답변');
  assert.match(h.nodes.get('ai-status-text').textContent, /중지/);
});
test('hidden tabs do not perform background polling', async () => {
  const h = editorHarness({ payload: { version: 0, state: 'RUNNING', status: 'OPEN' } });
  h.document.hidden = true;
  h.document.listeners.visibilitychange();
  assert.equal(h.timers.size, 0);assert.equal(h.calls, 0);
  h.document.hidden = false;h.document.listeners.visibilitychange();
  await h.tick();assert.equal(h.calls, 1);
});
