package src.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import src.client.ERMClient;
import src.model.Reflection;

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
        tableModel = new DefaultTableModel(new String[]{"Name", "Register No", "Status"}, 0) {
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
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1 && reflectionList != null) {
                Reflection selected = reflectionList.get(table.getSelectedRow());
                reflectionDetailArea.setText(selected.toString());
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
            JOptionPane.showMessageDialog(this, "Not connected to the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<List<Reflection>, Void>() {
            @Override
            protected List<Reflection> doInBackground() {
                try {
                    return client.fetchReflections();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return null; // Return null to indicate error
                }
            }

            @Override
            protected void done() {
                try {
                    reflectionList = get();
                    if (reflectionList == null) {
                        JOptionPane.showMessageDialog(FacultyUI.this,
                                "Failed to fetch reflections from server. Please check server connection and try again.",
                                "Fetch Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    tableModel.setRowCount(0); // Clear existing data
                    if (reflectionList.isEmpty()) {
                        JOptionPane.showMessageDialog(FacultyUI.this,
                                "No reflections found in the database.",
                                "No Data",
                                JOptionPane.INFORMATION_MESSAGE);
                        reflectionDetailArea.setText("No reflections available.");
                    } else {
                        for (Reflection r : reflectionList) {
                            tableModel.addRow(new Object[]{r.getStudentName(), r.getRegisterNumber(), r.getStatus()});
                        }
                        JOptionPane.showMessageDialog(FacultyUI.this,
                                "Successfully fetched " + reflectionList.size() + " reflection(s).",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(FacultyUI.this,
                            "An error occurred while fetching reflections: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FacultyUI::new);
    }
}
