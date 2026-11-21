import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getProject, uploadSource } from '../api/projects'

export default function ProjectDetail() {
    const { id } = useParams()
    const [project, setProject] = useState(null)
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(true)

    const [file, setFile] = useState(null)
    const [uploading, setUploading] = useState(false)
    const [uploadMessage, setUploadMessage] = useState('')

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

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadProject().then()
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
        } catch (err) {
            setError(err.response?.data?.message || 'Upload failed')
        } finally {
            setUploading(false)
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

            {error && <p style={{ color: 'red' }}>{error}</p>}

            <p style={{ color: '#666' }}>Scan history and results arrive on Day 10 to 11.</p>
        </div>
    )
}