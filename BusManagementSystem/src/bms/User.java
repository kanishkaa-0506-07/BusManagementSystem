package bms;

/** Model class: a registered user (role = USER or ADMIN). */
public class User {
    public String username, password, fullName, phone, role;

    public User(String username, String password, String fullName, String phone, String role) {
        this.username = username; this.password = password;
        this.fullName = fullName; this.phone = phone; this.role = role;
    }

    public boolean isAdmin() { return "ADMIN".equals(role); }

    public String toLine() {
        return String.join("|", username, password, fullName, phone, role);
    }

    public static User fromLine(String l) {
        String[] p = l.split("\\|", -1);
        return new User(p[0], p[1], p[2], p[3], p[4]);
    }
}
