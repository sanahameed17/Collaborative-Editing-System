@echo off
echo Starting Collaborative Document Editor Backend Services...
echo.

echo Building all services...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting API Gateway...
start "API Gateway" cmd /k "cd api-gateway && java -jar target\api-gateway-1.0.0.jar"

echo Starting User Management Service...
start "User Management" cmd /k "cd user-management-service && java -jar target\user-management-service-1.0.0.jar"

echo Starting Document Editing Service...
start "Document Editing" cmd /k "cd document-editing-service && java -jar target\document-editing-service-1.0.0.jar"

echo Starting Version Control Service...
start "Version Control" cmd /k "cd version-control-service && java -jar target\version-control-service-1.0.0.jar"

echo.
echo All services started! Waiting for them to initialize...
timeout /t 10 /nobreak > nul

echo.
echo Services should be running on:
echo - API Gateway: http://localhost:8080
echo - User Service: http://localhost:8081
echo - Document Service: http://localhost:8082
echo - Version Control: http://localhost:8083
echo.
echo Now start the frontend:
echo   cd frontend
echo   npm install
echo   npm start
echo.
echo Then open: http://localhost:3000
echo.
pause
