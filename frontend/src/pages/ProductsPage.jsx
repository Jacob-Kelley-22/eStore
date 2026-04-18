import { useEffect, useState } from 'react'
import api from '../api/client'
import { addToCart } from '../api/cart'

export default function ProductsPage() {
  const [products, setProducts] = useState([])
  const [pageNumber, setPageNumber] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [addingProductId, setAddingProductId] = useState(null)

  useEffect(() => {
    loadProducts(pageNumber)
  }, [pageNumber])

  async function loadProducts(page = 0) {
    setLoading(true)
    setError('')

    try {
      const res = await api.get('/api/products', {
        params: {
          page,
          size: 10,
        },
      })

      const pageData = res.data

      setProducts(pageData.content || [])
      setTotalPages(pageData.totalPages ?? 0)
      setTotalElements(pageData.totalElements ?? 0)
    } catch (err) {
      console.error('Failed to load products:', err)
      setError('Failed to load products.')
      setProducts([])
      setTotalPages(0)
      setTotalElements(0)
    } finally {
      setLoading(false)
    }
  }

  async function handleAddToCart(productId) {
    setMessage('')
    setError('')
    setAddingProductId(productId)

    try {
      await addToCart(productId, 1)
      setMessage('Item added to cart.')
    } catch (err) {
      console.error('Failed to add to cart:', err)
      const apiMessage =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to add item to cart.'
      setError(apiMessage)
    } finally {
      setAddingProductId(null)
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
      <h1>Products</h1>

      {loading && <p>Loading...</p>}
      {message && <p className="success-text">{message}</p>}
      {error && <p className="error-text">{error}</p>}

      {!loading && products.length === 0 && !error && (
        <p>No products available.</p>
      )}

      <div className="grid">
        {products.map((p) => (
          <div key={p.id} className="card">
            <h2>{p.name}</h2>
            <p>{p.description}</p>
            <p>${Number(p.price ?? 0).toFixed(2)}</p>

            <button
              onClick={() => handleAddToCart(p.id)}
              disabled={(p.quantity ?? 0) === 0 || addingProductId === p.id}
            >
              {(p.quantity ?? 0) === 0
                ? 'Out of Stock'
                : addingProductId === p.id
                ? 'Adding...'
                : 'Add to Cart'}
            </button>
          </div>
        ))}
      </div>

      {totalPages > 0 && (
        <div className="pagination">
          <button onClick={goToPreviousPage} disabled={pageNumber === 0}>
            Previous
          </button>

          <span>
            Page {pageNumber + 1} of {totalPages} ({totalElements} products)
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