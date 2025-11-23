# Bulk User Import System

A full-stack application for bulk importing user data from CSV files, built with Kotlin (Spring Boot) backend and React frontend, featuring an event-driven architecture using publish/subscribe pattern.

## Overview

This system allows users to upload CSV files containing user data through a React web interface. The backend receives the file, stores it temporarily, and publishes an event to a message queue. A separate worker service subscribes to these events, processes the CSV files, parses the data, and stores it (in-memory for this challenge).

## Architecture

### Components

1. **Frontend (React)**: Simple web interface for file upload with validation and user feedback
2. **Backend (Spring Boot + Kotlin)**: REST API for file upload, file storage, and event publishing
3. **Event System**: In-memory pub/sub using Kotlin Coroutines Channels
4. **Worker Service**: Asynchronous CSV processor that subscribes to events and handles data import

### Event-Driven Flow

```
User Uploads CSV → Backend Receives File → File Stored Temporarily 
→ FileUploadedEvent Published → Worker Subscribes & Processes 
→ CSV Parsed → Users Stored → File Cleaned Up
```

## Tech Stack

- **Backend**: Kotlin 1.9.20, Spring Boot 3.2.0, OpenCSV for parsing
- **Frontend**: React 18.2.0, Vite 5.0, Material-UI (MUI) 5.15, Axios for HTTP requests
- **Messaging**: Kotlin Coroutines Channels (in-memory pub/sub)
- **Build Tools**: Gradle 8.4, Vite, npm/yarn

## Prerequisites

### Backend
- Java 17 or higher
- Gradle 8.4 (wrapper included)

### Frontend
- Node.js 16.x or higher
- npm or yarn

## Project Structure

```
.
├── backend/
│   ├── src/main/kotlin/com/allride/
│   │   ├── domain/
│   │   │   ├── model/User.kt
│   │   │   └── event/FileUploadedEvent.kt
│   │   ├── application/
│   │   │   └── service/
│   │   │       ├── FileUploadService.kt
│   │   │       └── CsvProcessingService.kt
│   │   ├── infrastructure/
│   │   │   ├── messaging/EventChannel.kt
│   │   │   ├── storage/
│   │   │   │   ├── FileStorageService.kt
│   │   │   │   └── InMemoryUserRepository.kt
│   │   │   └── csv/CsvParser.kt
│   │   ├── presentation/
│   │   │   ├── controller/FileUploadController.kt
│   │   │   └── dto/UploadResponse.kt
│   │   └── BulkUserImportApplication.kt
│   ├── build.gradle.kts
│   └── src/main/resources/application.yml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   └── FileUpload.jsx
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
└── README.md
```

## Setup Instructions

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```
   (On Windows: `gradlew.bat build`)

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```
   (On Windows: `gradlew.bat bootRun`)

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```
   or
   ```bash
   yarn install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```
   or
   ```bash
   yarn dev
   ```

The frontend will start on `http://localhost:3000` and automatically open in your browser.

**Note**: The frontend is built with Vite and Material-UI (MUI) for a modern, fast development experience and beautiful UI components.

## CSV File Format

The CSV file should have the following structure:

```csv
id,firstName,lastName,email
1,John,Doe,john.doe@example.com
2,Jane,Smith,jane.smith@example.com
3,Bob,Johnson,bob.johnson@example.com
```

### Requirements:
- First row must be a header row with: `id,firstName,lastName,email`
- Each subsequent row should contain 4 columns: id, firstName, lastName, email
- Email addresses must be in valid format
- All fields are required (cannot be empty)

An example CSV file is provided at `example-users.csv` in the root directory.

## Design Decisions

### Pub/Sub Implementation

**Choice**: Kotlin Coroutines Channels (in-memory)

**Rationale**:
- Simple and lightweight for local development
- No external dependencies required
- Perfect for demonstrating event-driven architecture concepts
- Easy to replace with production solutions (GCP Pub/Sub, RabbitMQ, Kafka)

**Trade-offs**:
- In-memory solution: Events are lost on application restart
- Single-node only: Not suitable for distributed systems
- For production, would use GCP Pub/Sub as mentioned in requirements

### Domain-Driven Design

The codebase follows DDD principles:
- **Domain Layer**: Core entities (`User`) and events (`FileUploadedEvent`)
- **Application Layer**: Service orchestration (`FileUploadService`, `CsvProcessingService`)
- **Infrastructure Layer**: External concerns (file storage, messaging, CSV parsing, repository)
- **Presentation Layer**: REST controllers and DTOs

### Error Handling

- CSV parsing continues even if individual rows have errors
- Invalid rows are logged with row numbers and error messages
- Valid rows are still processed and stored
- File cleanup happens even if processing fails
- User-friendly error messages returned to frontend

### Storage

**Choice**: In-memory repository (`InMemoryUserRepository`)

**Rationale**:
- As per requirements, no database setup needed
- Users are stored in a `ConcurrentHashMap` for thread safety
- In production, this would be replaced with a proper database (PostgreSQL, etc.)

## API Endpoints

### POST `/api/files/upload`

Uploads a CSV file for processing.

**Request**:
- Method: `POST`
- Content-Type: `multipart/form-data`
- Body: Form data with `file` field containing the CSV file

**Response**:
```json
{
  "success": true,
  "message": "File uploaded successfully, processing started"
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Only CSV files are allowed"
}
```

## Testing the Application

1. Start both backend and frontend (see Setup Instructions)
2. Open the frontend at `http://localhost:3000`
3. Click "Choose File" and select a CSV file with user data
4. Click "Upload File"
5. Check the backend console logs to see the processing results

### Example CSV

Create a file named `users.csv`:
```csv
id,firstName,lastName,email
1,John,Doe,john.doe@example.com
2,Jane,Smith,jane.smith@example.com
3,Bob,Johnson,bob.johnson@example.com
```

## Logging

The backend logs detailed information about:
- File upload events
- CSV parsing results (success and errors)
- Imported users
- Processing errors

Check the console output to monitor the system's operation.

## Future Improvements (if time permitted)

- Database integration (PostgreSQL, MongoDB, etc.)
- Authentication and authorization
- File size limits and validation
- Progress tracking for large file processing
- Batch processing with configurable batch sizes
- REST API endpoints to query imported users
- Frontend improvements: file drag-and-drop, progress bars
- Docker containerization
- Unit and integration tests
- Production-ready pub/sub (GCP Pub/Sub, RabbitMQ)

## License

This project is created as part of a coding challenge for AllRide.

