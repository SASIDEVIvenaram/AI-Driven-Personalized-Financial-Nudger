import axios from 'axios'
import type { FormEvent } from 'react'
import { useEffect, useMemo, useState } from 'react'
import './App.css'
import { NavBar } from './components/NavBar'
import { Banner as BannerView } from './components/Banner'
import { AuthView } from './components/AuthView'
import { DashboardView } from './components/DashboardView'
import { CategoriesView } from './components/CategoriesView'
import { UploadView } from './components/UploadView'
import { ManualView } from './components/ManualView'
import { TransactionsView } from './components/TransactionsView'
import { AdminView } from './components/AdminView'
import { AccountView } from './components/AccountView'
import type { Banner, Category, FileUploadResponse, Transaction, User, View } from './types'

// Use relative URLs for API calls (Vite proxy in dev, direct in prod)
const API_BASE = ''

// Model supports only these 5 categories
const CATEGORY_LIBRARY = [
  'Food_Dining',
  'Groceries',
  'Transfer',
  'Transport',
  'Utilities',
]

const api = axios.create({ baseURL: API_BASE })

function normalizeAmount(value: number | string | undefined) {
  if (typeof value === 'number') return value
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

function parseDate(value: string) {
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? null : parsed
}

function App() {
  const [view, setView] = useState<View>('auth')
  const [role, setRole] = useState<'user' | 'admin'>('user')

  const [registerForm, setRegisterForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  })

  const [loginForm, setLoginForm] = useState({ email: '', password: '', userIdFallback: '' })

  const [updateForm, setUpdateForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    currencyPreference: 'INR',
  })

  const [currentUser, setCurrentUser] = useState<User | null>(null)
  const [userId, setUserId] = useState<number | null>(null)
  const [usersList, setUsersList] = useState<User[]>([])

  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [manualForm, setManualForm] = useState({ amount: '', note: '', date: '' })
  const [categoryForm, setCategoryForm] = useState({
    categoryName: CATEGORY_LIBRARY[0],
    categoryType: 'EXPENSE' as Category['categoryType'],
  })
  const [customCategoryName, setCustomCategoryName] = useState('')
  const [budget, setBudget] = useState('2500')
  const [receiptFile, setReceiptFile] = useState<File | null>(null)
  const [statementFile, setStatementFile] = useState<File | null>(null)
  const [feedbackDrafts, setFeedbackDrafts] = useState<Record<number, string>>({})
  const [banner, setBanner] = useState<Banner | null>(null)
  const [loadingTransactions, setLoadingTransactions] = useState(false)
  const [loadingCategories, setLoadingCategories] = useState(false)
  const [receiptResult, setReceiptResult] = useState<string>('')
  const [statementResult, setStatementResult] = useState<string>('')
  const [manualResult, setManualResult] = useState<string>('')

  useEffect(() => {
    if (!userId) return
    fetchTransactions(userId)
    fetchCategories(userId)
  }, [userId])

  const showBanner = (type: Banner['type'], message: string) => {
    setBanner({ type, message })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const handleError = (error: unknown, fallback: string) => {
    if (axios.isAxiosError(error)) {
      const serverMessage = (error.response?.data as { message?: string })?.message
      showBanner('error', `${fallback}${serverMessage ? `: ${serverMessage}` : ''}`)
      return
    }
    showBanner('error', fallback)
  }

  const registerUser = async (event: FormEvent) => {
    event.preventDefault()
    try {
      const res = await api.post<User>('/api/users', registerForm)
      setCurrentUser(res.data)
      setUserId(res.data.userId ?? null)
      setUpdateForm({
        firstName: res.data.firstName,
        lastName: res.data.lastName,
        email: res.data.email,
        password: registerForm.password,
        currencyPreference: res.data.currencyPreference ?? 'INR',
      })
      setRole('user')
      setView('dashboard')
      showBanner('success', `User created. ID: ${res.data.userId}`)
    } catch (error) {
      handleError(error, 'Registration failed (backend must be running)')
    }
  }

  const loginUser = async (event: FormEvent) => {
    event.preventDefault()
    if (!loginForm.email && !loginForm.userIdFallback) {
      showBanner('error', 'Enter email (or fallback user ID) to login')
      return
    }
    // Preferred: fetch users and match by email
    try {
      if (loginForm.email) {
        const res = await api.get<User[]>('/api/users')
        const match = res.data?.find((u) => u.email.toLowerCase() === loginForm.email.toLowerCase())
        if (match) {
          setCurrentUser(match)
          setUserId(match.userId)
          setUpdateForm({
            firstName: match.firstName,
            lastName: match.lastName,
            email: match.email,
            password: '',
            currencyPreference: match.currencyPreference ?? 'INR',
          })
          setRole('user')
          setView('dashboard')
          showBanner('success', `Logged in as ${match.email}`)
          return
        }
        showBanner('error', 'No user found for that email')
        return
      }
    } catch (error) {
      // fall through to fallback path
      handleError(error, 'Login failed (need backend running and /api/users accessible)')
      if (!loginForm.userIdFallback) return
    }

    if (loginForm.userIdFallback) {
      const parsedId = Number(loginForm.userIdFallback)
      if (!parsedId) {
        showBanner('error', 'Fallback user ID must be numeric')
        return
      }
      try {
        const res = await api.get<User>(`/api/users/${parsedId}`)
        setCurrentUser(res.data)
        setUserId(res.data.userId ?? parsedId)
        setUpdateForm({
          firstName: res.data.firstName,
          lastName: res.data.lastName,
          email: res.data.email,
          password: '',
          currencyPreference: res.data.currencyPreference ?? 'INR',
        })
        setRole('user')
        setView('dashboard')
        showBanner('success', `Loaded ${res.data.email}`)
      } catch (errorFallback) {
        handleError(errorFallback, 'Could not load user by ID')
      }
    }
  }

  const updateUser = async (event: FormEvent) => {
    event.preventDefault()
    if (!userId) {
      showBanner('error', 'Load a user first')
      return
    }
    try {
      const payload = { ...updateForm }
      if (!payload.password) delete (payload as Record<string, unknown>).password
      const res = await api.put<User>(`/api/users/${userId}`, payload)
      setCurrentUser(res.data)
      showBanner('success', 'Account updated')
    } catch (error) {
      handleError(error, 'Update failed')
    }
  }

  const deleteUser = async () => {
    if (!userId) {
      showBanner('error', 'Load a user first')
      return
    }
    try {
      await api.delete(`/api/users/${userId}`)
      setCurrentUser(null)
      setUserId(null)
      setTransactions([])
      setCategories([])
      showBanner('success', 'Account deleted')
      setView('auth')
    } catch (error) {
      handleError(error, 'Delete failed')
    }
  }

  const fetchUsersList = async () => {
    try {
      const res = await api.get<User[]>('/api/users')
      setUsersList(res.data ?? [])
      showBanner('success', 'Fetched users (admin view)')
    } catch (error) {
      handleError(error, 'Could not fetch users (backend must allow /api/users)')
    }
  }

  const fetchTransactions = async (id: number) => {
    setLoadingTransactions(true)
    try {
      const res = await api.get<Transaction[]>(`/api/transactions/user/${id}`)
      setTransactions(res.data ?? [])
    } catch (error) {
      handleError(error, 'Could not load transactions')
    } finally {
      setLoadingTransactions(false)
    }
  }

  const fetchCategories = async (id: number) => {
    setLoadingCategories(true)
    try {
      const res = await api.get<Category[]>(`/api/categories/user/${id}`)
      setCategories(res.data ?? [])
    } catch (error) {
      handleError(error, 'Could not load categories')
    } finally {
      setLoadingCategories(false)
    }
  }

  const submitManualTransaction = async (event: FormEvent) => {
    event.preventDefault()
    if (!userId) {
      showBanner('error', 'Load or create a user first')
      return
    }

    const payload = {
      ...manualForm,
      userId,
      amount: Number(manualForm.amount),
    }

    if (!payload.amount || !payload.note || !payload.date) {
      showBanner('error', 'Fill amount, note, and date before submitting')
      return
    }

    try {
      await api.post('/api/transactions/manual', payload)
      showBanner('success', 'Manual transaction saved')
      setManualForm({ amount: '', note: '', date: '' })
      setManualResult('Saved and ready in transactions list')
      fetchTransactions(userId)
    } catch (error) {
      handleError(error, 'Manual transaction failed')
    }
  }

  const uploadFile = async (kind: 'receipt' | 'statement') => {
    if (!userId) {
      showBanner('error', 'Load or create a user first')
      return
    }
    const file = kind === 'receipt' ? receiptFile : statementFile
    if (!file) {
      showBanner('error', 'Choose a file to upload')
      return
    }

    const formData = new FormData()
    formData.append('file', file)
    formData.append('userId', String(userId))

    try {
      const res = await api.post<FileUploadResponse>(`/api/files/upload-${kind}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      const message = res.data?.message ?? `${kind} uploaded`
      showBanner(res.data?.success === false ? 'error' : 'success', message)
      if (kind === 'receipt') setReceiptResult(message)
      if (kind === 'statement') setStatementResult(message)
      fetchTransactions(userId)
    } catch (error) {
      handleError(error, 'Upload failed')
    }
  }

  const submitFeedback = async (transactionId: number) => {
    if (!userId) {
      showBanner('error', 'Load or create a user first')
      return
    }
    const correctedCategoryName = feedbackDrafts[transactionId]
    if (!correctedCategoryName) {
      showBanner('error', 'Add a corrected category before sending feedback')
      return
    }

    try {
      await api.post(`/api/transactions/${transactionId}/feedback`, {
        transactionId,
        userId,
        correctedCategoryName,
      })
      showBanner('success', 'Feedback submitted')
      setFeedbackDrafts((prev) => ({ ...prev, [transactionId]: '' }))
      fetchTransactions(userId)
    } catch (error) {
      handleError(error, 'Could not send feedback')
    }
  }

  const createCategory = async (event: FormEvent) => {
    event.preventDefault()
    if (!userId) {
      showBanner('error', 'Load or create a user first')
      return
    }

    const categoryName = categoryForm.categoryName === 'Custom' && customCategoryName
      ? customCategoryName
      : categoryForm.categoryName

    try {
      await api.post('/api/categories', {
        categoryName,
        categoryType: categoryForm.categoryType,
        userId,
      })
      showBanner('success', 'Category created')
      setCategoryForm({ categoryName: CATEGORY_LIBRARY[0], categoryType: 'EXPENSE' })
      setCustomCategoryName('')
      fetchCategories(userId)
    } catch (error) {
      handleError(error, 'Category creation failed')
    }
  }

  const categoryLookup = useMemo(() => {
    const map = new Map<number, Category>()
    categories.forEach((cat) => map.set(cat.categoryId, cat))
    return map
  }, [categories])

  const spendingByCategoryData = useMemo(() => {
    const totals = new Map<string, number>()
    transactions.forEach((tx) => {
      const amount = normalizeAmount(tx.amount)
      const isDebit = (tx.type ?? 'DEBIT') === 'DEBIT'
      if (!isDebit) return
      const categoryName = categoryLookup.get(tx.categoryId ?? -1)?.categoryName ?? 'Uncategorized'
      totals.set(categoryName, (totals.get(categoryName) ?? 0) + amount)
    })
    return Array.from(totals.entries()).map(([name, value]) => ({ name, value }))
  }, [transactions, categoryLookup])

  const timelineData = useMemo(() => {
    const sorted = [...transactions].sort((a, b) => {
      const aDate = parseDate(a.date)?.getTime() ?? 0
      const bDate = parseDate(b.date)?.getTime() ?? 0
      return aDate - bDate
    })
    return sorted.map((tx) => {
      const amount = normalizeAmount(tx.amount)
      const signed = (tx.type ?? 'DEBIT') === 'DEBIT' ? -amount : amount
      return {
        date: tx.date,
        amount: signed,
        label: tx.merchantName || tx.description || 'Transaction',
      }
    })
  }, [transactions])

  const monthlySpend = useMemo(() => {
    const now = new Date()
    const month = now.getMonth()
    const year = now.getFullYear()
    return transactions.reduce((sum, tx) => {
      const date = parseDate(tx.date)
      if (!date) return sum
      const isDebit = (tx.type ?? 'DEBIT') === 'DEBIT'
      if (!isDebit || date.getMonth() !== month || date.getFullYear() !== year) return sum
      return sum + normalizeAmount(tx.amount)
    }, 0)
  }, [transactions])

  const budgetValue = Number(budget) || 0
  const remainingBudget = budgetValue - monthlySpend
  const aiCategorized = transactions.filter((tx) => tx.isAiCategorized).length
  const userCategorized = transactions.filter((tx) => tx.isUserCategorized).length

  const showNav = role === 'admin' || !!currentUser
  const navItems: { key: View; label: string }[] = [
    { key: 'dashboard', label: 'Analytics' },
    { key: 'categories', label: 'Categories' },
    { key: 'receipt', label: 'Upload Receipt' },
    { key: 'statement', label: 'Upload Statement' },
    { key: 'manual', label: 'Manual Entry' },
    { key: 'transactions', label: 'Transactions' },
    { key: 'account', label: 'My Account' },
  ]
  if (role === 'admin') navItems.push({ key: 'admin', label: 'Admin Users' })

  return (
    <div className="app">
      <header className="hero">
        <div>
          <p className="eyebrow">Financial Nudger</p>
          <h1>Focused flows for users and admins</h1>
          <p className="subhead">
            API base <span className="pill">{API_BASE}</span>
          </p>
        </div>
        <div className="hero-stats">
          <div className="stat">
            <span className="label">Transactions</span>
            <strong>{transactions.length}</strong>
          </div>
          <div className="stat">
            <span className="label">AI categorized</span>
            <strong>{aiCategorized}</strong>
          </div>
          <div className="stat">
            <span className="label">User fixed</span>
            <strong>{userCategorized}</strong>
          </div>
        </div>
      </header>

      {showNav && (
        <NavBar
          items={navItems}
          view={view}
          setView={setView}
          role={role}
          onToggleRole={(next) => {
            setRole(next)
            setView(next === 'admin' ? 'admin' : 'dashboard')
          }}
        />
      )}

      <BannerView banner={banner} />

      {!currentUser && role === 'user' && (
        <AuthView
          registerForm={registerForm}
          setRegisterForm={setRegisterForm}
          loginForm={loginForm}
          setLoginForm={setLoginForm}
          onRegister={registerUser}
          onLogin={loginUser}
          currentUser={currentUser}
        />
      )}

      {currentUser && view === 'dashboard' && (
        <DashboardView
          monthlySpend={monthlySpend}
          budget={budget}
          setBudget={setBudget}
          remainingBudget={remainingBudget}
          spendingByCategoryData={spendingByCategoryData}
          timelineData={timelineData}
          budgetValue={budgetValue}
          currentUser={currentUser}
        />
      )}

      {currentUser && view === 'categories' && (
        <CategoriesView
          loadingCategories={loadingCategories}
          categories={categories}
          categoryForm={categoryForm}
          setCategoryForm={setCategoryForm}
          customCategoryName={customCategoryName}
          setCustomCategoryName={setCustomCategoryName}
          library={CATEGORY_LIBRARY}
          onSubmit={createCategory}
        />
      )}

      {currentUser && view === 'receipt' && (
        <UploadView
          kind="receipt"
          file={receiptFile}
          setFile={setReceiptFile}
          onUpload={() => uploadFile('receipt')}
          result={receiptResult}
        />
      )}

      {currentUser && view === 'statement' && (
        <UploadView
          kind="statement"
          file={statementFile}
          setFile={setStatementFile}
          onUpload={() => uploadFile('statement')}
          result={statementResult}
        />
      )}

      {currentUser && view === 'manual' && (
        <ManualView
          manualForm={manualForm}
          setManualForm={setManualForm}
          onSubmit={submitManualTransaction}
          result={manualResult}
        />
      )}

      {currentUser && view === 'transactions' && (
        <TransactionsView
          loading={loadingTransactions}
          transactions={transactions}
          categoryLookup={categoryLookup}
          feedbackDrafts={feedbackDrafts}
          setFeedbackDrafts={setFeedbackDrafts}
          onFeedback={submitFeedback}
          currentUser={currentUser}
          onRefresh={() => userId && fetchTransactions(userId)}
          availableCategories={categories}
        />
      )}

      {role === 'admin' && view === 'admin' && (
        <AdminView users={usersList} onFetch={fetchUsersList} />
      )}

      {currentUser && view === 'account' && (
        <AccountView
          updateForm={updateForm}
          setUpdateForm={setUpdateForm}
          onUpdate={updateUser}
          onDelete={deleteUser}
        />
      )}
    </div>
  )
}

export default App
