import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class StudentEntry {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Add New Student (Validation) ===");
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Roll Number: ");
        String rollNumber = scanner.nextLine();
        System.out.print("Year: ");
        int year = Integer.parseInt(scanner.nextLine());
        System.out.print("Course: ");
        String course = scanner.nextLine();
        System.out.print("Gender: ");
        String gender = scanner.nextLine();
        System.out.print("Mobile Number: ");
        String mobileNumber = scanner.nextLine();
        System.out.print("Parent Details: ");
        String parentDetails = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();

        // Validate data
        if (name.isBlank() || rollNumber.isBlank() || year <= 0 || course.isBlank()
                || !(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"))
                || mobileNumber.length() < 10 || parentDetails.isBlank() || address.isBlank()) {
            System.out.println("Invalid data. Please check all fields and try again.");
            return;
        }

        // Check duplicate roll number
        if (isDuplicateRollNumber(rollNumber)) {
            System.out.println("Duplicate roll number found. Student already exists!");
            return;
        }

        Integer roomId = null; // Not assigned

        String sql = "INSERT INTO students (name, roll_number, year, course, gender, mobile_number, parent_details, address, room_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            if (roomId == null) {
                stmt.setNull(9, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(9, roomId);
            }

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Student added successfully!");
            } else {
                System.out.println("Failed to add student.");
            }
        } catch (SQLException e) {
            System.out.println("Error while adding student to database.");
            e.printStackTrace();
        }
    }

    // Checks for duplicate roll number in DB
    private static boolean isDuplicateRollNumber(String rollNumber) {
        String sql = "SELECT student_id FROM students WHERE roll_number = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
