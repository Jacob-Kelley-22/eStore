import api from './client'

export async function checkoutCart(paymentRequest) {
    const response = await api.post('/api/cart/checkout', paymentRequest)
    return response.data
}