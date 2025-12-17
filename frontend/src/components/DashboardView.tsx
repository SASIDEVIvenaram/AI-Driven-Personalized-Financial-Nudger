import type { User } from '../types'
import { Bar, BarChart, CartesianGrid, Cell, Legend, Line, LineChart, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'

const COLORS = [
  '#6b8bff',
  '#9a6bff',
  '#f77f94',
  '#f7c46c',
  '#49c6af',
  '#3d5a80',
  '#2e86ab',
  '#ff9f1c',
  '#e76f51',
  '#8338ec',
]

export function DashboardView({
  monthlySpend,
  budget,
  setBudget,
  remainingBudget,
  spendingByCategoryData,
  timelineData,
  budgetValue,
  currentUser,
}: {
  monthlySpend: number
  budget: string
  setBudget: (v: string) => void
  remainingBudget: number
  spendingByCategoryData: { name: string; value: number }[]
  timelineData: { date: string; amount: number; label: string }[]
  budgetValue: number
  currentUser: User | null
}) {
  return (
    <section className="card wide">
      <div className="card-header">
        <h2>Dashboard</h2>
        <p>Spending pulse and budget status</p>
      </div>
      <div className="dashboard">
        <div className="metric">
          <span className="label">Current month spend</span>
          <strong>{formatCurrency(monthlySpend, currentUser?.currencyPreference ?? 'INR')}</strong>
        </div>
        <div className="metric">
          <span className="label">Monthly budget</span>
          <input
            type="number"
            value={budget}
            onChange={(e) => setBudget(e.target.value)}
            className="inline-input"
            min={0}
          />
        </div>
        <div className={`metric ${remainingBudget >= 0 ? 'positive' : 'negative'}`}>
          <span className="label">Remaining budget</span>
          <strong>{formatCurrency(Math.abs(remainingBudget), currentUser?.currencyPreference ?? 'INR')}</strong>
          <small>{remainingBudget >= 0 ? 'Under budget' : 'Over budget'}</small>
        </div>
      </div>

      <div className="grid three charts">
        <div className="chart">
          <h3>Spending by category</h3>
          <ResponsiveContainer width="100%" height={240}>
            <PieChart>
              <Pie data={spendingByCategoryData} dataKey="value" nameKey="name" outerRadius={90}>
                {spendingByCategoryData.map((entry, index) => (
                  <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip formatter={(value: number) => formatCurrency(value, currentUser?.currencyPreference)} />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="chart">
          <h3>Transaction timeline</h3>
          <ResponsiveContainer width="100%" height={240}>
            <LineChart data={timelineData} margin={{ left: 12, right: 12 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#2f3955" />
              <XAxis dataKey="date" tick={{ fill: '#c8d3f5', fontSize: 11 }} />
              <YAxis tick={{ fill: '#c8d3f5', fontSize: 11 }} />
              <Tooltip formatter={(value: number) => formatCurrency(value, currentUser?.currencyPreference)} />
              <Legend />
              <Line type="monotone" dataKey="amount" stroke="#6b8bff" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="chart">
          <h3>Budget tracking</h3>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={[{ name: 'This month', spend: monthlySpend, budget: budgetValue }]}> 
              <CartesianGrid strokeDasharray="3 3" stroke="#2f3955" />
              <XAxis dataKey="name" tick={{ fill: '#c8d3f5', fontSize: 11 }} />
              <YAxis tick={{ fill: '#c8d3f5', fontSize: 11 }} />
              <Tooltip formatter={(value: number) => formatCurrency(value, currentUser?.currencyPreference)} />
              <Legend />
              <Bar dataKey="budget" fill="#1fbba6" radius={[6, 6, 0, 0]} />
              <Bar dataKey="spend" fill="#f77f94" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </section>
  )
}

function formatCurrency(value: number, currency = 'INR') {
  try {
    return value.toLocaleString('en-IN', { style: 'currency', currency })
  } catch (error) {
    return `${currency} ${value.toFixed(2)}`
  }
}
