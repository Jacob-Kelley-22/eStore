import { createBrowserRouter } from 'react-router-dom'
import ProductsPage from '../pages/ProductsPage'
import CartPage from '../pages/CartPage'
import LoginPage from '../pages/LoginPage'
import RegisterPage from '../pages/RegisterPage'
import CheckoutPage from '../pages/CheckoutPage'
import OrdersPage from '../pages/OrdersPage'
import AdminProductsPage from '../pages/AdminProductsPage'
import App from '../App'
import ProtectedRoute from '../components/ProtectedRoute'
import PublicOnlyRoute from '../components/PublicOnlyRoute'
import AdminRoute from '../components/AdminRoute'

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { path: '/', element: <ProductsPage /> },
      { path: '/products', element: <ProductsPage /> },
      {
        path: '/login',
        element: (
          <PublicOnlyRoute>
            <LoginPage />
          </PublicOnlyRoute>
        ),
      },
      {
        path: '/register',
        element: (
          <PublicOnlyRoute>
            <RegisterPage />
          </PublicOnlyRoute>
        ),
      },
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
      {
        path: '/admin/products',
        element: (
          <AdminRoute>
            <AdminProductsPage />
          </AdminRoute>
        ),
      },
    ],
  },
])

export default router