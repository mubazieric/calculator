import { createHash } from 'node:crypto';

export const PRODUCT_ID = 'iwat_ai_plus';
export const monthUtc = (date = new Date()) => date.toISOString().slice(0, 7);
export const tokenHash = token => createHash('sha256').update(token).digest('hex');

export function subscriptionActive(purchase, productId = PRODUCT_ID, now = Date.now()) {
  if (!['SUBSCRIPTION_STATE_ACTIVE', 'SUBSCRIPTION_STATE_IN_GRACE_PERIOD', 'SUBSCRIPTION_STATE_CANCELED'].includes(purchase?.subscriptionState)) return false;
  return Array.isArray(purchase.lineItems) && purchase.lineItems.some(item =>
    item.productId === productId && Number.isFinite(Date.parse(item.expiryTime)) && Date.parse(item.expiryTime) > now);
}

export function validatePhoto(base64) {
  if (typeof base64 !== 'string' || base64.length < 32 || base64.length > 2_000_000 || !/^[A-Za-z0-9+/]*={0,2}$/.test(base64))
    throw new Error('Invalid photo: use a JPEG under 1.5 MB');
  const image = Buffer.from(base64, 'base64');
  if (image.length > 1_500_000 || image[0] !== 0xff || image[1] !== 0xd8 || image[image.length - 2] !== 0xff || image[image.length - 1] !== 0xd9)
    throw new Error('Invalid JPEG image');
  return image;
}

export function validateSolution(value) {
  if (!value || typeof value.expression !== 'string' || typeof value.answer !== 'string' || typeof value.explanation !== 'string'
      || value.expression.length > 1000 || value.answer.length > 1000 || value.explanation.length > 5000)
    throw new Error('AI response was not a valid solution');
  return { expression: value.expression, answer: value.answer, explanation: value.explanation };
}
