import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { getOrders } from '../api/orders'

export default function OrdersPage() {
  const location = useLocation()

  const [orders, setOrders] = useState([])
  const [pageNumber, setPageNumber] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const checkoutOrder = location.state?.order
  const checkoutSuccess = location.state?.checkoutSuccess

  useEffect(() => {
    loadOrders(pageNumber)
  }, [pageNumber])

  async function loadOrders(page = 0) {
    setLoading(true)
    setError('')

    try {
      const data = await getOrders(page, 10, 'createdAt', 'desc')
      setOrders(data.content || [])
      setTotalPages(data.totalPages ?? 0)
      setTotalElements(data.totalElements ?? 0)
    } catch (err) {
      const responseData = err.response?.data
      const apiMessage =
        responseData?.message ||
        responseData?.error ||
        (typeof responseData === 'string' ? responseData : null) ||
        'Failed to load orders.'

      setError(apiMessage)
      setOrders([])
      setTotalPages(0)
      setTotalElements(0)
    } finally {
      setLoading(false)
    }
  }

  function goToPreviousPage() {
    setPageNumber((current) => Math.max(current - 1, 0))
  }

  function goToNextPage() {
    setPageNumber((current) => Math.min(current + 1, totalPages - 1))
  }

  return (
    <div>
      <h1>Orders</h1>

      {checkoutSuccess && checkoutOrder && (
        <p className="success-text">
          Order #{checkoutOrder.id} was placed successfully.
        </p>
      )}

      {loading && <p>Loading...</p>}
      {error && <p className="error-text">{error}</p>}

      {!loading && orders.length === 0 && !error && (
        <p>You have not placed any orders yet.</p>
      )}

      <div className="orders-list">
        {orders.map((order) => (
          <div key={order.id} className="card order-card">
            <div className="order-header">
              <div>
                <h2>Order #{order.id}</h2>
                <p>Status: {order.status}</p>
                <p>
                  Created:{' '}
                  {order.createdAt
                    ? new Date(order.createdAt).toLocaleString()
                    : 'Unknown'}
                </p>
              </div>

              <div className="order-total">
                <strong>${Number(order.totalPrice ?? 0).toFixed(2)}</strong>
              </div>
            </div>

            <div className="order-items">
              <h3>Items</h3>

              {order.items?.length > 0 ? (
                order.items.map((item) => (
                  <div
                    key={`${order.id}-${item.productId}`}
                    className="order-item-row"
                  >
                    <div>
                      <strong>{item.productName}</strong>
                      <p>Qty: {item.quantity}</p>
                    </div>

                    <div className="order-item-pricing">
                      <p>
                        ${Number(item.priceAtPurchase ?? 0).toFixed(2)} each
                      </p>
                      <p>
                        Line total: ${Number(item.lineTotal ?? 0).toFixed(2)}
                      </p>
                    </div>
                  </div>
                ))
              ) : (
                <p>No items found for this order.</p>
              )}
            </div>
          </div>
        ))}
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button onClick={goToPreviousPage} disabled={pageNumber === 0}>
            Previous
          </button>

          <span>
            Page {pageNumber + 1} of {totalPages} ({totalElements} orders)
          </span>

          <button
            onClick={goToNextPage}
            disabled={pageNumber >= totalPages - 1}
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}