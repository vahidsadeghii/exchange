
import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';

export const options = {
  discardResponseBodies: false,

  scenarios: {
    orders: {
      executor: 'shared-iterations',
      vus: 15,
      iterations: 100,
      maxDuration: '30s',
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
  // Globally unique order ID across all VUs
  const orderId = exec.scenario.iterationInTest + 1;

  const baseUrl = 'http://localhost:8089/api/v1/me/orders';

  // --------------------------------------------------
  // 1. CREATE ORDER
  // --------------------------------------------------

  const payload = JSON.stringify({
    orderId: orderId,
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
      'Accept': 'application/json',
    },
  };

  const createRes = http.post(
    baseUrl,
    payload,
    params
  );

  const createBody = createRes.body
    ? createRes.body.trim()
    : '';

  let createResponse = null;

  if (createBody !== '') {
    try {
      createResponse = JSON.parse(createBody);
    } catch (e) {
      // Invalid JSON
    }
  }

  check(createRes, {
    'CREATE - status is 200': (r) => r.status === 200,
    'CREATE - response body is not empty': () => createBody !== '',
    'CREATE - errorCode is 0': () =>
      createResponse !== null &&
      createResponse.errorCode === 0,
    'CREATE - response id matches orderId': () =>
      createResponse !== null &&
      createResponse.id === orderId,
  });

  console.log(
    `CREATE orderId=${orderId}, ` +
    `status=${createRes.status}, ` +
    `duration=${createRes.timings.duration}ms, ` +
    `body=${createBody || '<EMPTY>'}`
  );

  // --------------------------------------------------
  // 2. GET ORDER
  // --------------------------------------------------

  const getUrl =
    `${baseUrl}?id=${orderId}&pair=BTC_EURO`;

  const getRes = http.get(getUrl, params);

  const getBody = getRes.body
    ? getRes.body.trim()
    : '';

  let getResponse = null;

  if (getBody !== '') {
    try {
      getResponse = JSON.parse(getBody);
    } catch (e) {
      // Invalid JSON
    }
  }

  check(getRes, {
    'GET - status is 200': (r) => r.status === 200,
    'GET - response body is not empty': () => getBody !== '',
    'GET - response is valid JSON': () => getResponse !== null,
  });

  console.log(
    `GET orderId=${orderId}, ` +
    `status=${getRes.status}, ` +
    `duration=${getRes.timings.duration}ms, ` +
    `body=${getBody || '<EMPTY>'}`
  );
}

