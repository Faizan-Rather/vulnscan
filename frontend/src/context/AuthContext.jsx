import { createContext, useContext, useState } from 'react'
import client from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
    const [email, setEmail] = useState(localStorage.getItem('vulnscan_email'))

    async function login(email, password) {
        const res = await client.post('/auth/login', { email, password })
        localStorage.setItem('vulnscan_token', res.data.token)
        localStorage.setItem('vulnscan_email', res.data.email)
        setEmail(res.data.email)
    }

    async function register(email, password) {
        const res = await client.post('/auth/register', { email, password })
        localStorage.setItem('vulnscan_token', res.data.token)
        localStorage.setItem('vulnscan_email', res.data.email)
        setEmail(res.data.email)
    }

    function logout() {
        localStorage.removeItem('vulnscan_token')
        localStorage.removeItem('vulnscan_email')
        setEmail(null)
    }

    const value = { email, isLoggedIn: !!email, login, register, logout }

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
    return useContext(AuthContext)
}