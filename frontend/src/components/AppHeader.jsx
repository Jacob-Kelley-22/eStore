import { Link, useNavigate } from 'react-router-dom'
import { getCurrentUser, isLoggedIn, logout} from '../auth'

export default function AppHeader() {
    const navigate = useNavigate()
    const loggedIn = isLoggedIn()
    const user = getCurrentUser()

    function handleLogout() {
        logout()
        navigate('/login')
    }

    return (
        <header className="header">
            <nav className="nav">
                <Link to="/products">Products</Link>

                {loggedIn ? (
                    <>
                        <Link to="/cart">Cart</Link>
                        <Link to="/orders">Orders</Link>
                        <span className="nav-user">{user?.email}</span>
                        <button onClick={handleLogout}>Logout</button>
                        </>
                    ) : (
                        <>
                            <Link to="/login">Login</Link>
                            <Link to="/register">Register</Link>
                        </>
                        )
                    }
                </nav>
                </header>
        )

    }