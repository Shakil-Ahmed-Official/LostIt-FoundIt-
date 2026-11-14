package app.model;

import java.util.List;

public final class Catalog {

    public static final List<String> CATEGORIES = List.of(
            "Phone", "Wallet", "ID Card", "Keys", "Bag", "Bottle",
            "Books and Notes", "Electronics", "Clothing", "Other");

    public static final List<String> ZONES = List.of(
            "Main Building", "Library", "Cafeteria", "Playground",
            "Auditorium", "Classroom Block", "Hostel", "Parking",
            "Other");

    /** Zones that are close to each other share a cluster. */
    public static String zoneCluster(String zone) {
        return switch (zone) {
            case "Main Building", "Classroom Block", "Library",
                 "Auditorium" -> "ACADEMIC";
            case "Cafeteria", "Playground" -> "SOCIAL";
            default -> zone;
        };
    }

    private Catalog() { }
}
