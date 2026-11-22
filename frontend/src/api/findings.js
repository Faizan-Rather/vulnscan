import client from './client'

export async function listFindings(scanId, severity, page = 0, size = 20) {
    const params = { page, size }
    if (severity) params.severity = severity
    const res = await client.get(`/scans/${scanId}/findings`, { params })
    return res.data
}