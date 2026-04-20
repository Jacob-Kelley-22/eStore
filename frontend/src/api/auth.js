import api from './client'

export async function loginUser(email, password) {
  const response = await api.post('/api/auth/login', {
    email,
    password,
  })
  return response.data
}

export async function registerUser(email, password, role = 'USER') {
  const response = await api.post('/api/auth/register', {
    email,
    password,
    role,
  })
  return response.data
}