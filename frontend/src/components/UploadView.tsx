export function UploadView({
  kind,
  file,
  setFile,
  onUpload,
  result,
}: {
  kind: 'receipt' | 'statement'
  file: File | null
  setFile: (f: File | null) => void
  onUpload: () => void
  result: string
}) {
  const label = kind === 'receipt' ? 'Upload Receipt' : 'Upload Statement'
  const desc = kind === 'receipt' ? 'POST /api/files/upload-receipt' : 'POST /api/files/upload-statement'
  return (
    <section className="card">
      <div className="card-header">
        <h2>{label}</h2>
        <p>{desc}</p>
      </div>
      <div className="form">
        <input type="file" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
        {file && <p className="muted">Selected: {file.name}</p>}
        <button className="primary" onClick={onUpload}>{label}</button>
        {result && <p className="muted">{result}</p>}
      </div>
    </section>
  )
}
