
import http from 'k6/http';
import { expect } from 'https://jslib.k6.io/k6-testing/0.6.1/index.js';

export const options = {
  discardResponseBodies: true,

  scenarios: {
    contacts: {
      executor: 'shared-iterations',

      // Maximum number of Virtual Users
      vus: 15,

      // Total requests/iterations to execute
      iterations: 100,

      // Give enough time for all iterations to complete
      maxDuration: '30s',
    },
  },
};

export default function () {
  const url =
    'http://localhost:8089/api/v1/me/orders?id=10&pair=BTC_EURO';

  const res = http.get(url);

  expect(res.status).toBe(200);
}
