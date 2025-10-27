package ui;

import client.ERMClient;
import model.Reflection;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FacultyUI extends JFrame {
    private JTextArea displayArea;
    private ERMClient client;

    public FacultyUI() {
        client = new ERMClient();

        setTitle("Faculty - View Reflections");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        displayArea = new JTextArea();
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        JButton fetchBtn = new JButton("Fetch Reflections");
        fetchBtn.addActionListener(e -> fetchReflections());
        add(fetchBtn, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void fetchReflections() {
        List<Reflection> list = client.fetchReflections();
        displayArea.setText("");
        for (Reflection r : list) {
            displayArea.append("Name: " + r.getStudentName() + "\n");
            displayArea.append("Roll No: " + r.getRollNo() + "\n");
            displayArea.append("Reflection: " + r.getReflectionText() + "\n");
            displayArea.append("Status: " + r.getStatus() + "\n");
            displayArea.append("------------------------------------\n");
        }
    }

    public static void main(String[] args) {
        new FacultyUI();
    }
}
