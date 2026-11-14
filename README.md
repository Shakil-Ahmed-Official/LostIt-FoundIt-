# LostIt? FoundIt!

An automated lost-and-found matching system for campus communities.
Desktop application built with Java 21, JavaFX, AtlantaFX and SQLite.

## Run

Requires JDK 21 and Maven.

    mvn javafx:run

The database file `lostit.db` and the `photos/` folder are created
automatically on first launch.

## Test

    mvn test

## Demo accounts

On first launch an administrator account is created:

    admin@campus.edu / admin123

Sign in as the admin, open **Users** and press **Load sample data** to
create four students (password `student123`) and eleven reports, some
of which match each other:

    rahim@campus.edu, karim@campus.edu, nusrat@campus.edu, tanvir@campus.edu

New users register as students from the login screen.

## Credits

Poppins font by the Poppins Project Authors, SIL Open Font License 1.1
(see `src/main/resources/fonts/OFL.txt`).
