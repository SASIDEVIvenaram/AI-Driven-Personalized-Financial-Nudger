@echo off
REM Financial Nudger Startup Script
REM Starts ML API, Backend, and Frontend in parallel

echo.
echo Starting Financial Nudger Stack...
echo.

REM Start ML API (port 5000)
echo Starting ML API on port 5000...
start "ML API" cmd /k "cd /d %~dp0ml && python ml_api.py"
timeout /t 2 /nobreak

REM Start Backend (port 8081)
echo Starting Backend on port 8081...
start "Backend" cmd /k "cd /d %~dp0backend\financial-nudger && mvnw.cmd spring-boot:run"
timeout /t 5 /nobreak

REM Start Frontend (port 5173)
echo Starting Frontend on port 5173...
start "Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"
timeout /t 2 /nobreak

echo.
echo ============================================
echo All services started!
echo.
echo ML API:  http://localhost:5000/predict
echo Backend: http://localhost:8081/api/users
echo Frontend: http://localhost:5173
echo.
echo Wait 10-15 seconds for backend to boot
echo Then open http://localhost:5173 in your browser
echo ============================================
echo.
pause
