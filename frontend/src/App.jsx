import { Outlet } from 'react-router-dom'
import AppHeader from './components/AppHeader'

export default function App() {
  return (
    <div>
      <AppHeader />
      <main className="container">
        <Outlet />
      </main>
    </div>
  )
}