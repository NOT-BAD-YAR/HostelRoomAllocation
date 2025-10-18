import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AutoAllocation {
    public static void main(String[] args) {
        // Get all unassigned students
        String fetchStudents = "SELECT student_id, year, course, gender FROM students WHERE room_id IS NULL";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement psStudents = conn.prepareStatement(fetchStudents);
             ResultSet rsStudents = psStudents.executeQuery()) {

            while (rsStudents.next()) {
                int studentId = rsStudents.getInt("student_id");
                int year = rsStudents.getInt("year");
                String course = rsStudents.getString("course");
                String gender = rsStudents.getString("gender");

                // Find a room with vacancy for this group (year, course, gender)
                int roomId = findVacantRoom(conn, year, course, gender);

                if (roomId > 0) {
                    allocateStudentToRoom(conn, studentId, roomId);
                    System.out.println("Student ID " + studentId + " allocated to Room ID " + roomId + ".");
                } else {
                    System.out.println("No available room for Student ID " + studentId + " (year=" + year + ", course=" + course + ", gender=" + gender + ").");
                }
            }
            System.out.println("Automatic allocation completed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Find a vacant room for the group, preference order: exact match → any vacant room
    private static int findVacantRoom(Connection conn, int year, String course, String gender) throws SQLException {
        // You can enhance this query to restrict by block or other attributes as needed.
        String sql = "SELECT r.room_id " +
                "FROM rooms r " +
                "LEFT JOIN students s ON r.room_id = s.room_id " +
                "WHERE r.occupants < r.capacity " +
                "GROUP BY r.room_id " +
                "ORDER BY r.room_id ASC " + // allocation order
                "LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("room_id");
            }
        }
        return -1; // No room found
    }

    private static void allocateStudentToRoom(Connection conn, int studentId, int roomId) throws SQLException {
        // Update student's room assignment
        String updateStudent = "UPDATE students SET room_id = ? WHERE student_id = ?";
        // Add to allocations table
        String addAllocation = "INSERT INTO allocations (student_id, room_id, alloc_time) VALUES (?, ?, NOW())";
        // Update room's occupants count
        String updateRoom = "UPDATE rooms SET occupants = occupants + 1 WHERE room_id = ?";

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
        }
    }
}
