import { createBrowserRouter } from 'react-router-dom'
import ProductsPage from '../pages/ProductsPage'
import CartPage from '../pages/CartPage'
import LoginPage from '../pages/LoginPage'
import RegisterPage from '../pages/RegisterPage'
import CheckoutPage from '../pages/CheckoutPage'
import OrdersPage from '../pages/OrdersPage'
import App from '../App'
import ProtectedRoute from '../components/ProtectedRoute'

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { path: '/', element: <ProductsPage /> },
      { path: '/products', element: <ProductsPage /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
      {
        path: '/cart',
        element: (
          <ProtectedRoute>
            <CartPage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/checkout',
        element: (
          <ProtectedRoute>
            <CheckoutPage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/orders',
        element: (
          <ProtectedRoute>
            <OrdersPage />
          </ProtectedRoute>
        ),
      },
    ],
  },
])

export default router