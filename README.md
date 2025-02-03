# RESTfulWeb
![image](https://github.com/user-attachments/assets/b37abbf4-9ee4-4183-ad46-6c06208eda56)

A web-based, database-backed file management system and data portal built using Spring Boot, Thymeleaf, JPA, and PostgreSQL. This project allows users to upload, view, search, and manage files (images, PDFs, Word documents) as well as manage locations and measurements with filtering functionality. The application includes both RESTful API endpoints and a user-friendly web interface.

## Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Installation & Setup](#installation--setup)
  - [Local Setup](#local-setup)
  - [Heroku Deployment](#heroku-deployment)
- [Usage](#usage)
  - [Authentication & Security](#authentication--security)
  - [File Upload & Viewing](#file-upload--viewing)
  - [Locations Management](#locations-management)
  - [Measurements Management](#measurements-management)
- [API Endpoints](#api-endpoints)
- [Contributing](#contributing)
- [License](#license)

## Features

- **User Interface:**  
  Web-based UI built with Thymeleaf and Bootstrap.
- **File Upload & Management:**  
  Supports uploading and storing images, PDFs, and Word documents in a PostgreSQL database.
- **Search & Filtering:**  
  Users can filter files, measurements, and locations using various criteria such as date (with automatic conversion to day start/end), measurement unit, and city name.
- **Soft Delete & Restore:**  
  Files, measurements, and locations can be soft-deleted (moved to trash) and restored or permanently deleted.
- **RESTful API:**  
  Provides endpoints for file retrieval, CRUD operations, and filtering.
- **Security:**  
  User authentication with Spring Security and role-based access control (e.g., admin-only endpoints).

## Technologies

- **Backend:** Java, Spring Boot, Spring MVC, Spring Data JPA, Spring Security  
- **Database:** PostgreSQL (local and Heroku Postgres)  
- **Frontend:** Thymeleaf, Bootstrap, Flatpickr (for date inputs)  
- **Build Tool:** Maven  
- **Deployment:** Heroku

## Project Structure

RESTfulWeb/ ├── src/ │ ├── main/ │ │ ├── java/com/wefky/RESTfulWeb/ │ │ │ ├── config/ # Security, Thymeleaf, and other configurations │ │ │ ├── controller/ # REST and Web controllers (for images, locations, measurements, etc.) │ │ │ ├── entity/ # JPA entities (Image, Location, Measurement, etc.) │ │ │ ├── repository/ # Spring Data repositories │ │ │ └── service/ # Service layer for business logic │ │ └── resources/ │ │ ├── application.properties │ │ └── templates/ # Thymeleaf HTML templates │ └── test/ ├── pom.xml └── README.md

## Installation & Setup

### Local Setup

1. **Prerequisites:**  
   - Java 11 (or later) installed  
   - Maven installed  
   - PostgreSQL installed and running locally  
   - Git installed

2. **Clone the Repository:**

   ```bash
   [git clone https://github.com/Wiiifiii/RESTfulWeb.git
   cd RESTfulWeb
3. Configure the Database:
Update your src/main/resources/application.properties with your local PostgreSQL settings:
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
4. Build and Run:
mvn clean install
mvn spring-boot:run
The application should be available at http://localhost:8080.

Heroku Deployment
1. Heroku CLI:
Install the Heroku CLI.
Follow Heroku CLI installation instructions.
2. Create a Heroku App:
   heroku create spring-restful-app
3. Add Heroku Postgres:
   Heroku automatically sets DATABASE_URL. Also, set the Spring profile:
   heroku addons:create heroku-postgresql:hobby-dev --spring-restful-app
5. Set Environment Variables:
   git push heroku main
7. Deploy to Heroku:
8. Open the App:
   heroku open --app spring-restful-app
Usage
Authentication & Security
Login Required:
Users must log in to access most features.
Role-based Access:
Certain actions (e.g., permanent deletion) are restricted to admin users.
File Upload & Viewing
File Upload:
Upload images, PDFs, and Word documents via the web interface.
File Viewing:
Files are listed in the UI; deleted files can be viewed from the trash view.
Locations Management
Add/Edit/Delete Locations:
Manage locations (city, postal code, latitude, longitude) via the Locations section.
Filtering:
Filter locations by city name, postal code, etc.
Measurements Management
Auto-Set Timestamp:
When a new measurement is created, the timestamp is automatically set to the current date and time.
Filtering:
Filter Inputs: Users enter a start and end date in dd/MM/yyyy format.
Backend Conversion: The backend converts the start date to the beginning of the day (00:00) and the end date to the end of the day (23:59:59).
Filter Fields: Measurements can be filtered by measurement unit and city name.
Trash:
Deleted measurements are shown in a trash view where they can be restored or permanently deleted.
API Endpoints
The application exposes a secured RESTful API. Some key endpoints include:

Images:

GET /api/images – Retrieve active images (filterable).
GET /api/images/{id}/file – Retrieve file content for a non-deleted image.
GET /api/images/{id}/file-all – Retrieve file content even if the image is deleted.
Measurements:

GET /api/measurements – Retrieve active measurements with optional filters (measurement unit, date range, city).
POST /api/measurements – Create a new measurement.
PUT /api/measurements/{id} – Update an existing measurement.
DELETE /api/measurements/{id} – Soft delete a measurement.
POST /api/measurements/{id}/restore – Restore a soft-deleted measurement.
DELETE /api/measurements/{id}/permanent – Permanently delete a measurement (Admin only).
Locations:
Similar CRUD endpoints are provided for locations.

Contributing
Contributions are welcome! Please fork the repository and submit pull requests for improvements or bug fixes. Ensure that you follow the project's code style and include tests where applicable.

License
This project is licensed under the MIT License.



