import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class BlockEntry {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Add New Block (Validation) ===");
        System.out.print("Block Name: ");
        String blockName = scanner.nextLine();
        System.out.print("Description: ");
        String description = scanner.nextLine();

        // Validate data
        if (blockName.isBlank() || description.isBlank()) {
            System.out.println("Invalid data. Please check all fields and try again.");
            return;
        }

        // Check duplicate block name
        if (isDuplicateBlockName(blockName)) {
            System.out.println("Duplicate block name found.");
            return;
        }

        String sql = "INSERT INTO blocks (block_name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockName);
            stmt.setString(2, description);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Block added successfully!");
            } else {
                System.out.println("Failed to add block.");
            }
        } catch (SQLException e) {
            System.out.println("Error while adding block to database.");
            e.printStackTrace();
        }
    }

    private static boolean isDuplicateBlockName(String blockName) {
        String sql = "SELECT block_id FROM blocks WHERE block_name = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
