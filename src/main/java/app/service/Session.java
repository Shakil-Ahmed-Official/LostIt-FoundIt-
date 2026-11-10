package app.service;

import app.model.Role;
import app.model.User;

public final class Session {

    private static User current;

    private Session() { }

    public static User get() { return current; }

    public static void set(User user) { current = user; }

    public static void clear() { current = null; }

    public static boolean isAdmin() {
        return current != null && current.role() == Role.ADMIN;
    }
}
