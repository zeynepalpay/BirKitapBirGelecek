package koyokulukutuphaneotomasyonu;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.sql.*;

public class ReportsFrame extends JFrame {

    private JLabel toplamKitapLabel;
    private JLabel odunctekiLabel;
    private JLabel toplamOgrenciLabel;
    private JLabel kitapKurduLabel;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String[]> studentCombo;

    private final Color BG = new Color(248, 245, 235);
    private final Color CARD = new Color(255, 252, 242);
    private final Color GREEN = new Color(76, 125, 68);
    private final Color LIGHT_GREEN = new Color(110, 140, 90);
    private final Color BROWN = new Color(78, 52, 46);
    private final Color RED_BROWN = new Color(130, 70, 55);
    private final Color BORDER = new Color(190, 170, 130);

    public ReportsFrame() {
        setTitle("Bir Kitap Bir Gelecek - Rapor Merkezi");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        initComponents();
        loadStats();
        loadStudents();
        findBookworm();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 25, 15, 25));
        mainPanel.setBackground(BG);

        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setBackground(BG);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 3, 3));
        titlePanel.setBackground(BG);

        JLabel titleLabel = new JLabel("Kütüphane Rapor Merkezi", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 26));
        titleLabel.setForeground(BROWN);

        JLabel subLabel = new JLabel("Ayın kitap kurdu ve öğrenci okuma karneleri", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subLabel.setForeground(new Color(120, 100, 80));

        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBackground(BG);

        toplamKitapLabel = createStatLabel("Toplam Kitap: -", GREEN);
        odunctekiLabel = createStatLabel("Ödünçteki: -", RED_BROWN);
        toplamOgrenciLabel = createStatLabel("Toplam Öğrenci: -", LIGHT_GREEN);

        statsPanel.add(toplamKitapLabel);
        statsPanel.add(odunctekiLabel);
        statsPanel.add(toplamOgrenciLabel);

        topContainer.add(titlePanel, BorderLayout.NORTH);
        topContainer.add(statsPanel, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BG);

        JPanel bookwormPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 7));
        bookwormPanel.setBackground(CARD);
        bookwormPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER),
                "Ayın Kitap Kurdu"
            ),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        kitapKurduLabel = new JLabel("Hesaplanıyor...");
        kitapKurduLabel.setFont(new Font("Arial", Font.BOLD, 14));
        kitapKurduLabel.setForeground(BROWN);

        bookwormPanel.add(kitapKurduLabel);
        centerPanel.add(bookwormPanel, BorderLayout.NORTH);

        JPanel reportPanel = new JPanel(new BorderLayout(8, 8));
        reportPanel.setBackground(CARD);
        reportPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER),
                "Öğrenci Karnesi"
            ),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        selectPanel.setBackground(CARD);

        JLabel studentLabel = new JLabel("Öğrenci Seç:");
        studentLabel.setFont(new Font("Arial", Font.BOLD, 13));
        studentLabel.setForeground(BROWN);

        studentCombo = new JComboBox<>();
        studentCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        studentCombo.setPreferredSize(new Dimension(240, 28));
        studentCombo.setRenderer((list, value, index, isSelected, hasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value[1] : "");
            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setOpaque(true);
            }
            return lbl;
        });

        JButton showBtn = createButton("Göster", GREEN);
        showBtn.addActionListener(e -> loadStudentReport());

        selectPanel.add(studentLabel);
        selectPanel.add(studentCombo);
        selectPanel.add(showBtn);

        reportPanel.add(selectPanel, BorderLayout.NORTH);

        String[] columns = {"Kitap Adı", "Yazar", "Kategori", "Ödünç Tarihi", "İade Tarihi"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 13));
        reportTable.setRowHeight(26);
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        reportTable.getTableHeader().setBackground(new Color(235, 225, 205));
        reportTable.getTableHeader().setForeground(BROWN);
        reportTable.setGridColor(new Color(210, 195, 165));

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        reportPanel.add(scrollPane, BorderLayout.CENTER);

        JButton csvBtn = createButton("Öğrenci Karnesini CSV Olarak Dışa Aktar", GREEN);
        csvBtn.addActionListener(e -> exportCSV());
        reportPanel.add(csvBtn, BorderLayout.SOUTH);

        centerPanel.add(reportPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JLabel footer = new JLabel("© 2026 Bir Kitap Bir Gelecek", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.PLAIN, 11));
        footer.setForeground(Color.GRAY);
        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadStats() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM books");
            if (rs1.next()) {
                toplamKitapLabel.setText("Toplam Kitap: " + rs1.getInt(1));
            }

            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM books WHERE status='borrowed'");
            if (rs2.next()) {
                odunctekiLabel.setText("Ödünçteki: " + rs2.getInt(1));
            }

            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role='student'");
            if (rs3.next()) {
                toplamOgrenciLabel.setText("Toplam Öğrenci: " + rs3.getInt(1));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "İstatistik hatası: " + e.getMessage());
        }
    }

    private void findBookworm() {
        String query = """
            SELECT u.name, COUNT(l.id) AS kitap_sayisi
            FROM loans l
            JOIN users u ON l.user_id = u.id
            WHERE u.role = 'student'
              AND strftime('%Y-%m', l.return_date) = strftime('%Y-%m', 'now')
            GROUP BY l.user_id
            ORDER BY kitap_sayisi DESC
            LIMIT 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                String name = rs.getString("name");
                int count = rs.getInt("kitap_sayisi");
                kitapKurduLabel.setText(name + " — " + count + " kitap");
            } else {
                kitapKurduLabel.setText("Bu ay henüz iade edilmiş kitap yok.");
            }

        } catch (SQLException e) {
            kitapKurduLabel.setText("Sorgu hatası: " + e.getMessage());
        }
    }

    private void loadStudents() {
        studentCombo.removeAllItems();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name FROM users WHERE role = 'student' ORDER BY name");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                studentCombo.addItem(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("name")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private void loadStudentReport() {
        String[] selected = (String[]) studentCombo.getSelectedItem();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Lütfen bir öğrenci seçin.");
            return;
        }

        int userId = Integer.parseInt(selected[0]);
        tableModel.setRowCount(0);

        // Öğrenci karnesi sorgusu
String query = """
    SELECT COALESCE(b.title, l.book_title, '[Kitap Silindi]') AS title,
           b.author, b.category, l.loan_date,
           COALESCE(l.return_date, 'Henuz iade edilmedi') AS return_date
    FROM loans l
    LEFT JOIN books b ON l.book_id = b.id
    WHERE l.user_id = ?
    ORDER BY l.loan_date DESC
""";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    rs.getString("loan_date"),
                    rs.getString("return_date")
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Bu öğrenciye ait kayıt bulunamadı.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private void exportCSV() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Dışa aktarılacak veri yok.");
            return;
        }

        String[] selected = (String[]) studentCombo.getSelectedItem();
        String studentName = selected != null ? selected[1].replaceAll("\\s+", "_") : "ogrenci";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(studentName + "_karnesi.csv"));

        int result = fileChooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();

        if (!file.getName().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {

            StringBuilder header = new StringBuilder();

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                header.append(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) {
                    header.append(",");
                }
            }

            pw.println(header);

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                StringBuilder line = new StringBuilder();

                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object val = tableModel.getValueAt(row, col);
                    line.append(val != null ? val.toString() : "");

                    if (col < tableModel.getColumnCount() - 1) {
                        line.append(",");
                    }
                }

                pw.println(line);
            }

            JOptionPane.showMessageDialog(this,
                "CSV başarıyla kaydedildi:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Kaydetme hatası: " + e.getMessage());
        }
    }

    private JLabel createStatLabel(String text, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(color);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return lbl;
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}