import { Navigate } from 'react-router-dom'
import { isAdmin, isLoggedIn } from '../auth'

export default function AdminRoute({ children }) {
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />
  }

  if (!isAdmin()) {
    return <Navigate to="/products" replace />
  }

  return children
}