#!/bin/bash

echo "========================================"
echo " Collaborative Editing System Startup"
echo "========================================"
echo ""

echo "Starting all microservices..."
echo ""

echo "[1/4] Starting API Gateway..."
cd api-gateway && mvn spring-boot:run &
API_GATEWAY_PID=$!
cd ..
sleep 5

echo "[2/4] Starting User Management Service..."
cd user-management-service && mvn spring-boot:run &
USER_SERVICE_PID=$!
cd ..
sleep 5

echo "[3/4] Starting Document Editing Service..."
cd document-editing-service && mvn spring-boot:run &
DOC_SERVICE_PID=$!
cd ..
sleep 5

echo "[4/4] Starting Version Control Service..."
cd version-control-service && mvn spring-boot:run &
VERSION_SERVICE_PID=$!
cd ..
sleep 10

echo "[5/5] Starting Frontend..."
cd frontend && node server.js &
FRONTEND_PID=$!
cd ..

echo ""
echo "========================================"
echo "All services are starting up..."
echo ""
echo "Frontend will be available at: http://localhost:3000"
echo "API Gateway at: http://localhost:8080"
echo ""
echo "Demo users available:"
echo "  admin/admin123 (Administrator)"
echo "  editor/editor123 (Editor)"
echo "  user/user123 (Read-only user)"
echo ""
echo "Press Ctrl+C to stop all services"
echo ""

# Wait for user input to run demo
read -p "Press Enter to run the comprehensive demo script..."

echo "Running comprehensive demo..."
chmod +x demo.sh
./demo.sh

echo ""
echo "Demo completed! Check the generated files and visit http://localhost:3000"
echo ""

# Cleanup function
cleanup() {
    echo "Stopping all services..."
    kill $API_GATEWAY_PID 2>/dev/null
    kill $USER_SERVICE_PID 2>/dev/null
    kill $DOC_SERVICE_PID 2>/dev/null
    kill $VERSION_SERVICE_PID 2>/dev/null
    kill $FRONTEND_PID 2>/dev/null
    echo "All services stopped."
    exit 0
}

# Set trap to cleanup on script exit
trap cleanup SIGINT SIGTERM

# Wait for services to run
wait
