import { Navigate } from 'react-router-dom'
import { isLoggedIn } from '../auth'

export default function PublicOnlyRoute({ children }) {
  if (isLoggedIn()) {
    return <Navigate to="/products" replace />
  }

  return children
}