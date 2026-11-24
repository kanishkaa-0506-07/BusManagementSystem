package bms;

/** Model class: one bus service on one route. */
public class Bus {
    public int id;
    public String name, from, to, depart, arrive, type;
    public int fare, seats;

    public Bus(int id, String name, String from, String to, String depart,
               String arrive, String type, int fare, int seats) {
        this.id = id; this.name = name; this.from = from; this.to = to;
        this.depart = depart; this.arrive = arrive; this.type = type;
        this.fare = fare; this.seats = seats;
    }

    public String toLine() {
        return String.join("|", String.valueOf(id), name, from, to, depart, arrive, type,
                String.valueOf(fare), String.valueOf(seats));
    }

    public static Bus fromLine(String l) {
        String[] p = l.split("\\|", -1);
        return new Bus(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4], p[5], p[6],
                Integer.parseInt(p[7]), Integer.parseInt(p[8]));
    }
}
