import type { View } from '../types'

export function NavBar({
  items,
  view,
  setView,
  role,
  onToggleRole,
}: {
  items: { key: View; label: string }[]
  view: View
  setView: (v: View) => void
  role: 'user' | 'admin'
  onToggleRole: (next: 'user' | 'admin') => void
}) {
  return (
    <nav className="top-nav">
      {items.map((item) => (
        <button
          key={item.key}
          className={`nav-btn ${view === item.key ? 'active' : ''}`}
          onClick={() => setView(item.key)}
        >
          {item.label}
        </button>
      ))}
      <div className="nav-spacer" />
      <label className="pill toggle">
        <input
          type="checkbox"
          checked={role === 'admin'}
          onChange={(e) => onToggleRole(e.target.checked ? 'admin' : 'user')}
        />
        Admin mode
      </label>
    </nav>
  )
}
