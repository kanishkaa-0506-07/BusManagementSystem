# TNBus - Bus Management System

A web-based Bus Management System built with **Java**, HTML and CSS.
Passengers can search buses, select seats and book tickets. Admins can manage buses and bookings.
No database or external library is needed, only the JDK.

## Features

**Passenger**
- Register and login
- Search buses by source and destination
- View routes, schedule, fares and offers
- Live seat selection map (up to 6 seats per booking)
- Book and cancel tickets with refund rules
- View and print e-ticket
- Send feedback through the contact page

**Admin**
- Dashboard with tickets, cancellations and revenue
- Add and delete buses
- View all bookings and cancel them
- View registered users and feedback

## Technologies Used

| Layer | Technology |
|-------|------------|
| Language | Java (JDK 11 or higher) |
| Web server | com.sun.net.httpserver.HttpServer (built-in) |
| Front end | HTML5, CSS3 |
| Storage | Text files in the `data` folder |
| Session | Cookies |

## Project Structure

```
BusManagementSystem/
├── src/bms/
│   ├── Main.java        (server and URL routing)
│   ├── Pages.java       (all HTML pages)
│   ├── DataStore.java   (logic and file storage)
│   ├── Bus.java         (model)
│   ├── Booking.java     (model)
│   └── User.java        (model)
├── static/css/style.css (styling)
├── data/                (saved data, created automatically)
├── run.bat              (run on Windows)
├── run.sh               (run on Linux/Mac)
└── README.md
```

## How to Run in VS Code

1. Install JDK 11 or higher and the **Extension Pack for Java** in VS Code.
2. Open the **BusManagementSystem** folder using **File > Open Folder**.
3. Open the terminal (Ctrl + `) and run:

```
javac -d out src/bms/*.java
java -cp out bms.Main
```

4. Open **http://localhost:8080** in your browser.
5. Press **Ctrl + C** in the terminal to stop the server.

To use a different port: `java -cp out bms.Main 9090`

## Login Details

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| User | demo | demo123 |

## Website Pages

| Page | URL |
|------|-----|
| Home | / |
| About | /about |
| Routes | /routes |
| Search Buses | /search |
| Schedule | /schedule |
| Fares and Refund Policy | /fares |
| Offers | /offers |
| FAQ | /faq |
| Contact | /contact |
| Login / Register | /login, /register |
| Book Ticket | /book |
| My Bookings | /my-bookings |
| E-Ticket | /ticket |
| Admin Dashboard | /admin |
| Manage Buses | /admin/buses |
| All Bookings | /admin/bookings |
| Users | /admin/users |
| Feedback | /admin/feedback |

## Refund Policy

| Cancelled before departure | Refund |
|----------------------------|--------|
| More than 24 hours | 90% |
| 12 to 24 hours | 50% |
| Less than 12 hours | No refund |

## Java Concepts Used

- Object-oriented programming (classes, objects, encapsulation)
- Collections (ArrayList, HashMap, HashSet, TreeSet)
- File handling (java.nio.file)
- Exception handling (try-catch)
- Synchronized methods (prevents double booking of a seat)
- Streams and lambda expressions
- Networking (HttpServer, cookies, sessions)

## Future Enhancements

- Use MySQL with JDBC instead of text files
- Online payment gateway
- SMS and email confirmation
- Password encryption

## Author

Developed as an academic project at Thiagarajar College of Engineering, Madurai.
