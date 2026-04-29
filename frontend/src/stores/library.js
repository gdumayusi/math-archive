const FAVORITES_PREFIX = 'math-archive-favorites'
const MISTAKES_PREFIX = 'math-archive-mistakes'

function getFavoritesKey(userId) {
  return `${FAVORITES_PREFIX}-${userId}`
}

function getMistakesKey(userId) {
  return `${MISTAKES_PREFIX}-${userId}`
}

export function loadFavoriteCache(userId) {
  try {
    const raw = localStorage.getItem(getFavoritesKey(userId))
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

export function saveFavoriteCache(userId, questions) {
  localStorage.setItem(getFavoritesKey(userId), JSON.stringify(questions))
}

export function upsertFavoriteCache(userId, question) {
  const current = loadFavoriteCache(userId)
  const next = [
    { ...question, favorited: true },
    ...current.filter((item) => item.id !== question.id)
  ]
  saveFavoriteCache(userId, next)
  return next
}

export function removeFavoriteCache(userId, questionId) {
  const next = loadFavoriteCache(userId).filter((item) => item.id !== questionId)
  saveFavoriteCache(userId, next)
  return next
}

export function loadMistakeCache(userId) {
  try {
    const raw = localStorage.getItem(getMistakesKey(userId))
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

export function saveMistakeCache(userId, questions) {
  localStorage.setItem(getMistakesKey(userId), JSON.stringify(questions))
}

export function upsertMistakeCache(userId, question) {
  const current = loadMistakeCache(userId)
  const next = [
    { ...question, mistake: true },
    ...current.filter((item) => item.id !== question.id)
  ]
  saveMistakeCache(userId, next)
  return next
}

export function removeMistakeCache(userId, questionId) {
  const next = loadMistakeCache(userId).filter((item) => item.id !== questionId)
  saveMistakeCache(userId, next)
  return next
}
