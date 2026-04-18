import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { registerUser } from '../api/auth'
import {saveAuth } from '../auth'

export default function RegisterPage() {
    const navigate = useNavigate()
    const[email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    async function handleSubmit(e) {
        e.preventDefault()
        setError('')
        setLoading(true)

        try {
            const registerResponse = await registerUser(email, password, 'USER')
            saveAuth(registerResponse)
            navigate('/products')
            } catch (err) {
                const apiMessage =
                  err.response?.data?.message ||
                  err.response?.data?.error ||
                  'Registration failed.'
                 setError(apiMessage)
                } finally {
                    setLoading(false)
                    }
        }

    return (
        <div className="form-page">
            <h1>Register</h1>

            <form onSubmit={handleSubmit} className="form">
                <label>
                    Email
                    <input
                      type="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />

                </label>

                <label>
                    Password
                    <input
                     type="password"
                     value={password}
                     minLength="6"
                     onChange={(e) => setPassword(e.target.value)}
                     required
                />
                </label>

                <button type="submit" disabled={loading}>
                    {loading ? 'Registering...' : 'Register'}
                </button>

                {error && <p className="error-text">{error}</p>}
                </form>
                </div>
        )
    }