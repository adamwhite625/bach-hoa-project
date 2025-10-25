// ===== API CONFIG (AUTO PREFIX + CRUD WITH QUERY) =====
// Version & base (could be overridden via env)
export const API_VERSION = '';
export const API_BASE = `api`;

// ---------------- Helpers (pure) ----------------
export const buildPath = (template, params = {}) => template.replace(/:([A-Za-z0-9_]+)/g, (m, k) => {
  const v = params[k];
  if (v === undefined || v === null) return m;
  return encodeURIComponent(String(v));
});

export const buildUrl = (base, params = {}) => {
  const entries = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '' && v !== 'all');
  if (!entries.length) return base;
  const q = entries.map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`).join('&');
  return `${base}?${q}`;
};

// CRUD generator (LIST_Q for list with query params integrated)
const crud = (name) => ({
  LIST: `/${name}`,
  LIST_Q: (params = {}) => buildUrl(`/${name}`, params),
  CREATE: `/${name}`,
  DETAIL: (id) => `/${name}/${id}`,
  UPDATE: (id) => `/${name}/${id}`,
  DELETE: (id) => `/${name}/${id}`
});

// Recursively prefix API_BASE onto strings/functions returning path segments
const prefixAll = (node) => {
  if (typeof node === 'string') {
    return node.startsWith(API_BASE) ? node : `${API_BASE}${node}`;
  }
  if (typeof node === 'function') {
    return (...args) => {
      const val = node(...args);
      if (typeof val === 'string') return val.startsWith(API_BASE) ? val : `${API_BASE}${val}`;
      return val; // unexpected but keep
    };
  }
  if (node && typeof node === 'object' && !Array.isArray(node)) {
    const out = {};
    for (const k of Object.keys(node)) out[k] = prefixAll(node[k]);
    return Object.freeze(out);
  }
  return node;
};

// ---------------- Raw endpoint (relative) constants ----------------
const RAW_ENDPOINTS = Object.freeze({
  AUTH: Object.freeze({
    LOGIN: '/auth/login',
    ADMIN_LOGIN: '/auth/admin/login'
  }),
  PRODUCTS: Object.freeze(crud('products')),
  USERS: Object.freeze(crud('users')),
  CATEGORIES: Object.freeze({
    ...crud('categories'),
    LIST_Q: (params = {}) => buildUrl('/categories', params)
  }),
  DISCOUNTS: Object.freeze(crud('discounts')),
  ORDERS: Object.freeze({
    LIST: '/orders',
    CREATE: '/orders',
    DETAIL: (id) => `/orders/${id}`,
    USER_ORDERS: '/orders/user'
  })
});

// Public prefixed endpoints (already includes API_BASE) – no need to call withBase()
export const API_ENDPOINTS = prefixAll(RAW_ENDPOINTS);

// Backward compatible helper if someone still calls it
export const withBase = (path) => (path?.startsWith(API_BASE) ? path : `${API_BASE}${path}`);
