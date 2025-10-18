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
            System.out.println("1. Add Block");
            System.out.println("2. Add Room");
            System.out.println("3. Add Student");
            System.out.println("4. Manual Allocation");
            System.out.println("5. Automatic Allocation");
            System.out.println("6. View Allocations/Reports");
            System.out.println("7. Edit Block");
            System.out.println("8. Edit Room");
            System.out.println("9. Edit Student");
            System.out.println("10. Delete Block");
            System.out.println("11. Delete Room");
            System.out.println("12. Delete Student");
            System.out.println("13. Exit");

            System.out.print("Enter option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    BlockEntry.main(new String[0]);
                    break;
                case 2:
                    RoomEntry.main(new String[0]);
                    break;
                case 3:
                    StudentEntry.main(new String[0]);
                    break;
                case 4:
                    ManualAllocation.main(new String[0]);
                    break;
                case 5:
                    AutoAllocation.main(new String[0]);
                    break;
                case 6:
                    ViewAllocations.main(new String[0]);
                    break;
                case 7:
                    EditBlock.main(new String[0]);
                    break;
                case 8:
                    EditRoom.main(new String[0]);
                    break;
                case 9:
                    EditStudent.main(new String[0]);
                    break;
                case 10:
                    DeleteBlock.main(new String[0]);
                    break;
                case 11:
                    DeleteRoom.main(new String[0]);
                    break;
                case 12:
                    DeleteStudent.main(new String[0]);
                    break;
                case 13:
                    System.out.println("Exiting system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}
