package bms;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/** Data layer: keeps everything in memory and saves to text files in /data. */
public class DataStore {
    private final Path busFile, bookingFile, userFile, feedbackFile;
    public final List<Bus> buses = new ArrayList<>();
    public final List<Booking> bookings = new ArrayList<>();
    public final List<User> users = new ArrayList<>();
    public final List<String[]> feedback = new ArrayList<>(); // name, email, message, time

    public DataStore(String dir) {
        Path d = Paths.get(dir);
        try { Files.createDirectories(d); } catch (IOException e) { throw new RuntimeException(e); }
        busFile = d.resolve("buses.txt");
        bookingFile = d.resolve("bookings.txt");
        userFile = d.resolve("users.txt");
        feedbackFile = d.resolve("feedback.txt");
        load();
    }

    // ---------- helpers ----------
    public static String clean(String s) {
        return s == null ? "" : s.replace("|", "/").replace("\r", " ").replace("\n", " ").trim();
    }

    private List<String> lines(Path f) {
        try { return Files.exists(f) ? Files.readAllLines(f) : new ArrayList<>(); }
        catch (IOException e) { return new ArrayList<>(); }
    }

    private void write(Path f, List<String> lines) {
        try { Files.write(f, lines); } catch (IOException e) { System.out.println("Save error: " + e); }
    }

    private void load() {
        for (String l : lines(busFile)) if (!l.isBlank()) buses.add(Bus.fromLine(l));
        for (String l : lines(bookingFile)) if (!l.isBlank()) bookings.add(Booking.fromLine(l));
        for (String l : lines(userFile)) if (!l.isBlank()) users.add(User.fromLine(l));
        for (String l : lines(feedbackFile)) if (!l.isBlank()) feedback.add(l.split("\\|", -1));
        if (users.isEmpty()) {
            users.add(new User("admin", "admin123", "System Administrator", "9000000000", "ADMIN"));
            users.add(new User("demo", "demo123", "Demo Passenger", "9111111111", "USER"));
            saveUsers();
        }
        if (buses.isEmpty()) { seedBuses(); saveBuses(); }
    }

    private void seedBuses() {
        Object[][] s = {
            {"Madurai Express", "Madurai", "Chennai", "21:00", "04:30", "AC Sleeper", 950, 40},
            {"Pandian Travels", "Madurai", "Chennai", "22:30", "05:45", "Non-AC Seater", 520, 44},
            {"Meenakshi Deluxe", "Madurai", "Coimbatore", "06:00", "11:30", "AC Seater", 480, 40},
            {"Kovai Line", "Coimbatore", "Madurai", "14:00", "19:30", "Non-AC Seater", 350, 44},
            {"Rock Fort Super", "Madurai", "Trichy", "07:15", "10:15", "Non-AC Seater", 190, 48},
            {"Cauvery Travels", "Trichy", "Chennai", "23:00", "04:30", "AC Sleeper", 780, 36},
            {"Marina Star", "Chennai", "Madurai", "20:00", "03:30", "AC Sleeper", 980, 40},
            {"Garden City Volvo", "Chennai", "Bangalore", "22:00", "04:00", "AC Seater", 850, 40},
            {"Silicon Link", "Bangalore", "Chennai", "23:30", "05:30", "AC Seater", 870, 40},
            {"Temple City", "Madurai", "Rameswaram", "05:30", "09:30", "Non-AC Seater", 210, 48},
            {"Southern Pearl", "Madurai", "Kanyakumari", "08:00", "13:30", "AC Seater", 400, 40},
            {"Hill Queen", "Coimbatore", "Ooty", "09:00", "12:00", "Non-AC Seater", 150, 44},
        };
        int id = 1;
        for (Object[] r : s)
            buses.add(new Bus(id++, (String) r[0], (String) r[1], (String) r[2], (String) r[3],
                    (String) r[4], (String) r[5], (Integer) r[6], (Integer) r[7]));
    }

    private void saveBuses() { write(busFile, buses.stream().map(Bus::toLine).collect(Collectors.toList())); }
    private void saveBookings() { write(bookingFile, bookings.stream().map(Booking::toLine).collect(Collectors.toList())); }
    private void saveUsers() { write(userFile, users.stream().map(User::toLine).collect(Collectors.toList())); }
    private void saveFeedback() { write(feedbackFile, feedback.stream().map(a -> String.join("|", a)).collect(Collectors.toList())); }

    // ---------- buses ----------
    public synchronized Bus findBus(int id) {
        for (Bus b : buses) if (b.id == id) return b;
        return null;
    }

    public synchronized List<Bus> searchBuses(String from, String to) {
        List<Bus> out = new ArrayList<>();
        for (Bus b : buses) {
            boolean f = from == null || from.isBlank() || b.from.equalsIgnoreCase(from.trim());
            boolean t = to == null || to.isBlank() || b.to.equalsIgnoreCase(to.trim());
            if (f && t) out.add(b);
        }
        return out;
    }

    public synchronized Set<String> cities() {
        TreeSet<String> c = new TreeSet<>();
        for (Bus b : buses) { c.add(b.from); c.add(b.to); }
        return c;
    }

    public synchronized void addBus(Bus b) {
        int max = 0;
        for (Bus x : buses) max = Math.max(max, x.id);
        b.id = max + 1;
        buses.add(b);
        saveBuses();
    }

    public synchronized boolean deleteBus(int id) {
        boolean r = buses.removeIf(b -> b.id == id);
        if (r) saveBuses();
        return r;
    }

    // ---------- bookings ----------
    public synchronized Set<Integer> bookedSeats(int busId, String date) {
        Set<Integer> s = new HashSet<>();
        for (Booking b : bookings)
            if (b.busId == busId && b.date.equals(date) && b.status.equals("CONFIRMED")) s.add(b.seat);
        return s;
    }

    /** Books all seats or none. Returns the list of new bookings, or null if a seat is taken. */
    public synchronized List<Booking> book(int busId, String user, String name, int age, String gender,
                                           String date, List<Integer> seats) {
        Set<Integer> taken = bookedSeats(busId, date);
        for (int s : seats) if (taken.contains(s)) return null;
        int max = 1000;
        for (Booking b : bookings) max = Math.max(max, b.id);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        List<Booking> made = new ArrayList<>();
        for (int s : seats) {
            Booking b = new Booking(++max, busId, user, clean(name), age, gender, s, date, "CONFIRMED", now);
            bookings.add(b);
            made.add(b);
        }
        saveBookings();
        return made;
    }

    public synchronized Booking findBooking(int id) {
        for (Booking b : bookings) if (b.id == id) return b;
        return null;
    }

    public synchronized boolean cancel(int id, String user, boolean isAdmin) {
        Booking b = findBooking(id);
        if (b == null || !b.status.equals("CONFIRMED")) return false;
        if (!isAdmin && !b.username.equals(user)) return false;
        b.status = "CANCELLED";
        saveBookings();
        return true;
    }

    public synchronized List<Booking> userBookings(String user) {
        List<Booking> out = new ArrayList<>();
        for (Booking b : bookings) if (b.username.equals(user)) out.add(b);
        Collections.reverse(out);
        return out;
    }

    // ---------- users ----------
    public synchronized User login(String u, String p) {
        for (User x : users) if (x.username.equalsIgnoreCase(u) && x.password.equals(p)) return x;
        return null;
    }

    public synchronized User findUser(String u) {
        if (u == null) return null;
        for (User x : users) if (x.username.equalsIgnoreCase(u)) return x;
        return null;
    }

    public synchronized boolean register(String u, String p, String name, String phone) {
        if (findUser(u) != null) return false;
        users.add(new User(clean(u), clean(p), clean(name), clean(phone), "USER"));
        saveUsers();
        return true;
    }

    // ---------- feedback ----------
    public synchronized void addFeedback(String name, String email, String msg) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        feedback.add(new String[]{clean(name), clean(email), clean(msg), now});
        saveFeedback();
    }
}
