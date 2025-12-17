import React, { useEffect, useState } from 'react'
import { getMonthlySummary, getCategoryBreakdown, getBudgets, exportExcel } from '../services/api'

export default function Analytics() {
  const [year, setYear] = useState(new Date().getFullYear())
  const [month, setMonth] = useState(new Date().getMonth() + 1)
  const [summary, setSummary] = useState(null)
  const [breakdown, setBreakdown] = useState({})
  const [alerts, setAlerts] = useState([])

  useEffect(() => { void load() }, [])

  async function handleExport() {
    try {
      await exportExcel()
    } catch (err) {
      console.error(err)
      window.alert('Failed to export transactions.')
    }
  }

  async function load() {
    try {
      const [s, b, budgets] = await Promise.all([
        getMonthlySummary(year, month),
        getCategoryBreakdown(year, month),
        getBudgets(),
      ])
      const bd = b || {}
      setSummary(s)
      setBreakdown(bd)

      // Build alerts when spending exceeds budget per category
      const newAlerts = []
      if (Array.isArray(budgets)) {
        for (const budget of budgets) {
          const catName = budget.category?.name
          if (!catName) continue // only category-specific budgets
          const spent = bd[catName] || 0
          if (spent > (budget.monthlyLimit || 0)) {
            newAlerts.push({
              category: catName,
              spent,
              limit: budget.monthlyLimit || 0,
            })
          }
        }
      }
      setAlerts(newAlerts)

      if (newAlerts.length > 0) {
        const msgLines = newAlerts.map(a => `${a.category}: spent ${a.spent} / limit ${a.limit}`)
        window.alert(`Budget exceeded in:\n\n${msgLines.join('\n')}`)
      }
    } catch (err) { console.error(err) }
  }

  return (
    <section>
      <h2>Analytics</h2>
      <form onSubmit={(e) => { e.preventDefault(); load(); }} className="inline-form">
        <input type="number" value={year} onChange={(e) => setYear(Number(e.target.value))} min="2000" max="2100" />
        <input type="number" value={month} onChange={(e) => setMonth(Number(e.target.value))} min="1" max="12" />
        <button className="primary-btn" type="submit">Refresh</button>
        <button className="secondary-btn" type="button" onClick={handleExport}>Export Excel</button>
      </form>

      {alerts.length > 0 && (
        <div className="alert-panel">
          <h3>Budget Alerts</h3>
          <ul>
            {alerts.map((a) => (
              <li key={a.category} className="alert-item">
                <strong>{a.category}</strong>: spent {a.spent} / limit {a.limit} (over budget)
              </li>
            ))}
          </ul>
        </div>
      )}

      {summary && (
        <div className="summary-grid">
          <div className="summary-card"><span>Income</span><strong>{summary.income}</strong></div>
          <div className="summary-card"><span>Expense</span><strong>{summary.expense}</strong></div>
          <div className="summary-card"><span>Net</span><strong>{summary.net}</strong></div>
        </div>
      )}

      {breakdown && Object.keys(breakdown).length > 0 && (
        <div className="category-breakdown">
          <h3>By Category</h3>
          <ul>
            {Object.entries(breakdown).map(([k, v]) => (
              <li key={k}>
                <span>{k}</span>
                <span>{v}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  )
}
