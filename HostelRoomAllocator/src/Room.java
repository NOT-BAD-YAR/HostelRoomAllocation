public class Room {
    private int roomId;
    private String roomNumber;
    private int blockId;
    private String type; // single, twin, triple, four-sharing
    private int capacity;
    private int occupants;

    public Room(int roomId, String roomNumber, int blockId, String type, int capacity, int occupants) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.blockId = blockId;
        this.type = type;
        this.capacity = capacity;
        this.occupants = occupants;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getBlockId() { return blockId; }
    public void setBlockId(int blockId) { this.blockId = blockId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getOccupants() { return occupants; }
    public void setOccupants(int occupants) { this.occupants = occupants; }
}
