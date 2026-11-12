# HireTrack — Job Application Tracker

A beginner-friendly full-stack project built with HTML, CSS, JavaScript, Java, Spring Boot and MySQL.

## Requirements
- Java 17 or later
- Maven 3.6+ (or use the Maven wrapper if you add one)
- MySQL Server 8+

## 1. Create the database
Open MySQL Workbench or the MySQL command line and run:

```sql
CREATE DATABASE hiretrack;
```

## 2. Configure your database password
Open `src/main/resources/application.properties` and replace:

```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

with your local MySQL password. If your MySQL username is not `root`, update that too.

## 3. Run the application
Open a terminal in the folder containing `pom.xml` and run:

```bash
mvn spring-boot:run
```

On Windows, if Maven is not installed, install Maven or open the project in IntelliJ IDEA and run `HireTrackApplication.java`.

## 4. Open the website
Visit **http://localhost:8080**

Create an account, log in, and add job applications. The app and API are served from the same Spring Boot server, so no separate frontend server or CORS setup is required.

## Features
- Register, log in and log out
- Passwords stored as BCrypt hashes
- Session-based login
- Create, view, edit and delete applications
- Search by company or role
- Filter by application status
- Dashboard counts for total, in-progress, interviews and offers
- Responsive layout

## API endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `GET /api/applications`
- `POST /api/applications`
- `PUT /api/applications/{id}`
- `DELETE /api/applications/{id}`

## Notes
This is a learning/MVP project. Before production deployment, add HTTPS, stronger password policy, CSRF protection, rate limiting, secure environment-variable configuration, database backups and proper privacy/legal notices. Do not commit real passwords or secrets to GitHub.
