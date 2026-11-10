package app.service;

import app.dao.UserDao;
import app.dao.UserDao.StoredUser;
import app.model.Role;
import app.model.User;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern EMAIL =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserDao users = new UserDao();

    /** Creates a student account. Admins are never self-registered. */
    public User register(String fullName, String email,
                         String password, String confirm) {
        String name = fullName == null ? "" : fullName.trim();
        String mail = normalize(email);

        if (name.length() < 2) {
            throw new ServiceException("Please enter your full name.");
        }
        if (!EMAIL.matcher(mail).matches()) {
            throw new ServiceException("Please enter a valid email address.");
        }
        if (password == null || password.length() < 6) {
            throw new ServiceException(
                    "The password must be at least 6 characters long.");
        }
        if (!password.equals(confirm)) {
            throw new ServiceException("The two passwords do not match.");
        }
        if (users.findByEmail(mail).isPresent()) {
            throw new ServiceException(
                    "An account with this email already exists.");
        }

        int id = users.insert(name, mail,
                PasswordHasher.hash(password), Role.STUDENT);
        return new User(id, name, mail, Role.STUDENT);
    }

    public User login(String email, String password) {
        Optional<StoredUser> found = users.findByEmail(normalize(email));
        if (found.isEmpty() || password == null
                || !PasswordHasher.verify(password,
                        found.get().passwordHash())) {
            throw new ServiceException("Invalid email or password.");
        }
        User user = found.get().user();
        Session.set(user);
        return user;
    }

    public void logout() {
        Session.clear();
    }

    /** Admin-only: list every registered user. */
    public List<User> listUsers() {
        if (!Session.isAdmin()) {
            throw new ServiceException("Only an admin can view all users.");
        }
        return users.findAll();
    }

    /** Creates the first admin account so the app can be used right away. */
    public void seedAdminIfEmpty() {
        if (users.count() == 0) {
            users.insert("Campus Admin", "admin@campus.edu",
                    PasswordHasher.hash("admin123"), Role.ADMIN);
        }
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
