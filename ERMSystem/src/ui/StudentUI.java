package src.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.*;
import src.client.ERMClient;
import src.model.Reflection;

public class StudentUI extends JFrame {
    private final JTextField nameField, rollField, subjectField;
    private final JTextArea reflectionArea;
    private ERMClient client;

    public StudentUI() {
        setTitle("Student - End Review Reflection");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(3, 2));
        top.add(new JLabel("Name:"));
        nameField = new JTextField();
        top.add(nameField);
        top.add(new JLabel("Roll No:"));
        rollField = new JTextField();
        top.add(rollField);
        top.add(new JLabel("Subject:"));
        subjectField = new JTextField();
        top.add(subjectField);
        add(top, BorderLayout.NORTH);

        reflectionArea = new JTextArea("Write your reflection here...");
        add(new JScrollPane(reflectionArea), BorderLayout.CENTER);

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> submitReflection());
        add(submitBtn, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                dispose();
                System.exit(0);
            }
        });

        setVisible(true);
        connectToServer();
    }

    private void connectToServer() {
        new SwingWorker<ERMClient, Void>() {
            @Override
            protected ERMClient doInBackground() {
                try {
                    return new ERMClient();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    client = get();
                    if (client == null) {
                        JOptionPane.showMessageDialog(StudentUI.this,
                                "Failed to connect to the server. Please make sure the server is running on port 5000.",
                                "Connection Error",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(StudentUI.this,
                                "Successfully connected to the server.",
                                "Connected",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(StudentUI.this,
                            "An error occurred while connecting: " + e.getMessage(),
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void submitReflection() {
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Not connected to the server.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String rollNo = rollField.getText().trim();
        String subject = subjectField.getText().trim();
        String reflectionText = reflectionArea.getText().trim();

        if (name.isEmpty() || rollNo.isEmpty() || subject.isEmpty() || reflectionText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Reflection reflection = new Reflection(name, rollNo, subject, reflectionText);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    return client.sendReflection(reflection);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    if (response != null && response.startsWith("Error:")) {
                        JOptionPane.showMessageDialog(StudentUI.this, response, "Submission Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(StudentUI.this, response, "Submission Result", JOptionPane.INFORMATION_MESSAGE);
                        // Clear form after successful submission
                        nameField.setText("");
                        rollField.setText("");
                        subjectField.setText("");
                        reflectionArea.setText("Write your reflection here...");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(StudentUI.this,
                            "An error occurred while submitting: " + e.getMessage(),
                            "Submission Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentUI::new);
    }
}
