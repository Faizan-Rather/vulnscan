import { useAuth } from '../context/AuthContext'

export default function Projects() {
    const { email, logout } = useAuth()

    return (
        <div style={{ padding: 32, fontFamily: 'system-ui' }}>
            <h1>Projects</h1>
            <p>Logged in as {email}</p>
            <button onClick={logout}>Log out</button>
        </div>
    )
}