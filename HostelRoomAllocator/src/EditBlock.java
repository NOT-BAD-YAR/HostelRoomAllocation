import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class EditBlock {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        showBlocks();
        System.out.print("Enter Block ID to edit: ");
        int blockId = Integer.parseInt(scanner.nextLine());

        System.out.print("New Block Name: ");
        String blockName = scanner.nextLine();
        System.out.print("New Description: ");
        String description = scanner.nextLine();

        String sql = "UPDATE blocks SET block_name = ?, description = ? WHERE block_id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockName);
            stmt.setString(2, description);
            stmt.setInt(3, blockId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Block updated successfully!");
            } else {
                System.out.println("No such block or nothing changed.");
            }
        } catch (SQLException e) {
            System.out.println("Error while editing block.");
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
                        + ", Name: " + rs.getString("block_name")
                        + ", Desc: " + rs.getString("description"));
            }
            System.out.println("--------------");
        } catch (SQLException e) {
            System.out.println("Could not retrieve blocks.");
        }
    }
}
