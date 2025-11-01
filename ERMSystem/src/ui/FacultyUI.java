package ui;

import client.ERMClient;
import model.Reflection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FacultyUI extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea reflectionDetailArea;
    private ERMClient client;
    private List<Reflection> reflectionList;

    public FacultyUI() {
        setTitle("Faculty - View Reflections");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        setupTableAndDetails();

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

    private void setupTableAndDetails() {
        tableModel = new DefaultTableModel(new String[]{"Name", "Roll No", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScrollPane = new JScrollPane(table);

        reflectionDetailArea = new JTextArea("Select a reflection to see details.");
        reflectionDetailArea.setEditable(false);
        reflectionDetailArea.setWrapStyleWord(true);
        reflectionDetailArea.setLineWrap(true);
        JScrollPane detailScrollPane = new JScrollPane(reflectionDetailArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, detailScrollPane);
        splitPane.setDividerLocation(200);

        add(splitPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                Reflection selected = reflectionList.get(table.getSelectedRow());
                reflectionDetailArea.setText(selected.getReflectionText());
            }
        });
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
                    reflectionList = get();
                    tableModel.setRowCount(0); // Clear existing data
                    for (Reflection r : reflectionList) {
                        tableModel.addRow(new Object[]{r.getStudentName(), r.getRollNo(), r.getStatus()});
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
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
        SwingUtilities.invokeLater(FacultyUI::new);
    }
}
