package src.ui;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import src.client.ERMClient;
import src.model.Reflection;

public class StudentUI extends JFrame {
    private JTextField nameField, rollField, subjectField;
    private JTextArea reflectionArea;
    private JLabel connectionStatusLabel;
    private JLabel charCountLabel;
    private JButton submitBtn;
    private ERMClient client;
    private static final int MAX_REFLECTION_LENGTH = 5000;
    private static final Color PRIMARY_COLOR = new Color(52, 73, 94);
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private static final Color ERROR_COLOR = new Color(198, 40, 40);
    private static final Color BG_COLOR = new Color(250, 250, 250);

    public StudentUI() {
        setTitle("End Review Reflection System - Student Portal");
        setSize(650, 700);
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

        // Footer Panel with Submit Button
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Title
        JLabel titleLabel = new JLabel("End Review Reflection Submission");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Connection Status
        connectionStatusLabel = new JLabel("● Connecting...");
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        connectionStatusLabel.setForeground(new Color(255, 152, 0));
        connectionStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerPanel.add(connectionStatusLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);

        // Student Information Panel
        JPanel infoPanel = createStudentInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Reflection Panel
        JPanel reflectionPanel = createReflectionPanel();
        mainPanel.add(reflectionPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createStudentInfoPanel() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Student Information", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Name Field
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = createLabel("Full Name:");
        infoPanel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = createTextField("Enter your full name");
        infoPanel.add(nameField, gbc);

        // Roll No Field
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel rollLabel = createLabel("Roll Number:");
        infoPanel.add(rollLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        rollField = createTextField("Enter your roll number");
        infoPanel.add(rollField, gbc);

        // Subject Field
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel subjectLabel = createLabel("Subject:");
        infoPanel.add(subjectLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        subjectField = createTextField("Enter subject name");
        infoPanel.add(subjectField, gbc);

        return infoPanel;
    }

    private JPanel createReflectionPanel() {
        JPanel reflectionPanel = new JPanel(new BorderLayout());
        reflectionPanel.setBackground(Color.WHITE);
        reflectionPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                "Reflection", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));

        reflectionArea = new JTextArea();
        reflectionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reflectionArea.setLineWrap(true);
        reflectionArea.setWrapStyleWord(true);
        reflectionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        reflectionArea.setPreferredSize(new Dimension(0, 250));
        reflectionArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (reflectionArea.getText().equals("Write your reflection here...")) {
                    reflectionArea.setText("");
                    reflectionArea.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (reflectionArea.getText().trim().isEmpty()) {
                    reflectionArea.setText("Write your reflection here...");
                    reflectionArea.setForeground(new Color(150, 150, 150));
                }
            }
        });
        reflectionArea.setText("Write your reflection here...");
        reflectionArea.setForeground(new Color(150, 150, 150));

        // Add character counter
        charCountLabel = new JLabel("0 / " + MAX_REFLECTION_LENGTH + " characters");
        charCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        charCountLabel.setForeground(new Color(100, 100, 100));
        charCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        reflectionArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
            }
        });

        JScrollPane scrollPane = new JScrollPane(reflectionArea);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        wrapper.add(charCountLabel, BorderLayout.SOUTH);

        reflectionPanel.add(wrapper, BorderLayout.CENTER);

        return reflectionPanel;
    }

    private void updateCharCount() {
        int count = reflectionArea.getText().length();
        if (reflectionArea.getForeground() != new Color(150, 150, 150)) {
            count = reflectionArea.getText().replace("Write your reflection here...", "").length();
        }
        charCountLabel.setText(count + " / " + MAX_REFLECTION_LENGTH + " characters");
        if (count > MAX_REFLECTION_LENGTH) {
            charCountLabel.setForeground(ERROR_COLOR);
        } else if (count > MAX_REFLECTION_LENGTH * 0.9) {
            charCountLabel.setForeground(new Color(255, 152, 0));
        } else {
            charCountLabel.setForeground(new Color(100, 100, 100));
        }
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(BG_COLOR);

        submitBtn = new JButton("Submit Reflection");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.setPreferredSize(new Dimension(180, 40));
        submitBtn.setBackground(SUCCESS_COLOR);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> submitReflection());

        // Hover effect
        submitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                submitBtn.setBackground(new Color(SUCCESS_COLOR.getRed() - 10, 
                    SUCCESS_COLOR.getGreen() - 10, SUCCESS_COLOR.getBlue() - 10));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                submitBtn.setBackground(SUCCESS_COLOR);
            }
        });

        footerPanel.add(submitBtn);

        return footerPanel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    private JTextField createTextField(@SuppressWarnings("unused") String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(8, 10, 8, 10)
        ));
        field.setPreferredSize(new Dimension(0, 35));
        // Note: placeholder text would require custom implementation or JXTextField
        return field;
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(
                    StudentUI.this,
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
                        JOptionPane.showMessageDialog(StudentUI.this,
                            "<html><div style='text-align: center;'>" +
                            "Failed to connect to the server.<br>" +
                            "Please ensure the server is running on port 5000.</div></html>",
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        updateConnectionStatus("Connected", SUCCESS_COLOR);
                        // Don't show popup on successful connection - status indicator is enough
                    }
                } catch (Exception e) {
                    updateConnectionStatus("Connection Error", ERROR_COLOR);
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(StudentUI.this,
                        "An error occurred while connecting: " + e.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateConnectionStatus(String status, Color color) {
        connectionStatusLabel.setText("● " + status);
        connectionStatusLabel.setForeground(color);
    }

    private void submitReflection() {
        if (client == null) {
            JOptionPane.showMessageDialog(this,
                "Not connected to the server. Please wait for connection.",
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String rollNo = rollField.getText().trim();
        String subject = subjectField.getText().trim();
        String reflectionText = reflectionArea.getText().trim();

        // Validate fields
        if (name.isEmpty() || nameField.getText().trim().isEmpty()) {
            showFieldError(nameField, "Name is required");
            return;
        }

        if (rollNo.isEmpty() || rollField.getText().trim().isEmpty()) {
            showFieldError(rollField, "Roll number is required");
            return;
        }

        if (subject.isEmpty() || subjectField.getText().trim().isEmpty()) {
            showFieldError(subjectField, "Subject is required");
            return;
        }

        if (reflectionText.isEmpty() || reflectionText.equals("Write your reflection here...")) {
            JOptionPane.showMessageDialog(this,
                "Please write your reflection before submitting.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            reflectionArea.requestFocus();
            return;
        }

        if (reflectionText.length() > MAX_REFLECTION_LENGTH) {
            JOptionPane.showMessageDialog(this,
                "Reflection text exceeds maximum length of " + MAX_REFLECTION_LENGTH + " characters.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Disable submit button during submission
        submitBtn.setEnabled(false);
        submitBtn.setText("Submitting...");

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
                submitBtn.setEnabled(true);
                submitBtn.setText("Submit Reflection");
                try {
                    String response = get();
                    if (response != null && response.startsWith("Error:")) {
                        JOptionPane.showMessageDialog(StudentUI.this,
                            "<html><div style='text-align: center;'>" +
                            response.replace("Error: ", "") + "</div></html>",
                            "Submission Error",
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(StudentUI.this,
                            "<html><div style='text-align: center;'>" +
                            "<h3>Success!</h3>" +
                            "Your reflection has been submitted successfully.</div></html>",
                            "Submission Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
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

    private void showFieldError(JTextField field, String message) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ERROR_COLOR, 2),
            new EmptyBorder(6, 8, 6, 8)
        ));
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
        field.requestFocus();
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(8, 10, 8, 10)
                ));
            }
        });
    }

    private void clearForm() {
        nameField.setText("");
        rollField.setText("");
        subjectField.setText("");
        reflectionArea.setText("Write your reflection here...");
        reflectionArea.setForeground(new Color(150, 150, 150));
        updateCharCount();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentUI::new);
    }
}
