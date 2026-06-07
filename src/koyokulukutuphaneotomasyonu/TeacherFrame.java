package koyokulukutuphaneotomasyonu;

import javax.swing.*;
import java.awt.*;

public class TeacherFrame extends JFrame {

    private String teacherName;

    public TeacherFrame(String teacherName) {
        this.teacherName = teacherName;
        setTitle("Bir Kitap Bir Gelecek - Öğretmen Paneli");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 35, 18, 35));
        mainPanel.setBackground(new Color(248, 245, 235));

        JPanel titlePanel = new JPanel(new BorderLayout(5, 6));
        titlePanel.setBackground(new Color(248, 245, 235));

        ImageIcon icon = new ImageIcon(getClass().getResource("/images/logo.png"));
        Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);

        JLabel logoLabel = new JLabel(new ImageIcon(img));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel welcomeLabel = new JLabel("Öğretmen Paneli", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 26));
        welcomeLabel.setForeground(new Color(78, 52, 46));

        JLabel nameLabel = new JLabel("Hoş geldiniz, " + teacherName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        nameLabel.setForeground(new Color(90, 90, 90));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        textPanel.setBackground(new Color(248, 245, 235));
        textPanel.add(welcomeLabel);
        textPanel.add(nameLabel);

        titlePanel.add(logoLabel, BorderLayout.NORTH);
        titlePanel.add(textPanel, BorderLayout.CENTER);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 12, 12));
        buttonPanel.setBackground(new Color(248, 245, 235));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(190, 170, 130), 1),
            BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));

        JButton kitapYonetimiBtn = createButton("Kitap Arşivi ve Yönetimi");
        JButton uyeYonetimiBtn   = createButton("Öğrenci ve Öğretmen Kayıtları");
        JButton oduncIadeBtn     = createButton("Kitap Zimmet ve Teslim Takibi");
        JButton raporlarBtn      = createButton("Kütüphane Rapor Merkezi");
        JButton cikisBtn         = createExitButton("Çıkış Yap");

        kitapYonetimiBtn.addActionListener(e ->
            new BookManagementFrame().setVisible(true));

        uyeYonetimiBtn.addActionListener(e ->
            new MemberManagementFrame().setVisible(true));

        oduncIadeBtn.addActionListener(e ->
            new LoanManagementFrame().setVisible(true));

        raporlarBtn.addActionListener(e ->
            new ReportsFrame().setVisible(true));

        cikisBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        buttonPanel.add(kitapYonetimiBtn);
        buttonPanel.add(uyeYonetimiBtn);
        buttonPanel.add(oduncIadeBtn);
        buttonPanel.add(raporlarBtn);
        buttonPanel.add(cikisBtn);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel("© 2026 Bir Kitap Bir Gelecek", SwingConstants.CENTER);
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