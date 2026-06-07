package koyokulukutuphaneotomasyonu;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class MemberManagementFrame extends JFrame {

    private JTable memberTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    private final Color BG = new Color(248, 245, 235);
    private final Color CARD = new Color(255, 252, 242);
    private final Color GREEN = new Color(76, 125, 68);
    private final Color BROWN = new Color(78, 52, 46);
    private final Color RED_BROWN = new Color(130, 70, 55);
    private final Color BORDER = new Color(190, 170, 130);

    public MemberManagementFrame() {
        setTitle("Bir Kitap Bir Gelecek - Kullanıcı Kayıtları");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 600);
        setLocationRelativeTo(null);
        initComponents();
        loadMembers();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 25, 15, 25));
        mainPanel.setBackground(BG);

        JPanel northContainer = new JPanel(new BorderLayout(8, 8));
        northContainer.setBackground(BG);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 3, 3));
        titlePanel.setBackground(BG);

        JLabel titleLabel = new JLabel("Kullanıcı Kayıtları", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 26));
        titleLabel.setForeground(BROWN);

        JLabel subLabel = new JLabel("Öğrenci ve öğretmen bilgilerini düzenleyin", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subLabel.setForeground(Color.GRAY);

        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        topPanel.setBackground(CARD);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel araLabel = new JLabel("Ara:");
        araLabel.setFont(new Font("Arial", Font.BOLD, 13));
        araLabel.setForeground(BROWN);

        searchField = new JTextField(25);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));

        topPanel.add(araLabel);
        topPanel.add(searchField);

        northContainer.add(titlePanel, BorderLayout.NORTH);
        northContainer.add(topPanel, BorderLayout.SOUTH);
        mainPanel.add(northContainer, BorderLayout.NORTH);

        String[] columns = {"ID", "Ad Soyad", "Kullanıcı Adı", "Rol", "Öğrenci No"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        memberTable = new JTable(tableModel);
        memberTable.setFont(new Font("Arial", Font.PLAIN, 13));
        memberTable.setRowHeight(26);
        memberTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        memberTable.getTableHeader().setBackground(new Color(235, 225, 205));
        memberTable.getTableHeader().setForeground(BROWN);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.setGridColor(new Color(210, 195, 165));

        memberTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);

                if ("teacher".equals(value)) {
                    setText("Öğretmen");
                    setForeground(BROWN);
                } else {
                    setText("Öğrenci");
                    setForeground(GREEN);
                }
                return c;
            }
        });

        memberTable.getColumnModel().getColumn(0).setMinWidth(0);
        memberTable.getColumnModel().getColumn(0).setMaxWidth(0);

        sorter = new TableRowSorter<>(tableModel);
        memberTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout(5, 8));
        bottomContainer.setBackground(BG);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(BG);

        JButton addBtn = createButton("+ Üye Ekle", GREEN);
        JButton updateBtn = createButton("Güncelle", new Color(110, 140, 90));
        JButton deleteBtn = createButton("Sil", RED_BROWN);

        addBtn.addActionListener(e -> showAddDialog());
        updateBtn.addActionListener(e -> showUpdateDialog());
        deleteBtn.addActionListener(e -> deleteMember());

        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);

        JLabel footer = new JLabel("© 2026 Bir Kitap Bir Gelecek", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.PLAIN, 11));
        footer.setForeground(Color.GRAY);

        bottomContainer.add(buttonPanel, BorderLayout.NORTH);
        bottomContainer.add(footer, BorderLayout.SOUTH);

        mainPanel.add(bottomContainer, BorderLayout.SOUTH);

        add(mainPanel);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }
        });
    }

    private void filterTable() {
        String text = searchField.getText().trim();

        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2, 4));
        }
    }

    private void loadMembers() {
        tableModel.setRowCount(0);

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT id, name, username, role, student_number FROM users")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("student_number") != null ? rs.getString("student_number") : "-"
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog(this, "Üye Ekle", true);
        dialog.setSize(400, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        form.setBackground(CARD);

        JTextField nameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"student", "teacher"});
        JTextField studentNoField = new JTextField();
        JLabel studentNoLabel = new JLabel("Öğrenci No:");

        roleBox.addActionListener(e -> {
            boolean isStudent = "student".equals(roleBox.getSelectedItem());
            studentNoField.setEnabled(isStudent);
            studentNoLabel.setEnabled(isStudent);
        });

        form.add(createFormLabel("Ad Soyad:"));
        form.add(nameField);

        form.add(createFormLabel("Kullanıcı Adı:"));
        form.add(usernameField);

        form.add(createFormLabel("Şifre:"));
        form.add(passField);

        form.add(createFormLabel("Rol:"));
        form.add(roleBox);

        form.add(studentNoLabel);
        form.add(studentNoField);

        JButton saveBtn = createButton("Kaydet", GREEN);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String role = (String) roleBox.getSelectedItem();
            String studentNo = studentNoField.getText().trim();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Ad, kullanıcı adı ve şifre boş bırakılamaz.");
                return;
            }

            if ("student".equals(role) && studentNo.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Öğrenci numarası giriniz.");
                return;
            }

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (name, username, password, role, student_number) VALUES (?,?,?,?,?)")) {

                ps.setString(1, name);
                ps.setString(2, username);
                ps.setString(3, password);
                ps.setString(4, role);
                ps.setString(5, "student".equals(role) ? studentNo : null);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Üye eklendi.");
                dialog.dispose();
                loadMembers();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Hata: " + ex.getMessage());
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(saveBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showUpdateDialog() {
        int selectedRow = memberTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellenecek üyeyi seçin.");
            return;
        }

        int modelRow = memberTable.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String username = (String) tableModel.getValueAt(modelRow, 2);
        String role = (String) tableModel.getValueAt(modelRow, 3);
        String studentNo = (String) tableModel.getValueAt(modelRow, 4);

        JDialog dialog = new JDialog(this, "Üye Güncelle", true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        form.setBackground(CARD);

        JTextField nameField = new JTextField(name);
        JTextField usernameField = new JTextField(username);

        JComboBox<String> roleBox = new JComboBox<>(new String[]{"student", "teacher"});
        roleBox.setSelectedItem(role);

        JTextField studentNoField = new JTextField("-".equals(studentNo) ? "" : studentNo);
        studentNoField.setEnabled("student".equals(role));

        JLabel studentNoLabel = createFormLabel("Öğrenci No:");
        studentNoLabel.setEnabled("student".equals(role));

        roleBox.addActionListener(e -> {
            boolean isStudent = "student".equals(roleBox.getSelectedItem());
            studentNoField.setEnabled(isStudent);
            studentNoLabel.setEnabled(isStudent);
        });

        form.add(createFormLabel("Ad Soyad:"));
        form.add(nameField);

        form.add(createFormLabel("Kullanıcı Adı:"));
        form.add(usernameField);

        form.add(createFormLabel("Rol:"));
        form.add(roleBox);

        form.add(studentNoLabel);
        form.add(studentNoField);

        JButton saveBtn = createButton("Güncelle", GREEN);

        saveBtn.addActionListener(e -> {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET name=?, username=?, role=?, student_number=? WHERE id=?")) {

                ps.setString(1, nameField.getText().trim());
                ps.setString(2, usernameField.getText().trim());
                ps.setString(3, (String) roleBox.getSelectedItem());
                ps.setString(4, "student".equals(roleBox.getSelectedItem())
                    ? studentNoField.getText().trim() : null);
                ps.setInt(5, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Üye güncellendi.");
                dialog.dispose();
                loadMembers();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Hata: " + ex.getMessage());
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(saveBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteMember() {
        int selectedRow = memberTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek üyeyi seçin.");
            return;
        }

        int modelRow = memberTable.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        Object[] options = {"Evet", "Hayır"};

        int confirm = JOptionPane.showOptionDialog(this,
            "\"" + name + "\" adlı üyeyi silmek istediğinize emin misiniz?",
            "Silme Onayı",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {

                ps.setInt(1, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Üye silindi.");
                loadMembers();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
            }
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