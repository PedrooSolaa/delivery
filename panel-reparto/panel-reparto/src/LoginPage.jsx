import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { guardarToken } from './auth.js'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

export default function LoginPage() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [cargando, setCargando] = useState(false)

  const manejarEnvio = async (e) => {
    e.preventDefault()
    setError(null)
    setCargando(true)
    try {
      const respuesta = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      })

      if (!respuesta.ok) {
        const mensaje = await respuesta.text()
        setError(mensaje || 'Usuario o contraseña incorrectos.')
        setCargando(false)
        return
      }

      const datos = await respuesta.json()
      guardarToken(datos.token)
      navigate('/')
    } catch (e) {
      setError('No se pudo conectar con el servidor.')
      setCargando(false)
    }
  }

  return (
    <div className="login-panel">
      <form className="login-form" onSubmit={manejarEnvio}>
        <h1>Iniciar sesión</h1>
        <label>Usuario</label>
        <input value={username} onChange={(e) => setUsername(e.target.value)} autoFocus />
        <label>Contraseña</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <button type="submit" className="principal" disabled={cargando}>
          {cargando ? 'Entrando...' : 'Entrar'}
        </button>
        {error && <div className="pie-error">{error}</div>}
        <Link className="enlace-secundario" to="/olvide-password">¿Olvidaste tu contraseña?</Link>
      </form>
    </div>
  )
}