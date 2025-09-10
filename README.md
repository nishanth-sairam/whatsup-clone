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
- Keycloak: http://localhost:8080 (admin/admin)

## Docker Commands

```bash
./manage.sh up          # Start all services
./manage.sh down        # Stop all services
./manage.sh logs        # View logs
./manage.sh status      # Check status
```

## API Endpoints

### Authentication

```
GET    /api/auth/user-info     # Get user info
PUT    /api/auth/profile       # Update profile
```

### Users & Chats

```
GET    /api/users              # Get users
GET    /api/chats              # Get chats
POST   /api/chats              # Create chat
```

### Messages

```
GET    /api/messages/{chatId}  # Get messages
POST   /api/messages           # Send message
POST   /api/messages/media     # Upload media
```

### WebSocket

```
CONNECT /ws                    # WebSocket connection
SEND    /app/chat.sendMessage  # Send message
```

## License

MIT License
