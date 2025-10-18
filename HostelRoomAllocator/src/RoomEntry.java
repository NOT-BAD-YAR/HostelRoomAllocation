import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class RoomEntry {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Add New Room (Validation) ===");
        System.out.print("Room Number: ");
        String roomNumber = scanner.nextLine();

        showBlocks();
        System.out.print("Enter Block ID: ");
        int blockId = Integer.parseInt(scanner.nextLine());

        System.out.print("Room Type (single, twin, triple, four-sharing): ");
        String type = scanner.nextLine();
        System.out.print("Capacity (1/2/3/4): ");
        int capacity = Integer.parseInt(scanner.nextLine());
        int occupants = 0;

        // Validate data
        if (roomNumber.isBlank() || blockId <= 0 ||
                !(type.equalsIgnoreCase("single") || type.equalsIgnoreCase("twin") || type.equalsIgnoreCase("triple") || type.equalsIgnoreCase("four-sharing")) ||
                capacity <= 0) {
            System.out.println("Invalid data. Please check all fields and try again.");
            return;
        }

        // Check duplicate room number
        if (isDuplicateRoomNumber(roomNumber, blockId)) {
            System.out.println("Duplicate room number found in this block.");
            return;
        }

        String sql = "INSERT INTO rooms (room_number, block_id, type, capacity, occupants) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            stmt.setInt(2, blockId);
            stmt.setString(3, type);
            stmt.setInt(4, capacity);
            stmt.setInt(5, occupants);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Room added successfully!");
            } else {
                System.out.println("Failed to add room.");
            }
        } catch (SQLException e) {
            System.out.println("Error while adding room to database.");
            e.printStackTrace();
        }
    }

    private static boolean isDuplicateRoomNumber(String roomNumber, int blockId) {
        String sql = "SELECT room_id FROM rooms WHERE room_number = ? AND block_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            stmt.setInt(2, blockId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private static void showBlocks() {
        String sql = "SELECT * FROM blocks";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("--- Blocks ---");
            while (rs.next()) {
                System.out.println("Block ID: " + rs.getInt("block_id")
                        + ", Name: " + rs.getString("block_name"));
            }
            System.out.println("----------------");
        } catch (SQLException e) {
            System.out.println("Could not retrieve blocks.");
        }
    }
}
