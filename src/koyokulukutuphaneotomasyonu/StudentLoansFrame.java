package koyokulukutuphaneotomasyonu;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;

public class StudentLoansFrame extends JFrame {

    private int userId;
    private boolean historyMode;
    private JTable loansTable;
    private DefaultTableModel tableModel;
    private JLabel toplamLabel;
    private JLabel aktifLabel;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Color BG       = new Color(248, 245, 235);
    private final Color GREEN    = new Color(76, 125, 68);
    private final Color BROWN    = new Color(78, 52, 46);
    private final Color RED_BROWN = new Color(130, 70, 55);
    private final Color BORDER   = new Color(190, 170, 130);

    public StudentLoansFrame(int userId, boolean historyMode) {
        this.userId = userId;
        this.historyMode = historyMode;
        setTitle(historyMode ? "Bir Kitap Bir Gelecek - Okuma Geçmişim"
                             : "Bir Kitap Bir Gelecek - Mevcut Ödünçlerim");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 580);
        setLocationRelativeTo(null);
        initComponents();
        loadLoans();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 25, 15, 25));
        mainPanel.setBackground(BG);

        // --- ÜST ---
        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setBackground(BG);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 3, 3));
        titlePanel.setBackground(BG);

        JLabel titleLabel = new JLabel(
            historyMode ? "Okuma Geçmişim" : "Mevcut Ödünçlerim",
            SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 26));
        titleLabel.setForeground(BROWN);

        JLabel subLabel = new JLabel(
            historyMode ? "Tamamladığınız kitap kayıtlarını görüntüleyin"
                        : "Aktif ödünçlerinizi ve gecikmeleri görüntüleyin",
            SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subLabel.setForeground(new Color(120, 100, 80));

        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);

        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        statsPanel.setBackground(BG);

        toplamLabel = createStatLabel(
            historyMode ? "Tamamlanan: -" : "Aktif Ödünç: -",
            GREEN);
        aktifLabel = createStatLabel(
            historyMode ? "Toplam Kitap: -" : "Gecikmiş: -",
            RED_BROWN);

        statsPanel.add(toplamLabel);
        statsPanel.add(aktifLabel);

        topContainer.add(titlePanel, BorderLayout.NORTH);
        topContainer.add(statsPanel, BorderLayout.SOUTH);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // --- TABLO ---
        String[] columns = historyMode
            ? new String[]{"Kitap Adı", "Yazar", "Kategori", "Ödünç Tarihi", "İade Tarihi"}
            : new String[]{"Kitap Adı", "Yazar", "Kategori", "Ödünç Tarihi", "Son Teslim", "Durum"};

        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        loansTable = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) return c;

                if (historyMode) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(GREEN);
                } else {
                    int modelRow  = convertRowIndexToModel(row);
                    String dueStr = (String) tableModel.getValueAt(modelRow, 4);
                    String durum  = (String) tableModel.getValueAt(modelRow, 5);
                    if ("Gecikmiş".equals(durum)) {
                        c.setBackground(new Color(255, 225, 220));
                        c.setForeground(Color.RED);
                    } else {
                        c.setBackground(new Color(255, 252, 220));
                        c.setForeground(new Color(150, 95, 25));
                    }
                }
                return c;
            }
        };

        loansTable.setFont(new Font("Arial", Font.PLAIN, 13));
        loansTable.setRowHeight(26);
        loansTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        loansTable.getTableHeader().setBackground(new Color(235, 225, 205));
        loansTable.getTableHeader().setForeground(BROWN);
        loansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loansTable.setGridColor(new Color(210, 195, 165));

        JScrollPane scrollPane = new JScrollPane(loansTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- ALT ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 8));
        bottomPanel.setBackground(BG);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        legendPanel.setBackground(BG);

        if (historyMode) {
            legendPanel.add(colorLegend("Tamamlandı", GREEN));
        } else {
            legendPanel.add(colorLegend("Aktif",    new Color(150, 95, 25)));
            legendPanel.add(colorLegend("Gecikmiş", Color.RED));
        }

        JLabel footer = new JLabel("© 2026 Bir Kitap Bir Gelecek", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.PLAIN, 11));
        footer.setForeground(Color.GRAY);

        bottomPanel.add(legendPanel, BorderLayout.NORTH);
        bottomPanel.add(footer, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadLoans() {
        tableModel.setRowCount(0);
        int toplam = 0;
        int gecikme = 0;

        String query;
if (historyMode) {
    query = """
        SELECT COALESCE(b.title, l.book_title, '[Kitap Silindi]') AS title,
               b.author, b.category, l.loan_date, l.return_date
        FROM loans l
        LEFT JOIN books b ON l.book_id = b.id
        WHERE l.user_id = ?
        AND l.return_date IS NOT NULL
        ORDER BY l.return_date DESC
    """;
} else {
    query = """
        SELECT COALESCE(b.title, l.book_title, '[Kitap Silindi]') AS title,
               b.author, b.category, l.loan_date, l.due_date
        FROM loans l
        LEFT JOIN books b ON l.book_id = b.id
        WHERE l.user_id = ?
        AND l.return_date IS NULL
        ORDER BY l.due_date ASC
    """;
}

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            LocalDate today = LocalDate.now();

            while (rs.next()) {
                if (historyMode) {
                    tableModel.addRow(new Object[]{
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getString("loan_date"),
                        rs.getString("return_date")
                    });
                    toplam++;
                } else {
                    String dueDate = rs.getString("due_date");
                    String durum;
                    try {
                        LocalDate due = LocalDate.parse(dueDate, FMT);
                        durum = due.isBefore(today) ? "Gecikmiş" : "Aktif";
                        if (due.isBefore(today)) gecikme++;
                    } catch (Exception e) {
                        durum = "Aktif";
                    }
                    tableModel.addRow(new Object[]{
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getString("loan_date"),
                        dueDate,
                        durum
                    });
                    toplam++;
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }

        if (historyMode) {
            toplamLabel.setText("Tamamlanan: " + toplam);
            aktifLabel.setText("Toplam Kitap: " + toplam);
        } else {
            toplamLabel.setText("Aktif Ödünç: " + toplam);
            aktifLabel.setText("Gecikmiş: " + gecikme);
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

    private JLabel colorLegend(String text, Color color) {
        JLabel lbl = new JLabel("■ " + text);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(color);
        return lbl;
    }
}