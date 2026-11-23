export default function RiskTrend({ scans }) {
    const completed = scans.filter((s) => s.riskScore != null).slice().reverse()
    if (completed.length < 2) return null

    const max = Math.max(1, ...completed.map((s) => s.riskScore))
    const latest = completed[completed.length - 1].riskScore
    const previous = completed[completed.length - 2].riskScore
    const diff = latest - previous

    return (
        <div style={{ margin: '16px 0' }}>
            <h4 style={{ marginBottom: 4 }}>Risk trend</h4>
            <div style={{ display: 'flex', alignItems: 'flex-end', gap: 6, height: 60 }}>
                {completed.map((s) => (
                    <div
                        key={s.id}
                        title={`Scan #${s.id}: risk ${s.riskScore}`}
                        style={{
                            width: 14,
                            height: `${(s.riskScore / max) * 100}%`,
                            minHeight: 4,
                            background: '#1d4ed8',
                            borderRadius: '2px 2px 0 0',
                        }}
                    />
                ))}
            </div>
            <p style={{ fontSize: 13, color: diff > 0 ? '#b91c1c' : diff < 0 ? '#15803d' : '#666', marginTop: 4 }}>
                {diff > 0 && `Risk increased by ${diff} since the previous scan.`}
                {diff < 0 && `Risk decreased by ${Math.abs(diff)} since the previous scan.`}
                {diff === 0 && 'Risk unchanged since the previous scan.'}
            </p>
        </div>
    )
}