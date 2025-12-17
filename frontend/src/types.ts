export type Banner = {
  type: 'success' | 'error' | 'info'
  message: string
}

export type User = {
  userId: number
  email: string
  firstName: string
  lastName: string
  currencyPreference?: string
}

export type Transaction = {
  transactionId: number
  userId: number
  date: string
  amount: number | string
  type?: 'DEBIT' | 'CREDIT'
  categoryId?: number
  description?: string
  merchantName?: string
  categoryConfidence?: number
  isAiCategorized?: boolean
  isUserCategorized?: boolean
}

export type Category = {
  categoryId: number
  categoryName: string
  categoryType: 'EXPENSE' | 'INCOME' | 'TRANSFER'
  isUserDefined?: boolean
}

export type FileUploadResponse = {
  success?: boolean
  message?: string
}

export type View =
  | 'auth'
  | 'dashboard'
  | 'categories'
  | 'receipt'
  | 'statement'
  | 'manual'
  | 'transactions'
  | 'admin'
  | 'account'
