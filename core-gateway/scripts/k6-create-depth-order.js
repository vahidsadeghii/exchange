import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';

export const options = {
    scenarios: {
        createOrders: {
            executor: 'shared-iterations',
            vus: 15,
            iterations: 100,
            maxDuration: '30s',
            exec: 'createOrders',
        },

        orderDepth: {
            executor: 'constant-vus',
            vus: 2,
            duration: '30s',
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
    },
};

const BASE_URL = 'http://localhost:8089';

export function createOrders() {

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

    const res = http.post(
        `${BASE_URL}/api/v1/me/orders`,
        payload,
        {
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json',
            },
        }
    );

    check(res, {
        'order status is 200': (r) => r.status === 200,
    });
}

export function readOrderDepth() {

    const res = http.get(
        `${BASE_URL}/api/v1/me/orderdepth?pair=BTC_EURO&depth=10`
    );

    check(res, {
        'depth status is 200': (r) => r.status === 200,
    });

    if (res.status !== 200) {
        console.error(
            `DEPTH ERROR status=${res.status} body=${res.body}`
        );
        return;
    }

    if (!res.body) {
        console.error('DEPTH EMPTY BODY');
        return;
    }

    let book;

    try {
        book = JSON.parse(res.body);
    } catch (e) {
        console.error(`INVALID JSON => ${res.body}`);
        return;
    }

    check(book, {
        'depth has bids': (d) => Array.isArray(d.bids),
        'depth has asks': (d) => Array.isArray(d.asks),
    });

    console.log(
        `[DEPTH] bids=${book.bids?.length || 0}, asks=${book.asks?.length || 0}`
    );
}

export default function () {}