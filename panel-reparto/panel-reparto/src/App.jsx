import { Routes, Route, Navigate } from 'react-router-dom'
import ListaPedidos from './ListaPedidos.jsx'
import DetallePedido from './DetallePedido.jsx'
import LoginPage from './LoginPage.jsx'
import ForgotPasswordPage from './ForgotPasswordPage.jsx'
import ResetPasswordPage from './ResetPasswordPage.jsx'
import { hayToken } from './auth.js'

function RutaProtegida({ children }) {
  return hayToken() ? children : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/olvide-password" element={<ForgotPasswordPage />} />
      <Route path="/restablecer-password" element={<ResetPasswordPage />} />
      <Route path="/" element={<RutaProtegida><ListaPedidos /></RutaProtegida>} />
      <Route path="/pedidos/:id" element={<RutaProtegida><DetallePedido /></RutaProtegida>} />
    </Routes>
  )
}