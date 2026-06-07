package koyokulukutuphaneotomasyonu;

import javax.swing.*;
import java.awt.*;

public class StudentFrame extends JFrame {

    private int userId;
    private String studentName;

    public StudentFrame(int userId, String studentName) {
        this.userId = userId;
        this.studentName = studentName;

        setTitle("Bir Kitap Bir Gelecek - Öğrenci Paneli");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 35, 18, 35));
        mainPanel.setBackground(new Color(248, 245, 235));

        // ===== BAŞLIK ALANI =====
        JPanel titlePanel = new JPanel(new BorderLayout(5, 6));
        titlePanel.setBackground(new Color(248, 245, 235));

        ImageIcon icon = new ImageIcon(
                getClass().getResource("/images/logo.png"));
        Image img = icon.getImage().getScaledInstance(
                70, 70, Image.SCALE_SMOOTH);

        JLabel logoLabel = new JLabel(new ImageIcon(img));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel welcomeLabel =
                new JLabel("Öğrenci Paneli", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 26));
        welcomeLabel.setForeground(new Color(78, 52, 46));

        JLabel nameLabel =
                new JLabel("Hoş geldiniz, " + studentName,
                        SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        nameLabel.setForeground(new Color(90, 90, 90));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        textPanel.setBackground(new Color(248, 245, 235));
        textPanel.add(welcomeLabel);
        textPanel.add(nameLabel);

        titlePanel.add(logoLabel, BorderLayout.NORTH);
        titlePanel.add(textPanel, BorderLayout.CENTER);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // ===== BUTON PANELİ =====
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 12, 12));
        buttonPanel.setBackground(new Color(248, 245, 235));

        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(190, 170, 130), 1),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));

        JButton katalogBtn =
                createButton("Kitap Kataloğunu İncele");

        JButton oduncBtn =
                createButton("Mevcut Ödünçlerim");

        JButton gecmisBtn =
                createButton("Okuma Geçmişim");

        JButton cikisBtn =
                createExitButton("Çıkış Yap");

        katalogBtn.addActionListener(e ->
                new StudentCatalogFrame().setVisible(true));

        oduncBtn.addActionListener(e ->
                new StudentLoansFrame(userId, false).setVisible(true));

        gecmisBtn.addActionListener(e ->
                new StudentLoansFrame(userId, true).setVisible(true));

        cikisBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        buttonPanel.add(katalogBtn);
        buttonPanel.add(oduncBtn);
        buttonPanel.add(gecmisBtn);
        buttonPanel.add(cikisBtn);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // ===== ALT BİLGİ =====
        JLabel statusLabel =
                new JLabel("© 2026 Bir Kitap Bir Gelecek",
                        SwingConstants.CENTER);

        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(Color.GRAY);

        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);

        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(new Color(76, 125, 68));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);

        return btn;
    }

    private JButton createExitButton(String text) {
        JButton btn = createButton(text);
        btn.setBackground(new Color(130, 70, 55));
        return btn;
    }
}