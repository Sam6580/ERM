package ui;

import client.ERMClient;
import model.Reflection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FacultyUI extends JFrame {
    private final JTextArea displayArea;
    private ERMClient client;

    public FacultyUI() {
        setTitle("Faculty - View Reflections");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        JButton fetchBtn = new JButton("Fetch Reflections");
        fetchBtn.addActionListener(e -> fetchReflections());
        add(fetchBtn, BorderLayout.SOUTH);

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
                        JOptionPane.showMessageDialog(FacultyUI.this, "Failed to connect to the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void fetchReflections() {
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Not connected to the server.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<List<Reflection>, Void>() {
            @Override
            protected List<Reflection> doInBackground() {
                try {
                    return client.fetchReflections();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Reflection> list = get();
                    displayArea.setText("");
                    for (Reflection r : list) {
                        displayArea.append("Name: " + r.getStudentName() + "\n");
                        displayArea.append("Roll No: " + r.getRollNo() + "\n");
                        displayArea.append("Reflection: " + r.getReflectionText() + "\n");
                        displayArea.append("Status: " + r.getStatus() + "\n");
                        displayArea.append("------------------------------------\n");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FacultyUI::new);
    }
}
