const TOKEN_KEY = 'token'
const USER_KEY = 'user'

export function saveAuth(loginResponse) {
    localStorage.setItem(TOKEN_KEY, loginResponse.token)
    localStorage.setItem(
      USER_KEY,
      JSON.stringify({
        userId: loginResponse.userId,
        email: loginResponse.email,
        role: loginResponse.role,
      })
    )
}

export function getToken() {
    return localStorage.getItem(TOKEN_KEY)
}

export function getCurrentUser() {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
}

export function isLoggedIn() {
    return !!getToken()
}

export function logout() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
}