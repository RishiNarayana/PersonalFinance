const API_BASE = '/api'

function getToken() {
  return localStorage.getItem('jwt')
}

function setToken(token) {
  localStorage.setItem('jwt', token)
}

async function request(path, opts = {}) {
  const headers = opts.headers || {}
  if (!headers['Content-Type'] && !(opts.body instanceof FormData)) headers['Content-Type'] = 'application/json'
  const token = getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(API_BASE + path, { ...opts, headers })

  if (res.status === 204) return null

  const ct = res.headers.get('content-type') || ''
  const payload = ct.includes('application/json') ? await res.json() : await res.text()

  if (!res.ok) {
    const message =
      (typeof payload === 'string' && payload) ||
      (payload && payload.message) ||
      'Request failed'
    const error = new Error(message)
    error.status = res.status
    throw error
  }

  return payload
}

export async function register(user) {
  return request('/auth/register', { method: 'POST', body: JSON.stringify(user) })
}

export async function login(credentials) {
  const data = await request('/auth/login', { method: 'POST', body: JSON.stringify(credentials) })
  if (data && data.token) setToken(data.token)
  return data
}

export function logout() {
  localStorage.removeItem('jwt')
}

export async function getTransactions() {
  return request('/transactions')
}

export async function addTransaction(tx) {
  return request('/transactions', { method: 'POST', body: JSON.stringify(tx) })
}

export async function updateTransaction(id, tx) {
  return request(`/transactions/${id}`, { method: 'PUT', body: JSON.stringify(tx) })
}

export async function deleteTransaction(id) {
  return request(`/transactions/${id}`, { method: 'DELETE' })
}

export async function getCategories() {
  return request('/categories')
}

export async function createCategory(category) {
  return request('/categories', { method: 'POST', body: JSON.stringify(category) })
}

export async function getBudgets() {
  return request('/budgets')
}

export async function createBudget(budget) {
  return request('/budgets', { method: 'POST', body: JSON.stringify(budget) })
}

export async function deleteBudget(id) {
  return request(`/budgets/${id}`, { method: 'DELETE' })
}

export async function getMonthlySummary(year, month) {
  const qs = year && month ? `?year=${year}&month=${month}` : ''
  return request(`/analytics/monthly-summary${qs}`)
}

export async function getCategoryBreakdown(year, month) {
  const qs = year && month ? `?year=${year}&month=${month}` : ''
  return request(`/analytics/category-breakdown${qs}`)
}

export async function exportExcel() {
  const token = getToken()
  const headers = {}
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(API_BASE + '/export/excel', { headers })
  if (!res.ok) throw new Error('Export failed')
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'transactions.xlsx'
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

export default {
  register,
  login,
  logout,
  getTransactions,
  addTransaction,
  updateTransaction,
  deleteTransaction,
  getCategories,
  getBudgets,
  createBudget,
  deleteBudget,
  getMonthlySummary,
  getCategoryBreakdown,
  exportExcel,
}
