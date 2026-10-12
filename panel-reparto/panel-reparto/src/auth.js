const CLAVE_TOKEN = 'reparto_token'

export function guardarToken(token) {
  localStorage.setItem(CLAVE_TOKEN, token)
}

export function borrarToken() {
  localStorage.removeItem(CLAVE_TOKEN)
}

export function obtenerToken() {
  return localStorage.getItem(CLAVE_TOKEN)
}

export function hayToken() {
  return obtenerToken() !== null
}

export function cabeceraAuth() {
  const token = obtenerToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}