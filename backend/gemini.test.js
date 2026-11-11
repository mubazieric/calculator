import test from 'node:test';
import assert from 'node:assert/strict';
import { analyzeWithGemini, geminiRequest, parseGeminiResponse } from './gemini.js';

const success = {
  status: 'completed',
  steps: [{ type: 'model_output', content: [{ type: 'text', text: '{"expression":"2+2","answer":"4","explanation":"Add 2 and 2."}' }] }],
  usage: { total_input_tokens: 280, total_output_tokens: 32, total_thought_tokens: 9 }
};

test('sends an inline JPEG, structured response, and no stored interaction', () => {
  const request = geminiRequest('/9j/4AAQ');
  assert.equal(request.model, 'gemini-3.1-flash-lite');
  assert.equal(request.store, false);
  assert.deepEqual(request.input[1], { type: 'image', data: '/9j/4AAQ', mime_type: 'image/jpeg' });
  assert.equal(request.response_format.mime_type, 'application/json');
  assert.deepEqual(request.response_format.schema.required, ['expression', 'answer', 'explanation']);
  assert.throws(() => geminiRequest('abc', 'https://example.com'));
});

test('parses a completed Gemini interaction and tracks thinking tokens', () => {
  assert.deepEqual(parseGeminiResponse(success), {
    solution: { expression: '2+2', answer: '4', explanation: 'Add 2 and 2.' }, inputTokens: 280, outputTokens: 41
  });
  assert.throws(() => parseGeminiResponse({ ...success, status: 'incomplete' }));
  assert.throws(() => parseGeminiResponse({ ...success, steps: [] }));
  assert.throws(() => parseGeminiResponse({ ...success, steps: [{ type: 'model_output', content: [{ type: 'text', text: '{}' }] }] }));
});

test('sends the API key only in the server-side header and rejects provider errors', async () => {
  const result = await analyzeWithGemini('/9j/4AAQ', { key: 'server-secret', fetchImpl: async (url, options) => {
    assert.equal(url, 'https://generativelanguage.googleapis.com/v1beta/interactions');
    assert.equal(options.headers['x-goog-api-key'], 'server-secret');
    assert.equal(JSON.stringify(options.body).includes('server-secret'), false);
    return { ok: true, json: async () => success };
  } });
  assert.equal(result.solution.answer, '4');
  await assert.rejects(() => analyzeWithGemini('abc', { key: 'server-secret', fetchImpl: async () => ({ ok: false, status: 429 }) }), /429/);
});
