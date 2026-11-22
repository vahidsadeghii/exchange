import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8089';
const MAX_MATCHES = Number(__ENV.MAX_MATCHES || 100);

if (!Number.isInteger(MAX_MATCHES) || MAX_MATCHES < 1 || MAX_MATCHES > 65535) {
    throw new Error('MAX_MATCHES must be an integer between 1 and 65535');
}

export const options = {
    discardResponseBodies: false,

    scenarios: {
        getMatchInfo: {
            executor: 'shared-iterations',
            vus: 10,
            iterations: 1000,
            maxDuration: '2m',
        },
    },

    thresholds: {
        http_req_duration: ['p(95)<6000', 'p(99)<6000'],
        http_req_failed: ['rate<0.01'],
        checks: ['rate>0.99'],
    },
};

export default function () {
    const response = http.get(
        `${BASE_URL}/api/v1/me/matchinfo?maxMatches=${MAX_MATCHES}`,
        {
            headers: {
                Accept: 'application/json',
            },
        }
    );

    const statusOk = check(response, {
        'match info status is 200': (r) => r.status === 200,
    });

    if (!statusOk || !response.body) {
        return;
    }

    let matches;
    try {
        matches = response.json();
    } catch (error) {
        check(null, {
            'match info response is valid JSON': () => false,
        });
        return;
    }

    check(matches, {
        'match info response is an array': (body) => Array.isArray(body),
        'match count does not exceed maxMatches': (body) =>
            Array.isArray(body) && body.length <= MAX_MATCHES,
        'matches include identifiers': (body) =>
            Array.isArray(body) && body.every((match) =>
                typeof match.id === 'string' && match.id.length > 0
            ),
    });
}
