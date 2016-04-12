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
 ******************************************************************************/

import java.util.Scanner;

public class Driver {
    private Scanner scanner;

    public Driver() {
        scanner = new Scanner(System.in);
    }
    
    public void demo() {
        System.out.println("RUNNING DEMO");

        System.out.println();
    }

    public void createUser(String name, String email, String dob) {
        System.out.println("CREATING USER");

        System.out.println();
    }

    public void initiateFriendship(String initiator_id, String receiver_id) {
        System.out.println("INITIATING FRIENDSHIP");

        System.out.println();
    }
    
    public void establishFriendship(String initiator_id, String receiver_id) {
        System.out.println("ESTABLISHING FRIENDSHIP");

        System.out.println();
    }
    
    public static void main(String[] args) {
        Driver TestDriver = new Driver();

        String command;

        while (true) {
            System.out.println("Command list:\n "
                + "\tquit, createUser, initiateFriendship, establishFriendship");
            System.out.print("Enter a command: ");
            command = TestDriver.scanner.nextLine().toLowerCase();

            if (command.equals("quit")) break;

            System.out.println();

            String initiator_id;
            String receiver_id;

            switch (command) {
            case "createuser":
                System.out.println("FUNCTION: createUser()\n");

                System.out.print("Enter a name: ");
                String name = TestDriver.scanner.nextLine();
                
                System.out.print("Enter an email: ");
                String email = TestDriver.scanner.nextLine();
                
                System.out.print("Enter a dob (format: YYYY-MM-DD): ");
                String dob = TestDriver.scanner.nextLine();

                System.out.println();
                
                TestDriver.createUser(name, email, dob);
                break;

            case "initiatefriendship":
                System.out.println("FUNCTION: initiateFriendship()\n");

                System.out.print("Enter the initiator's user ID: ");
                initiator_id = TestDriver.scanner.nextLine();

                System.out.print("Enter the receiver's user ID: ");
                receiver_id = TestDriver.scanner.nextLine();

                System.out.println();

                TestDriver.initiateFriendship(initiator_id, receiver_id);
                break;

            case "establishfriendship":
                System.out.println("FUNCTION: establishFriendship()\n");

                System.out.print("Enter the initiator's user ID: ");
                initiator_id = TestDriver.scanner.nextLine();

                System.out.print("Enter the receiver's user ID: ");
                receiver_id = TestDriver.scanner.nextLine();

                System.out.println();

                TestDriver.establishFriendship(initiator_id, receiver_id);
                break;
                
            case "demo":
                TestDriver.demo();
                break;

            default:
                System.out.println("Unknown command.\n");
            }
        }
    }
}
