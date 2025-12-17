# Financial Nudger Startup Script
# Starts ML API, Backend, and Frontend in parallel

Write-Host "🚀 Starting Financial Nudger Stack..." -ForegroundColor Cyan

# Start ML API (port 5000)
Write-Host "📍 Starting ML API on port 5000..." -ForegroundColor Green
Start-Process pwsh -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\ml'; python .\ml_api.py"
Start-Sleep -Seconds 2

# Start Backend (port 8081)
Write-Host "📍 Starting Backend on port 8081..." -ForegroundColor Green
Start-Process pwsh -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\backend\financial-nudger'; .\mvnw.cmd spring-boot:run"
Start-Sleep -Seconds 3

# Start Frontend (port 5173)
Write-Host "📍 Starting Frontend on port 5173..." -ForegroundColor Green
Start-Process pwsh -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\frontend'; npm run dev"
Start-Sleep -Seconds 2

Write-Host "" 
Write-Host "All services started!" -ForegroundColor Cyan
Write-Host "   ML API:  http://localhost:5000/predict" -ForegroundColor Yellow
Write-Host "   Backend: http://localhost:8081/api/users" -ForegroundColor Yellow
Write-Host "   Frontend: http://localhost:5173" -ForegroundColor Yellow
Write-Host ""
Write-Host "Wait 10-15 seconds for backend to boot, then open http://localhost:5173 in your browser." -ForegroundColor Magenta
