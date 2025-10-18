import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EditRoom {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        showRooms();
        System.out.print("Enter Room ID to edit: ");
        int roomId = Integer.parseInt(scanner.nextLine());

        // Fetch the old capacity and occupant count before editing
        int oldCapacity = getRoomCapacity(roomId);
        int oldOccupants = getRoomOccupants(roomId);

        System.out.print("New Room Number: ");
        String roomNumber = scanner.nextLine();
        System.out.print("New Block ID: ");
        int blockId = Integer.parseInt(scanner.nextLine());
        System.out.print("New Type (single, twin, triple, four-sharing): ");
        String type = scanner.nextLine();
        System.out.print("New Capacity: ");
        int newCapacity = Integer.parseInt(scanner.nextLine());

        String sql = "UPDATE rooms SET room_number = ?, block_id = ?, type = ?, capacity = ? WHERE room_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            stmt.setInt(2, blockId);
            stmt.setString(3, type);
            stmt.setInt(4, newCapacity);
            stmt.setInt(5, roomId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Room updated successfully!");

                // If capacity decreased and over capacity, remove last joined students
                if (newCapacity < oldOccupants) {
                    int toRemove = oldOccupants - newCapacity;
                    removeLastJoinedStudents(roomId, toRemove);
                }
            } else {
                System.out.println("No such room or nothing changed.");
            }
        } catch (SQLException e) {
            System.out.println("Error while editing room.");
            e.printStackTrace();
        }
    }

    private static void showRooms() {
        String sql = "SELECT * FROM rooms";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("--- Rooms ---");
            while (rs.next()) {
                System.out.println("Room ID: " + rs.getInt("room_id")
                        + ", Room Number: " + rs.getString("room_number")
                        + ", Type: " + rs.getString("type")
                        + ", Capacity: " + rs.getInt("capacity")
                        + ", Block ID: " + rs.getInt("block_id"));
            }
            System.out.println("-------------");
        } catch (SQLException e) {
            System.out.println("Could not retrieve rooms.");
        }
    }

    private static int getRoomCapacity(int roomId) {
        String sql = "SELECT capacity FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("capacity");
            }
        } catch (SQLException e) {
            // ignore
        }
        return 0;
    }

    private static int getRoomOccupants(int roomId) {
        String sql = "SELECT COUNT(*) AS occupant_count FROM students WHERE room_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("occupant_count");
            }
        } catch (SQLException e) {
            // ignore
        }
        return 0;
    }

    // Removes the last joined students by allocation time (most recent first)
    private static void removeLastJoinedStudents(int roomId, int count) {
        String selectSql = "SELECT s.student_id FROM students s " +
                "JOIN allocations a ON s.student_id = a.student_id " +
                "WHERE s.room_id = ? " +
                "ORDER BY a.alloc_time DESC " +
                "LIMIT ?";
        String updateSql = "UPDATE students SET room_id = NULL WHERE student_id = ?";
        String updateOccupants = "UPDATE rooms SET occupants = occupants - 1 WHERE room_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql);
             PreparedStatement updateRoomStmt = conn.prepareStatement(updateOccupants)) {
            selectStmt.setInt(1, roomId);
            selectStmt.setInt(2, count);
            ResultSet rs = selectStmt.executeQuery();

            int removed = 0;
            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                updateStmt.setInt(1, studentId);
                updateStmt.executeUpdate();
                removed++;
            }
            // Update occupants count
            updateRoomStmt.setInt(1, roomId);
            for (int i = 0; i < removed; i++) {
                updateRoomStmt.executeUpdate();
            }
            if (removed > 0) {
                System.out.println("Removed " + removed + " last-joined student(s) from room due to reduced capacity.");
            }
        } catch (SQLException e) {
            System.out.println("Error auto-removing students for capacity change.");
            e.printStackTrace();
        }
    }
}
