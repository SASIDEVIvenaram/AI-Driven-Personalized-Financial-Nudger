import type { FormEvent } from 'react'

export function AccountView({
  updateForm,
  setUpdateForm,
  onUpdate,
  onDelete,
}: {
  updateForm: { firstName: string; lastName: string; email: string; password: string; currencyPreference: string }
  setUpdateForm: (v: { firstName: string; lastName: string; email: string; password: string; currencyPreference: string }) => void
  onUpdate: (e: FormEvent) => void
  onDelete: () => void
}) {
  return (
    <section className="card">
      <div className="card-header">
        <div>
          <h2>My Account</h2>
          <p>Update or delete</p>
        </div>
        <button className="secondary" onClick={onDelete}>Delete account</button>
      </div>
      <form className="form" onSubmit={onUpdate}>
        <div className="field-row">
          <label>First name</label>
          <input
            value={updateForm.firstName}
            onChange={(e) => setUpdateForm({ ...updateForm, firstName: e.target.value })}
            required
          />
        </div>
        <div className="field-row">
          <label>Last name</label>
          <input
            value={updateForm.lastName}
            onChange={(e) => setUpdateForm({ ...updateForm, lastName: e.target.value })}
            required
          />
        </div>
        <div className="field-row">
          <label>Email</label>
          <input
            type="email"
            value={updateForm.email}
            onChange={(e) => setUpdateForm({ ...updateForm, email: e.target.value })}
            required
          />
        </div>
        <div className="field-row">
          <label>Password (leave blank to keep)</label>
          <input
            type="password"
            value={updateForm.password}
            onChange={(e) => setUpdateForm({ ...updateForm, password: e.target.value })}
          />
        </div>
        <div className="field-row">
          <label>Currency</label>
          <select
            value={updateForm.currencyPreference}
            onChange={(e) => setUpdateForm({ ...updateForm, currencyPreference: e.target.value })}
          >
            <option value="INR">INR</option>
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
          </select>
        </div>
        <button type="submit" className="primary">Update account</button>
      </form>
    </section>
  )
}
