import client from './client'

export async function listProjects() {
    const res = await client.get('/projects')
    return res.data
}

export async function createProject(name, type, targetUrl) {
    const res = await client.post('/projects', { name, type, targetUrl })
    return res.data
}

export async function deleteProject(id) {
    await client.delete(`/projects/${id}`)
}

export async function uploadSource(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    const res = await client.post(`/projects/${id}/upload`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    })
    return res.data
}
export async function getProject(id) {
    const res = await client.get(`/projects/${id}`)
    return res.data
}