import http from 'k6/http';

export function login(baseUrl, email, password) {
  const response = http.post(
    `${baseUrl}/api/auth/login`,
    JSON.stringify({ email, password }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { api: 'login' },
    },
  );

  if (response.status !== 200) {
    throw new Error(`Login failed: status=${response.status}`);
  }

  const accessToken = response.json('data.token.accessToken');
  if (!accessToken) {
    throw new Error('Access token is missing from login response');
  }

  return accessToken;
}
