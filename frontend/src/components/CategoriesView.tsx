import type { FormEvent } from 'react'
import type { Category } from '../types'

export function CategoriesView({
  loadingCategories,
  categories,
  categoryForm,
  setCategoryForm,
  customCategoryName,
  setCustomCategoryName,
  library,
  onSubmit,
}: {
  loadingCategories: boolean
  categories: Category[]
  categoryForm: { categoryName: string; categoryType: Category['categoryType'] }
  setCategoryForm: (v: { categoryName: string; categoryType: Category['categoryType'] }) => void
  customCategoryName: string
  setCustomCategoryName: (v: string) => void
  library: string[]
  onSubmit: (e: FormEvent) => void
}) {
  return (
    <section className="card">
      <div className="card-header">
        <h2>Categories</h2>
        <p>GET /api/categories/user/:userId & POST /api/categories</p>
      </div>
      <form className="form inline" onSubmit={onSubmit}>
        <select
          value={categoryForm.categoryName}
          onChange={(e) => setCategoryForm({ ...categoryForm, categoryName: e.target.value })}
        >
          {library.map((name) => (
            <option key={name} value={name}>{name}</option>
          ))}
        </select>
        {categoryForm.categoryName === 'Custom' && (
          <input
            placeholder="Custom name"
            value={customCategoryName}
            onChange={(e) => setCustomCategoryName(e.target.value)}
            required
          />
        )}
        <select
          value={categoryForm.categoryType}
          onChange={(e) =>
            setCategoryForm({ ...categoryForm, categoryType: e.target.value as Category['categoryType'] })
          }
        >
          <option value="EXPENSE">EXPENSE</option>
          <option value="INCOME">INCOME</option>
          <option value="TRANSFER">TRANSFER</option>
        </select>
        <button type="submit" className="secondary">Create</button>
      </form>
      <div className="pill-row">
        {loadingCategories && <span className="pill muted">Loading categories…</span>}
        {!loadingCategories && categories.length === 0 && <span className="pill muted">No categories yet</span>}
        {categories.map((cat) => (
          <span key={cat.categoryId} className="pill">
            {cat.categoryName} · {cat.categoryType}
          </span>
        ))}
      </div>
    </section>
  )
}
