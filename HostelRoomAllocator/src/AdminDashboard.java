import java.util.Scanner;

public class AdminDashboard {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Welcome to Hostel Room Allocation System ===");

        // Require admin login before proceeding
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Admin username: ");
            String username = scanner.nextLine();
            System.out.print("Admin password: ");
            String password = scanner.nextLine();
            loggedIn = AdminLogin.authenticate(username, password);
            if (!loggedIn) {
                System.out.println("Invalid login, try again.\n");
            }
        }

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Add Student");
            System.out.println("2. Add Room");
            System.out.println("3. Manual Allocation");
            System.out.println("4. Automatic Allocation");
            System.out.println("5. View Allocations/Reports");
            System.out.println("6. Exit");

            System.out.print("Enter option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    StudentEntry.main(new String[0]);
                    break;
                case 2:
                    RoomEntry.main(new String[0]);
                    break;
                case 3:
                    ManualAllocation.main(new String[0]);
                    break;
                case 4:
                    AutoAllocation.main(new String[0]);
                    break;
                case 5:
                    ViewAllocations.main(new String[0]);
                    break;
                case 6:
                    System.out.println("Exiting system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}
