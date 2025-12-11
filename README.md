# Ping Pong Champions

A Spring Boot application for managing ping pong leagues with user authentication, public/private leagues, and invite codes.

## Features

- **User Authentication**: Register and login with JWT-based authentication
- **League Management**: Create public or private leagues
- **Public Leagues**: Browse and join public leagues
- **Private Leagues**: Join private leagues using invite codes
- **League Membership**: View your leagues and manage memberships

## Technology Stack

### Backend
- Spring Boot 3.2.0
- **Java 17** (Required - Java 25 is not compatible)
- PostgreSQL
- Spring Security with JWT
- Spring Data JPA
- Lombok

### Frontend
- HTML5
- CSS3 (Modern design with gradients and glassmorphism)
- Vanilla JavaScript
- Google Fonts (Inter)

## Prerequisites

- **Java 17** (IMPORTANT: Java 25 is not compatible with current Spring Boot/Maven setup)
- Maven 3.6+
- PostgreSQL 12+
- Node.js (optional, for serving frontend)

## Database Setup

### Option 1: Using the Setup Script (Recommended)

The easiest way to set up your database is using the provided script:

**Linux/macOS:**
```bash
./setup-database.sh
```

**Windows:**
```cmd
setup-database.bat
```

The script will:
- Load credentials from your `.env` file
- Test the database connection
- Create all necessary tables
- Optionally load sample data

### Option 2: Manual Setup

1. Create a PostgreSQL database:
```bash
createdb pingpong_champions
```

2. Run the database schema script:
```bash
psql -d pingpong_champions -f database/01_create_tables.sql
```

3. (Optional) Load sample data:
```bash
psql -d pingpong_champions -f database/02_sample_data.sql
```

### Using External Database (Clever Cloud)

If using an external database, the setup script will automatically use your `.env` credentials:

```bash
./setup-database.sh
```

Or manually with psql:
```bash
# Load environment variables
source .env

# Run setup scripts
psql "postgresql://$POSTGRESQL_ADDON_USER:$POSTGRESQL_ADDON_PASSWORD@$POSTGRESQL_ADDON_HOST:$POSTGRESQL_ADDON_PORT/$POSTGRESQL_ADDON_DB" -f database/01_create_tables.sql
```

### Configuration

4. Update database credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pingpong_champions
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Running the Application

### Backend

1. Build the project:
```bash
mvn clean install
```

2. Run the Spring Boot application:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

## Running with Docker

### Option 1: Docker with Local PostgreSQL

This will run both the Spring Boot app and a PostgreSQL database in Docker:

```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop and remove volumes (deletes database data)
docker-compose down -v
```

The database initialization scripts in `database/` will run automatically on first startup.

### Option 2: Docker with External PostgreSQL (Clever Cloud)

If you're using an external database like Clever Cloud, use the alternative compose file:

```bash
# Make sure your .env file has the correct database credentials
docker-compose -f docker-compose.external-db.yml up --build

# Or in detached mode
docker-compose -f docker-compose.external-db.yml up -d --build
```

### Building Docker Image Only

```bash
# Build the Docker image
docker build -t pingpong-champions .

# Run the container (requires .env file or environment variables)
docker run -p 8080:8080 --env-file .env pingpong-champions
```

## Running with Podman

If you're using Podman instead of Docker, use the provided script:

```bash
# Build and run with Podman
./podman-run.sh
```

Or manually:
```bash
# Build the image
podman build -t pingpong-champions .

# Run the container
podman run -d \
  --name pingpong-app \
  -p 8080:8080 \
  --env-file .env \
  pingpong-champions

# View logs
podman logs -f pingpong-app

# Stop the container
podman stop pingpong-app

# Start the container again
podman start pingpong-app
```

### Frontend

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Serve the frontend files using any HTTP server. For example, using Python:
```bash
# Python 3
python -m http.server 3000

# Or using Node.js http-server
npx http-server -p 3000
```

3. Open your browser and navigate to `http://localhost:3000`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and receive JWT token

### Leagues
- `POST /api/leagues` - Create a new league (authenticated)
- `GET /api/leagues/public` - Get all public leagues
- `GET /api/leagues/my` - Get user's leagues (authenticated)
- `POST /api/leagues/{id}/join` - Join a public league (authenticated)
- `POST /api/leagues/join-private` - Join a private league with invite code (authenticated)
- `DELETE /api/leagues/{id}/leave` - Leave a league (authenticated)

## Project Structure

```
PingPongChampionsV2/
├── src/
│   └── main/
│       ├── java/com/pingpong/
│       │   ├── config/          # Security and JWT configuration
│       │   ├── controller/      # REST controllers
│       │   ├── dto/             # Data Transfer Objects
│       │   ├── model/           # JPA entities
│       │   ├── repository/      # JPA repositories
│       │   └── service/         # Business logic
│       └── resources/
│           └── application.properties
├── database/
│   ├── 01_create_tables.sql    # Database schema
│   └── 02_sample_data.sql      # Sample data
├── frontend/
│   ├── index.html              # Landing page
│   ├── login.html              # Login page
│   ├── register.html           # Registration page
│   ├── dashboard.html          # User dashboard
│   ├── styles.css              # Styles
│   └── app.js                  # JavaScript API client
└── pom.xml
```

## Security

- Passwords are hashed using BCrypt
- JWT tokens are used for authentication
- Token expiration is set to 24 hours (configurable in application.properties)
- **Important**: Change the JWT secret in production (`jwt.secret` in application.properties)

## Default Credentials (Sample Data)

If you loaded the sample data, you can use these credentials:
- Username: `john_doe`, Password: `password123`
- Username: `jane_smith`, Password: `password123`

## Development

### Building for Production

```bash
mvn clean package
java -jar target/ping-pong-champions-1.0.0.jar
```

### Running Tests

```bash
mvn test
```

## Troubleshooting

### Java Version Issues

If you encounter compilation errors like `java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag`, you're likely using Java 25 which is not compatible.

**Solution**: Switch to Java 17

```bash
# Check your Java version
java -version

# If using SDKMAN
sdk install java 17.0.9-tem
sdk use java 17.0.9-tem

# If using mise/asdf
mise use java@17

# Verify the version
java -version
```

After switching Java versions, clean and rebuild:
```bash
mvn clean install
```

## License

© 2025 Ping Pong Champions. All rights reserved.
