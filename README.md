# Todo Summary Assistant - Backend

This is the backend service for the Todo Summary Assistant project, built with Spring Boot and Maven.

---

## Project Overview

- **Backend:** Java Spring Boot with Maven
- **Database:** Configurable via `application.properties`
- **Features:** CRUD for todos, summarize todos using an LLM API, send summaries to Slack via webhook.

---

## Environment Variables and Configuration

- All sensitive keys and configuration values are stored in a `.env` file which is **ignored** in Git via `.gitignore`.
- You will find a `.env.example` file in the repo listing all required environment variables like:
  - `GROQ_API_KEY`
  - `GROQ_API_URL`
  - `SLACK_WEBHOOK_URL`
- Replace placeholders with your actual values.

---

## Setup Instructions

1. Clone the repository:
   ```bash
   git clone <your-repo-url>
   cd TodoSummaryAssistant-backend
   ```

2. Create a `.env` file in the root directory based on `.env.example` and fill in your API keys and database credentials.

3. Update the `src/main/resources/application.properties` file to configure your database connection details (database name, username, password, etc.).

4. Build and run the Spring Boot app:
   ```bash
   ./mvnw spring-boot:run
   ```

---

## API Endpoints

| Method | Endpoint         | Description                       |
|--------|------------------|---------------------------------|
| GET    | `/todos`         | Fetch all todo items             |
| POST   | `/todos`         | Add a new todo item              |
| DELETE | `/todos/{id}`    | Delete a todo item by ID         |
| POST   | `/summarize`     | Summarize todos and send to Slack |

---

## Notes

- The `.env` file **should never be committed** to your GitHub repository.
- The `.env.example` file serves as a reference for all required environment variables.
- Slack integration uses Incoming Webhooks.
- LLM integration uses Groq API or other configured provider.

---

## Project Structure

```
TodoSummaryAssistant-backend/
├── src/
│   ├── main/
│   │   ├── java/com/todo/TodoSummaryAssistant/  # Java source files
│   │   └── resources/
│   │       └── application.properties            # Spring Boot configuration
├── .env.example                                  # Environment variables template
├── .gitignore                                   # To ignore .env and other files
├── pom.xml                                      # Maven project file
└── README.md                                    # This file
```

---

