import { useState } from 'react'
import { useSearchParams, useNavigate, Link } from 'react-router-dom'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')

  const [nuevaPassword, setNuevaPassword] = useState('')
  const [confirmarPassword, setConfirmarPassword] = useState('')
  const [mensaje, setMensaje] = useState(null)
  const [error, setError] = useState(null)
  const [cargando, setCargando] = useState(false)

  const manejarEnvio = async (e) => {
    e.preventDefault()
    setError(null)
    setMensaje(null)

    if (nuevaPassword !== confirmarPassword) {
      setError('Las contraseñas no coinciden.')
      return
    }
    if (!token) {
      setError('Falta el token del enlace. Usa el enlace completo que recibiste por correo.')
      return
    }

    setCargando(true)
    try {
      const respuesta = await fetch(`${API_BASE}/auth/restablecer-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, nuevaPassword }),
      })
      const texto = await respuesta.text()

      if (!respuesta.ok) {
        setError(texto)
        return
      }

      setMensaje(texto + ' Ya puedes iniciar sesión.')
      setTimeout(() => navigate('/login'), 2000)
    } catch (e) {
      setError('No se pudo conectar con el servidor.')
    } finally {
      setCargando(false)
    }
  }

  return (
    <div className="login-panel">
      <form className="login-form" onSubmit={manejarEnvio}>
        <h1>Elegir nueva contraseña</h1>
        <label>Nueva contraseña</label>
        <input
          type="password"
          value={nuevaPassword}
          onChange={(e) => setNuevaPassword(e.target.value)}
          autoFocus
          required
        />
        <label>Confirmar contraseña</label>
        <input
          type="password"
          value={confirmarPassword}
          onChange={(e) => setConfirmarPassword(e.target.value)}
          required
        />
        <button type="submit" className="principal" disabled={cargando}>
          {cargando ? 'Guardando...' : 'Guardar nueva contraseña'}
        </button>
        {mensaje && <div className="mensaje-info">{mensaje}</div>}
        {error && <div className="pie-error">{error}</div>}
        <Link className="enlace-secundario" to="/login">Volver al login</Link>
      </form>
    </div>
  )
}