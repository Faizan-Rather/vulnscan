import { useEffect, useRef, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getScan } from '../api/scans'
import FindingsTable from '../components/FindingsTable'

export default function ScanResult() {
    const { id } = useParams()
    const [scan, setScan] = useState(null)
    const [error, setError] = useState('')
    const intervalRef = useRef(null)

    async function poll() {
        try {
            const data = await getScan(id)
            setScan(data)
            if (data.status === 'COMPLETED' || data.status === 'FAILED') {
                clearInterval(intervalRef.current)
            }
        } catch {
            setError('Could not load scan')
            clearInterval(intervalRef.current)
        }
    }

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        poll()
        intervalRef.current = setInterval(poll, 2000)
        return () => clearInterval(intervalRef.current)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id])

    if (error) return <p style={{ padding: 32, color: 'red' }}>{error}</p>
    if (!scan) return <p style={{ padding: 32 }}>Loading...</p>

    return (
        <div style={{ maxWidth: 900, margin: '40px auto', fontFamily: 'system-ui' }}>
            <Link to={`/projects/${scan.projectId}`}>&larr; Back to project</Link>
            <h1>Scan #{scan.id}</h1>
            <p>
                Status: <strong>{scan.status}</strong>
                {scan.status === 'RUNNING' || scan.status === 'PENDING' ? ' (checking every 2 seconds...)' : ''}
            </p>

            {scan.riskScore != null && <p>Risk score: <strong>{scan.riskScore}</strong> / 100</p>}

            {scan.status === 'FAILED' && (
                <p style={{ color: 'red' }}>Error: {scan.errorMessage || 'Unknown error'}</p>
            )}

            {scan.summary && (
                <div style={{ display: 'flex', gap: 16, margin: '16px 0' }}>
                    {Object.entries(scan.summary).map(([severity, count]) => (
                        <div
                            key={severity}
                            style={{ padding: '8px 16px', border: '1px solid #ccc', textAlign: 'center' }}
                        >
                            <div style={{ fontSize: 20, fontWeight: 'bold' }}>{count}</div>
                            <div style={{ color: '#666' }}>{severity}</div>
                        </div>
                    ))}
                </div>
            )}

            {scan.status === 'COMPLETED' && <FindingsTable scanId={scan.id} />}
        </div>
    )
}