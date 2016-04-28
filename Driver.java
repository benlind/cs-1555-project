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

        try {
            dob = new Date(ymd_format.parse("1994-01-01").getTime());
        }
        catch (ParseException e) {
            System.out.println("Invalid date.");
            return;
        }
        createUser("Mutual User", "mutual@user.com", dob);
		
        //initiate friendship
        System.out.println("Initiate friendship between user 1 and user 2\n");
        initiateFriendship(1,2);
		
        //establishFriendship
        System.out.println("Establish friendship between user 1 and user 2\n");
        establishFriendship(1,2);

        System.out.println("Establish mutual friendship between user 1 and user 2 with user 3\n");
        initiateFriendship(1,3);
        establishFriendship(1,3);
        initiateFriendship(2,3);
        establishFriendship(2,3);
		
        //display friends
        System.out.println("Display friends of user 1\n");
        displayFriends(1);
		
        //create group
        System.out.println("Create a test group of enrollment limit 1\n");
        createGroup("Test Group", "This group is for testing purposes", 1);
		
        //add to group
        System.out.println("Add user 1 to test group\n");
        addToGroup(1,1);
        System.out.println("Add user 2 to test group\n");
        addToGroup(1,2);
		
        //send message
        System.out.println("Send a test message from user 1 to user 2\n");
        sendMessageToUser("Test message", "This message is a test", 2, 1);
		
        //display messages
        System.out.println("Display the messages of user 2\n");
        displayMessages(2);
		
        //search for user
        System.out.println("Search for the string \"Test User another@user.com\"\n");
        searchForUser("Test User another@user.com");
		
        //three connections
        System.out.println("Check for mutual friends (three degrees)\n");
        threeDegrees(1,2);
		
        //show top messagers
        System.out.println("Get top 5 messagers in the last 6 months\n");
        topMessagers(6,5);
		
        //drop user
        System.out.println("Drop user 1\n");
        dropUser(1);
		
        //list users
        System.out.println("List all users\n");
        listUsers();
		
        //list friendships
        System.out.println("List all friendships\n");
        listFriendships();
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

    public void initiateFriendship(long initiator_id, long receiver_id) {
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
    
    public void establishFriendship(long first_id, long second_id) {
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

    public void displayFriends(long user_id) {
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
                // The user has friends
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

    public void searchForUser(String search_str) {
        // Split string into tokens based on spaces
        String tokens = search_str.trim().replaceAll("\\s+", "|");

        try {
            // Search all relevant fields for the query
            query = "SELECT user_id, name, email, dob FROM FS_User " +
                "WHERE REGEXP_LIKE(user_id, ?, 'i') " +
                "OR    REGEXP_LIKE(name,    ?, 'i') " +
                "OR    REGEXP_LIKE(email,   ?, 'i') " +
                "OR    REGEXP_LIKE(dob,     ?, 'i')";

            prep_statement = connection.prepareStatement(query);
            prep_statement.setString(1, tokens);
            prep_statement.setString(2, tokens);
            prep_statement.setString(3, tokens);
            prep_statement.setString(4, tokens);
            
            result_set = prep_statement.executeQuery();
            
            if (!result_set.isBeforeFirst()) {
                // ...there are no results
                System.out.println("Your search returned no results.");
            }
            else {
                // Some users were found
                System.out.println("Search results:");
                
                while (result_set.next()) {
                    System.out.println("Name: '" + result_set.getString(2) + "' " +
                                       "(user_id: " + result_set.getLong(1) + ", " +
                                       "email: '" + result_set.getString(3) + "', " +
                                       "dob: " + result_set.getDate(4) + ")");
                }
            }
        }
        catch (SQLException e) {
            System.out.println("dropUser(): SQLException: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("dropUser(): Cannot close Statement. Machine error: " + e.toString());
            }
        }
        System.out.println();
    }

    public void dropUser(long user_id) {
        try {
            query = "DELETE FROM FS_User WHERE user_id=?";

            prep_statement = connection.prepareStatement(query);
            prep_statement.setLong(1, user_id);

            prep_statement.executeUpdate();
            connection.commit();
            System.out.println("Deleted user '" + user_id + "'.\n");
        }
        catch (SQLException e) {
            System.out.println("dropUser(): SQLException: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("dropUser(): Cannot close Statement. Machine error: " + e.toString());
            }
        }
    }

	public void createGroup(String name, String description, long membershipLimit) {
        try {
            query = "INSERT INTO User_Group (group_name, group_description, group_enroll_limit) "
                + "VALUES (?, ?, ?)";

            prep_statement = connection.prepareStatement(query);
            prep_statement.setString(1, name);
            prep_statement.setString(2, description);
            prep_statement.setLong(3, membershipLimit);

            prep_statement.executeUpdate();
            connection.commit();
            System.out.println("Created group '" + name + "'.\n");
        }
        catch (SQLException e) {
            System.out.println("createGroup(): SQLException: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("createGroup(): Cannot close Statement. Machine error: " + e.toString());
            }
        }
    }
	
    public void addToGroup(long group_id, long user_id){
	    try {
            query = "INSERT INTO Group_Member (group_id, user_id) "
                  + "VALUES (?, ?, ?)";

            prep_statement = connection.prepareStatement(query);
            prep_statement.setLong(1, group_id);
            prep_statement.setLong(2, user_id);

            prep_statement.executeUpdate();
            connection.commit();
            System.out.println("Added user " + user_id + " to group "+group_id+ ".\n");
        }
        catch (SQLException e) {
            System.out.println("addToGroup(): SQLException: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("addToGroup(): Cannot close Statement. Machine error: " + e.toString());
            }
        }
    }

    public void topMessagers(long x, long k){
	    try {
            query = "Select case when sender is null then recipient else sender end as id,(nvl(sendCount,0)+nvl(recipientCount,0)) "+
		    "from (Select sender, count(*) as sendCount " +
		    "from (Select * from Message WHERE date_sent > CURRENT_DATE - INTERVAL ? MONTH ) " +
                   "group by sender) " +
	        "full outer join (Select recipient, count(*) as recipientCount " +
	                          "from (Select * from Message where date_sent > CURRENT_DATE - INTERVAL ? MONTH) " +
							  "group by recipient) " +
	        "on sender = recipient " +
		    "order by (nvl(sendCount,0)+nvl(recipientCount,0)) desc " +
	        "where rownum <= ?;";
			
		    String xString = "" + x;

            prep_statement = connection.prepareStatement(query);
            prep_statement.setString(1, xString);
            prep_statement.setString(2, xString);
		    prep_statement.setLong(3, k);

            result_set = prep_statement.executeQuery();
		    System.out.println("Top messagers ids and message counts: ");
            while (result_set.next()) {
                System.out.println(""
                                   + result_set.getLong(1) + ", "
                                   + result_set.getLong(2) + ", "
                                  );
            }
        }
        catch (SQLException e) {
            System.out.println("topMessagers(): SQLException: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("topMessagers(): Cannot close Statement. Machine error: " + e.toString());
            }
        }
    }

    public void sendMessageToUser(String subject, String body, long recipient, long sender) {
      query = "INSERT INTO Message (subject, body, recipient, sender, date_sent) VALUES (?, ?, ?, ?, ?)";

      java.util.Date utilDate = new java.util.Date();
      Date dob = new Date(utilDate.getTime());

      try {
        prep_statement = connection.prepareStatement(query);
        prep_statement.setString(1, subject);
        prep_statement.setString(2, body);
        prep_statement.setLong(3, recipient);
        prep_statement.setLong(4, sender);
        prep_statement.setDate(5, dob);

        prep_statement.executeUpdate();
        connection.commit();
        System.out.println("Message sent to user ID '" + recipient + "'.\n");
      }
      catch (SQLException e) {
        System.out.println("sendMessageToUser(): SQLException: " + e.toString());
      }
    }

    public void displayMessages(long recipient) {
      query = "SELECT * FROM Message m INNER JOIN FS_USER u ON m.sender = u.user_id WHERE m.recipient = ? ORDER BY m.date_sent ASC";

      try {
        prep_statement = connection.prepareStatement(query);
        prep_statement.setLong(1, recipient);

        result_set = prep_statement.executeQuery();
        while (result_set.next()) {
          System.out.println("Message ID " + result_set.getLong("message_id"));
          System.out.println("-----------------------------------");
          System.out.println("Sender: " + result_set.getString("name") + " <" + result_set.getString("email") + ">");
          System.out.println("Date Sent: " + result_set.getDate("date_sent"));
          System.out.println("");
          System.out.println(" | " + result_set.getString("body"));
          System.out.println("");
        }
      }
      catch (SQLException e) {
        System.out.println("displayMessages(): SQLException: " + e.toString());
      }
    }

   /* public void threeDegrees(long first, long second) {
      query = "SELECT * FROM FS_User WHERE (user_id IN (SELECT friend_receiver FROM Friendship WHERE friend_initiator = ? AND established = 1) OR user_id IN (SELECT friend_initiator FROM Friendship WHERE friend_receiver = ? AND established = 1)) AND (user_id IN (SELECT friend_receiver FROM Friendship  WHERE friend_initiator = ? AND established = 1) OR user_id IN (SELECT friend_initiator FROM Friendship WHERE friend_receiver = ? AND established = 1))";
      String query_user = "SELECT * FROM FS_User WHERE user_id = ?";

      try {
        PreparedStatement prep_statement_f = connection.prepareStatement(query_user);
        prep_statement_f.setLong(1, first);
        ResultSet result_f = prep_statement_f.executeQuery();
        result_f.next();
        String name_f = result_f.getString("name");

        PreparedStatement prep_statement_s = connection.prepareStatement(query_user);
        prep_statement_s.setLong(1, second);
        ResultSet result_s = prep_statement_s.executeQuery();
        result_s.next();
        String name_s = result_s.getString("name");

        prep_statement = connection.prepareStatement(query);
        prep_statement.setLong(1, first);
        prep_statement.setLong(2, first);
        prep_statement.setLong(3, second);
        prep_statement.setLong(4, second);

        result_set = prep_statement.executeQuery();
        int n = 1;
        while (result_set.next()) {
          System.out.println(n + ": " + name_f + " - " + result_set.getString(2) + " - " + name_s);
          n++;
        }
      }
      catch (SQLException e) {
        System.out.println("threeDegrees(): SQLException: " + e.toString());
      }
    }*/
	
	public void threeDegrees(long first, long second) {
	      String query = "SELECT user_id FROM FS_User WHERE (user_id IN (SELECT friend_receiver FROM Friendship WHERE friend_initiator = ? AND established = 1) " +
	                           "OR user_id IN (SELECT friend_initiator FROM Friendship WHERE friend_receiver = ? AND established = 1))";
	      String query_user = "SELECT * FROM FS_User WHERE user_id = ?";

	      try {
	        PreparedStatement prep_statement_f = connection.prepareStatement(query_user);
	        prep_statement_f.setLong(1, first);
	        ResultSet result_f = prep_statement_f.executeQuery();
	        result_f.next();
	        String name_f = result_f.getString("name");

	        PreparedStatement prep_statement_s = connection.prepareStatement(query_user);
	        prep_statement_s.setLong(1, second);
	        ResultSet result_s = prep_statement_s.executeQuery();
	        result_s.next();
	        String name_s = result_s.getString("name");

	       PreparedStatement prep_statement_first = connection.prepareStatement(query);
	        prep_statement_first.setLong(1, first);
	        prep_statement_first.setLong(2, first);
	       
	        //Check to see if direct friends
	        ResultSet result_set_first = prep_statement_first.executeQuery();
			ArrayList <Long> firstFriends= new ArrayList<Long>();
	        while (result_set_first.next()) {
	          firstFriends.add(result_set_first.getLong("user_id"));
	        }
			result_set_first.close();
			
			if(firstFriends.contains(second)){
				System.out.println("" + name_f + " - " + name_s + "\n");
				return;
			}
			
			//Check to see if they share a mutual friend
			PreparedStatement prep_statement_second = connection.prepareStatement(query);
	        prep_statement_second.setLong(1, second);
	        prep_statement_second.setLong(2, second);
	       

	        ResultSet result_set_second = prep_statement_second.executeQuery();
			ArrayList <Long> secondFriends= new ArrayList<Long>();
	        while (result_set_second.next()) {
	          secondFriends.add(result_set_second.getLong("user_id"));
	        }
	        result_set_second.close();
	        
	        for(int i = 0; i < secondFriends.size(); i++){
	        	if(firstFriends.contains(secondFriends.get(i))){
	        		PreparedStatement prep_statement_mutual = connection.prepareStatement(query_user);
	    	        prep_statement_mutual.setLong(1, secondFriends.get(i));
	    	        ResultSet result_mutual = prep_statement_s.executeQuery();
	    	        result_mutual.next();
	    	        String name_mutual = result_mutual.getString("name");
	    	        result_mutual.close();
					System.out.println("" + name_f + " - " + name_mutual + " - "+ name_s + "\n");
					return;
				}
	        }
	        
	        //Check to see if one of each of their friends are friends (3 hops or links)
	        for(int i = 0; i < firstFriends.size(); i++){
	        	PreparedStatement prep_statement_temp = connection.prepareStatement(query);
		        prep_statement_temp.setLong(1, firstFriends.get(i));
		        prep_statement_temp.setLong(2, firstFriends.get(i));
		       

		        ResultSet result_set_temp = prep_statement_temp.executeQuery();
				ArrayList <Long> tempFriends= new ArrayList<Long>();
		        while (result_set_temp.next()) {
		          tempFriends.add(result_set_temp.getLong("user_id"));
		        }
		        result_set_temp.close();
		        
		        for(int j = 0; j < secondFriends.size(); j++){
		        	if(tempFriends.contains(secondFriends.get(j))){
		        		PreparedStatement prep_statement_link1 = connection.prepareStatement(query_user);
		    	        prep_statement_link1.setLong(1, firstFriends.get(i));
		    	        ResultSet result_link1 = prep_statement_link1.executeQuery();
		    	        result_link1.next();
		    	        String name_link1 = result_link1.getString("name");
		    	        result_link1.close();
		    	        
		        		PreparedStatement prep_statement_link2 = connection.prepareStatement(query_user);
		    	        prep_statement_link2.setLong(1, secondFriends.get(j));
		    	        ResultSet result_link2 = prep_statement_link2.executeQuery();
		    	        result_link2.next();
		    	        String name_link2 = result_link2.getString("name");
		    	        result_link2.close();
		    	        
						System.out.println("" + name_f + " - " + name_link1 + " - " + name_link2 + " - "+ name_s + "\n");
						return;
					}
		        }
	        }
	        
	        System.out.println("No connection of 3 degrees or less between users\n");
	      }
	      catch (SQLException e) {
	        System.out.println("threeDegrees(): SQLException: " + e.toString());
	      }
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
     * getFriendshipID(long first_id, long second_id) returns the
     * friendship ID that exists between the two passed user IDs. If
     * no friendship exists, -1 is returned.
     */
    public long getFriendshipID(long first_id, long second_id) {
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

    /**
     * inputUserID() prompts for a user ID and validates that the ID
     * exists and is numeric.
     *
     * Parameters:
     *  - prompt    string to prompt the user with for input
     *
     * Return values:
     *  - -1 if the inputted ID is non-numeric or does not exist
     *  - the numeric ID if the ID exists
     */
    public long inputUserID(String prompt) {
        System.out.print(prompt);
        String user_id_str = scanner.nextLine();
        if (!isInteger(user_id_str)) {
            System.out.println("\nERROR: Invalid ID (must be an integer)\n");
            return -1;
        }
        
        long user_id = Integer.parseInt(user_id_str);
        if (!userExists(user_id)) {
            System.out.println("\nERROR: User with id " + user_id + " does not exist.\n");
            return -1;
        }
        return user_id;
    }
	
    public boolean userExists(long user_id) {
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
    
	public long inputGroupID(String prompt){
		System.out.print(prompt);
        String group_id_str = scanner.nextLine();
        if (!isInteger(group_id_str)) {
            System.out.println("\nERROR: Invalid ID (must be an integer)\n");
            return -1;
        }
        
        long group_id = Integer.parseInt(group_id_str);
        if (!groupExists(group_id)) {
            System.out.println("\nERROR: Group with id " + group_id + " does not exist.\n");
            return -1;
        }
        return group_id;
	}
	
	 public boolean groupExists(long group_id) {
        boolean exists = false;
        try {
            query = "SELECT group_id FROM User_Group WHERE group_id = ?";

            prep_statement = connection.prepareStatement(query);
            prep_statement.setLong(1, group_id);
            result_set = prep_statement.executeQuery();

            if (result_set.next()) {
                exists = true;
            }
        }
        catch (SQLException e) {
            System.out.println("groupExists(): Error running SQL: " + e.toString());
        }
        finally {
            try {
                if (prep_statement != null) prep_statement.close();
            }
            catch (SQLException e) {
                System.out.println("groupExists(): Cannot close Statement. Machine error: " + e.toString());
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
                    + "\t(1)  createUser\n"
                    + "\t(2)  initiateFriendship\n"
                    + "\t(3)  establishFriendship\n"
                    + "\t(4)  displayFriends\n"
                    + "\t(5)  createGroup\n"
                    + "\t(6)  addToGroup\n"
                    + "\t(7)  sendMessageToUser\n"
                    + "\t(8)  displayMessages\n"
                    + "\t(9)  searchForUser\n"
                    + "\t(10) threeDegrees\n"
                    + "\t(11) topMessagers\n"
                    + "\t(12) dropUser\n"
                    + "\t(13) listUsers\n"
                    + "\t(14) listFriendships\n"
                    );

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
                    System.out.println("\nERROR: You must enter a name.\n");
                    continue;
                }
                else if (name.length() > 128) {
                    System.out.println("\nERROR: Name cannot be more than 128 characters.\n");
                    continue;
                }
                
                System.out.print("Enter an email: ");
                String email = TestDriver.scanner.nextLine().trim();
                if (email.isEmpty()) {
                    System.out.println("\nERROR: You must enter an email.\n");
                    continue;
                }
                else if (email.length() > 254) {
                    System.out.println("\nERROR: Email cannot be more than 254 characters.\n");
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

                long initiator_id = TestDriver.inputUserID("Enter the initiator's ID: ");
                if (initiator_id == -1) { continue; }

                long receiver_id = TestDriver.inputUserID("Enter the receiver's ID: ");
                if (receiver_id == -1) { continue; }

                if (initiator_id == receiver_id) {
                    System.out.println("\nERROR: A person cannot be friends with themselves.\n");
                    continue;
                }

                System.out.println();

                TestDriver.initiateFriendship(initiator_id, receiver_id);
            }
            else if (command.equals("establishfriendship") || command.equals("3")) {
                System.out.println("FUNCTION: establishFriendship()\n");

                long first_id = TestDriver.inputUserID("Enter the first user's ID: ");
                if (first_id == -1) { continue; }

                long second_id = TestDriver.inputUserID("Enter the second user's ID: ");
                if (second_id == -1) { continue; }

                if (first_id == second_id) {
                    System.out.println("\nERROR: A person cannot be friends with themselves.\n");
                    continue;
                }

                System.out.println();

                TestDriver.establishFriendship(first_id, second_id);
            }
            else if (command.equals("displayfriends") || command.equals("4")) {
                System.out.println("FUNCTION: displayFriends()\n");

                long user_id = TestDriver.inputUserID("Enter the user's ID: ");
                if (user_id == -1) { continue; }

                System.out.println();

                TestDriver.displayFriends(user_id);
            }
			else if (command.equals("creategroup") || command.equals("5")){
				System.out.println("FUNCTION: createGroup()\n");
				
				System.out.print("Enter a group name: ");
				String groupName = TestDriver.scanner.nextLine().trim();
				if (groupName.isEmpty()) {
                    System.out.println("\nERROR: You must enter a group name.\n");
                    continue;
                }
                else if (groupName.length() > 64) {
                    System.out.println("\nERROR: Name cannot be more than 64 characters.\n");
                    continue;
                }
				
				System.out.print("Enter a group description: ");
				String groupDescription = TestDriver.scanner.nextLine().trim();
				if (groupDescription.isEmpty()) {
                    System.out.println("\nERROR: You must enter a group description.\n");
                    continue;
                }
                else if (groupDescription.length() > 160) {
                    System.out.println("\nERROR: Description cannot be more than 160 characters.\n");
                    continue;
                }
				
			    System.out.print("Enter a group enrollment limit: ");
				long groupEnrollLimit = TestDriver.scanner.nextLong();
				if (groupEnrollLimit <= 0) {
                    System.out.println("\nERROR: Enroll limit must be greater than 0.\n");
                    continue;
                }
                else if (groupEnrollLimit > 999999) {
                    System.out.println("\nERROR: Enroll limit cannot be greater than 999999.\n");
                    continue;
                }
				
				System.out.println();

                TestDriver.createGroup(groupName, groupDescription, groupEnrollLimit);
			}
			else if (command.equals("addtogroup") || command.equals("6")) {
                System.out.println("FUNCTION: addToGroup()\n");

                long group_id = TestDriver.inputGroupID("Enter the group ID: ");
                if (group_id == -1) { continue; }

                long user_id = TestDriver.inputUserID("Enter the user ID: ");
                if (user_id == -1) { continue; }

                System.out.println();

                TestDriver.addToGroup(group_id, user_id);
            }
            else if (command.equals("searchforuser") || command.equals("9")) {
                System.out.println("FUNCTION: searchForUser()\n");

                System.out.print("Enter your search queries, separated by spaces: ");
                String search_str = TestDriver.scanner.nextLine();

                if (search_str.isEmpty()) {
                    System.out.println("ERROR: You must enter a query.\n");
                    continue;
                }
                
                System.out.println();

                TestDriver.searchForUser(search_str);
            }
      else if (command.equals("sendmessagetouser") || command.equals("7")) {
        System.out.println("FUNCTION: sendMessageToUser()\n");

        System.out.println("Enter a subject: ");
        String subject = TestDriver.scanner.nextLine();

        System.out.println("Enter a message: ");
        String message = TestDriver.scanner.nextLine();

        long recipient_id = TestDriver.inputUserID("Enter the ID of the recipient user: ");
        if (recipient_id == -1) { continue; }

        long sender_id = TestDriver.inputUserID("Enter your user ID: ");
        if (sender_id == -1) { continue; }

        TestDriver.sendMessageToUser(subject, message, recipient_id, sender_id);
      }
      else if (command.equals("displaymessages") || command.equals("8")) {
        System.out.println("FUNCTION: displayMessages()\n");

        long recipient_id = TestDriver.inputUserID("Enter your user ID: ");
        if (recipient_id == -1) { continue; }

        System.out.println("");
        TestDriver.displayMessages(recipient_id);
      }
      else if (command.equals("threedegrees") || command.equals("10")) {
        System.out.println("FUNCTION: threeDegrees()\n");

        long f_id = TestDriver.inputUserID("Enter the ID of the first friend: ");
        if (f_id == -1) { continue; }

        long s_id = TestDriver.inputUserID("Enter the ID of the second friend: ");
        if (s_id == -1) { continue; }

        TestDriver.threeDegrees(f_id, s_id);
      }
			else if (command.equals("topmessagers") || command.equals("11")) {
                System.out.println("FUNCTION: topMessagers()\n");
				
				System.out.print("Enter number of results to get back: ");
                long k = TestDriver.scanner.nextLong();
                if(k < 0){
					System.out.println("Error: Must be greater than or equal to 0.\n");
					continue;
				}

                System.out.print("Enter number of months to include: ");
                long x = TestDriver.scanner.nextLong();
                if(x < 1){
					System.out.println("Error: Must be greater than or equal to 1.\n");
					continue;
				}

                System.out.println();

                TestDriver.topMessagers(k, x);
            }
            else if (command.equals("dropuser") || command.equals("12")) {
                System.out.println("FUNCTION: dropUser()\n");

                long user_id = TestDriver.inputUserID("Enter the user's ID: ");
                if (user_id == -1) { continue; }

                System.out.print("\nAre you sure you want to delete user " + user_id + "? " +
                                 "Their data will be removed. (y/n) ");
                String yn = TestDriver.scanner.nextLine().toLowerCase();

                if (!yn.equals("y")) {
                    // Abort if they did not enter 'y'
                    System.out.println("Aborting. User " + user_id + " will not be deleted.\n");
                    continue;
                }
                
                System.out.println();

                TestDriver.dropUser(user_id);
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
                System.out.println("ERROR: Unknown command.\n");
            }
        } // end while

        TestDriver.connection.close();
    }
}
