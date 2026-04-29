import { reactive } from 'vue'

const STORAGE_KEY = 'math-archive-session'

function loadSession() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const sessionState = reactive({
  user: loadSession()
})

export function setSession(user) {
  sessionState.user = user
  localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
}

export function patchSession(partialUser) {
  if (!sessionState.user) {
    return
  }
  sessionState.user = {
    ...sessionState.user,
    ...partialUser
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(sessionState.user))
}

export function clearSession() {
  sessionState.user = null
  localStorage.removeItem(STORAGE_KEY)
}
