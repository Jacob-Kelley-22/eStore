import { useEffect, useState } from 'react'
import {
  createProduct,
  deleteProduct,
  getProducts,
  updateProduct,
} from '../api/products'

const emptyForm = {
  name: '',
  description: '',
  price: '',
  stockQuantity: '',
}

export default function AdminProductsPage() {
  const [products, setProducts] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    loadProducts()
  }, [])

  async function loadProducts() {
    setLoading(true)
    setError('')

    try {
      const data = await getProducts(0, 50)
      setProducts(data.content || [])
    } catch (err) {
      const responseData = err.response?.data
      const apiMessage =
        responseData?.message ||
        responseData?.error ||
        (typeof responseData === 'string' ? responseData : null) ||
        'Failed to load products.'

      setError(apiMessage)
    } finally {
      setLoading(false)
    }
  }

  function updateField(name, value) {
    setForm((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  function startEdit(product) {
    setEditingId(product.id)
    setForm({
      name: product.name ?? '',
      description: product.description ?? '',
      price: product.price?.toString() ?? '',
      stockQuantity: product.quantity?.toString() ?? '',
    })
    setError('')
    setMessage('')
  }

  function resetForm() {
    setEditingId(null)
    setForm(emptyForm)
  }

  function validateForm() {
    if (!form.name.trim()) return 'Name is required.'
    if (form.price === '' || Number(form.price) < 0) return 'Price must be 0 or greater.'
    if (form.stockQuantity === '' || Number(form.stockQuantity) < 0) {
      return 'Stock quantity must be 0 or greater.'
    }
    return ''
  }

  function buildPayload() {
    return {
      name: form.name.trim(),
      description: form.description.trim(),
      price: Number(form.price),
      stockQuantity: Number(form.stockQuantity),
    }
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    setMessage('')

    const validationError = validateForm()
    if (validationError) {
      setError(validationError)
      setSaving(false)
      return
    }

    try {
      const payload = buildPayload()

      if (editingId) {
        await updateProduct(editingId, payload)
        setMessage('Product updated successfully.')
      } else {
        await createProduct(payload)
        setMessage('Product created successfully.')
      }

      resetForm()
      await loadProducts()
    } catch (err) {
      const responseData = err.response?.data
      const apiMessage =
        responseData?.message ||
        responseData?.error ||
        (typeof responseData === 'string' ? responseData : null) ||
        'Failed to save product.'

      setError(apiMessage)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(productId) {
    setError('')
    setMessage('')

    try {
      await deleteProduct(productId)
      setMessage('Product deleted successfully.')
      await loadProducts()

      if (editingId === productId) {
        resetForm()
      }
    } catch (err) {
      const responseData = err.response?.data
      const apiMessage =
        responseData?.message ||
        responseData?.error ||
        (typeof responseData === 'string' ? responseData : null) ||
        'Failed to delete product.'

      setError(apiMessage)
    }
  }

  return (
    <div className="admin-page">
      <h1>Admin Products</h1>

      {message && <p className="success-text">{message}</p>}
      {error && <p className="error-text">{error}</p>}

      <div className="admin-layout">
        <form onSubmit={handleSubmit} className="form">
          <h2>{editingId ? 'Edit Product' : 'Create Product'}</h2>

          <label>
            Name
            <input
              type="text"
              value={form.name}
              onChange={(e) => updateField('name', e.target.value)}
              required
            />
          </label>

          <label>
            Description
            <textarea
              value={form.description}
              onChange={(e) => updateField('description', e.target.value)}
              rows="4"
              required
            />
          </label>

          <label>
            Price
            <input
              type="number"
              min="0"
              step="0.01"
              value={form.price}
              onChange={(e) => updateField('price', e.target.value)}
              required
            />
          </label>

          <label>
            Stock Quantity
            <input
              type="number"
              min="0"
              step="1"
              value={form.stockQuantity}
              onChange={(e) => updateField('stockQuantity', e.target.value)}
              required
            />
          </label>

          <div className="form-actions">
            <button type="submit" disabled={saving}>
              {saving
                ? 'Saving...'
                : editingId
                ? 'Update Product'
                : 'Create Product'}
            </button>

            {editingId && (
              <button type="button" onClick={resetForm}>
                Cancel
              </button>
            )}
          </div>
        </form>

        <div>
          <h2>Existing Products</h2>
          {loading && <p>Loading products...</p>}

          <div className="orders-list">
            {products.map((product) => (
              <div key={product.id} className="card">
                <h3>{product.name}</h3>
                <p>{product.description}</p>
                <p>${Number(product.price ?? 0).toFixed(2)}</p>
                <p>Stock: {product.quantity ?? 0}</p>

                <div className="form-actions">
                  <button type="button" onClick={() => startEdit(product)}>
                    Edit
                  </button>
                  <button type="button" onClick={() => handleDelete(product.id)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}