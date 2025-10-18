import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class DeleteBlock {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        showBlocks();
        System.out.print("Enter Block ID to delete: ");
        int blockId = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM blocks WHERE block_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, blockId);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Block deleted successfully!");
            } else {
                System.out.println("No such block or delete failed.");
            }
        } catch (SQLException e) {
            System.out.println("Error while deleting block.");
            e.printStackTrace();
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
            System.out.println("--------------");
        } catch (SQLException e) {
            System.out.println("Could not retrieve blocks.");
        }
    }
}
