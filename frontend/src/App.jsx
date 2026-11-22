import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import Login from './pages/Login'
import Register from './pages/Register'
import Projects from './pages/Projects'
import ProtectedRoute from './pages/ProtectedRoute'
import ProjectDetail from './pages/ProjectDetail'
import ScanResult from './pages/ScanResult'
export default function App() {
  return (
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
                path="/projects"
                element={
                  <ProtectedRoute>
                    <Projects />
                  </ProtectedRoute>
                }
            />
              <Route
                  path="/projects/:id"
                  element={
                      <ProtectedRoute>
                          <ProjectDetail />
                      </ProtectedRoute>
                  }
              />
              <Route
                  path="/scans/:id"
                  element={
                      <ProtectedRoute>
                          <ScanResult />
                      </ProtectedRoute>
                  }
              />
            <Route path="/" element={<Navigate to="/projects" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
  )
}