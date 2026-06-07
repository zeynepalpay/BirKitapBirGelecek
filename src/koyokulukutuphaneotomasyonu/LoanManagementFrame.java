package koyokulukutuphaneotomasyonu;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;

public class LoanManagementFrame extends JFrame {

    private JTable loanTable;
    private DefaultTableModel tableModel;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Color BG       = new Color(248, 245, 235);
    private final Color CARD     = new Color(255, 252, 242);
    private final Color GREEN    = new Color(76, 125, 68);
    private final Color BROWN    = new Color(78, 52, 46);
    private final Color RED_BROWN = new Color(130, 70, 55);
    private final Color BORDER   = new Color(190, 170, 130);

    public LoanManagementFrame() {
        setTitle("Bir Kitap Bir Gelecek - Zimmet ve Teslim Takibi");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        initComponents();
        loadLoans();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 25, 15, 25));
        mainPanel.setBackground(BG);

        JPanel northContainer = new JPanel(new GridLayout(2, 1, 3, 3));
        northContainer.setBackground(BG);

        JLabel titleLabel = new JLabel("Kitap Zimmet ve Teslim Takibi", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 26));
        titleLabel.setForeground(BROWN);

        JLabel infoLabel = new JLabel("Kırmızı satırlar iade tarihi geçmiş kitapları gösterir", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(120, 100, 80));

        northContainer.add(titleLabel);
        northContainer.add(infoLabel);
        mainPanel.add(northContainer, BorderLayout.NORTH);

        String[] columns = {"Ödünç ID", "Öğrenci", "Kitap", "Alış Tarihi", "Son Teslim", "Durum"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        loanTable = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                int modelRow = convertRowIndexToModel(row);
                String durum = (String) tableModel.getValueAt(modelRow, 5);
                if (!isRowSelected(row)) {
                    if ("Gecikmiş".equals(durum)) {
                        c.setBackground(new Color(255, 225, 220));
                        c.setForeground(Color.RED);
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(new Color(45, 45, 45));
                    }
                }
                return c;
            }
        };

        loanTable.setFont(new Font("Arial", Font.PLAIN, 13));
        loanTable.setRowHeight(26);
        loanTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        loanTable.getTableHeader().setBackground(new Color(235, 225, 205));
        loanTable.getTableHeader().setForeground(BROWN);
        loanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loanTable.setGridColor(new Color(210, 195, 165));

        loanTable.getColumnModel().getColumn(0).setMinWidth(0);
        loanTable.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(loanTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout(5, 8));
        bottomContainer.setBackground(BG);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(BG);

        JButton loanBtn    = createButton("+ Ödünç Ver", GREEN);
        JButton returnBtn  = createButton("İade Al",     new Color(110, 140, 90));
        JButton refreshBtn = createButton("Yenile",      RED_BROWN);

        loanBtn.addActionListener(e -> showLoanDialog());
        returnBtn.addActionListener(e -> returnBook());
        refreshBtn.addActionListener(e -> loadLoans());

        buttonPanel.add(loanBtn);
        buttonPanel.add(returnBtn);
        buttonPanel.add(refreshBtn);

        JLabel footer = new JLabel("© 2026 Bir Kitap Bir Gelecek", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.PLAIN, 11));
        footer.setForeground(Color.GRAY);

        bottomContainer.add(buttonPanel, BorderLayout.NORTH);
        bottomContainer.add(footer, BorderLayout.SOUTH);
        mainPanel.add(bottomContainer, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadLoans() {
        tableModel.setRowCount(0);
        String query = """
            SELECT l.id, u.name, b.title, l.loan_date, l.due_date
            FROM loans l
            JOIN users u ON l.user_id = u.id
            JOIN books b ON l.book_id = b.id
            WHERE l.return_date IS NULL
            ORDER BY l.due_date ASC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            LocalDate today = LocalDate.now();
            while (rs.next()) {
                String dueDateStr = rs.getString("due_date");
                LocalDate dueDate = LocalDate.parse(dueDateStr, FMT);
                String durum = dueDate.isBefore(today) ? "Gecikmiş" : "Aktif";
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("title"),
                    rs.getString("loan_date"),
                    dueDateStr,
                    durum
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private void showLoanDialog() {
        JDialog dialog = new JDialog(this, "Ödünç Ver", true);
        dialog.setSize(430, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        form.setBackground(CARD);

        JComboBox<String[]> studentCombo = new JComboBox<>();
        JComboBox<String[]> bookCombo    = new JComboBox<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name FROM users WHERE role = 'student'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                studentCombo.addItem(new String[]{
                    String.valueOf(rs.getInt("id")), rs.getString("name")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT id, title FROM books WHERE status = 'available'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bookCombo.addItem(new String[]{
                    String.valueOf(rs.getInt("id")), rs.getString("title")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }

        if (studentCombo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Sistemde kayıtlı öğrenci bulunmuyor.");
            return;
        }
        if (bookCombo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Ödünç verilecek müsait kitap bulunmuyor.");
            return;
        }

        studentCombo.setRenderer((list, value, index, isSelected, hasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value[1] : "");
            if (isSelected) { lbl.setBackground(list.getSelectionBackground()); lbl.setOpaque(true); }
            return lbl;
        });
        bookCombo.setRenderer((list, value, index, isSelected, hasFocus) -> {
            JLabel lbl = new JLabel(value != null ? value[1] : "");
            if (isSelected) { lbl.setBackground(list.getSelectionBackground()); lbl.setOpaque(true); }
            return lbl;
        });

        String loanDate = LocalDate.now().format(FMT);
        String defaultDue = LocalDate.now().plusDays(14).format(FMT);

        // Son teslim tarihi düzenlenebilir alan
        JTextField dueDateField = new JTextField(defaultDue);
        dueDateField.setFont(new Font("Arial", Font.PLAIN, 13));

        form.add(createFormLabel("Öğrenci:"));              form.add(studentCombo);
        form.add(createFormLabel("Kitap:"));                form.add(bookCombo);
        form.add(createFormLabel("Son Teslim (yyyy-MM-dd):")); form.add(dueDateField);

        JButton saveBtn = createButton("Ödünç Ver", GREEN);
        saveBtn.addActionListener(e -> {
            String[] student = (String[]) studentCombo.getSelectedItem();
            String[] book    = (String[]) bookCombo.getSelectedItem();

            if (student == null || book == null) {
                JOptionPane.showMessageDialog(dialog, "Öğrenci ve kitap seçiniz.");
                return;
            }

            // Tarih doğrulama
            String customDue = dueDateField.getText().trim();
            try {
                LocalDate.parse(customDue, FMT);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Geçersiz tarih formatı!\nÖrnek: 2026-07-01");
                return;
            }

            int userId = Integer.parseInt(student[0]);
            int bookId = Integer.parseInt(book[0]);

            try (Connection conn = DatabaseManager.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    PreparedStatement ps1 = conn.prepareStatement(
    "INSERT INTO loans (book_id, user_id, book_title, loan_date, due_date) VALUES (?,?,?,?,?)");
ps1.setInt(1, bookId);
ps1.setInt(2, userId);
ps1.setString(3, book[1]);
ps1.setString(4, loanDate);
ps1.setString(5, customDue);
                    ps1.executeUpdate();

                    PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE books SET status = 'borrowed' WHERE id = ?");
                    ps2.setInt(1, bookId);
                    ps2.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(dialog,
                        "Kitap ödünç verildi.\nSon teslim tarihi: " + customDue);
                    dialog.dispose();
                    loadLoans();
                } catch (SQLException ex) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(dialog, "Hata: " + ex.getMessage());
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Bağlantı hatası: " + ex.getMessage());
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(saveBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void returnBook() {
        int selectedRow = loanTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen iade edilecek kaydı seçin.");
            return;
        }

        int modelRow   = loanTable.convertRowIndexToModel(selectedRow);
        int loanId     = (int)    tableModel.getValueAt(modelRow, 0);
        String student = (String) tableModel.getValueAt(modelRow, 1);
        String book    = (String) tableModel.getValueAt(modelRow, 2);

        Object[] options = {"Evet", "Hayır"};
        int confirm = JOptionPane.showOptionDialog(this,
            student + " adlı öğrenciden \"" + book + "\" kitabını iade almak istiyor musunuz?",
            "İade Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[1]);

        if (confirm != JOptionPane.YES_OPTION) return;

        String returnDate = LocalDate.now().format(FMT);

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps1 = conn.prepareStatement(
                    "UPDATE loans SET return_date = ? WHERE id = ?");
                ps1.setString(1, returnDate);
                ps1.setInt(2, loanId);
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE books SET status = 'available' WHERE id = " +
                    "(SELECT book_id FROM loans WHERE id = ?)");
                ps2.setInt(1, loanId);
                ps2.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Kitap iade alındı.");
                loadLoans();
            } catch (SQLException ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Hata: " + ex.getMessage());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Bağlantı hatası: " + e.getMessage());
        }
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(BROWN);
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