# TNBus - Bus Management System (Java)

A complete web-based Bus Management System written in **pure Java**.
No Maven, Tomcat, or database needed - only the JDK (version 11 or higher).

## How to Run

**Windows:** double-click `run.bat`
**Linux / macOS:** `chmod +x run.sh && ./run.sh`

Then open **http://localhost:8080** in your browser.

Manual way:
```
javac -d out src/bms/*.java
java -cp out bms.Main
```

## Login Details
| Role  | Username | Password |
|-------|----------|----------|
| Admin | admin    | admin123 |
| User  | demo     | demo123  |

## Website Pages (21 pages)
| # | Page | URL |
|---|------|-----|
| 1 | Home | / |
| 2 | About Us | /about |
| 3 | All Routes | /routes |
| 4 | Search Buses | /search |
| 5 | Bus Schedule | /schedule |
| 6 | Fares and Cancellation Policy | /fares |
| 7 | Offers | /offers |
| 8 | FAQ | /faq |
| 9 | Contact / Feedback | /contact |
| 10 | Login | /login |
| 11 | Register | /register |
| 12 | Book Ticket (seat map) | /book |
| 13 | My Bookings | /my-bookings |
| 14 | E-Ticket (printable) | /ticket |
| 15 | Admin Dashboard | /admin |
| 16 | Admin - Manage Buses | /admin/buses |
| 17 | Admin - All Bookings | /admin/bookings |
| 18 | Admin - Users | /admin/users |
| 19 | Admin - Feedback | /admin/feedback |
| 20 | 404 Page | any wrong URL |
| 21 | Logout | /logout |

## Project Structure
| File | Purpose |
|------|---------|
| src/bms/Main.java | Web server + URL routing (Controller) |
| src/bms/Pages.java | All HTML pages (View) |
| src/bms/DataStore.java | Business logic + file storage (Data layer) |
| src/bms/Bus.java, Booking.java, User.java | Model classes |
| static/css/style.css | Styling |
| data/*.txt | Saved data (created automatically) |

## Java Concepts Used (for viva)
- OOP: classes, objects, encapsulation, constructors
- Collections: ArrayList, HashMap, HashSet, TreeSet
- File I/O: java.nio.file (Files.readAllLines / Files.write)
- Exception handling: try-catch
- Synchronization: `synchronized` methods (safe seat booking)
- Streams and lambdas
- Networking: built-in HttpServer, sessions with cookies

## Ideas to Extend
- Replace text files with MySQL using JDBC
- Add online payment gateway
- Add SMS / email confirmation
