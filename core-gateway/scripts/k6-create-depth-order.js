import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';

export const options = {
    scenarios: {
        createOrders: {
            executor: 'shared-iterations',
            vus: 1000,
            iterations: 100000,
            maxDuration: '5m',
            exec: 'createOrders',
        },

        orderDepth: {
            executor: 'constant-vus',
            vus: 2,
            duration: '5m',
            startTime: '2s',
            exec: 'readOrderDepth',
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

const BASE_URL = 'http://localhost:8089';

export function createOrders() {
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

    const createOk = check(createRes, {
        'create status is 200': (r) => r.status === 200,
    });

    if (!createOk) {
        return;
    }

    // CANCEL ORDER
    const cancelUrl =
        `${BASE_URL}/api/v1/me/orders` +
        `?id=${orderId}&pair=${encodeURIComponent(pair)}`;

    const cancelRes = http.del(
        cancelUrl,
        null,
        {
            headers: {
                Accept: 'application/json',
            },
        }
    );

    check(cancelRes, {
        'cancel status is 200': (r) => r.status === 200,
    });
}

export function readOrderDepth() {
    const res = http.get(
        `${BASE_URL}/api/v1/me/orderdepth?pair=BTC_EURO&depth=10`
    );

    check(res, {
        'depth status is 200': (r) => r.status === 200,
    });

    if (res.status !== 200 || !res.body) {
        return;
    }

    const book = JSON.parse(res.body);

    check(book, {
        'depth has bids': (d) => Array.isArray(d.bids),
        'depth has asks': (d) => Array.isArray(d.asks),
    });
}

export default function () {}