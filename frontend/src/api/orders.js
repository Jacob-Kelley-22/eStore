import api from './client'

export async function getOrders(page = 0, size = 10, sortBy = 'createdAt', sortDirection = 'desc') {
  const response = await api.get('/api/orders', {
    params: { page, size, sortBy, sortDirection },
  })
  return response.data
}