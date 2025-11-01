package ui;

import client.ERMClient;
import model.Reflection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class StudentUI extends JFrame {
    private final JTextField nameField, rollField;
    private final JTextArea reflectionArea;
    private final JButton submitBtn;
    private ERMClient client;

    public StudentUI() {
        setTitle("Student - End Review Reflection");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        formPanel.add(new JLabel("Roll No:"));
        rollField = new JTextField();
        formPanel.add(rollField);
        add(formPanel, BorderLayout.NORTH);

        reflectionArea = new JTextArea("Write your reflection here...");
        reflectionArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(new JScrollPane(reflectionArea), BorderLayout.CENTER);

        submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> submitReflection());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        southPanel.add(submitBtn);
        add(southPanel, BorderLayout.SOUTH);

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
                        JOptionPane.showMessageDialog(StudentUI.this, "Failed to connect to the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
        String reflectionText = reflectionArea.getText().trim();

        if (name.isEmpty() || rollNo.isEmpty() || reflectionText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Reflection reflection = new Reflection(name, rollNo, reflectionText);
        submitBtn.setEnabled(false);

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
                    JOptionPane.showMessageDialog(StudentUI.this, response);
                    if (response.startsWith("Success")) {
                        nameField.setText("");
                        rollField.setText("");
                        reflectionArea.setText("");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    submitBtn.setEnabled(true);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to default look and feel.
        }
        SwingUtilities.invokeLater(StudentUI::new);
    }
}
