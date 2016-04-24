/*******************************************************************************
 * Class: CS 1555 Database Management Systems
 * Instructor: Prof. Mohamed Sharaf
 * Contributors:
 *   - Benjamin Lind (bdl22)
 *   - Autumn Good (alg161)
 *   - Fadi Alchoufete (fba4)
 *
 * Driver.java contains functions that interact with the FaceSpace database. It
 * also contains a demo program that demonstrates the use of all of the
 * functions.
 *
 * Note on concurrency: this program assumes that only one person will
 * access the database at a time. There are not protections in place
 * to ensure concurrent access works correctly.
 ******************************************************************************/

import java.util.Scanner;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.io.Console;
import java.lang.*;

public class Driver {
    private static Scanner scanner;
    private static Connection connection;
    private Statement statement;
    private PreparedStatement prep_statement;
    private ResultSet result_set;
    private String query;
    private SimpleDateFormat ymd_format = new SimpleDateFormat("yyyy-MM-dd");

    public Driver() {
        scanner = new Scanner(System.in);
    }
    
    public void demo() {
        System.out.println("RUNNING DEMO");

        // Create some users
        Date dob;
        try {
            dob = new Date(ymd_format.parse("1990-04-13").getTime());
        }
        catch (ParseException e) {
            System.out.println("Invalid date.");
            return;
        }
        createUser("Test User", "test@user.com", dob);

        try {
            dob = new Date(ymd_format.parse("1992-01-28").getTime());
        }
        catch (ParseException e) {
            System.out.println("Invalid date.");
            return;
        }
        createUser("Another User", "another@user.com", dob);
    }

    public void createUser(String name, String email, Date dob) {
        try {
            query = "INSERT INTO FS_User (name, email, dob, last_login) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

            prep_statement = connection.prepareStatement(query);
            prep_statement.setString(1, name);
            prep_statement.setString(2, email);
            prep_statement.setDate(3, dob);

            prep_statement.executeUpdate();
            connection.commit();
            System.out.println("Created user '" + name + "'.\n");
        }
        catch (SQLException e) {
            System.out.println("createUser(): SQLException: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("createUser(): Cannot close Statement. Machine error: " + e.toString());
            }
        }
    }

    public void initiateFriendship(int initiator_id, int receiver_id) {
        if (getFriendshipID(initiator_id, receiver_id) != -1) {
            System.out.println("Could not create friendship (already exists?)\n");
            return;
        }

        try {
            query = "INSERT INTO Friendship (friend_initiator, friend_receiver, established) "
                + "VALUES (?, ?, 0)";

            prep_statement = connection.prepareStatement(query);
            prep_statement.setLong(1, initiator_id);
            prep_statement.setLong(2, receiver_id);

            prep_statement.executeUpdate();
            connection.commit();
        }
        catch (SQLException e) {
            System.out.println("initiateFriendship(): Error running SQL: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("initiateFriendship(): Cannot close Statement. Machine error: " + e.toString());
            }
        }
        
        System.out.println("Initiated friendship.\n");
    }
    
    public void establishFriendship(int first_id, int second_id) {
        try {
            long friendship_id = getFriendshipID(first_id, second_id);
            
            if (friendship_id == -1) {
                // Friendship doesn't exist yet, so create friendship
                initiateFriendship(first_id, second_id);
            }

            // Mark friendship as established
            statement = connection.createStatement();

            query = "UPDATE Friendship SET established = 1, "
                + "date_established = CURRENT_TIMESTAMP "
                + "WHERE friendship_id = " + friendship_id;

            statement.executeQuery(query);
            connection.commit();
            System.out.println("Established friendship.\n");
        }
        catch (SQLException e) {
            System.out.println("establishFriendship(): Error running SQL: " + e.toString());
        }
        finally {
            try {
                if (statement != null) statement.close();
            }
            catch (SQLException e) {
                System.out.println("establishFriendship(): Cannot close Statement. Machine error: " + e.toString());
            }
        }
    }

    public void displayFriends(int user_id) {
        try {
            query = "SELECT friendship_id, user_id, name, established, " +
                "friend_initiator, friend_receiver " +
                "FROM Friendship, FS_User " +
                "WHERE (friend_initiator = ? OR friend_receiver = ?) AND " +
                "((friend_initiator = ? AND friend_receiver = user_id) OR " +
                " (friend_receiver = ? AND friend_initiator = user_id))";

            prep_statement = connection.prepareStatement(query);
            prep_statement.setLong(1, user_id);
            prep_statement.setLong(2, user_id);
            prep_statement.setLong(3, user_id);
            prep_statement.setLong(4, user_id);

            result_set = prep_statement.executeQuery();

            if (!result_set.isBeforeFirst()) {
                // ...there are no results
                System.out.println("User " + user_id + " does not have any friends.");
            }
            else {
                // The user has friends.
                System.out.println("Friends of user " + user_id + ":");
                
                while (result_set.next()) {
                    System.out.print("Name: '" + result_set.getString(3) + "' " +
                                     "(friendship_id: " + result_set.getLong(1) + ", " +
                                     "user_id: " + result_set.getLong(2) + ", ");
                    if (result_set.getLong(4) == 1) {
                        System.out.println("ESTABLISHED)");
                    }
                    else {
                        System.out.println("PENDING)");
                    }
                }
            }
        }
        catch (SQLException e) {
            System.out.println("listUsers(): Error running SQL: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("displayFriends(): Cannot close Statement. Machine error: " + e.toString());
            }
        }        

        System.out.println();
    }


    /***** HELPER FUNCTIONS *****/

    public void listUsers() {
        System.out.println("LISTING USERS");

        try {
            statement = connection.createStatement();

            query = "SELECT * FROM FS_User ORDER BY user_id ASC";

            result_set = statement.executeQuery(query);

            while (result_set.next()) {
                System.out.println("User (user_id, name, email, dob, last_login) = ("
                                   + result_set.getLong(1) + ", "
                                   + result_set.getString(2) + ", "
                                   + result_set.getString(3) + ", "
                                   + result_set.getDate(4) + ", "
                                   + result_set.getTimestamp(5) + ")");
            }
        }
        catch (SQLException e) {
            System.out.println("listUsers(): Error running SQL: " + e.toString());
        }
        finally {
            try {
                if (statement != null) statement.close();
            }
            catch (SQLException e) {
                System.out.println("listUsers(): Cannot close Statement. Machine error: " + e.toString());
            }
        }        

        System.out.println();
    }

    public void listFriendships() {
        System.out.println("LISTING FRIENDSHIPS");

        try {
            statement = connection.createStatement();

            query = "SELECT * FROM Friendship ORDER BY friendship_id ASC";

            result_set = statement.executeQuery(query);

            while (result_set.next()) {
                System.out.println("Friendship (id, initiator, receiver, established, date_established) = ("
                                   + result_set.getLong(1) + ", "
                                   + result_set.getLong(2) + ", "
                                   + result_set.getLong(3) + ", "
                                   + result_set.getLong(4) + ", "
                                   + result_set.getTimestamp(5) + ")");
            }
        }
        catch (SQLException e) {
            System.out.println("listFriendships(): Error running SQL: " + e.toString());
        }
        finally {
            try {
                if (statement != null) statement.close();
            }
            catch (SQLException e) {
                System.out.println("listFriendships(): Cannot close Statement. Machine error: " + e.toString());
            }
        }        

        System.out.println();
    }

    /**
     * getFriendshipID(int first_id, int second_id) returns the
     * friendship ID that exists between the two passed user IDs. If
     * no friendship exists, -1 is returned.
     */
    public long getFriendshipID(int first_id, int second_id) {
        long friendship_id = -1;
        try {
            query = "SELECT friendship_id FROM Friendship "
                + "WHERE (friend_initiator = ? AND friend_receiver = ?) "
                + "OR (friend_initiator = ? AND friend_receiver = ?)";

            prep_statement = connection.prepareStatement(query);
            prep_statement.setLong(1, first_id);
            prep_statement.setLong(2, second_id);
            prep_statement.setLong(3, second_id);
            prep_statement.setLong(4, first_id);

            result_set = prep_statement.executeQuery();

            if (result_set.next()) {
                friendship_id = result_set.getLong(1);
            }
        }
        catch (SQLException e) {
            System.out.println("getFriendshipID(): Error running SQL: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("getFriendshipID(): Cannot close Statement. Machine error: " + e.toString());
            }
        }
        return friendship_id;
    }

    public boolean userExists(int user_id) {
        boolean exists = false;
        try {
            query = "SELECT user_id FROM FS_User WHERE user_id = ?";

            prep_statement = connection.prepareStatement(query);
            prep_statement.setLong(1, user_id);
            result_set = prep_statement.executeQuery();

            if (result_set.next()) {
                exists = true;
            }
        }
        catch (SQLException e) {
            System.out.println("userExists(): Error running SQL: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("userExists(): Cannot close Statement. Machine error: " + e.toString());
            }
        }
        return exists;
    }
            
    public static boolean isInteger(String str) {
        try { int d = Integer.parseInt(str); }
        catch (NumberFormatException nfe) { return false; }
        return true;  
    }
    
    public static void main(String[] args) throws SQLException {
        Driver TestDriver = new Driver();

        // Input database credentials
        String db_user = Constants.JDBC_USERNAME;
        String db_pass = Constants.JDBC_PASSWORD;

        try {
            // DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            // Connect to the database
            String db_url = Constants.JDBC_URL;
            TestDriver.connection = DriverManager.getConnection(db_url, db_user, db_pass);
            System.out.println("\nConnected to DB.\n");
        }
        catch (Exception e)  {
            System.out.println("Error connecting to database. Machine Error: " + e.toString());
            System.exit(0);
        }

        String command;
        boolean showHelp = true;

        while (true) {
            if (showHelp == true) {
                System.out.println("Command list:\n"
                    + "\t(q) quit\n"
                    + "\t(h) help\n"
                    + "\t(d) demo\n"
                    + "\t(1) createUser\n"
                    + "\t(2) initiateFriendship\n"
                    + "\t(3) establishFriendship\n"
                    + "\t(4) displayFriends\n"
                    + "\t(13) listUsers\n"
                    + "\t(14) listFriendships\n");

                showHelp = false;
            }

            System.out.print("Enter a command: ");
            command = TestDriver.scanner.nextLine().toLowerCase();

            if (command.equals("quit") || command.equals("q")) break;

            System.out.println();

            if (command.equals("help") || command.equals("h")) {
                showHelp = true;
            }
            else if (command.equals("createuser") || command.equals("1")) {
                System.out.println("FUNCTION: createUser()\n");
                
                System.out.print("Enter a name: ");
                String name = TestDriver.scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("\nYou must enter a name.\n");
                    continue;
                }
                else if (name.length() > 128) {
                    System.out.println("\nName cannot be more than 128 characters.\n");
                    continue;
                }
                
                System.out.print("Enter an email: ");
                String email = TestDriver.scanner.nextLine().trim();
                if (email.isEmpty()) {
                    System.out.println("\nYou must enter an email.\n");
                    continue;
                }
                else if (email.length() > 254) {
                    System.out.println("\nEmail cannot be more than 254 characters.\n");
                    continue;
                }
                
                System.out.print("Enter a dob (format: YYYY-MM-DD): ");
                String dob_str = TestDriver.scanner.nextLine();

                // Parse and validate dob
                Date dob;
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    dob = new Date(df.parse(dob_str).getTime());
                }
                catch (ParseException e) {
                    System.out.println("\nInvalid date.\n");
                    continue;
                }
	    
                System.out.println();
                
                TestDriver.createUser(name, email, dob);
            }
            else if (command.equals("initiatefriendship") || command.equals("2")) {
                System.out.println("FUNCTION: initiateFriendship()\n");

                System.out.print("Enter the initiator's user ID: ");
                String initiator_id_str = TestDriver.scanner.nextLine();
                
                if (!isInteger(initiator_id_str)) {
                    System.out.println("\nInvalid ID (must be an integer)\n");
                    continue;
                }
                int initiator_id = Integer.parseInt(initiator_id_str);
                if (!TestDriver.userExists(initiator_id)) {
                    System.out.println("\nUser with id " + initiator_id + " does not exist.\n");
                    continue;
                }

                System.out.print("Enter the receiver's user ID: ");
                String receiver_id_str = TestDriver.scanner.nextLine();

                if (!isInteger(receiver_id_str)) {
                    System.out.println("\nInvalid ID (must be an integer)\n");
                    continue;
                }
                int receiver_id = Integer.parseInt(receiver_id_str);
                if (!TestDriver.userExists(receiver_id)) {
                    System.out.println("\nUser with id " + receiver_id + " does not exist.\n");
                    continue;
                }
                if (initiator_id == receiver_id) {
                    System.out.println("\nA person cannot be friends with themselves.\n");
                    continue;
                }

                System.out.println();

                TestDriver.initiateFriendship(initiator_id, receiver_id);
            }
            else if (command.equals("establishfriendship") || command.equals("3")) {
                System.out.println("FUNCTION: establishFriendship()\n");

                System.out.print("Enter the first user's ID: ");
                String first_id_str = TestDriver.scanner.nextLine();
                
                if (!isInteger(first_id_str)) {
                    System.out.println("\nInvalid ID (must be an integer)\n");
                    continue;
                }
                int first_id = Integer.parseInt(first_id_str);
                if (!TestDriver.userExists(first_id)) {
                    System.out.println("\nUser with id " + first_id + " does not exist.\n");
                    continue;
                }

                System.out.print("Enter the second user's ID: ");
                String second_id_str = TestDriver.scanner.nextLine();

                if (!isInteger(second_id_str)) {
                    System.out.println("\nInvalid ID (must be an integer)\n");
                    continue;
                }
                int second_id = Integer.parseInt(second_id_str);
                if (!TestDriver.userExists(second_id)) {
                    System.out.println("\nUser with id " + second_id + " does not exist.\n");
                    continue;
                }
                if (first_id == second_id) {
                    System.out.println("\nA person cannot be friends with themselves.\n");
                    continue;
                }

                System.out.println();

                TestDriver.establishFriendship(first_id, second_id);
            }
            else if (command.equals("displayFriends") || command.equals("4")) {
                System.out.println("FUNCTION: displayFriends()\n");

                System.out.print("Enter the user's ID: ");
                String user_id_str = TestDriver.scanner.nextLine();
                
                if (!isInteger(user_id_str)) {
                    System.out.println("\nInvalid ID (must be an integer)\n");
                    continue;
                }
                int user_id = Integer.parseInt(user_id_str);
                if (!TestDriver.userExists(user_id)) {
                    System.out.println("\nUser with id " + user_id + " does not exist.\n");
                    continue;
                }
                
                System.out.println();

                TestDriver.displayFriends(user_id);
            }
            else if (command.equals("listusers") || command.equals("13")) {
                TestDriver.listUsers();
            }
            else if (command.equals("listfriendships") || command.equals("14")) {
                TestDriver.listFriendships();
            }
            else if (command.equals("demo") || command.equals("d")) {
                TestDriver.demo();
            }
            else {
                System.out.println("Unknown command.\n");
            }
        } // end while

        TestDriver.connection.close();
    }
}
