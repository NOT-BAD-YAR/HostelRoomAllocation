import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class DeleteStudent {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        showStudents();
        System.out.print("Enter Student ID to delete: ");
        int studentId = Integer.parseInt(scanner.nextLine());

        String deleteAlloc = "DELETE FROM allocations WHERE student_id = ?";
        String deleteStudent = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement allocStmt = conn.prepareStatement(deleteAlloc);
             PreparedStatement studentStmt = conn.prepareStatement(deleteStudent)) {

            allocStmt.setInt(1, studentId);
            allocStmt.executeUpdate();

            studentStmt.setInt(1, studentId);
            int rowsDeleted = studentStmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Student deleted successfully!");
            } else {
                System.out.println("No such student or delete failed.");
            }
        } catch (SQLException e) {
            System.out.println("Error while deleting student (check foreign key/allocations constraint):");
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
