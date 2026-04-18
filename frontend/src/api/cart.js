import api from './client'

export async function addToCart(productId, quantity = 1) {
  const payload = {
    productId: Number(productId),
    quantity: Number(quantity),
  }

  const response = await api.post('/api/cart/items', payload)
  return response.data
}

export async function getCart() {
  const response = await api.get('/api/cart')
  return response.data
}

export async function removeCartItem(productId) {
  const response = await api.delete(`/api/cart/items/${productId}`)
  return response.data
}