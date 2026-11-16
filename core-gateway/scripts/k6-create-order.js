import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';

export const options = {
  discardResponseBodies: false,

  scenarios: {
    orders: {
      executor: 'shared-iterations',
      vus: 1000,
      iterations: 100000,
      maxDuration: '2m',
    },
  },

  thresholds: {
    http_req_duration: [
      'p(95)<6000',
      'p(99)<6000',
    ],
    http_req_failed: [
      'rate<0.01',
    ],
    checks: ['rate>0.99'],
  },

  summaryTrendStats: [
    'min',
    'avg',
    'med',
    'p(90)',
    'p(95)',
    'p(99)',
    'max',
  ],
};

export default function () {
  const orderId = exec.scenario.iterationInTest + 1;

  const payload = JSON.stringify({
    orderId,
    userId: 5,
    tradeSide: 'SELL',
    orderType: 'LIMIT',
    pair: 'BTC_EURO',
    marketType: 'SPOT',
    quantity: 6,
    price: 800,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
  };

  const res = http.post(
      'http://localhost:8089/api/v1/me/orders',
      payload,
      params,
  );

  check(res, {
    'status is 200': (r) => r.status === 200,
  });
}