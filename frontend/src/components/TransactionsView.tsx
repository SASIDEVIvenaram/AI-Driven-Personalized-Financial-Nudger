import type { Category, Transaction, User } from '../types'

function formatCurrency(value: number, currency = 'INR') {
  try {
    return value.toLocaleString('en-IN', { style: 'currency', currency })
  } catch (error) {
    return `${currency} ${value.toFixed(2)}`
  }
}

export function TransactionsView({
  loading,
  transactions,
  categoryLookup,
  feedbackDrafts,
  setFeedbackDrafts,
  onFeedback,
  currentUser,
  onRefresh,
  availableCategories,
}: {
  loading: boolean
  transactions: Transaction[]
  categoryLookup: Map<number, Category>
  feedbackDrafts: Record<number, string>
  setFeedbackDrafts: (v: Record<number, string>) => void
  onFeedback: (id: number) => void
  currentUser: User | null
  onRefresh: () => void
  availableCategories: Category[]
}) {
  return (
    <section className="card wide">
      <div className="card-header">
        <div>
          <h2>Transactions</h2>
          <p>GET /api/transactions/user/:userId & feedback</p>
        </div>
        <button className="ghost" onClick={onRefresh}>
          Refresh
        </button>
      </div>
      {loading && <p className="muted">Loading transactions…</p>}
      {!loading && transactions.length === 0 && (
        <p className="muted">No transactions yet. Upload a file or add one manually.</p>
      )}
      {!loading && transactions.length > 0 && (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Description</th>
                <th>Amount</th>
                <th>Category</th>
                <th>Type</th>
                <th>Confidence</th>
                <th>Feedback</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((tx) => {
                const categoryName = categoryLookup.get(tx.categoryId ?? -1)?.categoryName ?? 'Uncategorized'
                const amount = typeof tx.amount === 'number' ? tx.amount : Number(tx.amount) || 0
                const amountValue = Number.isFinite(amount) ? amount : 0
                return (
                  <tr key={tx.transactionId}>
                    <td>{tx.date}</td>
                    <td>{tx.merchantName || tx.description || '—'}</td>
                    <td className={tx.type === 'DEBIT' ? 'debit' : 'credit'}>
                      {formatCurrency(amountValue, currentUser?.currencyPreference ?? 'INR')}
                    </td>
                    <td>{categoryName}</td>
                    <td>{tx.type ?? '—'}</td>
                    <td>{tx.categoryConfidence ? `${(tx.categoryConfidence * 100).toFixed(0)}%` : '—'}</td>
                    <td>
                      <div className="feedback-row">
                        <select
                          value={feedbackDrafts[tx.transactionId] ?? ''}
                          onChange={(e) =>
                            setFeedbackDrafts({ ...feedbackDrafts, [tx.transactionId]: e.target.value })
                          }
                        >
                          <option value="">Select category...</option>
                          {availableCategories.map((cat) => (
                            <option key={cat.categoryId} value={cat.categoryName}>
                              {cat.categoryName}
                            </option>
                          ))}
                        </select>
                        <button className="secondary" onClick={() => onFeedback(tx.transactionId)}>
                          Send
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
