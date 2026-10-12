import { useState } from 'react'
import { Link } from 'react-router-dom'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [mensaje, setMensaje] = useState(null)
  const [cargando, setCargando] = useState(false)

  const manejarEnvio = async (e) => {
    e.preventDefault()
    setCargando(true)
    setMensaje(null)
    try {
      const respuesta = await fetch(`${API_BASE}/auth/olvide-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      })
      const texto = await respuesta.text()
      setMensaje(texto)
    } catch (e) {
      setMensaje('No se pudo conectar con el servidor.')
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="login-panel">
      <form className="login-form" onSubmit={manejarEnvio}>
        <h1>Recuperar contraseña</h1>
        <label>Email</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoFocus required />
        <button type="submit" className="principal" disabled={cargando}>
          {cargando ? 'Enviando...' : 'Enviar enlace'}
        </button>
        {mensaje && <div className="mensaje-info">{mensaje}</div>}
        <Link className="enlace-secundario" to="/login">Volver al login</Link>
      </form>
    </div>
  )
}