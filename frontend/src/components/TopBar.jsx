import React from 'react'

export default function TopBar({ onLogout }) {
  return (
    <div className="top-bar">
      <span>Logged in</span>
      <button type="button" onClick={onLogout}>
        Logout
      </button>
    </div>
  )
}
