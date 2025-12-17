import React, { useEffect, useState, useRef } from 'react'
import './App.css'
import Auth from './pages/Auth'
import TopBar from './components/TopBar'
import Transactions from './pages/Transactions'
import Categories from './pages/Categories'
import Budgets from './pages/Budgets'
import Analytics from './pages/Analytics'
import { logout as apiLogout } from './services/api'


export default function App() {
  const [userToken, setUserToken] = useState(() => localStorage.getItem('jwt'))
  const [view, setView] = useState(() => (localStorage.getItem('jwt') ? 'dashboard' : 'home'))
  const [authMode, setAuthMode] = useState('login')
  const [activeTab, setActiveTab] = useState('transactions')
  const logoutTimer = useRef(null)

  // Auto-logout on app start
  useEffect(() => {
    apiLogout()
    setUserToken(null)
    setView('home')
  }, [])

  // Auto-logout after inactivity (e.g., 30 min)
  useEffect(() => {
    if (!userToken) return
    // 30 minutes in ms
    const timeout = 30 * 60 * 1000
    const resetTimer = () => {
      if (logoutTimer.current) clearTimeout(logoutTimer.current)
      logoutTimer.current = setTimeout(() => {
        handleLogout()
        alert('Session expired. You have been logged out.')
      }, timeout)
    }
    // Listen for user activity
    window.addEventListener('mousemove', resetTimer)
    window.addEventListener('keydown', resetTimer)
    resetTimer()
    return () => {
      window.removeEventListener('mousemove', resetTimer)
      window.removeEventListener('keydown', resetTimer)
      if (logoutTimer.current) clearTimeout(logoutTimer.current)
    }
  }, [userToken])

  function handleLogout() {
    apiLogout()
    setUserToken(null)
    setView('home')
  }

  function handleAuthSuccess() {
    setUserToken(localStorage.getItem('jwt'))
    setView('dashboard')
  }

  if (!userToken && view === 'home') {
    return (
      <div className="landing-shell">
        <header className="landing-nav">
          <div className="brand">MONETA</div>
          <div className="nav-actions">
            <button type="button" className="ghost-btn" onClick={() => { setAuthMode('login'); setView('auth') }}>Login</button>
            <button type="button" className="solid-btn" onClick={() => { setAuthMode('register'); setView('auth') }}>Register</button>
          </div>
        </header>

        <main className="landing-hero">
          <div className="hero-text">
            <h1>
              Track your <br />
              Expenses here <br />
              with our <span className='des-brand'>MONETA</span>
            </h1>
            <p className="lede">
              Go ahead and manage your expenses dynamically
            </p>
          </div>
          <div className="hero-visual">
            <img src="/landing-illustration.png" alt="Calculator with coins" />
          </div>
        </main>
      </div>
    )
  }

  if (!userToken && view === 'auth') {
    return (
      <div className="app-container auth-page">
        <header className="landing-nav compact">
          <div className="brand"><p>MONETA</p></div>
          <div className="nav-actions">
            <button type="button" className="ghost-btn" onClick={() => setView('home')}>Go back</button>
          </div>
        </header>

        <h1 className="app-title">{authMode === 'login' ? 'Welcome back' : 'Create your account'}</h1>
        <Auth initialMode={authMode} onModeChange={setAuthMode} onLogin={handleAuthSuccess} />
        <div className="auth-helper">
          {authMode === 'login' ? (
            <p>
              Don&apos;t have an account?{' '}
              <button type="button" className="link-btn" onClick={() => setAuthMode('register')}>
                Register
              </button>
            </p>
          ) : (
            <p>
              Already have an account?{' '}
              <button type="button" className="link-btn" onClick={() => setAuthMode('login')}>
                Login
              </button>
            </p>
          )}
        </div>
      </div>
    )
  }

  return (
    <div className="app-container">
      <h1 className="app-title">MONETA</h1>
      <TopBar onLogout={handleLogout} />

      <div className="section-tabs">
        <button type="button" className={activeTab === 'transactions' ? 'active' : ''} onClick={() => setActiveTab('transactions')}>Transactions</button>
        <button type="button" className={activeTab === 'categories' ? 'active' : ''} onClick={() => setActiveTab('categories')}>Categories</button>
        <button type="button" className={activeTab === 'budgets' ? 'active' : ''} onClick={() => setActiveTab('budgets')}>Budgets</button>
        <button type="button" className={activeTab === 'analytics' ? 'active' : ''} onClick={() => setActiveTab('analytics')}>Analytics</button>
      </div>

      <main className="dashboard">
        {activeTab === 'transactions' && <Transactions />}
        {activeTab === 'categories' && <Categories />}
        {activeTab === 'budgets' && <Budgets />}
        {activeTab === 'analytics' && <Analytics />}
      </main>
    </div>
  )
}
