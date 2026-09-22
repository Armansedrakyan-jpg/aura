const TOKEN_KEY = 'aura_token';

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (token) => localStorage.setItem(TOKEN_KEY, token);
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

async function parseBody(res) {
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function errorMessage(data, fallback) {
  if (typeof data === 'string' && data.trim()) return data;
  if (data && typeof data === 'object' && data.message) return data.message;
  return fallback;
}

async function request(path, { method = 'GET', body, formData, auth = false } = {}) {
  const headers = {};
  if (body) headers['Content-Type'] = 'application/json';
  if (auth) headers['Authorization'] = `Bearer ${getToken()}`;

  let res;
  try {
    res = await fetch(`/api${path}`, {
      method,
      headers,
      body: formData ?? (body ? JSON.stringify(body) : undefined),
    });
  } catch {
    throw new ApiError('Не удалось связаться с сервером', 0);
  }

  const data = await parseBody(res);
  if (!res.ok) {
    if (res.status === 401 && auth) clearToken();
    throw new ApiError(errorMessage(data, `Ошибка запроса (${res.status})`), res.status);
  }
  return data;
}

export const api = {
  login: (usernameOrEmail, password) =>
    request('/auth/login', { method: 'POST', body: { usernameOrEmail, password } }),

  register: (username, email, password) =>
    request('/auth/register', { method: 'POST', body: { username, email, password } }),

  analyze: (photo, aboutMe) => {
    const formData = new FormData();
    formData.append('photo', photo);
    formData.append('aboutMe', aboutMe);
    return request('/aura/analyze', { method: 'POST', formData, auth: true });
  },

  history: () => request('/aura/history', { auth: true }),
};
