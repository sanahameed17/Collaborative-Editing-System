@echo off
echo Starting Collaborative Document Editor Frontend...
echo.

cd frontend

echo Installing dependencies...
call npm install
if %errorlevel% neq 0 (
    echo npm install failed!
    pause
    exit /b 1
)

echo.
echo Starting frontend server...
call npm start

pause
