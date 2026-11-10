import test from 'node:test';
import assert from 'node:assert/strict';
import { monthUtc, subscriptionActive, tokenHash, validatePhoto, validateSolution } from './logic.js';

test('subscription requires active state, matching product and future expiry', () => {
  const valid = { subscriptionState: 'SUBSCRIPTION_STATE_ACTIVE', lineItems: [{productId:'iwat_ai_plus',expiryTime:'2030-01-01T00:00:00Z'}] };
  assert.equal(subscriptionActive(valid), true);
  assert.equal(subscriptionActive({...valid, subscriptionState:'SUBSCRIPTION_STATE_CANCELED'}), true);
  assert.equal(subscriptionActive({...valid, subscriptionState:'SUBSCRIPTION_STATE_ON_HOLD'}), false);
  assert.equal(subscriptionActive({...valid, lineItems:[{productId:'other', expiryTime:'2030-01-01T00:00:00Z'}]}), false);
  assert.equal(subscriptionActive(valid,'iwat_ai_plus',Date.parse('2031-01-01')), false);
});
test('photos and solutions are constrained', () => {
  const jpeg = Buffer.from([0xff,0xd8,...Array(30).fill(0),0xff,0xd9]).toString('base64');
  assert.equal(validatePhoto(jpeg).length,34);
  assert.throws(()=>validatePhoto('not-an-image'));
  assert.deepEqual(validateSolution({expression:'2+2',answer:'4',explanation:'Add two and two.'}),{expression:'2+2',answer:'4',explanation:'Add two and two.'});
  assert.throws(()=>validateSolution({expression:'2+2'}));
});
test('billing key is one-way and month uses UTC', () => {
  assert.equal(tokenHash('token').length,64);
  assert.equal(monthUtc(new Date('2026-12-31T23:59:59Z')),'2026-12');
});
