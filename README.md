# Daily Diary Web (Spring Boot + PWA)

A professional daily diary that works on desktop and mobile, and can be
installed on a phone like a normal app.

## Features
- Register / log in (BCrypt-hashed passwords, CSRF protection)
- Every user sees only their own entries
- Add, edit, delete entries (date, title, category, mood 1-5, notes)
- Search by keyword, category and date range
- Weekly dashboard with a 7-day mood chart
- Download all entries as a .txt file
- Installable PWA with offline page, responsive layout, dark mode

## Tech
Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, Thymeleaf,
H2 (development), PostgreSQL (production), Docker.

## Run locally
    mvn spring-boot:run
Open http://localhost:8080, create an account and start writing.
Local data is saved in the `data/` folder.

## Build
    mvn clean package
    java -jar target/daily-diary-web-1.0.0.jar

## Install on your phone
1. Open the HTTPS link of the deployed app in Chrome (Android) or Safari (iPhone).
2. Android: tap "Install app" (or menu -> Install app).
   iPhone: Share -> Add to Home Screen.
(Installing needs HTTPS. localhost also works on your computer.)

## Deploy on Render (free)
1. Push this folder to a GitHub repository (do NOT commit *.db or *.zip files).
2. On render.com: New -> PostgreSQL. Note the Hostname, Database, Username and
   Password. Create it in the SAME region as the web service.
3. New -> Web Service -> connect your GitHub repo -> Runtime: Docker.
4. Add environment variables (the URL must start with jdbc:postgresql://):
       SPRING_PROFILES_ACTIVE = prod
       DB_URL                 = jdbc:postgresql://HOST:5432/DBNAME
       DB_USER                = USER
       DB_PASSWORD            = PASSWORD
5. Deploy. Render gives you an https://your-app.onrender.com link.

Shortcut: New -> Blueprint -> pick this repo. render.yaml creates the web
service and the database and wires the variables automatically.

Troubleshooting: "Driver org.postgresql.Driver claims to not accept jdbcUrl"
means the datasource URL is wrong (for example still the H2 one, or missing
the jdbc: prefix). Check DB_URL and that SPRING_PROFILES_ACTIVE=prod.

Note: the free web service sleeps after inactivity, so the first load can take
about a minute. Free PostgreSQL plans on Render expire after 30 days, so back
up your data (use the Download button) or upgrade the database.

## Folder structure
    src/main/java/com/diary/
      model/        AppUser, DiaryEntry
      repository/   database queries
      service/      business logic
      dto/          form objects with validation
      controller/   web pages
      config/       security
    src/main/resources/
      templates/    Thymeleaf pages
      static/       css, js, icons, manifest.webmanifest, sw.js
