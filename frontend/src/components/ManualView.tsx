import type { FormEvent } from 'react'

export function ManualView({
  manualForm,
  setManualForm,
  onSubmit,
  result,
}: {
  manualForm: { amount: string; note: string; date: string }
  setManualForm: (v: { amount: string; note: string; date: string }) => void
  onSubmit: (e: FormEvent) => void
  result: string
}) {
  return (
    <section className="card">
      <div className="card-header">
        <h2>Manual Transaction</h2>
        <p>POST /api/transactions/manual</p>
      </div>
      <form className="form" onSubmit={onSubmit}>
        <div className="field-row">
          <label>Amount</label>
          <input
            type="number"
            min="0"
            step="0.01"
            value={manualForm.amount}
            onChange={(e) => setManualForm({ ...manualForm, amount: e.target.value })}
            placeholder="1200"
            required
          />
        </div>
        <div className="field-row">
          <label>Description</label>
          <input
            value={manualForm.note}
            onChange={(e) => setManualForm({ ...manualForm, note: e.target.value })}
            placeholder="Team lunch"
            required
          />
        </div>
        <div className="field-row">
          <label>Date</label>
          <input
            type="date"
            value={manualForm.date}
            onChange={(e) => setManualForm({ ...manualForm, date: e.target.value })}
            required
          />
        </div>
        <button type="submit" className="primary">Save transaction</button>
        {result && <p className="muted">{result}</p>}
      </form>
    </section>
  )
}
