const SEVERITIES = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']

const COLORS = {
    CRITICAL: '#7f1d1d',
    HIGH: '#b91c1c',
    MEDIUM: '#b45309',
    LOW: '#1d4ed8',
    INFO: '#555',
}

export default function SeverityChart({ summary }) {
    if (!summary) return null

    const max = Math.max(1, ...SEVERITIES.map((s) => summary[s] || 0));

    return (
        <div style={{ display: 'flex', alignItems: 'flex-end', gap: 16, height: 140, margin: '16px 0' }}>
            {SEVERITIES.map((sev) => {
                const count = summary[sev] || 0
                const heightPct = (count / max) * 100
                return (
                    <div key={sev} style={{ textAlign: 'center', width: 60 }}>
                        <div style={{ height: 100, display: 'flex', alignItems: 'flex-end' }}>
                            <div
                                title={`${sev}: ${count}`}
                                style={{
                                    width: '100%',
                                    height: `${heightPct}%`,
                                    minHeight: count > 0 ? 4 : 0,
                                    background: COLORS[sev],
                                    borderRadius: '3px 3px 0 0',
                                    transition: 'height 0.3s ease',
                                }}
                            />
                        </div>
                        <div style={{ fontWeight: 'bold', marginTop: 4 }}>{count}</div>
                        <div style={{ fontSize: 11, color: '#666' }}>{sev}</div>
                    </div>
                )
            })}
        </div>
    )
}