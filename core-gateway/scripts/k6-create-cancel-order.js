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
    const orderId = exec.scenario.iterationInTest + 1;
    const pair = 'BTC_EURO';


    // CREATE ORDER
    const payload = JSON.stringify({
        orderId: orderId,
        userId: 5,
        tradeSide: 'SELL',
        orderType: 'LIMIT',
        pair: pair,
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
        'http://localhost:8089/api/v1/me/orders',
        payload,
        params
    );

    check(createRes, {
        'create order status is 200': (r) => r.status === 200,
    });

    console.log(
        `CREATE orderId=${orderId}, ` +
        `status=${createRes.status}, ` +
        `duration=${createRes.timings.duration}ms, ` +
        `body=${createRes.body || '<EMPTY>'}`
    );

    if (createRes.status !== 200) {
        return;
    }


    //  CANCEL ORDER
    const cancelUrl =
        `http://localhost:8089/api/v1/me/orders` +
        `?id=${orderId}&pair=${encodeURIComponent(pair)}`;

    const cancelRes = http.del(
        cancelUrl,
        null,
        {
            headers: {
                'Accept': 'application/json',
            },
        }
    );

    check(cancelRes, {
        'cancel order status is 200': (r) => r.status === 200,
    });

    console.log(
        `CANCEL orderId=${orderId}, ` +
        `status=${cancelRes.status}, ` +
        `duration=${cancelRes.timings.duration}ms, ` +
        `body=${cancelRes.body || '<EMPTY>'}`
    );
}