package bms;

import java.time.LocalDate;
import java.util.*;

/** View layer: every web page is built here as an HTML string. */
public class Pages {

    public static String h(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    // ================= common layout =================
    public static String layout(String title, User u, String msg, String body) {
        StringBuilder s = new StringBuilder();
        s.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>")
         .append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
         .append("<title>").append(h(title)).append(" | TNBus</title>")
         .append("<link rel='stylesheet' href='/css/style.css'></head><body>");
        s.append("<header><div class='wrap bar'><a class='logo' href='/'>&#128652; TN<span>Bus</span></a><nav>")
         .append("<a href='/'>Home</a><a href='/search'>Search Buses</a><a href='/routes'>Routes</a>")
         .append("<a href='/schedule'>Schedule</a><a href='/fares'>Fares</a><a href='/offers'>Offers</a>")
         .append("<a href='/faq'>FAQ</a><a href='/about'>About</a><a href='/contact'>Contact</a>");
        if (u == null) {
            s.append("<a class='btn small' href='/login'>Login</a><a class='btn small alt' href='/register'>Register</a>");
        } else {
            s.append("<a href='/my-bookings'>My Bookings</a>");
            if (u.isAdmin()) s.append("<a href='/admin'>Admin</a>");
            s.append("<a class='btn small alt' href='/logout'>Logout (").append(h(u.username)).append(")</a>");
        }
        s.append("</nav></div></header>");
        if (msg != null && !msg.isBlank()) s.append("<div class='wrap'><div class='flash'>").append(h(msg)).append("</div></div>");
        s.append("<main class='wrap'>").append(body).append("</main>");
        s.append("<footer><div class='wrap foot'>")
         .append("<div><h4>TNBus</h4><p>Safe, simple and affordable bus travel across South India.</p></div>")
         .append("<div><h4>Quick Links</h4><a href='/search'>Search Buses</a><a href='/routes'>Routes</a><a href='/offers'>Offers</a><a href='/faq'>FAQ</a></div>")
         .append("<div><h4>Contact</h4><p>Madurai, Tamil Nadu</p><p>support@tnbus.example</p><p>Helpline: 1800-000-1234</p></div>")
         .append("</div><div class='copy'>&copy; 2026 TNBus Bus Management System &ndash; Academic Project</div></footer>")
         .append("</body></html>");
        return s.toString();
    }

    private static String cityOptions(Set<String> cities, String selected, String placeholder) {
        StringBuilder o = new StringBuilder("<option value=''>" + placeholder + "</option>");
        for (String c : cities)
            o.append("<option").append(c.equalsIgnoreCase(selected) ? " selected" : "").append(">").append(h(c)).append("</option>");
        return o.toString();
    }

    private static String busTable(List<Bus> list, boolean showBook) {
        if (list.isEmpty()) return "<p class='muted'>No buses found.</p>";
        StringBuilder t = new StringBuilder("<div class='scroll'><table><tr><th>Bus</th><th>From</th><th>To</th><th>Departs</th><th>Arrives</th><th>Type</th><th>Fare</th><th>Seats</th>")
                .append(showBook ? "<th></th>" : "").append("</tr>");
        for (Bus b : list) {
            t.append("<tr><td><b>").append(h(b.name)).append("</b></td><td>").append(h(b.from)).append("</td><td>")
             .append(h(b.to)).append("</td><td>").append(b.depart).append("</td><td>").append(b.arrive).append("</td><td>")
             .append(h(b.type)).append("</td><td>&#8377;").append(b.fare).append("</td><td>").append(b.seats).append("</td>");
            if (showBook) t.append("<td><a class='btn small' href='/book?busId=").append(b.id).append("'>Book</a></td>");
            t.append("</tr>");
        }
        return t.append("</table></div>").toString();
    }

    // ================= public pages =================
    public static String home(DataStore db) {
        long confirmed = db.bookings.stream().filter(b -> b.status.equals("CONFIRMED")).count();
        StringBuilder s = new StringBuilder();
        s.append("<section class='hero'><h1>Book Your Bus Ticket in Minutes</h1><p>Compare buses, pick your seat and travel with confidence.</p>")
         .append("<form class='searchbar' action='/search' method='get'>")
         .append("<select name='from'>").append(cityOptions(db.cities(), "", "From")).append("</select>")
         .append("<select name='to'>").append(cityOptions(db.cities(), "", "To")).append("</select>")
         .append("<button class='btn' type='submit'>Search Buses</button></form></section>");
        s.append("<section class='stats'><div><b>").append(db.buses.size()).append("</b><span>Bus Services</span></div>")
         .append("<div><b>").append(db.cities().size()).append("</b><span>Cities Covered</span></div>")
         .append("<div><b>").append(confirmed).append("</b><span>Tickets Booked</span></div>")
         .append("<div><b>").append(db.users.size()).append("</b><span>Registered Users</span></div></section>");
        s.append("<h2>Popular Routes</h2><div class='cards'>");
        String[][] pop = {{"Madurai", "Chennai"}, {"Madurai", "Coimbatore"}, {"Chennai", "Bangalore"}, {"Madurai", "Trichy"}};
        for (String[] r : pop)
            s.append("<a class='card' href='/search?from=").append(r[0]).append("&to=").append(r[1]).append("'><h3>")
             .append(r[0]).append(" &rarr; ").append(r[1]).append("</h3><p>View buses &amp; fares</p></a>");
        s.append("</div><h2>How It Works</h2><div class='cards'>")
         .append("<div class='card'><h3>1. Search</h3><p>Choose your source and destination.</p></div>")
         .append("<div class='card'><h3>2. Select Seat</h3><p>Pick your seats from the live seat map.</p></div>")
         .append("<div class='card'><h3>3. Confirm</h3><p>Enter passenger details and confirm.</p></div>")
         .append("<div class='card'><h3>4. Travel</h3><p>Show your e-ticket and enjoy the journey.</p></div></div>")
         .append("<h2>Why Choose TNBus?</h2><div class='cards'>")
         .append("<div class='card'><h3>&#128274; Secure</h3><p>Your account and booking data stay protected.</p></div>")
         .append("<div class='card'><h3>&#128176; Best Fares</h3><p>Transparent prices with no hidden charges.</p></div>")
         .append("<div class='card'><h3>&#9201; Fast Booking</h3><p>Book a ticket in under a minute.</p></div>")
         .append("<div class='card'><h3>&#128222; 24x7 Support</h3><p>Our helpline is always available.</p></div></div>")
         .append("<h2>What Passengers Say</h2><div class='cards'>")
         .append("<div class='card quote'><p>&ldquo;Very easy to book. The seat map is great!&rdquo;</p><small>&ndash; Arun, Madurai</small></div>")
         .append("<div class='card quote'><p>&ldquo;Clean buses and on-time departures.&rdquo;</p><small>&ndash; Divya, Chennai</small></div>")
         .append("<div class='card quote'><p>&ldquo;Cancelling a ticket took just one click.&rdquo;</p><small>&ndash; Karthik, Coimbatore</small></div></div>");
        return s.toString();
    }

    public static String about() {
        return "<h1>About Us</h1><p>TNBus is a Bus Management System developed as an academic project using <b>Java</b>. "
             + "It helps passengers search buses, book seats and manage tickets, and helps administrators manage buses and bookings.</p>"
             + "<h2>Our Mission</h2><p>To make intercity bus travel simple, transparent and reliable for everyone.</p>"
             + "<h2>Key Features</h2><ul><li>Search buses by source and destination</li><li>Live seat selection map</li>"
             + "<li>User registration and login</li><li>E-ticket generation and printing</li><li>Ticket cancellation with refund rules</li>"
             + "<li>Admin panel to add/remove buses and view all bookings</li><li>Feedback and contact form</li></ul>"
             + "<h2>Technology Used</h2><div class='scroll'><table><tr><th>Layer</th><th>Technology</th></tr>"
             + "<tr><td>Language</td><td>Java (JDK 11 or higher)</td></tr><tr><td>Web Server</td><td>com.sun.net.httpserver.HttpServer (built-in)</td></tr>"
             + "<tr><td>Front End</td><td>HTML5 and CSS3</td></tr><tr><td>Storage</td><td>Text files (data folder)</td></tr>"
             + "<tr><td>Design</td><td>MVC style: Model, DataStore, Pages</td></tr></table></div>"
             + "<h2>Team</h2><p>Developed by students of Thiagarajar College of Engineering, Madurai.</p>";
    }

    public static String routes(DataStore db) {
        Map<String, List<Bus>> map = new TreeMap<>();
        for (Bus b : db.buses) map.computeIfAbsent(b.from + " \u2192 " + b.to, k -> new ArrayList<>()).add(b);
        StringBuilder s = new StringBuilder("<h1>All Routes</h1><p>Click a route to see its buses.</p><div class='scroll'><table><tr><th>Route</th><th>Buses</th><th>Lowest Fare</th><th></th></tr>");
        for (Map.Entry<String, List<Bus>> e : map.entrySet()) {
            Bus f = e.getValue().get(0);
            int min = e.getValue().stream().mapToInt(x -> x.fare).min().orElse(0);
            s.append("<tr><td>").append(h(e.getKey())).append("</td><td>").append(e.getValue().size())
             .append("</td><td>&#8377;").append(min).append("</td><td><a class='btn small' href='/search?from=")
             .append(h(f.from)).append("&to=").append(h(f.to)).append("'>View</a></td></tr>");
        }
        return s.append("</table></div>").toString();
    }

    public static String search(DataStore db, String from, String to, boolean searched) {
        StringBuilder s = new StringBuilder("<h1>Search Buses</h1><form class='searchbar light' action='/search' method='get'>")
          .append("<select name='from'>").append(cityOptions(db.cities(), from, "Any source")).append("</select>")
          .append("<select name='to'>").append(cityOptions(db.cities(), to, "Any destination")).append("</select>")
          .append("<button class='btn'>Search</button></form>");
        List<Bus> r = db.searchBuses(from, to);
        s.append("<p class='muted'>").append(r.size()).append(" bus(es) found").append(searched ? "" : " (showing all)").append("</p>");
        return s.append(busTable(r, true)).toString();
    }

    public static String schedule(DataStore db) {
        List<Bus> sorted = new ArrayList<>(db.buses);
        sorted.sort(Comparator.comparing((Bus b) -> b.depart));
        return "<h1>Bus Schedule</h1><p>All services sorted by departure time.</p>" + busTable(sorted, true);
    }

    public static String fares(DataStore db) {
        List<Bus> sorted = new ArrayList<>(db.buses);
        sorted.sort(Comparator.comparingInt(b -> b.fare));
        return "<h1>Fares &amp; Cancellation Policy</h1><h2>Fare List (low to high)</h2>" + busTable(sorted, false)
             + "<h2>Cancellation &amp; Refund Policy</h2><div class='scroll'><table><tr><th>Cancelled before departure</th><th>Refund</th></tr>"
             + "<tr><td>More than 24 hours</td><td>90% of fare</td></tr><tr><td>12 to 24 hours</td><td>50% of fare</td></tr>"
             + "<tr><td>Less than 12 hours</td><td>No refund</td></tr></table></div>"
             + "<h2>Concessions</h2><ul><li>Children below 5 years: free (no seat)</li><li>Senior citizens (60+): 10% off on Non-AC buses</li><li>Students: 10% off on production of ID at boarding</li></ul>";
    }

    public static String offers() {
        return "<h1>Offers &amp; Discounts</h1><div class='cards'>"
             + "<div class='card offer'><h3>WELCOME10</h3><p>10% off on your first booking.</p></div>"
             + "<div class='card offer'><h3>WEEKEND50</h3><p>&#8377;50 off on weekend travel.</p></div>"
             + "<div class='card offer'><h3>GROUP5</h3><p>Book 5 or more seats and get 8% off.</p></div>"
             + "<div class='card offer'><h3>FESTIVE15</h3><p>15% off during festival seasons.</p></div></div>"
             + "<p class='muted'>Note: Offer codes are shown for demonstration in this academic project.</p>";
    }

    public static String faq() {
        String[][] q = {
            {"How do I book a ticket?", "Register or log in, search a bus, choose a date, select seats and confirm."},
            {"Can I cancel my ticket?", "Yes. Go to My Bookings and click Cancel. Refund depends on the time left before departure."},
            {"How many seats can I book at once?", "Up to 6 seats in a single booking."},
            {"Do I need to print my ticket?", "No. Showing the e-ticket on your phone is enough."},
            {"How far ahead can I book?", "Up to 60 days before the journey date."},
            {"I forgot my password. What now?", "Please contact the administrator through the Contact page."}};
        StringBuilder s = new StringBuilder("<h1>Frequently Asked Questions</h1>");
        for (String[] x : q) s.append("<details><summary>").append(x[0]).append("</summary><p>").append(x[1]).append("</p></details>");
        return s.toString();
    }

    public static String contact() {
        return "<h1>Contact Us</h1><div class='two'><div><h3>Head Office</h3><p>TNBus Transport Services<br>Madurai, Tamil Nadu, India</p>"
             + "<p>Email: support@tnbus.example</p><p>Helpline: 1800-000-1234</p><p>Hours: 24 x 7</p></div>"
             + "<form method='post' action='/contact' class='form'><h3>Send Feedback</h3>"
             + "<label>Name<input name='name' required></label><label>Email<input type='email' name='email' required></label>"
             + "<label>Message<textarea name='message' rows='5' required></textarea></label><button class='btn'>Submit</button></form></div>";
    }

    // ================= login / register =================
    public static String login(String error) {
        return "<div class='narrow'><h1>Login</h1>" + (error == null ? "" : "<div class='error'>" + h(error) + "</div>")
             + "<form method='post' action='/login' class='form'><label>Username<input name='username' required></label>"
             + "<label>Password<input type='password' name='password' required></label><button class='btn'>Login</button></form>"
             + "<p class='muted'>New user? <a href='/register'>Create an account</a></p>"
             + "<p class='muted'>Demo user: demo / demo123 &nbsp;|&nbsp; Admin: admin / admin123</p></div>";
    }

    public static String register(String error) {
        return "<div class='narrow'><h1>Create Account</h1>" + (error == null ? "" : "<div class='error'>" + h(error) + "</div>")
             + "<form method='post' action='/register' class='form'><label>Full Name<input name='fullName' required></label>"
             + "<label>Phone<input name='phone' pattern='[0-9]{10}' title='10 digit number' required></label>"
             + "<label>Username<input name='username' minlength='3' required></label>"
             + "<label>Password<input type='password' name='password' minlength='4' required></label><button class='btn'>Register</button></form></div>";
    }

    // ================= booking =================
    public static String book(DataStore db, Bus b, String date, String error) {
        StringBuilder s = new StringBuilder("<h1>Book: ").append(h(b.name)).append("</h1>")
          .append("<p class='muted'>").append(h(b.from)).append(" &rarr; ").append(h(b.to)).append(" | ").append(b.depart)
          .append(" - ").append(b.arrive).append(" | ").append(h(b.type)).append(" | &#8377;").append(b.fare).append(" per seat</p>");
        if (error != null) s.append("<div class='error'>").append(h(error)).append("</div>");
        LocalDate today = LocalDate.now();
        s.append("<form method='get' action='/book' class='inline'><input type='hidden' name='busId' value='").append(b.id).append("'>")
         .append("<label>Journey date <input type='date' name='date' min='").append(today).append("' max='").append(today.plusDays(60))
         .append("' value='").append(date == null ? "" : h(date)).append("' required></label><button class='btn small'>Show Seats</button></form>");
        if (date == null) return s.toString();

        Set<Integer> taken = db.bookedSeats(b.id, date);
        s.append("<form method='post' action='/book'><input type='hidden' name='busId' value='").append(b.id)
         .append("'><input type='hidden' name='date' value='").append(h(date)).append("'>")
         .append("<h3>Select seats for ").append(h(date)).append(" (max 6)</h3>")
         .append("<div class='legend'><span class='seat free'></span> Available <span class='seat sold'></span> Booked <span class='seat pick'></span> Selected</div>")
         .append("<div class='bus'><div class='driver'>&#128652; Driver</div><div class='seatgrid'>");
        for (int i = 1; i <= b.seats; i++) {
            boolean sold = taken.contains(i);
            s.append("<label class='seatbox'><input type='checkbox' name='seat' value='").append(i).append("'")
             .append(sold ? " disabled" : "").append("><span class='seat ").append(sold ? "sold" : "free").append("'>").append(i).append("</span></label>");
            if (i % 2 == 0 && i % 4 != 0) s.append("<span class='aisle'></span>");
        }
        s.append("</div></div><div class='form'><h3>Passenger Details</h3><label>Passenger Name<input name='passenger' required></label>")
         .append("<label>Age<input type='number' name='age' min='1' max='110' required></label>")
         .append("<label>Gender<select name='gender'><option>Male</option><option>Female</option><option>Other</option></select></label>")
         .append("<button class='btn'>Confirm Booking</button></div></form>");
        return s.toString();
    }

    public static String myBookings(DataStore db, User u) {
        List<Booking> list = db.userBookings(u.username);
        StringBuilder s = new StringBuilder("<h1>My Bookings</h1>");
        if (list.isEmpty()) return s.append("<p class='muted'>You have no bookings yet. <a href='/search'>Book a bus now</a>.</p>").toString();
        s.append("<div class='scroll'><table><tr><th>Ticket</th><th>Bus</th><th>Route</th><th>Date</th><th>Seat</th><th>Passenger</th><th>Status</th><th></th></tr>");
        for (Booking b : list) {
            Bus bus = db.findBus(b.busId);
            s.append("<tr><td>#").append(b.id).append("</td><td>").append(bus == null ? "(removed)" : h(bus.name)).append("</td><td>")
             .append(bus == null ? "-" : h(bus.from + " \u2192 " + bus.to)).append("</td><td>").append(h(b.date)).append("</td><td>")
             .append(b.seat).append("</td><td>").append(h(b.passenger)).append("</td><td><span class='tag ")
             .append(b.status.toLowerCase()).append("'>").append(b.status).append("</span></td><td>")
             .append("<a class='btn small' href='/ticket?id=").append(b.id).append("'>Ticket</a> ");
            if (b.status.equals("CONFIRMED"))
                s.append("<form class='inline' method='post' action='/cancel' onsubmit=\"return confirm('Cancel this ticket?')\">")
                 .append("<input type='hidden' name='id' value='").append(b.id).append("'><button class='btn small danger'>Cancel</button></form>");
            s.append("</td></tr>");
        }
        return s.append("</table></div>").toString();
    }

    public static String ticket(DataStore db, Booking b) {
        Bus bus = db.findBus(b.busId);
        String name = bus == null ? "(removed)" : bus.name;
        return "<div class='ticket'><div class='thead'>&#128652; TNBus E-Ticket <span class='tag " + b.status.toLowerCase() + "'>" + b.status + "</span></div>"
             + "<div class='tbody'><table class='plain'>"
             + "<tr><td>Ticket No</td><td><b>#" + b.id + "</b></td></tr><tr><td>Passenger</td><td>" + h(b.passenger) + " (" + b.age + ", " + h(b.gender) + ")</td></tr>"
             + "<tr><td>Bus</td><td>" + h(name) + (bus == null ? "" : " (" + h(bus.type) + ")") + "</td></tr>"
             + "<tr><td>Route</td><td>" + (bus == null ? "-" : h(bus.from + " \u2192 " + bus.to)) + "</td></tr>"
             + "<tr><td>Journey Date</td><td>" + h(b.date) + "</td></tr>"
             + "<tr><td>Departure / Arrival</td><td>" + (bus == null ? "-" : bus.depart + " / " + bus.arrive) + "</td></tr>"
             + "<tr><td>Seat No</td><td><b>" + b.seat + "</b></td></tr><tr><td>Fare</td><td>&#8377;" + (bus == null ? 0 : bus.fare) + "</td></tr>"
             + "<tr><td>Booked On</td><td>" + h(b.bookedOn) + "</td></tr></table></div>"
             + "<div class='tfoot'>Please reach the boarding point 15 minutes early. Carry a valid photo ID.</div></div>"
             + "<p class='noprint'><button class='btn' onclick='window.print()'>Print Ticket</button> <a class='btn alt' href='/my-bookings'>Back</a></p>";
    }

    // ================= admin =================
    private static String adminTabs() {
        return "<div class='tabs'><a href='/admin'>Dashboard</a><a href='/admin/buses'>Manage Buses</a><a href='/admin/bookings'>All Bookings</a>"
             + "<a href='/admin/users'>Users</a><a href='/admin/feedback'>Feedback</a></div>";
    }

    public static String adminHome(DataStore db) {
        long conf = db.bookings.stream().filter(b -> b.status.equals("CONFIRMED")).count();
        long canc = db.bookings.size() - conf;
        long revenue = 0;
        for (Booking b : db.bookings) if (b.status.equals("CONFIRMED")) { Bus x = db.findBus(b.busId); if (x != null) revenue += x.fare; }
        return "<h1>Admin Dashboard</h1>" + adminTabs() + "<section class='stats'>"
             + "<div><b>" + db.buses.size() + "</b><span>Buses</span></div><div><b>" + conf + "</b><span>Confirmed Tickets</span></div>"
             + "<div><b>" + canc + "</b><span>Cancelled</span></div><div><b>&#8377;" + revenue + "</b><span>Revenue</span></div></section>"
             + "<p class='muted'>Users: " + db.users.size() + " | Feedback received: " + db.feedback.size() + "</p>";
    }

    public static String adminBuses(DataStore db) {
        StringBuilder s = new StringBuilder("<h1>Manage Buses</h1>").append(adminTabs())
          .append("<h3>Add New Bus</h3><form method='post' action='/admin/add-bus' class='form grid'>")
          .append("<label>Bus Name<input name='name' required></label><label>From<input name='from' required></label>")
          .append("<label>To<input name='to' required></label><label>Departure<input type='time' name='depart' required></label>")
          .append("<label>Arrival<input type='time' name='arrive' required></label>")
          .append("<label>Type<select name='type'><option>AC Sleeper</option><option>AC Seater</option><option>Non-AC Seater</option></select></label>")
          .append("<label>Fare (&#8377;)<input type='number' name='fare' min='1' required></label>")
          .append("<label>Seats<input type='number' name='seats' min='4' max='60' value='40' required></label><button class='btn'>Add Bus</button></form>")
          .append("<h3>Existing Buses</h3><div class='scroll'><table><tr><th>ID</th><th>Bus</th><th>Route</th><th>Time</th><th>Type</th><th>Fare</th><th>Seats</th><th></th></tr>");
        for (Bus b : db.buses)
            s.append("<tr><td>").append(b.id).append("</td><td>").append(h(b.name)).append("</td><td>").append(h(b.from + " \u2192 " + b.to))
             .append("</td><td>").append(b.depart).append(" - ").append(b.arrive).append("</td><td>").append(h(b.type)).append("</td><td>&#8377;")
             .append(b.fare).append("</td><td>").append(b.seats).append("</td><td><form class='inline' method='post' action='/admin/delete-bus' onsubmit=\"return confirm('Delete this bus?')\">")
             .append("<input type='hidden' name='id' value='").append(b.id).append("'><button class='btn small danger'>Delete</button></form></td></tr>");
        return s.append("</table></div>").toString();
    }

    public static String adminBookings(DataStore db) {
        StringBuilder s = new StringBuilder("<h1>All Bookings</h1>").append(adminTabs())
          .append("<div class='scroll'><table><tr><th>Ticket</th><th>User</th><th>Bus</th><th>Date</th><th>Seat</th><th>Passenger</th><th>Status</th><th></th></tr>");
        List<Booking> rev = new ArrayList<>(db.bookings);
        Collections.reverse(rev);
        for (Booking b : rev) {
            Bus bus = db.findBus(b.busId);
            s.append("<tr><td>#").append(b.id).append("</td><td>").append(h(b.username)).append("</td><td>").append(bus == null ? "(removed)" : h(bus.name))
             .append("</td><td>").append(h(b.date)).append("</td><td>").append(b.seat).append("</td><td>").append(h(b.passenger))
             .append("</td><td><span class='tag ").append(b.status.toLowerCase()).append("'>").append(b.status).append("</span></td><td>");
            if (b.status.equals("CONFIRMED"))
                s.append("<form class='inline' method='post' action='/cancel'><input type='hidden' name='id' value='").append(b.id)
                 .append("'><button class='btn small danger'>Cancel</button></form>");
            s.append("</td></tr>");
        }
        return s.append("</table></div>").toString();
    }

    public static String adminUsers(DataStore db) {
        StringBuilder s = new StringBuilder("<h1>Registered Users</h1>").append(adminTabs())
          .append("<div class='scroll'><table><tr><th>Username</th><th>Name</th><th>Phone</th><th>Role</th></tr>");
        for (User u : db.users)
            s.append("<tr><td>").append(h(u.username)).append("</td><td>").append(h(u.fullName)).append("</td><td>").append(h(u.phone))
             .append("</td><td>").append(u.role).append("</td></tr>");
        return s.append("</table></div>").toString();
    }

    public static String adminFeedback(DataStore db) {
        StringBuilder s = new StringBuilder("<h1>Feedback</h1>").append(adminTabs());
        if (db.feedback.isEmpty()) return s.append("<p class='muted'>No feedback yet.</p>").toString();
        for (int i = db.feedback.size() - 1; i >= 0; i--) {
            String[] f = db.feedback.get(i);
            s.append("<div class='card'><b>").append(h(f[0])).append("</b> &lt;").append(h(f[1])).append("&gt; <small class='muted'>")
             .append(h(f[3])).append("</small><p>").append(h(f[2])).append("</p></div>");
        }
        return s.toString();
    }

    public static String notFound() {
        return "<div class='narrow'><h1>404 - Page Not Found</h1><p>The page you are looking for does not exist.</p><a class='btn' href='/'>Go Home</a></div>";
    }
}
