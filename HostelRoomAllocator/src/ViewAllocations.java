import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewAllocations {
    public static void main(String[] args) {
        showAllocations();
        showVacancyReport();
    }

    // List all students with room assignment
    private static void showAllocations() {
        String sql = "SELECT s.student_id, s.name, r.room_number, r.type, b.block_name " +
                "FROM students s " +
                "LEFT JOIN rooms r ON s.room_id = r.room_id " +
                "LEFT JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("--- Student Room Allocations ---");
            while (rs.next()) {
                System.out.println("Student ID: " + rs.getInt("student_id")
                        + ", Name: " + rs.getString("name")
                        + ", Room: " + rs.getString("room_number")
                        + ", Room Type: " + rs.getString("type")
                        + ", Block: " + rs.getString("block_name"));
            }
            System.out.println("-------------------------------");
        } catch (SQLException e) {
            System.out.println("Could not retrieve allocations.");
        }
    }

    // List all rooms with number of occupants and vacancy
    private static void showVacancyReport() {
        String sql = "SELECT r.room_number, r.type, b.block_name, r.capacity, r.occupants " +
                "FROM rooms r " +
                "JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("--- Room Vacancy Report ---");
            while (rs.next()) {
                int capacity = rs.getInt("capacity");
                int occupants = rs.getInt("occupants");
                System.out.println("Room: " + rs.getString("room_number")
                        + ", Type: " + rs.getString("type")
                        + ", Block: " + rs.getString("block_name")
                        + ", Occupancy: " + occupants + "/" + capacity
                        + ", Vacancy: " + (capacity - occupants) + " beds free");
            }
            System.out.println("--------------------------");
        } catch (SQLException e) {
            System.out.println("Could not retrieve vacancy report.");
        }
    }
}
