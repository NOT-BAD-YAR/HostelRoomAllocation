import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class DeleteRoom {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        showRooms();
        System.out.print("Enter Room ID to delete: ");
        int roomId = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Room deleted successfully!");
            } else {
                System.out.println("No such room or delete failed.");
            }
        } catch (SQLException e) {
            System.out.println("Error while deleting room.");
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
                        + ", Room Number: " + rs.getString("room_number"));
            }
            System.out.println("-------------");
        } catch (SQLException e) {
            System.out.println("Could not retrieve rooms.");
        }
    }
}
