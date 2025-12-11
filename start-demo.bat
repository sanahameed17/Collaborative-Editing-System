@echo off
echo ========================================
echo  Collaborative Editing System Startup
echo ========================================
echo.

echo Starting all microservices...
echo.

echo [1/4] Starting API Gateway...
start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"

timeout /t 5 /nobreak > nul

echo [2/4] Starting User Management Service...
start "User Management" cmd /k "cd user-management-service && mvn spring-boot:run"

timeout /t 5 /nobreak > nul

echo [3/4] Starting Document Editing Service...
start "Document Service" cmd /k "cd document-editing-service && mvn spring-boot:run"

timeout /t 5 /nobreak > nul

echo [4/4] Starting Version Control Service...
start "Version Control" cmd /k "cd version-control-service && mvn spring-boot:run"

timeout /t 10 /nobreak > nul

echo [5/5] Starting Frontend...
start "Frontend" cmd /k "cd frontend && node server.js"

echo.
echo ========================================
echo All services are starting up...
echo.
echo Frontend will be available at: http://localhost:3000
echo API Gateway at: http://localhost:8080
echo.
echo Demo users available:
echo   admin/admin123 (Administrator)
echo   editor/editor123 (Editor)
echo   user/user123 (Read-only user)
echo.
echo Press any key to run the demo script...
pause > nul

echo Running comprehensive demo...
call demo.sh

echo.
echo Demo completed! Check the generated files and visit http://localhost:3000
echo.
pause
