import React, { useEffect, useState } from 'react'
import { getTransactions, addTransaction, deleteTransaction, getCategories, exportExcel } from '../services/api'

export default function Transactions() {
  const [transactions, setTransactions] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(false)

  // form state
  const [amount, setAmount] = useState('')
  const [type, setType] = useState('EXPENSE')
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [note, setNote] = useState('')
  const [categoryId, setCategoryId] = useState('')

  useEffect(() => { void load() }, [])

  async function load() {
    setLoading(true)
    try {
      const [tx, cats] = await Promise.all([getTransactions(), getCategories()])
      setTransactions(Array.isArray(tx) ? tx : [])
      setCategories(Array.isArray(cats) ? cats : [])
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  async function handleAdd(e) {
    e.preventDefault()
    if (!amount) return
    try {
      const payload = {
        amount: Number(amount),
        type,
        date,
        note,
      }
      if (categoryId) payload.category = { id: categoryId }
      await addTransaction(payload)
      setAmount('')
      setNote('')
      setCategoryId('')
      setType('EXPENSE')
      setDate(new Date().toISOString().slice(0, 10))
      await load()
    } catch (err) {
      console.error(err)
    }
  }

  async function handleDelete(id) {
    try {
      await deleteTransaction(id)
      await load()
    } catch (err) { console.error(err) }
  }

  async function handleExport() {
    try {
      await exportExcel()
    } catch (err) {
      console.error(err)
      alert('Failed to export transactions.')
    }
  }

  if (loading) return <p>Loading...</p>
  return (
    <section>
      <h2>Your Transactions</h2>

      <form onSubmit={handleAdd} className="inline-form transaction-form">
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="EXPENSE">Expense</option>
          <option value="INCOME">Income</option>
        </select>
        <input
          type="number"
          step="0.01"
          placeholder="Amount"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          required
        />
        <input
          type="date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
          required
        />
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
          <option value="">No category</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        <input
          type="text"
          placeholder="Note (optional)"
          value={note}
          onChange={(e) => setNote(e.target.value)}
        />
        <button type="submit" className="primary-btn">Add</button>
        <button type="button" className="secondary-btn" onClick={handleExport}>Export Excel</button>
      </form>

      {transactions.length === 0 && <p>No transactions yet.</p>}
      {transactions.length > 0 && (
        <ul className="transaction-list">
          {transactions.map((tx) => (
            <li key={tx.id} className="transaction-item">
              <div className="transaction-main">
                <span className="transaction-category">{tx.category?.name || tx.category || 'Uncategorized'}</span>
                <span className="transaction-amount">{tx.amount}</span>
              </div>
              <div className="transaction-meta">
                <span>{tx.date}</span>
                <span>{tx.type}</span>
              </div>
              {tx.note && (
                <div className="transaction-note">
                  <span>{tx.note}</span>
                </div>
              )}
              <button className="secondary-btn" onClick={() => handleDelete(tx.id)}>Delete</button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
