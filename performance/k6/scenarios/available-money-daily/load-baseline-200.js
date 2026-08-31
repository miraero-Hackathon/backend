import http from 'k6/http';
import exec from 'k6/execution';
import { check, sleep } from 'k6';
import { login } from '../../common/auth.js';
import { requireEnv } from '../../common/env.js';

const API_NAME = 'available-money-daily';
const BASE_URL = requireEnv('BASE_URL');
const TEST_PASSWORD = requireEnv('TEST_PASSWORD');
const USER_COUNT = 200;

export const options = {
  setupTimeout: '5m',
  scenarios: {
    warmup: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [{ duration: '2m', target: 200 }],
      gracefulRampDown: '0s',
      gracefulStop: '0s',
      tags: { phase: 'warmup' },
    },
    baseline: {
      executor: 'constant-vus',
      vus: 200,
      duration: '10m',
      startTime: '2m',
      gracefulStop: '0s',
      tags: { phase: 'baseline' },
    },
    cooldown: {
      executor: 'ramping-vus',
      startVUs: 200,
      stages: [{ duration: '1m', target: 0 }],
      startTime: '12m',
      gracefulRampDown: '30s',
      gracefulStop: '30s',
      tags: { phase: 'cooldown' },
    },
  },
  thresholds: {
    [`http_req_failed{api:${API_NAME},phase:baseline}`]: ['rate<0.01'],
    [`http_req_duration{api:${API_NAME},phase:baseline}`]: [
      'p(95)<500',
      'p(99)<1000',
    ],
  },
};

export function setup() {
  console.log(`BASELINE_SETUP_START=${new Date().toISOString()}`);
  const users = [];

  for (let index = 1; index <= USER_COUNT; index += 1) {
    const email = `perf${String(index).padStart(3, '0')}@miraero.test`;
    const accessToken = login(BASE_URL, email, TEST_PASSWORD);
    const goalId = findPrimaryGoalId(accessToken, email);

    users.push({ accessToken, goalId });
  }

  console.log(`BASELINE_LOAD_START=${new Date().toISOString()}`);
  return { users };
}

export default function (data) {
  const user = data.users[(exec.vu.idInTest - 1) % data.users.length];
  const response = http.get(
    `${BASE_URL}/api/goals/${user.goalId}/available-money/daily`,
    {
      headers: { Authorization: `Bearer ${user.accessToken}` },
      tags: { api: API_NAME },
    },
  );

  const body = parseJson(response);

  check(response, {
    'status is 200': (result) => result.status === 200,
    'response is JSON': () => body !== null,
    'todayAvailableMoney is number': () =>
      body !== null && typeof body.todayAvailableMoney === 'number',
    'todayExpense is number': () =>
      body !== null && typeof body.todayExpense === 'number',
    'remainingAvailableMoney is number': () =>
      body !== null && typeof body.remainingAvailableMoney === 'number',
  });

  sleep(1);
}

export function teardown() {
  console.log(`BASELINE_END=${new Date().toISOString()}`);
}

function findPrimaryGoalId(accessToken, email) {
  const response = http.get(`${BASE_URL}/api/goals`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    tags: { api: 'setup-goals' },
  });

  if (response.status !== 200) {
    throw new Error(`Goal lookup failed: email=${email}, status=${response.status}`);
  }

  const goals = response.json('data');
  if (!Array.isArray(goals)) {
    throw new Error(`Goal list is missing: email=${email}`);
  }

  const primaryGoal = goals.find((goal) =>
    goal.goalName.startsWith('성능테스트 독립 목표 '));

  if (!primaryGoal || !primaryGoal.goalId) {
    throw new Error(`Primary goal is missing: email=${email}`);
  }

  return primaryGoal.goalId;
}

function parseJson(response) {
  try {
    return response.json();
  } catch (error) {
    return null;
  }
}
