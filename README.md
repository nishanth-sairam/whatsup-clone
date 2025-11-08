# WhatsApp Clone

A full-stack messaging application built with Spring Boot, React, and Keycloak authentication.

## Features

- Real-time messaging with WebSocket
- Secure OAuth2/JWT authentication via Keycloak
- Media file sharing
- Responsive React UI with TypeScript
- Docker containerization

## Tech Stack

- **Backend**: Spring Boot 3.5.5, Java 17, PostgreSQL
- **Frontend**: React 19, TypeScript, Tailwind CSS
- **Auth**: Keycloak 26.2.5
- **Container**: Docker & Docker Compose

## Quick Start

**Prerequisites**: Docker and Docker Compose

1. Clone and start

```bash
git clone <repository-url>
cd whatsup-clone
./manage.sh up
```

2. Access

- Frontend: http://localhost:5173
- Backend: http://localhost:9090
- API Documentation (Swagger): http://localhost:9090/swagger-ui.html
- Keycloak: http://localhost:8080 (admin/admin)

## Development & Debugging

For detailed debugging instructions, see [DEBUGGING.md](DEBUGGING.md)

### Quick Debug Setup

1. **Start required services:**

   ```bash
   # Windows
   debug-start.bat

   # Linux/Mac
   ./debug-start.sh
   ```

2. **Debug in VS Code:**
   - Press `Ctrl+Shift+D` (Run and Debug)
   - Select "Debug Spring Boot Backend"
   - Press `F5` to start debugging
   - Set breakpoints and enjoy debugging!

### VS Code Tasks

Press `Ctrl+Shift+P` and type "Tasks: Run Task":

- Build Backend
- Run Backend Tests
- Start Dev Services (DB + Keycloak)
- Stop Dev Services
- Start Full Stack
- Stop Full Stack
- View Backend Logs

## Docker Commands

```bash
./manage.sh up          # Start all services
./manage.sh down        # Stop all services
./manage.sh logs        # View logs
./manage.sh status      # Check status
```

## API Documentation

The complete API documentation is available through Swagger UI:

- **Swagger UI**: http://localhost:9090/swagger-ui.html
- **OpenAPI JSON**: http://localhost:9090/api-docs

### Key API Features

- **Authentication**: OAuth2/JWT via Keycloak integration
- **Real-time Messaging**: WebSocket endpoints for live chat
- **Media Sharing**: File upload/download endpoints
- **User Management**: User discovery and profile management
- **Chat Management**: Create and manage chat conversations

### Using Swagger UI

1. Start the application using `./manage.sh up`
2. Navigate to http://localhost:9090/swagger-ui.html
3. To test authenticated endpoints:
   - First, obtain a JWT token from Keycloak at http://localhost:8080
   - Click the "Authorize" button in Swagger UI
   - Enter your Bearer token in the format: `Bearer <your-jwt-token>`


