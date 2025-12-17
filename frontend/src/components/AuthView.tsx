import type { FormEvent } from 'react'
import type { User } from '../types'

export function AuthView({
  registerForm,
  setRegisterForm,
  loginForm,
  setLoginForm,
  onRegister,
  onLogin,
  currentUser,
}: {
  registerForm: { firstName: string; lastName: string; email: string; password: string }
  setRegisterForm: (v: { firstName: string; lastName: string; email: string; password: string }) => void
  loginForm: { email: string; password: string; userIdFallback: string }
  setLoginForm: (v: { email: string; password: string; userIdFallback: string }) => void
  onRegister: (e: FormEvent) => void
  onLogin: (e: FormEvent) => void
  currentUser: User | null
}) {
  return (
    <div className="grid two">
      <section className="card">
        <div className="card-header">
          <h2>Create Account</h2>
          <p>POST /api/users</p>
        </div>
        <form className="form" onSubmit={onRegister}>
          <div className="field-row">
            <label>First name</label>
            <input
              value={registerForm.firstName}
              onChange={(e) => setRegisterForm({ ...registerForm, firstName: e.target.value })}
              placeholder="Anika"
              required
            />
          </div>
          <div className="field-row">
            <label>Last name</label>
            <input
              value={registerForm.lastName}
              onChange={(e) => setRegisterForm({ ...registerForm, lastName: e.target.value })}
              placeholder="Rao"
              required
            />
          </div>
          <div className="field-row">
            <label>Email</label>
            <input
              type="email"
              value={registerForm.email}
              onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
              placeholder="you@example.com"
              required
            />
          </div>
          <div className="field-row">
            <label>Password</label>
            <input
              type="password"
              value={registerForm.password}
              onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
              placeholder="Min 8 chars"
              required
            />
          </div>
          <button type="submit" className="primary">Sign up</button>
        </form>
      </section>

      <section className="card">
        <div className="card-header">
          <h2>Login</h2>
          <p>Fetch user by email</p>
        </div>
        <form className="form" onSubmit={onLogin}>
          <div className="field-row">
            <label>Email</label>
            <input
              type="email"
              value={loginForm.email}
              onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
              placeholder="you@example.com"
              required
            />
          </div>
          <div className="field-row">
            <label>Password</label>
            <input
              type="password"
              value={loginForm.password}
              onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
              placeholder="Used only client-side"
              required
            />
          </div>
          <details className="muted" style={{ marginTop: '4px' }}>
            <summary>Developer fallback: login by user ID</summary>
            <input
              value={loginForm.userIdFallback}
              onChange={(e) => setLoginForm({ ...loginForm, userIdFallback: e.target.value })}
              placeholder="123"
            />
            <p className="muted">Uses GET /api/users/:id</p>
          </details>
          <button type="submit" className="secondary">Login</button>
        </form>
        {currentUser && (
          <div className="summary">
            <p className="eyebrow">Current user</p>
            <strong>{`${currentUser.firstName} ${currentUser.lastName}`}</strong>
            <p>{currentUser.email}</p>
          </div>
        )}
      </section>
    </div>
  )
}
