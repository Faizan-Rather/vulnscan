import axios from 'axios'

const client = axios.create({
    baseURL: '/api',
})

// Attach the saved token to every outgoing request, if we have one.
client.interceptors.request.use((config) => {
    const token = localStorage.getItem('vulnscan_token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// If the server says our token is invalid or expired, clear it so the user
// is sent back to login instead of seeing confusing errors everywhere.
client.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('vulnscan_token')
            localStorage.removeItem('vulnscan_email')
        }
        return Promise.reject(error)
    }
)

export default client