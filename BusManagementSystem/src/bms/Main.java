package bms;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Controller: starts the web server and routes each URL to the right page/action. */
public class Main {
    static final DataStore db = new DataStore("data");
    static final Map<String, String> sessions = new ConcurrentHashMap<>(); // sessionId -> username

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", Main::handle);
        server.start();
        System.out.println("TNBus Bus Management System running at http://localhost:" + port);
        System.out.println("Admin login: admin / admin123   |   Demo user: demo / demo123");
    }

    // ---------------- request helpers ----------------
    static class Req {
        Map<String, List<String>> params = new HashMap<>();
        String get(String k) { List<String> l = params.get(k); return l == null || l.isEmpty() ? null : l.get(0); }
        List<String> all(String k) { return params.getOrDefault(k, new ArrayList<>()); }
        int num(String k, int def) { try { return Integer.parseInt(get(k).trim()); } catch (Exception e) { return def; } }
    }

    static void parse(String q, Req r) {
        if (q == null || q.isEmpty()) return;
        for (String pair : q.split("&")) {
            int i = pair.indexOf('=');
            String k = URLDecoder.decode(i < 0 ? pair : pair.substring(0, i), StandardCharsets.UTF_8);
            String v = i < 0 ? "" : URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8);
            r.params.computeIfAbsent(k, x -> new ArrayList<>()).add(v);
        }
    }

    static User currentUser(HttpExchange ex) {
        String cookie = ex.getRequestHeaders().getFirst("Cookie");
        if (cookie == null) return null;
        for (String c : cookie.split(";")) {
            c = c.trim();
            if (c.startsWith("SID=")) {
                String name = sessions.get(c.substring(4));
                return name == null ? null : db.findUser(name);
            }
        }
        return null;
    }

    static void send(HttpExchange ex, int code, String type, byte[] data) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, data.length);
        ex.getResponseBody().write(data);
        ex.close();
    }

    static void html(HttpExchange ex, User u, String title, String msg, String body) throws IOException {
        send(ex, 200, "text/html; charset=UTF-8", Pages.layout(title, u, msg, body).getBytes(StandardCharsets.UTF_8));
    }

    static void redirect(HttpExchange ex, String url) throws IOException {
        ex.getResponseHeaders().set("Location", url);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    static String enc(String s) { return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8); }

    // ---------------- main router ----------------
    static void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            boolean post = ex.getRequestMethod().equalsIgnoreCase("POST");
            Req r = new Req();
            parse(ex.getRequestURI().getRawQuery(), r);
            if (post) parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), r);
            User u = currentUser(ex);
            String msg = r.get("msg");

            if (path.equals("/css/style.css")) {
                Path f = Paths.get("static/css/style.css");
                send(ex, 200, "text/css", Files.readAllBytes(f));
                return;
            }

            switch (path) {
                case "/": html(ex, u, "Home", msg, Pages.home(db)); break;
                case "/about": html(ex, u, "About", msg, Pages.about()); break;
                case "/routes": html(ex, u, "Routes", msg, Pages.routes(db)); break;
                case "/search": html(ex, u, "Search Buses", msg, Pages.search(db, r.get("from"), r.get("to"), r.get("from") != null || r.get("to") != null)); break;
                case "/schedule": html(ex, u, "Schedule", msg, Pages.schedule(db)); break;
                case "/fares": html(ex, u, "Fares", msg, Pages.fares(db)); break;
                case "/offers": html(ex, u, "Offers", msg, Pages.offers()); break;
                case "/faq": html(ex, u, "FAQ", msg, Pages.faq()); break;
                case "/contact":
                    if (post) {
                        db.addFeedback(r.get("name"), r.get("email"), r.get("message"));
                        redirect(ex, "/contact?msg=" + enc("Thank you! Your feedback has been submitted."));
                    } else html(ex, u, "Contact", msg, Pages.contact());
                    break;

                case "/login":
                    if (post) {
                        User x = db.login(r.get("username"), r.get("password"));
                        if (x == null) { html(ex, null, "Login", null, Pages.login("Invalid username or password.")); break; }
                        String sid = UUID.randomUUID().toString();
                        sessions.put(sid, x.username);
                        ex.getResponseHeaders().add("Set-Cookie", "SID=" + sid + "; Path=/; HttpOnly");
                        redirect(ex, x.isAdmin() ? "/admin" : "/my-bookings?msg=" + enc("Welcome, " + x.fullName));
                    } else html(ex, u, "Login", msg, Pages.login(null));
                    break;
                case "/register":
                    if (post) {
                        String err = null;
                        String phone = r.get("phone") == null ? "" : r.get("phone").trim();
                        if (!phone.matches("\\d{10}")) err = "Phone must be 10 digits.";
                        else if (db.findUser(r.get("username")) != null) err = "Username already taken.";
                        else db.register(r.get("username"), r.get("password"), r.get("fullName"), phone);
                        if (err != null) html(ex, null, "Register", null, Pages.register(err));
                        else redirect(ex, "/login?msg=" + enc("Account created. Please login."));
                    } else html(ex, u, "Register", msg, Pages.register(null));
                    break;
                case "/logout": {
                    String cookie = ex.getRequestHeaders().getFirst("Cookie");
                    if (cookie != null) for (String c : cookie.split(";")) if (c.trim().startsWith("SID=")) sessions.remove(c.trim().substring(4));
                    ex.getResponseHeaders().add("Set-Cookie", "SID=; Path=/; Max-Age=0");
                    redirect(ex, "/?msg=" + enc("You have been logged out."));
                    break;
                }

                // ---------- user area ----------
                case "/book": {
                    if (u == null) { redirect(ex, "/login?msg=" + enc("Please login to book tickets.")); break; }
                    Bus bus = db.findBus(r.num("busId", -1));
                    if (bus == null) { redirect(ex, "/search?msg=" + enc("Bus not found.")); break; }
                    String date = r.get("date");
                    if (!post) { html(ex, u, "Book Ticket", msg, Pages.book(db, bus, validDate(date) ? date : null, null)); break; }
                    List<Integer> seats = new ArrayList<>();
                    for (String s : r.all("seat")) try { int n = Integer.parseInt(s); if (n >= 1 && n <= bus.seats && !seats.contains(n)) seats.add(n); } catch (Exception ignored) { }
                    String err = null;
                    if (!validDate(date)) err = "Please choose a valid journey date.";
                    else if (seats.isEmpty()) err = "Select at least one seat.";
                    else if (seats.size() > 6) err = "You can book at most 6 seats.";
                    else if (r.get("passenger") == null || r.get("passenger").isBlank()) err = "Passenger name is required.";
                    int age = r.num("age", 0);
                    if (err == null && (age < 1 || age > 110)) err = "Enter a valid age.";
                    List<Booking> made = err != null ? null : db.book(bus.id, u.username, r.get("passenger"), age, r.get("gender"), date, seats);
                    if (err == null && made == null) err = "Sorry, one of the seats was just booked. Please choose again.";
                    if (err != null) { html(ex, u, "Book Ticket", null, Pages.book(db, bus, validDate(date) ? date : null, err)); break; }
                    redirect(ex, "/my-bookings?msg=" + enc("Booking confirmed! " + made.size() + " seat(s) booked. Total fare Rs. " + (made.size() * bus.fare)));
                    break;
                }
                case "/my-bookings":
                    if (u == null) { redirect(ex, "/login?msg=" + enc("Please login first.")); break; }
                    html(ex, u, "My Bookings", msg, Pages.myBookings(db, u));
                    break;
                case "/ticket": {
                    if (u == null) { redirect(ex, "/login?msg=" + enc("Please login first.")); break; }
                    Booking b = db.findBooking(r.num("id", -1));
                    if (b == null || (!b.username.equals(u.username) && !u.isAdmin())) { html(ex, u, "Ticket", "Ticket not found.", Pages.notFound()); break; }
                    html(ex, u, "E-Ticket", msg, Pages.ticket(db, b));
                    break;
                }
                case "/cancel": {
                    if (u == null || !post) { redirect(ex, "/login"); break; }
                    int id = r.num("id", -1);
                    Booking b = db.findBooking(id);
                    String refundInfo = "";
                    if (b != null) refundInfo = refundText(b);
                    boolean ok = db.cancel(id, u.username, u.isAdmin());
                    redirect(ex, (u.isAdmin() ? "/admin/bookings" : "/my-bookings") + "?msg=" + enc(ok ? "Ticket #" + id + " cancelled. " + refundInfo : "Could not cancel this ticket."));
                    break;
                }

                // ---------- admin area ----------
                case "/admin": if (admin(ex, u)) html(ex, u, "Admin", msg, Pages.adminHome(db)); break;
                case "/admin/buses": if (admin(ex, u)) html(ex, u, "Manage Buses", msg, Pages.adminBuses(db)); break;
                case "/admin/bookings": if (admin(ex, u)) html(ex, u, "All Bookings", msg, Pages.adminBookings(db)); break;
                case "/admin/users": if (admin(ex, u)) html(ex, u, "Users", msg, Pages.adminUsers(db)); break;
                case "/admin/feedback": if (admin(ex, u)) html(ex, u, "Feedback", msg, Pages.adminFeedback(db)); break;
                case "/admin/add-bus":
                    if (admin(ex, u) && post) {
                        int fare = r.num("fare", 0), seats = r.num("seats", 0);
                        if (fare < 1 || seats < 4 || seats > 60) { redirect(ex, "/admin/buses?msg=" + enc("Invalid fare or seats.")); break; }
                        db.addBus(new Bus(0, DataStore.clean(r.get("name")), DataStore.clean(r.get("from")), DataStore.clean(r.get("to")),
                                r.get("depart"), r.get("arrive"), r.get("type"), fare, seats));
                        redirect(ex, "/admin/buses?msg=" + enc("Bus added successfully."));
                    }
                    break;
                case "/admin/delete-bus":
                    if (admin(ex, u) && post) {
                        boolean ok = db.deleteBus(r.num("id", -1));
                        redirect(ex, "/admin/buses?msg=" + enc(ok ? "Bus deleted." : "Bus not found."));
                    }
                    break;

                default:
                    send(ex, 404, "text/html; charset=UTF-8", Pages.layout("Not Found", u, null, Pages.notFound()).getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, 500, "text/plain; charset=UTF-8", ("Server error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    static boolean admin(HttpExchange ex, User u) throws IOException {
        if (u == null) { redirect(ex, "/login?msg=" + enc("Please login as admin.")); return false; }
        if (!u.isAdmin()) { redirect(ex, "/?msg=" + enc("Admin access only.")); return false; }
        return true;
    }

    static boolean validDate(String d) {
        if (d == null) return false;
        try {
            LocalDate x = LocalDate.parse(d);
            return !x.isBefore(LocalDate.now()) && !x.isAfter(LocalDate.now().plusDays(60));
        } catch (Exception e) { return false; }
    }

    /** Refund rule: >24h = 90%, 12-24h = 50%, <12h = 0% */
    static String refundText(Booking b) {
        Bus bus = db.findBus(b.busId);
        if (bus == null) return "";
        try {
            LocalDateTime dep = LocalDateTime.of(LocalDate.parse(b.date), LocalTime.parse(bus.depart));
            long hours = Duration.between(LocalDateTime.now(), dep).toHours();
            int pct = hours > 24 ? 90 : hours >= 12 ? 50 : 0;
            return "Refund: " + pct + "% = Rs. " + (bus.fare * pct / 100);
        } catch (Exception e) { return ""; }
    }
}
