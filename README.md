# Library Management System

A reactive RESTful API for managing a simple library system built with Spring Boot WebFlux, PostgreSQL, and containerized with Docker.

## 🏗️ Architecture Overview

This application follows a reactive, non-blocking architecture using Spring WebFlux and R2DBC for database operations. The system manages books, borrowers, and book loans through a clean RESTful API.

### Technology Stack

- **Java 17** - Programming language
- **Spring Boot 3.5.5** - Application framework
- **Spring WebFlux** - Reactive web framework
- **Spring Data R2DBC** - Reactive database access
- **PostgreSQL** - Primary database
- **Flyway** - Database migration tool
- **OpenAPI/Swagger** - API documentation
- **Lombok** - Boilerplate code reduction
- **Maven** - Dependency management
- **Docker & Docker Compose** - Containerization

## 📊 Database Design

The system uses three main tables:

### Tables Structure

```sql
CREATE TABLE borrowers (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           name VARCHAR(255) NOT NULL,
                           email VARCHAR(255) NOT NULL UNIQUE,
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create books table
CREATE TABLE books (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       isbn VARCHAR(20) NOT NULL,
                       title VARCHAR(500) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP

    -- Constraint to ensure books with same ISBN have same title and author
--                        CONSTRAINT unique_isbn_title_author UNIQUE (isbn, title, author)
);

-- Create borrowing_records table to track who borrows which book and for how long
CREATE TABLE borrowing_records (
                                   id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   borrower_id UUID NOT NULL REFERENCES borrowers(id) ON DELETE CASCADE,
                                   book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
                                   borrowed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   returned_at TIMESTAMP WITH TIME ZONE NULL,
                                   due_date TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP + INTERVAL '14 days'),
                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Ensure one book can only be borrowed by one person at a time (if not returned)
                                   CONSTRAINT unique_active_book_borrowing
                                       EXCLUDE (book_id WITH =) WHERE (returned_at IS NULL)
);

-- Create indexes for better performance
CREATE INDEX idx_borrowers_email ON borrowers(email);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_borrowing_records_borrower_id ON borrowing_records(borrower_id);
CREATE INDEX idx_borrowing_records_book_id ON borrowing_records(book_id);
CREATE INDEX idx_borrowing_records_borrowed_at ON borrowing_records(borrowed_at);
CREATE INDEX idx_borrowing_records_returned_at ON borrowing_records(returned_at);
CREATE INDEX idx_borrowing_records_active ON borrowing_records(book_id) WHERE returned_at IS NULL;
```

### Database Choice Justification

**PostgreSQL** was chosen for the following reasons:
- **ACID Compliance**: Ensures data integrity for financial transactions
- **Concurrent Access**: Excellent handling of multiple users accessing the system
- **JSON Support**: Native JSON data type for future extensibility
- **Mature Ecosystem**: Well-supported with Spring Boot and R2DBC
- **Scalability**: Handles growing data volumes efficiently
- **Open Source**: No licensing costs

## 🏛️ Class Structure

### Entity Layer
```
src/main/java/com/library/entity/
├── Borrower.java                   # Borrower entity with validation
├── Book.java                       # Book entity with ISBN constraints
└── BorrowingRecord.java            # Borrowing Record tracking entity
```

### Repository Layer
```
src/main/java/com/library/repository/
├── BorrowerRepository.java             # R2DBC repository for borrowers
├── BookRepository.java                 # R2DBC repository for books
└── BorrowingRecordRepository.java      # R2DBC repository for loans
```

### Service Layer
```
src/main/java/com/library/service/
├── BorrowerService.java                # Business logic for borrowers
├── BookService.java                    # Business logic for books
└── BorrowingService.java               # Business logic for loans
```

### Controller Layer
```
src/main/java/com/library/controller/
├── BorrowerController.java     # REST endpoints for borrowers
├── BookController.java         # REST endpoints for books
└── BorrowingController.java    # REST endpoints for loans
```

### DTO Layer
```
src/main/java/com/library/dto/
├── request/
│   ├── CreateBorrowerRequest.java
│   ├── BookDto.java
│   ├── BorrowBookRequest.java
│   ├── BorrowerDto.java
│   ├── BorrowingRecordDto.java
│   ├── CreateBorrowerRequest.java
│   ├── CreateBookRequest.java   
│   └── ReturnBookRequest.java
└── response/
    └── ApiResponse.java
```

### Configuration
```
src/main/java/com/library/config/
├── DatabaseConfig.java         # R2DBC configuration
├── OpenApiConfig.java          # Swagger/OpenAPI configuration
└── WebConfig.java              # WebFlux configuration
```

## 🚀 API Endpoints

### Borrower Management
```http
POST   /api/v1/borrowers                # Register new borrower
GET    /api/v1/borrowers                # Get all borrowers
GET    /api/v1/borrowers/{id}           # Get borrower by ID
GET    /api/v1/email/{email}            # Get borrower by ID
PUT    /api/v1/borrowers/{id}           # Update borrower
DELETE /api/v1/borrowers/{id}           # Delete borrower
```

### Book Management
```http
POST   /api/v1/books                        # Register new book
GET    /api/v1/books                        # Get all books
GET    /api/v1/books/{id}                   # Get book by ID
GET    /api/v1/books/isbn/{isbn}            # Get books by ISBN
GET    /api/v1/books/title                  # Get books by Title
GET    /api/v1/books/author                 # Get books by Author
GET    /api/v1/available                    # Get available Books
DELETE /api/v1/books/{id}                   # Delete book
```

### Booking Management
```http
POST   /api/v1/borrowing/borrow                                                 # Borrow a book
POST   /api/v1/borrowing/return                                                 # Return a book
GET    /api/v1/borrowing/records                                                # Get all loans
GET    /api/v1/borrowing/records/overdue                                        # Get loans by borrower
GET    /api/v1/borrowing/records/borrower/{borrowerId}                          # Get active loans
GET    /api/v1/borrowing/records/borrower/{borrowerId}/active                   # Get active loans
GET    /api/v1/borrowing/records/book/{bookId}                                  # Get active loans
GET    /api/v1/borrowing/records/active                                         # Get active loans
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default                                        |
|----------|-------------|------------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `development`                                  |
| `DATABASE_URL` | PostgreSQL connection URL | `r2dbc:postgresql://localhost:5435/library_db` |
| `DATABASE_USERNAME` | Database username | `library_user`                                 |
| `DATABASE_PASSWORD` | Database password | `library_pass`                             |
| `SERVER_PORT` | Application port | `8080`                                         |
| `FLYWAY_URL` | Flyway JDBC URL | `jdbc:postgresql://localhost:5432/library_db`  |

### Application Profiles

- **development**: Local development with debug logging
- **production**: Production-ready configuration
- **test**: Testing configuration with H2 database

## 🐳 Docker Setup

### Prerequisites
- Docker Engine 20.x or higher
- Docker Compose 2.x or higher

### Quick Start

1. **Clone the repository**
```bash
git clone <repository-url>
cd library-management-system
```

2. **Start the application**
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- Library API on port 8080
- Automatic database migration via Flyway

3. **Access the application**
- API Base URL: http://localhost:8080
- OpenAPI Documentation: http://localhost:8080/swagger-ui.html
- API Docs JSON: http://localhost:8080/v3/api-docs

### Docker Compose Services

```yaml
services:
  postgres:
    image: postgres:15-alpine
    ports: ["5435:5432"]
    
  library-api:
    build: .
    ports: ["8080:8080"]
    depends_on: [postgres]
```

## 🚀 Local Development

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 13+

### Setup Steps

1. **Start PostgreSQL**
```bash
# Using Docker
docker run --name postgres -e POSTGRES_DB=library_db -e POSTGRES_USER=library_user -e POSTGRES_PASSWORD=library_pass -p 5435:5432 -d postgres:15-alpine
```

2. **Run the application**
```bash
# Clean and compile
mvn clean compile

# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=development

# Or run the JAR
mvn package
java -jar target/library-0.0.1-SNAPSHOT.jar
```
3. **Run Via Docker for psql and application**
```bash
#through docker compose
docker-compose up --build
```

### API Testing
Use the Swagger UI at http://localhost:8080/swagger-ui.html or import the OpenAPI spec into Postman.

## 📋 Business Rules & Constraints

### Book Management
- Multiple books can have the same ISBN (different copies)
- Each book instance has a unique ID
- Books with same ISBN must have identical title and author
- Only available books can be borrowed

### Borrowing Rules
- One book (by ID) can only be borrowed by one person at a time
- Borrowers must return books before borrowing the same book again
- Active loans prevent book deletion
- Email addresses must be unique for borrowers

### Data Validation
- ISBN format validation (10 or 13 digits)
- Email format validation
- Required field validation
- String length constraints

## 🏗️ 12-Factor App Compliance

This application follows the 12-Factor App methodology:

1. **Codebase**: Single codebase in version control
2. **Dependencies**: Maven manages all dependencies
3. **Config**: Environment variables for configuration
4. **Backing Services**: PostgreSQL as attached resource
5. **Build/Release/Run**: Docker images for deployment
6. **Processes**: Stateless reactive application
7. **Port Binding**: Self-contained with embedded server
8. **Concurrency**: Reactive streams for scaling
9. **Disposability**: Graceful shutdown handling
10. **Dev/Prod Parity**: Docker ensures consistency
11. **Logs**: Structured logging to stdout
12. **Admin Processes**: Database migrations via Flyway

## 🔍 Health Checks & Monitoring

### Health Endpoints
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Logging
Structured JSON logging with configurable levels per environment.

## 🚨 Error Handling

The API returns consistent error responses:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Book is already borrowed",
  "path": "/api/v1/borrowing/borrow"
}
```

### HTTP Status Codes
- `200` - Success
- `201` - Created
- `400` - Bad Request (validation errors)
- `404` - Resource not found
- `409` - Conflict (business rule violation)
- `500` - Internal server error

## 📈 Performance Considerations

### Reactive Architecture Benefits
- **Non-blocking I/O**: Better resource utilization
- **Backpressure**: Automatic flow control
- **Scalability**: Handles high concurrent loads
- **Resource Efficiency**: Lower memory footprint

### Database Optimization
- Proper indexing on frequently queried fields
- Connection pooling via R2DBC
- Prepared statements for security and performance

## 🔒 Security Considerations

### Current Implementation
- Input validation and sanitization
- SQL injection prevention via parameterized queries
- Error message sanitization

### Future Enhancements
- JWT authentication
- Role-based authorization
- Rate limiting
- HTTPS enforcement

## 📚 API Usage Examples

### Register a Borrower
```bash
curl -X POST http://localhost:8080/api/v1/borrowers \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john.doe@email.com"}'
```

### Register a Book
```bash
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{"isbn": "9781234567890", "title": "Spring Boot Guide", "author": "Tech Author"}'
```

### Borrow a Book
```bash
curl -X POST http://localhost:8080/api/v1/borrowing/borrow \
  -H "Content-Type: application/json" \
  -d '{"borrowerId": 1, "bookId": 1}'
```

### Return a Book
```bash
curl -X POST http://localhost:8080/api/v1/borrowing/return \
  -H "Content-Type: application/json" \
  -d '{"borrowerId": 1, "bookId": 1}'
```

## 🛠️ Troubleshooting

### Common Issues

1. **Database Connection Failed**
    - Verify PostgreSQL is running
    - Check connection parameters in application.yml

2. **Port Already in Use**
    - Change `SERVER_PORT` environment variable
    - Kill process using the port: `lsof -ti:8080 | xargs kill`

3. **Migration Failures**
    - Check Flyway migration files in `src/main/resources/db/migration`
    - Verify database schema permissions

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes with tests
4. Submit a pull request

## 📞 Support

For questions or issues, please create an issue in the project repository.