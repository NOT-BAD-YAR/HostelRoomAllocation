import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class ExportReport {
    public static void main(String[] args) {
        exportStudentAllocations("student_allocations.csv");
        exportRoomVacancies("room_vacancies.csv");
        exportStudentList("student_list.csv");
        System.out.println("Reports exported as CSV successfully!");
    }

    private static void exportStudentAllocations(String filename) {
        String sql = "SELECT s.student_id, s.name, r.room_number, r.type, b.block_name " +
                "FROM students s " +
                "LEFT JOIN rooms r ON s.room_id = r.room_id " +
                "LEFT JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             FileWriter writer = new FileWriter(filename)) {
            writer.write("Student ID,Name,Room Number,Room Type,Block Name\n");
            while (rs.next()) {
                writer.write(rs.getInt("student_id") + "," +
                        rs.getString("name") + "," +
                        rs.getString("room_number") + "," +
                        rs.getString("type") + "," +
                        rs.getString("block_name") + "\n");
            }
        } catch (SQLException | IOException e) {
            System.out.println("Issue exporting allocations: " + e.getMessage());
        }
    }

    private static void exportRoomVacancies(String filename) {
        String sql = "SELECT r.room_number, r.type, b.block_name, r.capacity, r.occupants " +
                "FROM rooms r JOIN blocks b ON r.block_id = b.block_id";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             FileWriter writer = new FileWriter(filename)) {
            writer.write("Room Number,Room Type,Block Name,Capacity,Occupants,Vacancy\n");
            while (rs.next()) {
                int capacity = rs.getInt("capacity");
                int occupants = rs.getInt("occupants");
                writer.write(rs.getString("room_number") + "," +
                        rs.getString("type") + "," +
                        rs.getString("block_name") + "," +
                        capacity + "," + occupants + "," + (capacity - occupants) + "\n");
            }
        } catch (SQLException | IOException e) {
            System.out.println("Issue exporting vacancies: " + e.getMessage());
        }
    }

    private static void exportStudentList(String filename) {
        String sql = "SELECT student_id, name, roll_number, year, course, gender, mobile_number FROM students";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             FileWriter writer = new FileWriter(filename)) {
            writer.write("Student ID,Name,Roll Number,Year,Course,Gender,Mobile Number\n");
            while (rs.next()) {
                writer.write(rs.getInt("student_id") + "," +
                        rs.getString("name") + "," +
                        rs.getString("roll_number") + "," +
                        rs.getInt("year") + "," +
                        rs.getString("course") + "," +
                        rs.getString("gender") + "," +
                        rs.getString("mobile_number") + "\n");
            }
        } catch (SQLException | IOException e) {
            System.out.println("Issue exporting student list: " + e.getMessage());
        }
    }
}
