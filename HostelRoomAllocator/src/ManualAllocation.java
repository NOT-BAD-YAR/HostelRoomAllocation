import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ManualAllocation {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Manual Student to Room Allocation ===");

        // List unassigned students
        showUnassignedStudents();
        System.out.print("Enter Student ID to assign: ");
        int studentId = Integer.parseInt(scanner.nextLine());

        // List available rooms
        showAvailableRooms();
        System.out.print("Enter Room ID to assign student to: ");
        int roomId = Integer.parseInt(scanner.nextLine());

        // Update student's room_id
        String updateStudent = "UPDATE students SET room_id = ? WHERE student_id = ?";
        // Add allocation record
        String addAllocation = "INSERT INTO allocations (student_id, room_id, alloc_time) VALUES (?, ?, NOW())";
        // Update room's occupant count
        String updateRoom = "UPDATE rooms SET occupants = occupants + 1 WHERE room_id = ?";

        try (Connection conn = DatabaseHelper.getConnection()) {
            conn.setAutoCommit(false); // Transaction

            try (PreparedStatement ps1 = conn.prepareStatement(updateStudent);
                 PreparedStatement ps2 = conn.prepareStatement(addAllocation);
                 PreparedStatement ps3 = conn.prepareStatement(updateRoom)) {
                ps1.setInt(1, roomId);
                ps1.setInt(2, studentId);
                ps1.executeUpdate();

                ps2.setInt(1, studentId);
                ps2.setInt(2, roomId);
                ps2.executeUpdate();

                ps3.setInt(1, roomId);
                ps3.executeUpdate();

                conn.commit();
                System.out.println("Student assigned to room successfully!");
            } catch (SQLException ex) {
                conn.rollback();
                System.out.println("Allocation failed. Database rolled back.");
                ex.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showUnassignedStudents() {
        String sql = "SELECT student_id, name FROM students WHERE room_id IS NULL";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("--- Unassigned Students ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("student_id") +
                        ", Name: " + rs.getString("name"));
            }
            System.out.println("---------------------------");
        } catch (SQLException e) {
            System.out.println("Could not retrieve students.");
        }
    }

    private static void showAvailableRooms() {
        String sql = "SELECT room_id, room_number, type, occupants, capacity FROM rooms WHERE occupants < capacity";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("--- Available Rooms ---");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("room_id")
                        + ", Room Number: " + rs.getString("room_number")
                        + ", Type: " + rs.getString("type")
                        + ", Occupants: " + rs.getInt("occupants")
                        + ", Capacity: " + rs.getInt("capacity"));
            }
            System.out.println("----------------------");
        } catch (SQLException e) {
            System.out.println("Could not retrieve rooms.");
        }
    }
}
