package ui;

import client.ERMClient;
import model.Reflection;

import javax.swing.*;
import java.awt.*;

public class StudentUI extends JFrame {
    private JTextField nameField, rollField;
    private JTextArea reflectionArea;
    private ERMClient client;

    public StudentUI() {
        client = new ERMClient();

        setTitle("Student - End Review Reflection");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2, 2));
        top.add(new JLabel("Name:"));
        nameField = new JTextField();
        top.add(nameField);
        top.add(new JLabel("Roll No:"));
        rollField = new JTextField();
        top.add(rollField);
        add(top, BorderLayout.NORTH);

        reflectionArea = new JTextArea("Write your reflection here...");
        add(new JScrollPane(reflectionArea), BorderLayout.CENTER);

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> submitReflection());
        add(submitBtn, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void submitReflection() {
        Reflection reflection = new Reflection(
                nameField.getText(),
                rollField.getText(),
                reflectionArea.getText()
        );
        String response = client.sendReflection(reflection);
        JOptionPane.showMessageDialog(this, response);
    }

    public static void main(String[] args) {
        new StudentUI();
    }
}
