import { useEffect, useState, useCallback, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { cabeceraAuth, borrarToken } from './auth.js'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

const ESTADOS_EN_PROCESO = ['PENDIENTE', 'EN_PREPARACION', 'EN_REPARTO']

const ETAPA_LABEL = {
  PENDIENTE: 'Pendiente',
  EN_PREPARACION: 'En preparación',
  EN_REPARTO: 'En reparto',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
}

const ETAPA_COLOR = {
  PENDIENTE: { bg: '#eef1fb', color: '#2952cc' },
  EN_PREPARACION: { bg: '#fdf1e0', color: '#b9791f' },
  EN_REPARTO: { bg: '#e8f6ec', color: '#2fa84f' },
  ENTREGADO: { bg: '#eef2f5', color: '#5b6472' },
  CANCELADO: { bg: '#fbe9e8', color: '#d1453b' },
}

function minutosDesde(fecha) {
  return Math.max(0, Math.floor((Date.now() - new Date(fecha).getTime()) / 60000))
}

function colorTiempo(minutos) {
  if (minutos > 30) return 'var(--rojo)'
  if (minutos >= 15) return 'var(--ambar)'
  return 'var(--verde)'
}

function textoAsignacion(pedido) {
  if (pedido.repartidorAsignado) return pedido.repartidorAsignado.nombre
  if (pedido.proveedorExterno) {
    if (pedido.estadoEnvioExterno === 'PENDIENTE_ACEPTACION') {
      return `${pedido.proveedorExterno} · esperando aceptación…`
    }
    if (pedido.nombreRepartidorExterno) {
      return `${pedido.proveedorExterno} · ${pedido.nombreRepartidorExterno}`
    }
    return pedido.proveedorExterno
  }
  return 'Sin asignar'
}

export default function ListaPedidos() {
  const navigate = useNavigate()
  const [pedidos, setPedidos] = useState([])
  const [error, setError] = useState(null)
  const [hora, setHora] = useState(new Date())
  const [pestana, setPestana] = useState('proceso')
  const [configuracion, setConfiguracion] = useState({ nombreNegocio: 'Panel de reparto' })

  const manejarRespuesta = useCallback((respuesta) => {
    if (respuesta.status === 401) {
      borrarToken()
      navigate('/login')
      throw new Error('sesion expirada')
    }
    return respuesta
  }, [navigate])

  const cargarConfiguracion = useCallback(async () => {
    try {
      const respuesta = await fetch(`${API_BASE}/configuracion`, { headers: cabeceraAuth() })
      manejarRespuesta(respuesta)
      if (respuesta.ok) setConfiguracion(await respuesta.json())
    } catch (e) {}
  }, [manejarRespuesta])

  const cargarPedidos = useCallback(async () => {
    try {
      const respuesta = await fetch(`${API_BASE}/pedidos/recientes?horas=24`, { headers: cabeceraAuth() })
      manejarRespuesta(respuesta)
      if (!respuesta.ok) throw new Error('error')
      const datos = await respuesta.json()
      setPedidos(datos)
      setError(null)
    } catch (e) {
      setError('No se puede conectar con el backend en ' + API_BASE)
    }
  }, [manejarRespuesta])

  const cambiarEstado = useCallback(async (id, nuevoEstado) => {
    try {
      const respuesta = await fetch(`${API_BASE}/pedidos/${id}/estado?nuevoEstado=${nuevoEstado}`, {
        method: 'PATCH',
        headers: cabeceraAuth(),
      })
      manejarRespuesta(respuesta)
      if (!respuesta.ok) {
        const mensaje = await respuesta.text()
        alert(mensaje)
        return
      }
      cargarPedidos()
    } catch (e) {
      setError('No se pudo actualizar el pedido')
    }
  }, [cargarPedidos, manejarRespuesta])

  const cerrarSesion = () => {
    borrarToken()
    navigate('/login')
  }

  useEffect(() => {
    cargarPedidos()
    cargarConfiguracion()
    const intervaloPedidos = setInterval(cargarPedidos, 5000)
    const intervaloReloj = setInterval(() => setHora(new Date()), 1000)
    return () => {
      clearInterval(intervaloPedidos)
      clearInterval(intervaloReloj)
    }
  }, [cargarPedidos, cargarConfiguracion])

  const enProceso = useMemo(
    () => pedidos.filter((p) => ESTADOS_EN_PROCESO.includes(p.estado)),
    [pedidos]
  )
  const completados = useMemo(() => pedidos.filter((p) => p.estado === 'ENTREGADO'), [pedidos])
  const cancelados = useMemo(() => pedidos.filter((p) => p.estado === 'CANCELADO'), [pedidos])

  const buckets = useMemo(() => {
    let verde = 0, ambar = 0, rojo = 0
    enProceso.forEach((p) => {
      const min = minutosDesde(p.fechaCreacion)
      if (min > 30) rojo++
      else if (min >= 15) ambar++
      else verde++
    })
    return { verde, ambar, rojo }
  }, [enProceso])

  const filasVisibles =
    pestana === 'proceso' ? enProceso : pestana === 'completados' ? completados : cancelados

  return (
    <div className="panel">
      <div className="encabezado">
        <div className="marca">
          <span className="logo">📦</span>
          <h1>{configuracion.nombreNegocio}</h1>
        </div>
        <div className="encabezado-derecha">
          <div className="reloj">
            {hora.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
          </div>
          <button className="cerrar-sesion" onClick={cerrarSesion}>Cerrar sesión</button>
        </div>
      </div>

      <div className="tarjeta">
        <div className="pestanas">
          <button className={pestana === 'proceso' ? 'activa' : ''} onClick={() => setPestana('proceso')}>
            En proceso <span className="contador">{enProceso.length}</span>
          </button>
          <button className={pestana === 'completados' ? 'activa' : ''} onClick={() => setPestana('completados')}>
            Completados <span className="contador">{completados.length}</span>
          </button>
          <button className={pestana === 'cancelados' ? 'activa' : ''} onClick={() => setPestana('cancelados')}>
            Cancelados <span className="contador">{cancelados.length}</span>
          </button>
        </div>

        {pestana === 'proceso' && (
          <div className="buckets">
            <span><span className="punto" style={{ background: 'var(--verde)' }} /> Menos de 15 min ({buckets.verde})</span>
            <span><span className="punto" style={{ background: 'var(--ambar)' }} /> 15 a 30 min ({buckets.ambar})</span>
            <span><span className="punto" style={{ background: 'var(--rojo)' }} /> Más de 30 min ({buckets.rojo})</span>
          </div>
        )}

        {filasVisibles.length === 0 ? (
          <div className="vacio">Sin pedidos en esta vista.</div>
        ) : (
          <table className="tabla">
            <thead>
              <tr>
                <th>Pedido</th>
                <th>Detalle</th>
                <th>Distancia</th>
                <th>Asignado a</th>
                <th>Tiempo</th>
                <th>Etapa</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filasVisibles.map((pedido) => {
                const minutos = minutosDesde(pedido.fechaCreacion)
                const etapaColor = ETAPA_COLOR[pedido.estado]
                return (
                  <tr
                    key={pedido.id}
                    className="fila-clicable"
                    onClick={() => navigate(`/pedidos/${pedido.id}`)}
                  >
                    <td className="celda-pedido">
                      <div className="numero">{pedido.cliente}</div>
                      <div className="direccion">{pedido.direccion} · {pedido.telefono}</div>
                    </td>
                    <td>{pedido.detallePedido}</td>
                    <td>
                      {pedido.distanciaKm != null ? `${pedido.distanciaKm} km` : '—'}
                    </td>
                    <td>{textoAsignacion(pedido)}</td>
                    <td>
                      <span className="tiempo" style={{ color: colorTiempo(minutos) }}>
                        {minutos} min
                      </span>
                    </td>
                    <td>
                      <span className="etapa-badge" style={{ background: etapaColor.bg, color: etapaColor.color }}>
                        {ETAPA_LABEL[pedido.estado]}
                      </span>
                    </td>
                    <td onClick={(e) => e.stopPropagation()}>
                      <div className="acciones-fila">
                        {pedido.estado === 'PENDIENTE' && (
                          <button className="principal" onClick={() => cambiarEstado(pedido.id, 'EN_PREPARACION')}>Preparar</button>
                        )}
                        {pedido.estado === 'EN_PREPARACION' && (
                          <button className="principal" onClick={() => cambiarEstado(pedido.id, 'EN_REPARTO')}>Enviar a reparto</button>
                        )}
                        {pedido.estado === 'EN_REPARTO' && (
                          <button className="principal" onClick={() => cambiarEstado(pedido.id, 'ENTREGADO')}>Marcar entregado</button>
                        )}
                        {ESTADOS_EN_PROCESO.includes(pedido.estado) && (
                          <button onClick={() => cambiarEstado(pedido.id, 'CANCELADO')}>Cancelar</button>
                        )}
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>

      {error && <div className="pie-error">{error}</div>}
    </div>
  )
}