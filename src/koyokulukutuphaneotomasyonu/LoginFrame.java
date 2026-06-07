package koyokulukutuphaneotomasyonu;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    private final Color BG = new Color(248, 245, 235);
    private final Color CARD = new Color(255, 252, 242);
    private final Color GREEN = new Color(76, 125, 68);
    private final Color BROWN = new Color(78, 52, 46);
    private final Color BORDER = new Color(190, 170, 130);

    public LoginFrame() {
        setTitle("Bir Kitap Bir Gelecek - Giriş");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 35, 18, 35));
        mainPanel.setBackground(BG);

        JPanel titlePanel = new JPanel(new BorderLayout(5, 8));
        titlePanel.setBackground(BG);

        ImageIcon icon = new ImageIcon(getClass().getResource("/images/logo.png"));
        Image img = icon.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);

        JLabel logoLabel = new JLabel(new ImageIcon(img));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel("Bir Kitap Bir Gelecek", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 30));
        titleLabel.setForeground(BROWN);

        JLabel subtitleLabel = new JLabel(
            "Öğretmen ve öğrenciler için kütüphane takip sistemi",
            SwingConstants.CENTER
        );
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        subtitleLabel.setForeground(new Color(95, 95, 95));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        textPanel.setBackground(BG);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        titlePanel.add(logoLabel, BorderLayout.NORTH);
        titlePanel.add(textPanel, BorderLayout.CENTER);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(20, 28, 20, 28)
        ));
        formPanel.setBackground(CARD);

        JLabel usernameLabel = new JLabel("Kullanıcı Adı");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        usernameLabel.setForeground(BROWN);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel passwordLabel = new JLabel("Şifre");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 13));
        passwordLabel.setForeground(BROWN);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JButton loginButton = new JButton("Kütüphaneye Giriş Yap");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(GREEN);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(100, 42));

        loginButton.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setBackground(BG);

        bottomPanel.add(loginButton, BorderLayout.NORTH);

        JLabel footerLabel = new JLabel(
            "© 2026 Bir Kitap Bir Gelecek",
            SwingConstants.CENTER
        );
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        footerLabel.setForeground(Color.GRAY);

        bottomPanel.add(footerLabel, BorderLayout.SOUTH);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Kullanıcı adı ve şifre boş bırakılamaz.",
                "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, role FROM users WHERE username = ? AND password = ?")) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String name = rs.getString("name");
                String role = rs.getString("role");

                dispose();

                if ("teacher".equals(role)) {
                    new TeacherFrame(name).setVisible(true);
                } else if ("student".equals(role)) {
                    new StudentFrame(userId, name).setVisible(true);
                }

            } else {
                JOptionPane.showMessageDialog(this,
                    "Kullanıcı adı veya şifre hatalı!",
                    "Giriş Başarısız", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Veritabanı hatası: " + e.getMessage(),
                "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}