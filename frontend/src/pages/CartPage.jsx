import { useEffect, useState } from 'react'
import { getCart, removeCartItem } from '../api/cart'

export default function CartPage() {
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    loadCart()
  }, [])

  async function loadCart() {
    setLoading(true)
    setError('')
    try {
      const data = await getCart()
      setCart(data)
    } catch (err) {
      const apiMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to load cart.'
      setError(apiMessage)
    } finally {
      setLoading(false)
    }
  }

  async function handleRemove(productId) {
    setMessage('')
    setError('')

    try {
      await removeCartItem(productId)
      setMessage('Item removed from cart.')
      await loadCart()
    } catch (err) {
      const apiMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to remove cart item.'
      setError(apiMessage)
    }
  }

  const items = cart?.items ?? []
  const total = Number(cart?.totalPrice ?? 0)

  return (
    <div>
      <h1>Cart</h1>

      {loading && <p>Loading...</p>}
      {message && <p className="success-text">{message}</p>}
      {error && <p className="error-text">{error}</p>}

      {!loading && items.length === 0 && <p>Your cart is empty.</p>}

      {items.length > 0 && (
        <>
          <div className="cart-list">
            {items.map((item) => (
              <div key={item.productId} className="card cart-item">
                <div>
                  <h2>{item.productName}</h2>
                  <p>${Number(item.unitPrice).toFixed(2)} each</p>
                  <p>Quantity: {item.quantity}</p>
                  <p>Line total: ${Number(item.lineTotal).toFixed(2)}</p>
                </div>

                <div className="cart-controls">
                  <button onClick={() => handleRemove(item.productId)}>
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

          <h2>Total: ${total.toFixed(2)}</h2>
        </>
      )}
    </div>
  )
}