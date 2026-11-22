import { useEffect, useState } from 'react'
import { listFindings } from '../api/findings'

const SEVERITIES = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']

const SEVERITY_COLORS = {
    CRITICAL: '#7f1d1d',
    HIGH: '#b91c1c',
    MEDIUM: '#b45309',
    LOW: '#1d4ed8',
    INFO: '#555',
}

export default function FindingsTable({ scanId }) {
    const [severity, setSeverity] = useState('')
    const [page, setPage] = useState(0)
    const [data, setData] = useState(null)
    const [loading, setLoading] = useState(true)

    async function load() {
        setLoading(true)
        try {
            const result = await listFindings(scanId, severity || null, page, 20)
            setData(result)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        load()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [scanId, severity, page])

    function handleSeverityChange(e) {
        setSeverity(e.target.value)
        setPage(0)
    }

    return (
        <div>
            <h3>Findings</h3>
            <div style={{ marginBottom: 12 }}>
                <label>Filter by severity: </label>
                <select value={severity} onChange={handleSeverityChange} style={{ padding: 6 }}>
                    <option value="">All</option>
                    {SEVERITIES.map((s) => (
                        <option key={s} value={s}>{s}</option>
                    ))}
                </select>
            </div>

            {loading ? (
                <p>Loading findings...</p>
            ) : !data || data.content.length === 0 ? (
                <p style={{ color: '#666' }}>No findings for this filter.</p>
            ) : (
                <>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                        <tr style={{ textAlign: 'left', borderBottom: '2px solid #ccc' }}>
                            <th style={{ padding: 8 }}>Severity</th>
                            <th style={{ padding: 8 }}>Title</th>
                            <th style={{ padding: 8 }}>File</th>
                            <th style={{ padding: 8 }}>Remediation</th>
                        </tr>
                        </thead>
                        <tbody>
                        {data.content.map((f) => (
                            <tr key={f.id} style={{ borderBottom: '1px solid #eee' }}>
                                <td style={{ padding: 8 }}>
                    <span
                        style={{
                            color: '#fff',
                            background: SEVERITY_COLORS[f.severity] || '#555',
                            padding: '2px 8px',
                            borderRadius: 4,
                            fontSize: 12,
                        }}
                    >
                      {f.severity}
                    </span>
                                </td>
                                <td style={{ padding: 8 }}>
                                    <div>{f.title}</div>
                                    {f.description && (
                                        <div style={{ color: '#666', fontSize: 13 }}>{f.description}</div>
                                    )}
                                </td>
                                <td style={{ padding: 8, fontFamily: 'monospace', fontSize: 13 }}>
                                    {f.filePath || '—'}
                                </td>
                                <td style={{ padding: 8, fontSize: 13 }}>{f.remediation || '—'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    <div style={{ marginTop: 12 }}>
                        <button disabled={data.first} onClick={() => setPage((p) => p - 1)}>
                            Previous
                        </button>
                        <span style={{ margin: '0 12px' }}>
              Page {data.number + 1} of {data.totalPages}
            </span>
                        <button disabled={data.last} onClick={() => setPage((p) => p + 1)}>
                            Next
                        </button>
                    </div>
                </>
            )}
        </div>
    )
}