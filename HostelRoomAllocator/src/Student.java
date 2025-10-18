public class Student {
    private int studentId;
    private String name;
    private String rollNumber;
    private int year;
    private String course;
    private String gender;
    private String mobileNumber;
    private String parentDetails;
    private String address;
    private int roomId;

    public Student(int studentId, String name, String rollNumber, int year, String course, String gender,
                   String mobileNumber, String parentDetails, String address, int roomId) {
        this.studentId = studentId;
        this.name = name;
        this.rollNumber = rollNumber;
        this.year = year;
        this.course = course;
        this.gender = gender;
        this.mobileNumber = mobileNumber;
        this.parentDetails = parentDetails;
        this.address = address;
        this.roomId = roomId;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getParentDetails() { return parentDetails; }
    public void setParentDetails(String parentDetails) { this.parentDetails = parentDetails; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
}
