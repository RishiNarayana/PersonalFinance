import React, { useEffect, useState } from 'react'
import { login as apiLogin, register as apiRegister } from '../services/api'


export default function Auth({ onLogin, initialMode = 'login', onModeChange = () => {} }) {
  const [mode, setMode] = useState(initialMode)
  // Separate state for login and register fields
  const [loginEmail, setLoginEmail] = useState('')
  const [loginPassword, setLoginPassword] = useState('')
  const [registerName, setRegisterName] = useState('')
  const [registerEmail, setRegisterEmail] = useState('')
  const [registerPassword, setRegisterPassword] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  // Keep internal mode aligned with parent intention (e.g., Login/Register buttons)
  useEffect(() => {
    setMode(initialMode)
    onModeChange(initialMode)
  }, [initialMode, onModeChange])

  async function handleLogin(e) {
    e.preventDefault()
    setError('')
    setMessage('')
    try {
      const data = await apiLogin({ email: loginEmail, password: loginPassword })
      if (data && data.token) {
        onLogin()
      } else {
        setError('Login failed. Check your email/password or register first.')
      }
    } catch (err) {
      console.error(err)
      setError(err?.message || 'Login failed. Check your email/password or register first.')
    }
  }

  async function handleRegister(e) {
    e.preventDefault()
    setError('')
    setMessage('')

    try {
      const response = await apiRegister({
        name: registerName,
        email: registerEmail,
        password: registerPassword,
      })

      // If API call succeeds, assume registration success
      const successMessage = response?.message || 'Registered successfully. Please log in to continue.'
      setMessage(successMessage)

      // Optional: switch to login tab automatically
      setMode('login')
      onModeChange('login')

      // Clear register fields
      setRegisterName('')
      setRegisterEmail('')
      setRegisterPassword('')
    } catch (err) {
      console.error(err)
      setError(err?.message || 'Registration failed. Please try again.')
    }
  }

  return (
    <section className="auth-card">
      <div className="tabs">
        <button
          type="button"
          className={mode === 'login' ? 'active' : ''}
          onClick={() => {
            setMode('login')
            onModeChange('login')
            setError('')
            setMessage('')
            setLoginEmail('')
            setLoginPassword('')
          }}
        >
          Login
        </button>
        <button
          type="button"
          className={mode === 'register' ? 'active' : ''}
          onClick={() => {
            setMode('register')
            onModeChange('register')
            setError('')
            setMessage('')
            setRegisterName('')
            setRegisterEmail('')
            setRegisterPassword('')
          }}
        >
          Register
        </button>
      </div>

      {mode === 'login' ? (
        <form onSubmit={handleLogin} className="form" autoComplete="off">
          <label className="field">
            <span>Email</span>
            <input
              type="email"
              value={loginEmail}
              onChange={(e) => setLoginEmail(e.target.value)}
              autoComplete="email"
              required
            />
          </label>
          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={loginPassword}
              onChange={(e) => setLoginPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
          </label>
          <button type="submit" className="primary-btn">Login</button>
        </form>
      ) : (
        <form onSubmit={handleRegister} className="form" autoComplete="off">
          <label className="field">
            <span>Name</span>
            <input
              type="text"
              value={registerName}
              onChange={(e) => setRegisterName(e.target.value)}
              autoComplete="name"
              required
            />
          </label>
          <label className="field">
            <span>Email</span>
            <input
              type="email"
              value={registerEmail}
              onChange={(e) => setRegisterEmail(e.target.value)}
              autoComplete="email"
              required
            />
          </label>
          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={registerPassword}
              onChange={(e) => setRegisterPassword(e.target.value)}
              autoComplete="new-password"
              required
            />
          </label>
          <button type="submit" className="primary-btn">Register</button>
        </form>
      )}

      {message && <p className="message">{message}</p>}
      {error && <p className="error">{error}</p>}
    </section>
  )
}
