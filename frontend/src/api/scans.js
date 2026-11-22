import client from './client'

export async function startScan(projectId) {
    const res = await client.post(`/projects/${projectId}/scans`)
    return res.data
}

export async function listScans(projectId) {
    const res = await client.get(`/projects/${projectId}/scans`)
    return res.data
}

export async function getScan(scanId) {
    const res = await client.get(`/scans/${scanId}`)
    return res.data
}