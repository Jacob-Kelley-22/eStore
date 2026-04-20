import api from './client'

export async function getProducts(page = 0, size = 10) {
  const response = await api.get('/api/products', {
    params: { page, size },
  })
  return response.data
}

export async function createProduct(product) {
  const response = await api.post('/api/products', product)
  return response.data
}

export async function updateProduct(productId, product) {
  const response = await api.put(`/api/products/${productId}`, product)
  return response.data
}

export async function deleteProduct(productId) {
  const response = await api.delete(`/api/products/${productId}`)
  return response.data
}