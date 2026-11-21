import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { listProjects, createProject, deleteProject } from '../api/projects'

export default function Projects() {
    const { email, logout } = useAuth()
    const [projects, setProjects] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const [name, setName] = useState('')
    const [type, setType] = useState('CODE')
    const [targetUrl, setTargetUrl] = useState('')
    const [creating, setCreating] = useState(false)

    async function loadProjects() {
        setLoading(true)
        try {
            const data = await listProjects()
            setProjects(data)
        } catch {
            setError('Could not load projects')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadProjects().then()
    }, [])

    async function handleCreate(e) {
        e.preventDefault()
        setError('')
        setCreating(true)
        try {
            await createProject(name, type, type === 'WEB' ? targetUrl : null)
            setName('')
            setTargetUrl('')
            setType('CODE')
            await loadProjects()
        } catch (err) {
            setError(err.response?.data?.message || 'Could not create project')
        } finally {
            setCreating(false)
        }
    }

    async function handleDelete(id) {
        if (!confirm('Delete this project and all its scans?')) return
        try {
            await deleteProject(id)
            await loadProjects()
        } catch {
            setError('Could not delete project')
        }
    }

    return (
        <div style={{ maxWidth: 700, margin: '40px auto', fontFamily: 'system-ui' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
                <h1>Projects</h1>
                <div>
                    <span style={{ marginRight: 12 }}>{email}</span>
                    <button onClick={logout}>Log out</button>
                </div>
            </div>

            <form onSubmit={handleCreate} style={{ margin: '24px 0', padding: 16, border: '1px solid #ccc' }}>
                <h3 style={{ marginTop: 0 }}>New project</h3>
                <div style={{ marginBottom: 8 }}>
                    <input
                        placeholder="Project name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                        style={{ padding: 8, width: '60%', marginRight: 8 }}
                    />
                    <select value={type} onChange={(e) => setType(e.target.value)} style={{ padding: 8 }}>
                        <option value="CODE">Code (upload zip)</option>
                        <option value="WEB">Website (URL)</option>
                    </select>
                </div>
                {type === 'WEB' && (
                    <div style={{ marginBottom: 8 }}>
                        <input
                            placeholder="https://example.com"
                            value={targetUrl}
                            onChange={(e) => setTargetUrl(e.target.value)}
                            required
                            style={{ padding: 8, width: '100%' }}
                        />
                    </div>
                )}
                <button type="submit" disabled={creating}>
                    {creating ? 'Creating...' : 'Create project'}
                </button>
            </form>

            {error && <p style={{ color: 'red' }}>{error}</p>}

            {loading ? (
                <p>Loading...</p>
            ) : projects.length === 0 ? (
                <p>No projects yet. Create one above.</p>
            ) : (
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    {projects.map((p) => (
                        <li
                            key={p.id}
                            style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                padding: 12,
                                borderBottom: '1px solid #eee',
                            }}
                        >
                            <div>
                                <Link to={`/projects/${p.id}`} style={{ fontWeight: 'bold' }}>{p.name}</Link>
                                <span style={{ marginLeft: 8, color: '#666' }}>
                  {p.type}{p.targetUrl ? ` · ${p.targetUrl}` : ''}
                </span>
                            </div>
                            <button onClick={() => handleDelete(p.id)}>Delete</button>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    )
}