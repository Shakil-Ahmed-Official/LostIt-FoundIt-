package app.service;

import app.dao.UserDao;
import app.model.ReportType;
import app.model.User;

import java.time.LocalDate;

/** Sample students and reports for testing and demonstrations. */
public final class DemoData {

    private static final String PASSWORD = "student123";

    private DemoData() { }

    /** Loads four students and eleven reports, some of which match. */
    public static void load() {
        if (new UserDao().findByEmail("rahim@campus.edu").isPresent()) {
            throw new ServiceException("The sample data is already loaded.");
        }
        AuthService auth = new AuthService();
        ReportService rs = new ReportService();
        User rahim = auth.register("Rahim Uddin", "rahim@campus.edu",
                PASSWORD, PASSWORD);
        User karim = auth.register("Karim Hasan", "karim@campus.edu",
                PASSWORD, PASSWORD);
        User nusrat = auth.register("Nusrat Jahan", "nusrat@campus.edu",
                PASSWORD, PASSWORD);
        User tanvir = auth.register("Tanvir Ahmed", "tanvir@campus.edu",
                PASSWORD, PASSWORD);
        LocalDate today = LocalDate.now();

        rs.submit(rahim, ReportType.LOST, "Blue Nike water bottle", "Bottle",
                "Blue sports bottle with a small dent near the bottom "
                + "and a white lid.", "Library", today.minusDays(3), null);
        rs.submit(nusrat, ReportType.LOST, "Black Samsung phone", "Phone",
                "Black Samsung Galaxy with a blue back cover and a "
                + "cracked screen protector.", "Classroom Block",
                today.minusDays(4), null);
        rs.submit(karim, ReportType.LOST, "Brown leather wallet", "Wallet",
                "Brown leather purse with a student ID and some cash.",
                "Main Building", today.minusDays(5), null);
        rs.submit(tanvir, ReportType.LOST, "Student ID card", "ID Card",
                "Student ID card in a green holder.", "Auditorium",
                today.minusDays(6), null);
        rs.submit(karim, ReportType.LOST, "House keys with red keychain",
                "Keys", "Three keys on a red keyring.", "Hostel",
                today.minusDays(7), null);

        rs.submit(karim, ReportType.FOUND, "Blue sports flask", "Bottle",
                "Found a blue plastic flask with a dent and a white cap "
                + "on a table.", "Library", today.minusDays(2), null);
        rs.submit(tanvir, ReportType.FOUND, "Mobile phone, Samsung", "Phone",
                "Black Samsung mobile with a blue back cover, found near "
                + "room 301.", "Classroom Block", today.minusDays(3), null);
        rs.submit(rahim, ReportType.FOUND, "Wallet found near cafeteria",
                "Wallet", "Small brown wallet with a student card inside.",
                "Cafeteria", today.minusDays(4), null);
        rs.submit(nusrat, ReportType.FOUND, "ID card with green lanyard",
                "ID Card", "Found a student id card with a green holder "
                + "near the auditorium.", "Auditorium", today.minusDays(5),
                null);
        rs.submit(tanvir, ReportType.FOUND, "Black steel thermos", "Bottle",
                "Black steel thermos with a sticker on the side.",
                "Hostel", today.minusDays(13), null);
        rs.submit(nusrat, ReportType.FOUND, "Black folding umbrella", "Other",
                "Black umbrella left at the parking entrance.", "Parking",
                today.minusDays(1), null);
    }
}
