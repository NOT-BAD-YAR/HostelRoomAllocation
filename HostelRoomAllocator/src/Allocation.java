public class Allocation {
    private int allocId;
    private int studentId;
    private int roomId;
    private String allocTime;

    public Allocation(int allocId, int studentId, int roomId, String allocTime) {
        this.allocId = allocId;
        this.studentId = studentId;
        this.roomId = roomId;
        this.allocTime = allocTime;
    }

    public int getAllocId() { return allocId; }
    public void setAllocId(int allocId) { this.allocId = allocId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getAllocTime() { return allocTime; }
    public void setAllocTime(String allocTime) { this.allocTime = allocTime; }
}
