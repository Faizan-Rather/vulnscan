import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getProject, uploadSource } from '../api/projects'
import { startScan, listScans } from '../api/scans'
export default function ProjectDetail() {
    const { id } = useParams()
    const [project, setProject] = useState(null)
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(true)

    const [file, setFile] = useState(null)
    const [uploading, setUploading] = useState(false)
    const [uploadMessage, setUploadMessage] = useState('')
    const [scans, setScans] = useState([])
    const [starting, setStarting] = useState(false)
    async function loadProject() {
        setLoading(true)
        try {
            const data = await getProject(id)
            setProject(data)
        } catch {
            setError('Could not load project')
        } finally {
            setLoading(false)
        }
    }
    async function loadScans() {
        try {
            const data = await listScans(id)
            setScans(data)
        } catch {
            // Not fatal if this fails; the project itself still shows.
        }
    }
    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadProject()
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadScans()
    }, [id])

    async function handleUpload(e) {
        e.preventDefault()
        if (!file) return
        setUploading(true)
        setUploadMessage('')
        setError('')
        try {
            await uploadSource(id, file)
            setUploadMessage('Upload successful. You can now start a scan.')
            setFile(null)
            await loadProject()
            await loadScans()
        } catch (err) {
            setError(err.response?.data?.message || 'Upload failed')
        } finally {
            setUploading(false)
        }
    }
    async function handleStartScan() {
        setStarting(true)
        setError('')
        try {
            await startScan(id)
            await loadScans()
        } catch (err) {
            setError(err.response?.data?.message || 'Could not start scan')
        } finally {
            setStarting(false)
        }
    }
    if (loading) return <p style={{ padding: 32 }}>Loading...</p>
    if (!project) return <p style={{ padding: 32 }}>{error || 'Project not found'}</p>

    return (
        <div style={{ maxWidth: 700, margin: '40px auto', fontFamily: 'system-ui' }}>
            <Link to="/projects">&larr; Back to projects</Link>
            <h1>{project.name}</h1>
            <p style={{ color: '#666' }}>
                {project.type}{project.targetUrl ? ` · ${project.targetUrl}` : ''}
            </p>

            {project.type === 'CODE' && (
                <div style={{ margin: '24px 0', padding: 16, border: '1px solid #ccc' }}>
                    <h3 style={{ marginTop: 0 }}>Upload source code (.zip)</h3>
                    <form onSubmit={handleUpload}>
                        <input
                            type="file"
                            accept=".zip"
                            onChange={(e) => setFile(e.target.files[0])}
                        />
                        <button type="submit" disabled={!file || uploading} style={{ marginLeft: 8 }}>
                            {uploading ? 'Uploading...' : 'Upload'}
                        </button>
                    </form>
                    {uploadMessage && <p style={{ color: 'green' }}>{uploadMessage}</p>}
                </div>
            )}

            <div style={{ margin: '24px 0' }}>
                <button onClick={handleStartScan} disabled={starting}>
                    {starting ? 'Starting...' : 'Start scan'}
                </button>
            </div>

            {error && <p style={{ color: 'red' }}>{error}</p>}

            <h3>Scan history</h3>
            {scans.length === 0 ? (
                <p style={{ color: '#666' }}>No scans yet.</p>
            ) : (
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    {scans.map((s) => (
                        <li
                            key={s.id}
                            style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                padding: 10,
                                borderBottom: '1px solid #eee',
                            }}
                        >
                            <Link to={`/scans/${s.id}`}>
                                Scan #{s.id} — {s.status}
                                {s.riskScore != null ? ` (risk ${s.riskScore})` : ''}
                            </Link>
                            <span style={{ color: '#666' }}>
                {new Date(s.createdAt).toLocaleString()}
              </span>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    )
}