package bms;

/** Model class: one booked seat (one ticket). */
public class Booking {
    public int id, busId, age, seat;
    public String username, passenger, gender, date, status, bookedOn;

    public Booking(int id, int busId, String username, String passenger, int age,
                   String gender, int seat, String date, String status, String bookedOn) {
        this.id = id; this.busId = busId; this.username = username;
        this.passenger = passenger; this.age = age; this.gender = gender;
        this.seat = seat; this.date = date; this.status = status; this.bookedOn = bookedOn;
    }

    public String toLine() {
        return String.join("|", String.valueOf(id), String.valueOf(busId), username, passenger,
                String.valueOf(age), gender, String.valueOf(seat), date, status, bookedOn);
    }

    public static Booking fromLine(String l) {
        String[] p = l.split("\\|", -1);
        return new Booking(Integer.parseInt(p[0]), Integer.parseInt(p[1]), p[2], p[3],
                Integer.parseInt(p[4]), p[5], Integer.parseInt(p[6]), p[7], p[8], p[9]);
    }
}
