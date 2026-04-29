const API_BASE = import.meta.env.VITE_API_BASE || ''

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    ...options
  })

  const contentType = response.headers.get('content-type') || ''
  const data = contentType.includes('application/json') ? await response.json() : await response.text()

  if (!response.ok) {
    const message = typeof data === 'object' && data?.message ? data.message : '请求失败'
    throw new Error(message)
  }

  return data
}

function buildQuery(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    query.set(key, Array.isArray(value) ? value.join(',') : value)
  })
  const text = query.toString()
  return text ? `?${text}` : ''
}

export const api = {
  login(payload) {
    return request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  register(payload) {
    return request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getProfile(userId) {
    return request(`/api/users/${userId}/profile`)
  },
  updateProfile(userId, payload) {
    return request(`/api/users/${userId}/profile`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  },
  changePassword(userId, payload) {
    return request(`/api/users/${userId}/password`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  },
  getDashboard(userId) {
    return request(`/api/dashboard${buildQuery({ userId })}`)
  },
  getQuestions(params) {
    return request(`/api/questions${buildQuery(params)}`)
  },
  getQuestionTaxonomy() {
    return request('/api/questions/taxonomy')
  },
  getQuestionDetail(id, userId) {
    return request(`/api/questions/${id}${buildQuery({ userId })}`)
  },
  getFavorites(userId) {
    return request(`/api/users/${userId}/favorites`)
  },
  addFavorite(userId, questionId) {
    return request(`/api/users/${userId}/favorites/${questionId}`, { method: 'POST' })
  },
  removeFavorite(userId, questionId) {
    return request(`/api/users/${userId}/favorites/${questionId}`, { method: 'DELETE' })
  },
  getMistakes(userId) {
    return request(`/api/users/${userId}/mistakes`)
  },
  addMistake(userId, questionId, note = '') {
    return request(`/api/users/${userId}/mistakes/${questionId}`, {
      method: 'POST',
      body: JSON.stringify({ note })
    })
  },
  removeMistake(userId, questionId) {
    return request(`/api/users/${userId}/mistakes/${questionId}`, { method: 'DELETE' })
  },
  recordPractice(userId, payload) {
    return request(`/api/users/${userId}/practice-records`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getStudyStats(userId) {
    return request(`/api/users/${userId}/study-stats`)
  },
  getAdminOverview() {
    return request('/api/admin/overview')
  },
  getAdminQuestions(params) {
    return request(`/api/admin/questions${buildQuery(params)}`)
  },
  createQuestion(payload) {
    return request('/api/admin/questions', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  updateQuestion(id, payload) {
    return request(`/api/admin/questions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  },
  deleteQuestion(id) {
    return request(`/api/admin/questions/${id}`, { method: 'DELETE' })
  },
  getAdminReviews(params = {}) {
    return request(`/api/admin/reviews${buildQuery(params)}`)
  },
  reviewQuestion(id, payload) {
    return request(`/api/admin/reviews/${id}/action`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getUsers(params = {}) {
    return request(`/api/admin/users${buildQuery(params)}`)
  },
  createUser(payload) {
    return request('/api/admin/users', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  updateUser(id, payload) {
    return request(`/api/admin/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  },
  deleteUser(id) {
    return request(`/api/admin/users/${id}`, { method: 'DELETE' })
  },
  getAdminStats() {
    return request('/api/admin/stats')
  },
  getAdminSettings() {
    return request('/api/admin/settings')
  },
  updateAdminSettings(payload) {
    return request('/api/admin/settings', {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  },
  getAdminLogs(params = {}) {
    return request(`/api/admin/logs${buildQuery(params)}`)
  }
}
