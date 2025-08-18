# Business Appointment Scheduler 🗓️

Business Appointment Scheduler is a prototype appointment management system for businesses.
It allows customers to book appointments online, and business owners to manage services,
opening hours, and upcoming appointments through an admin dashboard.

---

## 🚀 Features

### Customer Module
- View available services.
- Book appointments based on availability.
- Book as a guest (with full details form).
- Book as a registered user (without filling details every time).
- View booked appointments (for registered users).

### Admin Module
- Log in to the admin dashboard.
- Manage opening hours.
- Add/Edit/Delete services.
- View future appointments.
- Edit homepage content.
- Export appointments to XML.

### Other Features
- Email notifications when booking and on appointment day at 7 AM.
- Asynchronous Operations
- Responsive and clean interface (Bootstrap + Thymeleaf).
- Forms with validation.

---

## 🛠️ Technologies

- **Java 17**
- **Spring Boot 3.5** — REST, business logic, configuration
- **Spring Security** — authentication & authorization
- **Spring Data JPA (Hibernate)** — ORM
- **MySQL / H2 (for tests)** — database
- **Thymeleaf** — template engine
- **Bootstrap 5** — responsive UI
- **JavaMailSender** — email support
- **JUnit 5 + Spring Boot Test** — testing

---

## 📂 Project Structure

```plaintext
business_appointment_scheduler/
 ├── src/
 │   ├── main/
 │   │   ├── java/com/javaworkshop/business_scheduler/
 │   │   │   ├── controller/    # Controllers
 │   │   │   ├── service/       # Business logic
 │   │   │   ├── repository/    # JPA repositories
 │   │   │   ├── model/         # Entities
 │   │   │   └── util/          # Utilities
 │   │   └── resources/
 │   │       ├── templates/     # Thymeleaf HTML templates
 │   │       ├── static/        # CSS, JS, images
 │   │       └── application.properties
 │   └── test/
 │       └── java/...           # Tests (Web, Service, Repository)
 └── pom.xml
 ```

---


## ⚙️ Installation & Run

1. **Clone the repo**
   ```bash
   git clone https://github.com/gallevi10/business_appointment_scheduler.git
   cd business_appointment_scheduler

 3. **Set environment variables**
    ```bash
    # Database
    export DB_URL="your-db-url"
    export DATABASE_USER="your-db-user"
    export DATABASE_PASS="your-db-password"
  
    # Mail
    export MAIL_USER="your-app-email"
    export MAIL_PASS="your-app-password"

 4. **Run the project**
    ```bash
    ./mvnw spring-boot:run
  Or use IntelliJ → Run BusinessSchedulerApplication.

5. **Access the system**
   - Customer: http://localhost:8080/customer-dashboard
   - Owner: http://localhost:8080/owner-dashboard


---


## 🧪 Testing

- Tests include:
  - Repository layer (JPA)
  - Service layer
  - Controller layer (MockMvc)
- Run all tests:
   ```bash
   ./mvnw test
**In the test profile, H2 database is used instead of MySQL, and email sending is mocked.**

---


## 👨‍💻 Author

Gal Levi
B.Sc Computer Science, The Open University
