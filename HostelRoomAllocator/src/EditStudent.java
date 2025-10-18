import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class EditStudent {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        showStudents();
        System.out.print("Enter Student ID to edit: ");
        int studentId = Integer.parseInt(scanner.nextLine());

        System.out.print("New Name: ");
        String name = scanner.nextLine();
        System.out.print("New Roll Number: ");
        String rollNumber = scanner.nextLine();
        System.out.print("New Year: ");
        int year = Integer.parseInt(scanner.nextLine());
        System.out.print("New Course: ");
        String course = scanner.nextLine();
        System.out.print("New Gender: ");
        String gender = scanner.nextLine();
        System.out.print("New Mobile Number: ");
        String mobileNumber = scanner.nextLine();
        System.out.print("New Parent Details: ");
        String parentDetails = scanner.nextLine();
        System.out.print("New Address: ");
        String address = scanner.nextLine();

        String sql = "UPDATE students SET name = ?, roll_number = ?, year = ?, course = ?, gender = ?, mobile_number = ?, parent_details = ?, address = ? WHERE student_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, rollNumber);
            stmt.setInt(3, year);
            stmt.setString(4, course);
            stmt.setString(5, gender);
            stmt.setString(6, mobileNumber);
            stmt.setString(7, parentDetails);
            stmt.setString(8, address);
            stmt.setInt(9, studentId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Student updated successfully!");
            } else {
                System.out.println("No such student or nothing changed.");
            }
        } catch (SQLException e) {
            System.out.println("Error while editing student.");
            e.printStackTrace();
        }
    }

    private static void showStudents() {
        String sql = "SELECT student_id, name FROM students";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("--- Students ---");
            while (rs.next()) {
                System.out.println("Student ID: " + rs.getInt("student_id")
                        + ", Name: " + rs.getString("name"));
            }
            System.out.println("---------------");
        } catch (SQLException e) {
            System.out.println("Could not retrieve students.");
        }
    }
}
