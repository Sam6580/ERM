package src.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Reflection implements Serializable {

    private int id;
    private String studentName;
    private String registerNumber;
    private String subject;
    private String reflectionText;
    private String facultyFeedback;
    private double rating;
    private LocalDateTime submittedAt;
    private String status;

    public Reflection(String studentName, String registerNumber, String subject, String reflectionText) {
        this(0, studentName, registerNumber, subject, reflectionText, "", 0.0, LocalDateTime.now());
        this.status = "Pending";
    }

    public Reflection(int id, String studentName, String registerNumber, String subject,
                      String reflectionText, String facultyFeedback, double rating, LocalDateTime submittedAt) {
        this.id = id;
        this.studentName = studentName;
        this.registerNumber = registerNumber;
        this.subject = subject;
        this.reflectionText = reflectionText;
        this.facultyFeedback = facultyFeedback;
        this.rating = rating;
        this.submittedAt = submittedAt;
        this.status = "Pending";
    }

    // Getters
    public int getId() { return id; }
    public String getStudentName() { return studentName; }
    public String getRegisterNumber() { return registerNumber; }
    public String getSubject() { return subject; }
    public String getReflectionText() { return reflectionText; }
    public String getFacultyFeedback() { return facultyFeedback; }
    public double getRating() { return rating; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public String getStatus() { return status; }

    // Setters
    public void setFacultyFeedback(String feedback) { this.facultyFeedback = feedback; }
    public void setRating(double rating) { this.rating = rating; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "\n--- Reflection Record ---" +
                "\nStudent: " + studentName +
                "\nRegister No: " + registerNumber +
                "\nSubject: " + subject +
                "\nReflection: " + reflectionText +
                "\nFaculty Feedback: " + facultyFeedback +
                "\nRating: " + rating +
                "\nStatus: " + status +
                "\nSubmitted At: " + submittedAt;
    }
}
