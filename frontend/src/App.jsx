import { useState } from 'react'

export default function App() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setLoading(true)
    setResult(null)

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      })

      if (response.status === 200) {
        setResult({ type: 'success', message: 'Credenciales válidas.' })
      } else if (response.status === 401) {
        setResult({ type: 'error', message: 'Usuario o contraseña inválidos.' })
      } else {
        setResult({ type: 'error', message: 'El servicio no está disponible. Inténtalo más tarde.' })
      }
    } catch {
      setResult({ type: 'error', message: 'El servicio no está disponible. Inténtalo más tarde.' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="page">
      <form className="login-card" onSubmit={submit}>
        <h1>Autenticación</h1>
        <label htmlFor="username">Usuario</label>
        <input
          id="username"
          name="username"
          autoComplete="username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          required
        />

        <label htmlFor="password">Contraseña</label>
        <input
          id="password"
          name="password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Verificando…' : 'Ingresar'}
        </button>

        <section className="result" aria-live="polite" aria-label="Resultado">
          {result && <p className={result.type}>{result.message}</p>}
        </section>
      </form>
    </main>
  )
}
