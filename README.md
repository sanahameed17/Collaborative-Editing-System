Collaborative Editing System

A microservices-based system that allows multiple users to create, edit, share, and collaborate on documents in real-time.

Main Features

User Authentication (JWT, roles: Admin, Editor, User)
Create & Edit Documents
Real-Time Collaboration
Version History
Document Sharing (Read / Write)

Templates

Export Files (TXT, HTML, JSON)

Tech Stack

Java 8, Spring Boot
H2 Database
HTML, CSS, JavaScript
Maven
Microservices Architecture

Quick Start
1. Start Backend Services

Run each service in a separate terminal:

cd api-gateway && mvn spring-boot:run
cd user-management-service && mvn spring-boot:run
cd document-editing-service && mvn spring-boot:run
cd version-control-service && mvn spring-boot:run

2. Start Frontend
cd frontend
npm start

3. Open App

Visit: http://localhost:3000

Demo Users
User	Password	Role
admin	admin123	Administrator
editor	editor123	Editor
user	user123	Viewer

What You Can Test
1. Login & Roles

Test Admin, Editor, User access.

2. Document Editing

Create + edit + test real-time editing.

3. Share Documents

Give other users read-only or read-write access.

4. Use Templates

Create documents using pre-made templates.

5. Export Files

Download TXT, HTML, JSON versions.

Useful API Endpoints

Auth

POST /api/auth/register
POST /api/auth/login

Documents

GET /api/documents
POST /api/documents
PUT /api/documents/{id}

Sharing

POST /api/documents/{id}/share

Export

/api/documents/{id}/export/txt
/api/documents/{id}/export/html
/api/documents/{id}/export/json

Troubleshooting

Check Java: java -version
Check Maven: mvn -version
Check Node: node -v
Check ports: netstat -an | find "8080"
