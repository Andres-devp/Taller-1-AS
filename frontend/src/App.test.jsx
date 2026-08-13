import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, expect, test, vi } from 'vitest'
import App from './App.jsx'

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

test('shows success only when the API accepts the credentials', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ authenticated: true }), { status: 200 })))
  render(<App />)

  fireEvent.change(screen.getByLabelText('Usuario'), { target: { value: 'student' } })
  fireEvent.change(screen.getByLabelText('Contraseña'), { target: { value: 'laboratory-password' } })
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }))

  expect(await screen.findByText('Credenciales válidas.')).toBeInTheDocument()
})

test('uses the same invalid message for a 401 response', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ authenticated: false }), { status: 401 })))
  render(<App />)

  fireEvent.change(screen.getByLabelText('Usuario'), { target: { value: 'unknown' } })
  fireEvent.change(screen.getByLabelText('Contraseña'), { target: { value: 'anything' } })
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }))

  expect(await screen.findByText('Usuario o contraseña inválidos.')).toBeInTheDocument()
})
