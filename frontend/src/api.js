async function getCsrfToken() {
  const response = await fetch('/api/auth/csrf', { headers: { Accept: 'application/json' } });
  if (!response.ok) throw new Error('セキュリティトークンを取得できませんでした。');
  const result = await response.json();
  return result.token;
}

export async function request(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const headers = { Accept: 'application/json', 'Content-Type': 'application/json', ...options.headers };
  if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
    headers['X-XSRF-TOKEN'] = await getCsrfToken();
  }

  const response = await fetch(path, { ...options, method, headers });
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}));
    throw new Error(problem.message || `通信に失敗しました（${response.status}）`);
  }
  return response.status === 204 ? null : response.json();
}
