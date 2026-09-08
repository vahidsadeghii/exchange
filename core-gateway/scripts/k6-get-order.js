import http from 'k6/http';
import { expect } from 'https://jslib.k6.io/k6-testing/0.6.1/index.js';

export const options = {
  discardResponseBodies: true,
  scenarios: {
    contacts: {
      executor: 'shared-iterations',
      vus: 15,
      iterations: 100,
      maxDuration: '1s',
    },
  },
};


export default function () {
  const res = http.get('http://localhost:8089/api/v1/me/orders?id=10&pair=BTC_EURO');
  expect(res.status).toBe(200);
}