# Bulk User Import System

Simple app to upload CSV files and import users. Built for AllRide coding challenge.

## What it does

Upload a CSV file through the web UI, backend processes it asynchronously using an event-driven approach. Users are stored in memory (no DB needed for this challenge).

## Tech Stack

- Backend: Kotlin + Spring Boot 3.2
- Frontend: React + Vite + Material-UI
- Messaging: Kotlin Coroutines Channels (simple in-memory pub/sub)

## How to run

### Backend

```bash
cd backend
./gradlew bootRun
```

Runs on `http://localhost:8080`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:3000`

## CSV Format

The CSV needs a header row and then user data:

```csv
id,firstName,lastName,email
1,John,Doe,john.doe@example.com
2,Jane,Smith,jane.smith@example.com
```

Check `example-users.csv` for a sample file.

## How it works

1. User uploads CSV via frontend
2. Backend saves file temporarily and publishes an event
3. Worker service picks up the event, parses CSV, stores users
4. File gets cleaned up after processing

I used Kotlin Coroutines Channels for the pub/sub since it's simple and doesn't need external dependencies. In production you'd probably use GCP Pub/Sub or RabbitMQ.

## API

POST `/api/files/upload` - upload CSV file

Returns:
```json
{
  "success": true,
  "message": "File uploaded successfully, processing started"
}
```

## Notes

- Files are stored in `uploads/` directory (relative to backend)
- Users are stored in memory (ConcurrentHashMap)
- Invalid CSV rows are logged but processing continues
- Check backend console for processing logs

## Project Structure

```
backend/
  src/main/kotlin/com/allride/
    domain/          # User model, events
    application/     # Services
    infrastructure/  # Storage, messaging, CSV parsing
    presentation/    # REST controller

frontend/
  src/
    components/      # FileUpload component
    App.jsx
    main.jsx
```

That's pretty much it. Let me know if you have questions!
