import { useEffect, useState, useRef, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { cabeceraAuth, borrarToken } from './auth.js'

import iconoMarcador from 'leaflet/dist/images/marker-icon.png'
import iconoMarcador2x from 'leaflet/dist/images/marker-icon-2x.png'
import sombraMarcador from 'leaflet/dist/images/marker-shadow.png'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: iconoMarcador2x,
  iconUrl: iconoMarcador,
  shadowUrl: sombraMarcador,
})

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

const EMPRESAS_EXTERNAS = ['Glovo', 'Uber Eats', 'Catcher']

export default function DetallePedido() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [pedido, setPedido] = useState(null)
  const [configuracion, setConfiguracion] = useState(null)
  const [repartidores, setRepartidores] = useState([])
  const [error, setError] = useState(null)

  const contenedorMapa = useRef(null)
  const mapaRef = useRef(null)

  const manejarRespuesta = useCallback((respuesta) => {
    if (respuesta.status === 401) {
      borrarToken()
      navigate('/login')
      throw new Error('sesion expirada')
    }
    return respuesta
  }, [navigate])

  const cargarPedido = useCallback(async () => {
    try {
      const respuesta = await fetch(`${API_BASE}/pedidos/${id}`, { headers: cabeceraAuth() })
      manejarRespuesta(respuesta)
      if (!respuesta.ok) throw new Error('error')
      setPedido(await respuesta.json())
    } catch (e) {
      setError('No se pudo cargar el pedido')
    }
  }, [id, manejarRespuesta])

  useEffect(() => {
    cargarPedido()
    fetch(`${API_BASE}/configuracion`, { headers: cabeceraAuth() })
      .then((r) => r.json()).then(setConfiguracion).catch(() => {})
    fetch(`${API_BASE}/repartidores`, { headers: cabeceraAuth() })
      .then((r) => r.json()).then(setRepartidores).catch(() => {})

    // Refresca el pedido cada 5 segundos para enterarnos en cuanto una
    // empresa externa acepta el envio.
    const intervalo = setInterval(cargarPedido, 5000)
    return () => clearInterval(intervalo)
  }, [cargarPedido])

  // El mapa solo se vuelve a dibujar si cambian las coordenadas, no en cada
  // refresco del pedido (si no, parpadearia cada 5 segundos).
  const latPedido = pedido?.latitud
  const lonPedido = pedido?.longitud
  const latNegocio = configuracion?.latitud
  const lonNegocio = configuracion?.longitud

  useEffect(() => {
    if (latPedido == null || latNegocio == null) return
    if (!contenedorMapa.current || mapaRef.current) return

    const mapa = L.map(contenedorMapa.current)
    mapaRef.current = mapa

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(mapa)

    const puntoNegocio = [latNegocio, lonNegocio]
    const puntoPedido = [latPedido, lonPedido]

    L.marker(puntoNegocio).addTo(mapa).bindPopup(configuracion.nombreNegocio)
    L.marker(puntoPedido).addTo(mapa).bindPopup(pedido.cliente)

    L.polyline([puntoNegocio, puntoPedido], { color: '#2952cc', dashArray: '6 6' }).addTo(mapa)

    mapa.fitBounds([puntoNegocio, puntoPedido], { padding: [40, 40] })

    return () => {
      mapa.remove()
      mapaRef.current = null
    }
  }, [latPedido, lonPedido, latNegocio, lonNegocio])

  const asignarRepartidor = async (repartidorId) => {
    if (!repartidorId) return
    const respuesta = await fetch(`${API_BASE}/pedidos/${id}/repartidor?repartidorId=${repartidorId}`, {
      method: 'PATCH',
      headers: cabeceraAuth(),
    })
    manejarRespuesta(respuesta)
    cargarPedido()
  }

  const asignarProveedorExterno = async (nombre) => {
    if (!nombre) return
    const respuesta = await fetch(`${API_BASE}/pedidos/${id}/proveedor-externo?nombre=${encodeURIComponent(nombre)}`, {
      method: 'PATCH',
      headers: cabeceraAuth(),
    })
    manejarRespuesta(respuesta)
    if (!respuesta.ok) {
      alert(await respuesta.text())
      return
    }
    cargarPedido()
  }

  const cambiarEstado = async (nuevoEstado) => {
    const respuesta = await fetch(`${API_BASE}/pedidos/${id}/estado?nuevoEstado=${nuevoEstado}`, {
      method: 'PATCH',
      headers: cabeceraAuth(),
    })
    manejarRespuesta(respuesta)
    if (!respuesta.ok) {
      alert(await respuesta.text())
      return
    }
    cargarPedido()
  }

  if (error) return <div className="panel"><div className="pie-error">{error}</div></div>
  if (!pedido) return <div className="panel">Cargando...</div>

  const valorAsignacionActual = pedido.repartidorAsignado
    ? `propio:${pedido.repartidorAsignado.id}`
    : pedido.proveedorExterno
      ? `externo:${pedido.proveedorExterno}`
      : ''

  const manejarCambioAsignacion = (valor) => {
    if (!valor) return
    const [tipo, dato] = valor.split(':')
    if (tipo === 'propio') asignarRepartidor(dato)
    else asignarProveedorExterno(dato)
  }

  return (
    <div className="panel">
      <button className="volver" onClick={() => navigate('/')}>&larr; Volver a la lista</button>

      <div className="tarjeta detalle-pedido">
        <div className="detalle-cabecera">
          <div>
            <h2>{pedido.cliente}</h2>
            <div className="direccion">{pedido.direccion} · {pedido.telefono}</div>
            <div className="direccion">{pedido.detallePedido}</div>
            {pedido.distanciaKm != null && (
              <div className="distancia-destacada">{pedido.distanciaKm} km desde {configuracion?.nombreNegocio}</div>
            )}
            {pedido.proveedorExterno && (
              <div className="envio-externo-box">
                <span className="envio-externo-empresa">{pedido.proveedorExterno}</span>
                {pedido.idEnvioExterno && <span>Envío #{pedido.idEnvioExterno}</span>}
                {pedido.estadoEnvioExterno === 'PENDIENTE_ACEPTACION' ? (
                  <span className="envio-esperando">Esperando que un repartidor lo acepte…</span>
                ) : (
                  <>
                    {pedido.nombreRepartidorExterno && (
                      <span>Repartidor: <strong>{pedido.nombreRepartidorExterno}</strong></span>
                    )}
                    {pedido.tiempoEstimadoExternoMinutos != null && (
                      <span>ETA: {pedido.tiempoEstimadoExternoMinutos} min</span>
                    )}
                  </>
                )}
              </div>
            )}
          </div>

          <div className="asignacion-box">
            <label>Asignar a</label>
            <select value={valorAsignacionActual} onChange={(e) => manejarCambioAsignacion(e.target.value)}>
              <option value="">Sin asignar</option>
              <optgroup label="Repartidores propios">
                {repartidores.map((r) => (
                  <option key={r.id} value={`propio:${r.id}`}>{r.nombre}</option>
                ))}
              </optgroup>
              <optgroup label="Empresas externas">
                {EMPRESAS_EXTERNAS.map((nombre) => (
                  <option key={nombre} value={`externo:${nombre}`}>{nombre}</option>
                ))}
              </optgroup>
            </select>
          </div>
        </div>

        {pedido.latitud != null && configuracion?.latitud != null ? (
          <div className="mapa-contenedor" ref={contenedorMapa} />
        ) : (
          <div className="mapa-sin-coords">No hay coordenadas suficientes para mostrar el mapa.</div>
        )}

        <div className="acciones-detalle">
          {pedido.estado === 'PENDIENTE' && (
            <button className="principal" onClick={() => cambiarEstado('EN_PREPARACION')}>Empezar a preparar</button>
          )}
          {pedido.estado === 'EN_PREPARACION' && (
            <button className="principal" onClick={() => cambiarEstado('EN_REPARTO')}>Enviar a reparto</button>
          )}
          {pedido.estado === 'EN_REPARTO' && (
            <button className="principal" onClick={() => cambiarEstado('ENTREGADO')}>Marcar entregado</button>
          )}
          {['PENDIENTE', 'EN_PREPARACION', 'EN_REPARTO'].includes(pedido.estado) && (
            <button onClick={() => cambiarEstado('CANCELADO')}>Cancelar</button>
          )}
        </div>
      </div>
    </div>
  )
}