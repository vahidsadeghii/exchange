import http from 'k6/http';
import { expect } from 'https://jslib.k6.io/k6-testing/0.6.1/index.js';
import exec from 'k6/execution';

export const options = {
  discardResponseBodies: false,

  scenarios: {
    contacts: {
      executor: 'shared-iterations',
      vus: 15,
      iterations: 100,
      maxDuration: '30s',
    },
  },
};

const BASE_URL = 'http://localhost:8089';

export default function () {
  const orderId = exec.scenario.iterationInTest + 1;
  const pair = 'BTC_EURO';

  // CREATE ORDER
  const payload = JSON.stringify({
    orderId,
    userId: 5,
    tradeSide: 'SELL',
    orderType: 'LIMIT',
    pair,
    marketType: 'SPOT',
    quantity: 6,
    price: 800,
  });

  const createRes = http.post(
      `${BASE_URL}/api/v1/me/orders`,
      payload,
      {
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
      }
  );

  expect(createRes.status).toBe(200);

  // GET ORDER
  const getRes = http.get(
      `${BASE_URL}/api/v1/me/orders?id=${orderId}&pair=${encodeURIComponent(pair)}`
  );

  expect(getRes.status).toBe(200);
}