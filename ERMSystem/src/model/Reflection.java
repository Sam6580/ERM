package model;

import java.io.Serializable;

public class Reflection implements Serializable {
    private String studentName;
    private String rollNo;
    private String reflectionText;
    private String status;

    public Reflection(String studentName, String rollNo, String reflectionText) {
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.reflectionText = reflectionText;
        this.status = "Pending";
    }

    public String getStudentName() { return studentName; }
    public String getRollNo() { return rollNo; }
    public String getReflectionText() { return reflectionText; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
