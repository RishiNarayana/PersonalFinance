import React, { useEffect, useState } from 'react'
import { getCategories, createCategory } from '../services/api'

export default function Categories() {
  const [categories, setCategories] = useState([])
  const [newName, setNewName] = useState('')

  useEffect(() => { void load() }, [])

  async function load() {
    try {
      const data = await getCategories()
      setCategories(Array.isArray(data) ? data : [])
    } catch (err) { console.error(err) }
  }

  async function handleCreate(e) {
    e.preventDefault()
    if (!newName) return
    try {
      await createCategory({ name: newName })
      setNewName('')
      await load()
    } catch (err) { console.error(err) }
  }

  return (
    <section>
      <h2>Categories</h2>
      <form onSubmit={handleCreate} className="inline-form">
        <input type="text" placeholder="New category" value={newName} onChange={(e) => setNewName(e.target.value)} />
        <button type="submit" className="primary-btn">Add Category</button>
      </form>

      {categories.length === 0 && <p>No categories yet.</p>}
      {categories.length > 0 && (
        <ul className="pill-list">
          {categories.map((c) => <li key={c.id} className="pill-item">{c.name}</li>)}
        </ul>
      )}
    </section>
  )
}
