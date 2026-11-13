import { useEffect, useState } from 'react'
import axios from 'axios'

function App() {
  const [status, setStatus] = useState('checking...')

  useEffect(() => {
    axios.get('/api/health')
        .then(r => setStatus(r.data.status))
        .catch(() => setStatus('backend unreachable'))
  }, [])

  return <h1>VulnScan backend: {status}</h1>
}

export default App
