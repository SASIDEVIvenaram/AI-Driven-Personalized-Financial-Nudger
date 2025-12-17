import type { User } from '../types'

export function AdminView({ users, onFetch }: { users: User[]; onFetch: () => void }) {
  return (
    <section className="card wide">
      <div className="card-header">
        <div>
          <h2>Admin · Users</h2>
          <p>GET /api/users</p>
        </div>
        <button className="secondary" onClick={onFetch}>Fetch users</button>
      </div>
      {users.length === 0 && <p className="muted">No users loaded yet.</p>}
      {users.length > 0 && (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Currency</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.userId}>
                  <td>{u.userId}</td>
                  <td>{u.firstName} {u.lastName}</td>
                  <td>{u.email}</td>
                  <td>{u.currencyPreference ?? 'INR'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
