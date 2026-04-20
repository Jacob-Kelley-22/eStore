import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { checkoutCart } from '../api/checkout'

function createIdempotencyKey() {
  if (window.crypto?.randomUUID) {
    return `checkout_${window.crypto.randomUUID().replace(/-/g, '')}`.slice(0, 40)
  }
  return `checkout_${Date.now()}_${Math.random().toString(36).slice(2, 18)}`
}

export default function CheckoutPage() {
  const navigate = useNavigate()

  const [checkoutKey, setCheckoutKey] = useState(() => createIdempotencyKey())

  const [form, setForm] = useState({
    cardNumber: '',
    cardHolderName: '',
    expirationMonth: '',
    expirationYear: '',
    cvv: '',
  })

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})

  const currentYear = new Date().getFullYear()

  const yearOptions = useMemo(() => {
    return Array.from({ length: 12 }, (_, i) => currentYear + i)
  }, [currentYear])

  function updateField(name, value) {
    setForm((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  function buildPaymentRequest() {
    return {
      idempotencyKey: checkoutKey,
      cardNumber: form.cardNumber.trim(),
      cardHolderName: form.cardHolderName.trim(),
      expirationMonth: Number(form.expirationMonth),
      expirationYear: Number(form.expirationYear),
      cvv: form.cvv.trim(),
    }
  }

  function validateForm() {
    const errors = {}

    if (!form.cardNumber.trim()) errors.cardNumber = 'Card number is required.'
    if (!form.cardHolderName.trim()) errors.cardHolderName = 'Cardholder name is required.'
    if (!form.expirationMonth) errors.expirationMonth = 'Expiration month is required.'
    if (!form.expirationYear) errors.expirationYear = 'Expiration year is required.'
    if (!form.cvv.trim()) errors.cvv = 'CVV is required.'

    setFieldErrors(errors)
    return Object.keys(errors).length === 0
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setFieldErrors({})

    if (!validateForm()) return

    setLoading(true)

    try {
      const order = await checkoutCart(buildPaymentRequest())

      setForm({
        cardNumber: '',
        cardHolderName: '',
        expirationMonth: '',
        expirationYear: '',
        cvv: '',
      })
      setFieldErrors({})
      setCheckoutKey(createIdempotencyKey())

      navigate('/orders', {
        state: { checkoutSuccess: true, order },
      })
    } catch (err) {
      const responseData = err.response?.data

      if (responseData && typeof responseData === 'object' && !Array.isArray(responseData)) {
        setFieldErrors(responseData)
      }

      const apiMessage =
        responseData?.message ||
        responseData?.error ||
        (typeof responseData === 'string' ? responseData : null) ||
        'Checkout failed.'

      setError(apiMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="form-page">
      <h1>Checkout</h1>
      <p>Enter payment details to place your order.</p>

      <form onSubmit={handleSubmit} className="form">
        <label>
          Card Number
          <input
            type="text"
            placeholder="4111 1111 1111 1111"
            value={form.cardNumber}
            onChange={(e) => updateField('cardNumber', e.target.value)}
            required
          />
          {fieldErrors.cardNumber && (
            <span className="error-text">{fieldErrors.cardNumber}</span>
          )}
        </label>

        <label>
          Cardholder Name
          <input
            type="text"
            value={form.cardHolderName}
            onChange={(e) => updateField('cardHolderName', e.target.value)}
            required
          />
          {fieldErrors.cardHolderName && (
            <span className="error-text">{fieldErrors.cardHolderName}</span>
          )}
        </label>

        <label>
          Expiration Month
          <select
            value={form.expirationMonth}
            onChange={(e) => updateField('expirationMonth', e.target.value)}
            required
          >
            <option value="">Select month</option>
            {Array.from({ length: 12 }, (_, i) => i + 1).map((month) => (
              <option key={month} value={month}>
                {month}
              </option>
            ))}
          </select>
          {fieldErrors.expirationMonth && (
            <span className="error-text">{fieldErrors.expirationMonth}</span>
          )}
        </label>

        <label>
          Expiration Year
          <select
            value={form.expirationYear}
            onChange={(e) => updateField('expirationYear', e.target.value)}
            required
          >
            <option value="">Select year</option>
            {yearOptions.map((year) => (
              <option key={year} value={year}>
                {year}
              </option>
            ))}
          </select>
          {fieldErrors.expirationYear && (
            <span className="error-text">{fieldErrors.expirationYear}</span>
          )}
        </label>

        <label>
          CVV
          <input
            type="password"
            value={form.cvv}
            onChange={(e) => updateField('cvv', e.target.value)}
            required
          />
          {fieldErrors.cvv && (
            <span className="error-text">{fieldErrors.cvv}</span>
          )}
        </label>

        <button type="submit" disabled={loading}>
          {loading ? 'Processing...' : 'Place Order'}
        </button>

        {error && <p className="error-text">{error}</p>}
      </form>
    </div>
  )
}