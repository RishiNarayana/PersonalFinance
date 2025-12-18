import React, { useEffect, useState } from 'react'
import { getBudgets, createBudget, deleteBudget, getCategories } from '../services/api'

export default function Budgets() {
  const [budgets, setBudgets] = useState([])
  const [categories, setCategories] = useState([])
  const [catId, setCatId] = useState('')
  const [limit, setLimit] = useState('')

  useEffect(() => { void load() }, [])

  async function load() {
    try {
      const [b, c] = await Promise.all([getBudgets(), getCategories()])
      setBudgets(Array.isArray(b) ? b : [])
      setCategories(Array.isArray(c) ? c : [])
    } catch (err) { console.error(err) }
  }

  async function handleCreate(e) {
    e.preventDefault()
    if (!limit) return
    try {
      const payload = { monthlyLimit: Number(limit) }
      if (catId) payload.categoryId = catId
      await createBudget(payload)
      setLimit('')
      setCatId('')
      await load()
    } catch (err) { console.error(err) }
  }

  async function handleDelete(id) {
    if (!window.confirm('Are you sure you want to delete this budget?')) return
    try {
      console.log('Deleting budget with id:', id)
      await deleteBudget(id)
      console.log('Budget deleted successfully')
      await load()
    } catch (err) {
      console.error('Delete error:', err)
      alert(`Failed to delete budget: ${err.message || 'Unknown error'}`)
    }
  }

  return (
    <section>
      <h2>Budgets</h2>
      <form onSubmit={handleCreate} className="inline-form">
        <select value={catId} onChange={(e) => setCatId(e.target.value)}>
          <option value="">General</option>
          {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <input type="number" step="0.01" placeholder="Monthly limit" value={limit} onChange={(e) => setLimit(e.target.value)} required />
        <button type="submit" className="primary-btn">Create Budget</button>
      </form>

      {budgets.length === 0 && <p>No budgets yet.</p>}
      {budgets.length > 0 && (
        <ul className="budget-list">
          {budgets.map((b) => (
            <li key={b.id} className="budget-item">
              <div className="budget-main">
                <span>{b.category?.name || 'General'}</span>
                <div className="budget-actions">
                  <span className="budget-amount">{b.monthlyLimit}</span>
                  <button
                    type="button"
                    className="delete-btn"
                    onClick={(e) => {
                      e.preventDefault()
                      e.stopPropagation()
                      handleDelete(b.id)
                    }}
                    title="Delete budget"
                  >
                    ×
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
