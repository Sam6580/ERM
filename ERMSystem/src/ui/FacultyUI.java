package src.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import src.client.ERMClient;
import src.model.Reflection;

public class FacultyUI extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea reflectionDetailArea;
    private JTextField searchField;
    private JLabel connectionStatusLabel;
    private JLabel countLabel;
    private JComboBox<String> statusFilter;
    private JButton reviewBtn;
    private ERMClient client;
    private List<Reflection> reflectionList;
    private List<Reflection> filteredList;
    private Reflection currentReflection;
    private static final Color PRIMARY_COLOR = new Color(52, 73, 94);
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private static final Color ERROR_COLOR = new Color(198, 40, 40);
    private static final Color PENDING_COLOR = new Color(255, 152, 0);
    private static final Color BG_COLOR = new Color(250, 250, 250);

    public FacultyUI() {
        setTitle("End Review Reflection System - Faculty Portal");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Use default look and feel if system L&F fails
        }

        createUI();
        setupWindowListener();
        setVisible(true);
        connectToServer();
    }

    private void createUI() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        // Footer Panel with Actions
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Title
        JLabel titleLabel = new JLabel("Reflection Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Connection Status and Count
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statusPanel.setBackground(BG_COLOR);
        
        countLabel = new JLabel("Total: 0");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(new Color(100, 100, 100));
        statusPanel.add(countLabel);

        connectionStatusLabel = new JLabel("● Connecting...");
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        connectionStatusLabel.setForeground(new Color(255, 152, 0));
        statusPanel.add(connectionStatusLabel);

        headerPanel.add(statusPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);

        // Toolbar Panel
        JPanel toolbarPanel = createToolbarPanel();
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);

        // Content Panel with Table and Details
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbarPanel.setBackground(BG_COLOR);
        toolbarPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Search Field
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toolbarPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.addActionListener(e -> filterReflections());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterReflections();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterReflections();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterReflections();
            }
        });
        toolbarPanel.add(searchField);

        // Status Filter
        JLabel filterLabel = new JLabel("Status:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toolbarPanel.add(filterLabel);

        statusFilter = new JComboBox<>(new String[]{"All", "Pending", "Reviewed", "Approved", "Rejected"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setPreferredSize(new Dimension(120, 30));
        statusFilter.addActionListener(e -> filterReflections());
        toolbarPanel.add(statusFilter);

        // Refresh Button
        JButton refreshBtn = createStyledButton("Refresh", PRIMARY_COLOR, 100, 30);
        refreshBtn.addActionListener(e -> fetchReflections());
        toolbarPanel.add(refreshBtn);

        return toolbarPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_COLOR);

        // Table Panel
        JPanel tablePanel = createTablePanel();
        
        // Details Panel
        JPanel detailsPanel = createDetailsPanel();

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, detailsPanel);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(null);

        contentPanel.add(splitPane, BorderLayout.CENTER);

        return contentPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Reflections List", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));

        // Table Model
        String[] columns = {"Name", "Roll No", "Subject", "Status", "Submitted Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        // Custom renderer for status column
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1 && filteredList != null) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < filteredList.size()) {
                    currentReflection = filteredList.get(selectedRow);
                    displayReflectionDetails(currentReflection);
                    reviewBtn.setEnabled(true);
                }
            } else {
                reviewBtn.setEnabled(false);
                currentReflection = null;
            }
        });

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Reflection Details", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));

        reflectionDetailArea = new JTextArea();
        reflectionDetailArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reflectionDetailArea.setEditable(false);
        reflectionDetailArea.setWrapStyleWord(true);
        reflectionDetailArea.setLineWrap(true);
        reflectionDetailArea.setBackground(Color.WHITE);
        reflectionDetailArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        reflectionDetailArea.setText("Select a reflection from the list to view details.");

        JScrollPane scrollPane = new JScrollPane(reflectionDetailArea);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Review Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        reviewBtn = createStyledButton("Review & Provide Feedback", PRIMARY_COLOR, 200, 35);
        reviewBtn.setEnabled(false);
        reviewBtn.addActionListener(e -> openReviewDialog());
        buttonPanel.add(reviewBtn);

        detailsPanel.add(scrollPane, BorderLayout.CENTER);
        detailsPanel.add(buttonPanel, BorderLayout.SOUTH);

        return detailsPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(BG_COLOR);

        JButton fetchBtn = createStyledButton("Fetch Reflections", SUCCESS_COLOR, 150, 40);
        fetchBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fetchBtn.addActionListener(e -> fetchReflections());

        footerPanel.add(fetchBtn);

        return footerPanel;
    }

    private JButton createStyledButton(String text, Color bgColor, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(
                    Math.max(0, bgColor.getRed() - 15),
                    Math.max(0, bgColor.getGreen() - 15),
                    Math.max(0, bgColor.getBlue() - 15)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(
                    FacultyUI.this,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (option == JOptionPane.YES_OPTION) {
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
            }
        });
    }

    private void connectToServer() {
        updateConnectionStatus("Connecting...", new Color(255, 152, 0));
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
                        updateConnectionStatus("Disconnected", ERROR_COLOR);
                        JOptionPane.showMessageDialog(FacultyUI.this,
                            "Failed to connect to the server.",
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        updateConnectionStatus("Connected", SUCCESS_COLOR);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    updateConnectionStatus("Connection Error", ERROR_COLOR);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void updateConnectionStatus(String status, Color color) {
        connectionStatusLabel.setText("● " + status);
        connectionStatusLabel.setForeground(color);
    }

    private void fetchReflections() {
        if (client == null) {
            JOptionPane.showMessageDialog(this,
                "Not connected to the server. Please wait for connection.",
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        new SwingWorker<List<Reflection>, Void>() {
            @Override
            protected List<Reflection> doInBackground() {
                try {
                    return client.fetchReflections();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
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

                    filteredList = reflectionList;
                    updateTable();
                    currentReflection = null;
                    reviewBtn.setEnabled(false);
                    
                    if (reflectionList.isEmpty()) {
                        reflectionDetailArea.setText("No reflections available in the database.");
                        countLabel.setText("Total: 0");
                    } else {
                        countLabel.setText("Total: " + reflectionList.size());
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

    private void filterReflections() {
        if (reflectionList == null) return;

        String searchText = searchField.getText().toLowerCase().trim();
        String statusFilterValue = (String) statusFilter.getSelectedItem();

        filteredList = reflectionList.stream()
            .filter(r -> {
                boolean matchesSearch = searchText.isEmpty() ||
                    r.getStudentName().toLowerCase().contains(searchText) ||
                    r.getRegisterNumber().toLowerCase().contains(searchText) ||
                    r.getSubject().toLowerCase().contains(searchText) ||
                    r.getReflectionText().toLowerCase().contains(searchText);
                
                boolean matchesStatus = statusFilterValue == null || 
                    statusFilterValue.equals("All") ||
                    r.getStatus().equalsIgnoreCase(statusFilterValue);
                
                return matchesSearch && matchesStatus;
            })
            .collect(java.util.stream.Collectors.toList());

        updateTable();
        countLabel.setText("Showing: " + filteredList.size() + " / " + reflectionList.size());
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        if (filteredList != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            for (Reflection r : filteredList) {
                String dateStr = dateFormat.format(java.sql.Timestamp.valueOf(r.getSubmittedAt()));
                tableModel.addRow(new Object[]{
                    r.getStudentName(),
                    r.getRegisterNumber(),
                    r.getSubject(),
                    r.getStatus(),
                    dateStr
                });
            }
        }
    }

    private void displayReflectionDetails(Reflection reflection) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm:ss");
        String details = String.format(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "                         REFLECTION DETAILS\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "STUDENT INFORMATION\n" +
            "─────────────────────────────────────────────────────────────────────────────\n" +
            "Name:           %s\n" +
            "Roll Number:    %s\n" +
            "Subject:        %s\n\n" +
            "REFLECTION\n" +
            "─────────────────────────────────────────────────────────────────────────────\n" +
            "%s\n\n" +
            "SUBMISSION DETAILS\n" +
            "─────────────────────────────────────────────────────────────────────────────\n" +
            "Status:         %s\n" +
            "Submitted At:   %s\n" +
            "Rating:         %.1f / 10.0\n" +
            "Feedback:       %s\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            reflection.getStudentName(),
            reflection.getRegisterNumber(),
            reflection.getSubject(),
            reflection.getReflectionText(),
            reflection.getStatus(),
            dateFormat.format(java.sql.Timestamp.valueOf(reflection.getSubmittedAt())),
            reflection.getRating(),
            reflection.getFacultyFeedback() != null && !reflection.getFacultyFeedback().isEmpty() 
                ? reflection.getFacultyFeedback() : "No feedback provided yet."
        );
        reflectionDetailArea.setText(details);
    }

    private void openReviewDialog() {
        if (currentReflection == null || client == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a reflection first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog reviewDialog = new JDialog(this, "Review Reflection", true);
        reviewDialog.setSize(600, 500);
        reviewDialog.setLocationRelativeTo(this);
        reviewDialog.setLayout(new BorderLayout(10, 10));
        ((JPanel) reviewDialog.getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // Student Info Panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Student Information", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), PRIMARY_COLOR));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(currentReflection.getStudentName()), gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(new JLabel("Roll No:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(currentReflection.getRegisterNumber()), gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(currentReflection.getSubject()), gbc);

        // Rating Panel
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        ratingPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Rating (0.0 - 10.0)", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), PRIMARY_COLOR));
        
        SpinnerNumberModel ratingModel = new SpinnerNumberModel(
            currentReflection.getRating(), 0.0, 10.0, 0.5);
        JSpinner ratingSpinner = new JSpinner(ratingModel);
        ratingSpinner.setPreferredSize(new Dimension(100, 30));
        ratingPanel.add(ratingSpinner);

        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        statusPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Status", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), PRIMARY_COLOR));
        
        String[] statuses = {"Pending", "Reviewed", "Approved", "Rejected"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setSelectedItem(currentReflection.getStatus());
        statusCombo.setPreferredSize(new Dimension(150, 30));
        statusPanel.add(statusCombo);

        // Feedback Panel
        JPanel feedbackPanel = new JPanel(new BorderLayout());
        feedbackPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Faculty Feedback", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), PRIMARY_COLOR));
        
        JTextArea feedbackArea = new JTextArea(
            currentReflection.getFacultyFeedback() != null ? currentReflection.getFacultyFeedback() : "", 5, 30);
        feedbackArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane feedbackScroll = new JScrollPane(feedbackArea);
        feedbackPanel.add(feedbackScroll, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton cancelBtn = createStyledButton("Cancel", new Color(100, 100, 100), 100, 35);
        cancelBtn.addActionListener(e -> reviewDialog.dispose());
        
        JButton saveBtn = createStyledButton("Save Review", SUCCESS_COLOR, 120, 35);
        saveBtn.addActionListener(e -> {
            double rating = ((Number) ratingSpinner.getValue()).doubleValue();
            String status = (String) statusCombo.getSelectedItem();
            String feedback = feedbackArea.getText().trim();
            
            currentReflection.setRating(rating);
            currentReflection.setStatus(status);
            currentReflection.setFacultyFeedback(feedback);
            
            updateReflection(currentReflection);
            reviewDialog.dispose();
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        // Layout
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        topPanel.add(ratingPanel);
        topPanel.add(statusPanel);
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(feedbackPanel, BorderLayout.SOUTH);
        
        reviewDialog.add(mainPanel, BorderLayout.CENTER);
        reviewDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        reviewDialog.setVisible(true);
    }

    private void updateReflection(Reflection reflection) {
        if (client == null) {
            JOptionPane.showMessageDialog(this,
                "Not connected to the server.",
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        reviewBtn.setEnabled(false);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    return client.updateReflection(reflection);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void done() {
                reviewBtn.setEnabled(true);
                try {
                    String response = get();
                    if (response != null && response.startsWith("Error:")) {
                        JOptionPane.showMessageDialog(FacultyUI.this,
                            response,
                            "Update Error",
                            JOptionPane.ERROR_MESSAGE);
                    } else if (response != null && response.startsWith("✅")) {
                        JOptionPane.showMessageDialog(FacultyUI.this,
                            "<html><div style='text-align: center;'>" +
                            "<h3>Success!</h3>" +
                            "Reflection review has been updated successfully.</div></html>",
                            "Update Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Refresh the display
                        displayReflectionDetails(reflection);
                        
                        // Update the table
                        int selectedRow = table.getSelectedRow();
                        if (selectedRow >= 0 && filteredList != null && selectedRow < filteredList.size()) {
                            filteredList.set(selectedRow, reflection);
                            updateTable();
                            table.setRowSelectionInterval(selectedRow, selectedRow);
                        }
                    } else {
                        JOptionPane.showMessageDialog(FacultyUI.this,
                            response,
                            "Update Result",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(FacultyUI.this,
                        "An error occurred while updating: " + e.getMessage(),
                        "Update Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Custom cell renderer for status column
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String status = value != null ? value.toString() : "";
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
            ));

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                String statusLower = status.toLowerCase();
                if (statusLower.equals("pending")) {
                    setBackground(new Color(PENDING_COLOR.getRed(), PENDING_COLOR.getGreen(), PENDING_COLOR.getBlue(), 50));
                    setForeground(PENDING_COLOR);
                } else if (statusLower.equals("approved") || statusLower.equals("reviewed")) {
                    setBackground(new Color(SUCCESS_COLOR.getRed(), SUCCESS_COLOR.getGreen(), SUCCESS_COLOR.getBlue(), 50));
                    setForeground(SUCCESS_COLOR);
                } else if (statusLower.equals("rejected")) {
                    setBackground(new Color(ERROR_COLOR.getRed(), ERROR_COLOR.getGreen(), ERROR_COLOR.getBlue(), 50));
                    setForeground(ERROR_COLOR);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(new Color(100, 100, 100));
                }
            }

            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FacultyUI::new);
    }
}
