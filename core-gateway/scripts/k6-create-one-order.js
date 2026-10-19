import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  const payload = JSON.stringify({
    orderId: 1,
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

  const res = http.post(
      'http://localhost:8089/api/v1/me/orders',
      payload,
      params
  );

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  console.log(
      `status=${res.status}, duration=${res.timings.duration}ms, body=${res.body}`
  );
}