/* Shared editor state rules, also exercised by node:test without a browser. */
(function (root) {
  'use strict';
  const maxAge = 24 * 60 * 60 * 1000;
  function readDraft(storage, key, version, now = Date.now()) {
    try {
      const raw = storage.getItem(key);
      if (!raw) return { kind: 'none' };
      const saved = JSON.parse(raw);
      if (typeof saved.text !== 'string' || saved.text.length > 5000 ||
          !Number.isInteger(saved.version) || !Number.isFinite(saved.updatedAt)) return { kind: 'invalid' };
      if (now - saved.updatedAt > maxAge || saved.updatedAt > now + 60000) {
        storage.removeItem(key); return { kind: 'expired' };
      }
      if (saved.version !== version) return { kind: 'stale', text: saved.text, version: saved.version };
      return { kind: 'saved', text: saved.text, version: saved.version };
    } catch (_) { return { kind: 'unavailable' }; }
  }
  function saveDraft(storage, key, version, text, now = Date.now()) {
    try {
      if (!Number.isInteger(version) || typeof text !== 'string' || text.length > 5000) return false;
      storage.setItem(key, JSON.stringify({ version, text, updatedAt: now })); return true;
    } catch (_) { return false; }
  }
  function clearDraft(storage, key) { try { storage.removeItem(key); return true; } catch (_) { return false; } }
  function statusDecision(snapshot, version) {
    if (!snapshot || !Number.isInteger(snapshot.version) || typeof snapshot.state !== 'string') return 'invalid';
    if (snapshot.version !== version) return 'conflict';
    if (snapshot.status === 'ANSWERED') return 'answered';
    if (snapshot.expired) return 'expired';
    if (snapshot.state === 'RUNNING' || snapshot.state === 'PENDING') return 'waiting';
    if (snapshot.state === 'READY') return 'ready';
    if (snapshot.state === 'FAILED') return 'failed';
    return 'idle';
  }
  const api = { readDraft, saveDraft, clearDraft, statusDecision };
  if (typeof module !== 'undefined' && module.exports) module.exports = api;
  if (root) root.SupportEditor = api;
})(typeof window !== 'undefined' ? window : null);
