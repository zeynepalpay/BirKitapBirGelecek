package koyokulukutuphaneotomasyonu;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class BookManagementFrame extends JFrame {

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel coverLabel;
    private JLabel bookNameLabel;
    private JLabel authorLabel;

    private final Color BG       = new Color(248, 245, 235);
    private final Color CARD     = new Color(255, 252, 242);
    private final Color GREEN    = new Color(76, 125, 68);
    private final Color BROWN    = new Color(78, 52, 46);
    private final Color RED_BROWN = new Color(130, 70, 55);

    public BookManagementFrame() {
        setTitle("Bir Kitap Bir Gelecek - Kitap Arşivi");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 600);
        setLocationRelativeTo(null);
        initComponents();
        loadBooks();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 25, 15, 25));
        mainPanel.setBackground(BG);

        // --- BAŞLIK ---
        JPanel northContainer = new JPanel(new BorderLayout(8, 8));
        northContainer.setBackground(BG);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 3, 3));
        titlePanel.setBackground(BG);

        JLabel titleLabel = new JLabel("Kitap Arşivi", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 26));
        titleLabel.setForeground(BROWN);

        JLabel subLabel = new JLabel("Bir Kitap Bir Gelecek", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subLabel.setForeground(Color.GRAY);

        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);

        // --- ARAMA PANELİ ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        topPanel.setBackground(CARD);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(190, 170, 130), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel araLabel = new JLabel("Ara:");
        araLabel.setFont(new Font("Arial", Font.BOLD, 13));
        araLabel.setForeground(BROWN);

        searchField = new JTextField(22);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel kategoriLabel = new JLabel("Kategori:");
        kategoriLabel.setFont(new Font("Arial", Font.BOLD, 13));
        kategoriLabel.setForeground(BROWN);

        categoryCombo = new JComboBox<>(new String[]{
            "Tümü", "Roman", "Bilim", "Tarih", "Çocuk", "Şiir", "Diğer"
        });
        categoryCombo.setFont(new Font("Arial", Font.PLAIN, 13));

        topPanel.add(araLabel);
        topPanel.add(searchField);
        topPanel.add(kategoriLabel);
        topPanel.add(categoryCombo);

        northContainer.add(titlePanel, BorderLayout.NORTH);
        northContainer.add(topPanel, BorderLayout.SOUTH);
        mainPanel.add(northContainer, BorderLayout.NORTH);

        // --- TABLO ---
        String[] columns = {"ID", "Başlık", "Yazar", "Kategori", "Durum"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        bookTable = new JTable(tableModel);
        bookTable.setFont(new Font("Arial", Font.PLAIN, 13));
        bookTable.setRowHeight(26);
        bookTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        bookTable.getTableHeader().setBackground(new Color(235, 225, 205));
        bookTable.getTableHeader().setForeground(BROWN);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setGridColor(new Color(210, 195, 165));

        bookTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, col);
                if ("borrowed".equals(value)) {
                    setText("Ödünçte");
                    setForeground(Color.RED);
                } else {
                    setText("Rafta");
                    setForeground(GREEN);
                }
                return c;
            }
        });

        bookTable.getColumnModel().getColumn(0).setMinWidth(0);
        bookTable.getColumnModel().getColumn(0).setMaxWidth(0);

        sorter = new TableRowSorter<>(tableModel);
        bookTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(190, 170, 130), 1));

        // --- KAPAK PANELİ ---
        JPanel previewPanel = new JPanel(new BorderLayout(10, 10));
        previewPanel.setPreferredSize(new Dimension(220, 0));
        previewPanel.setBackground(CARD);
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(190, 170, 130)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        coverLabel = new JLabel();
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadCoverImage("default.png");

        bookNameLabel = new JLabel("Kitap Seçilmedi", SwingConstants.CENTER);
        bookNameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        bookNameLabel.setForeground(BROWN);

        authorLabel = new JLabel("", SwingConstants.CENTER);
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        authorLabel.setForeground(new Color(120, 100, 80));

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        infoPanel.setBackground(CARD);
        infoPanel.add(bookNameLabel);
        infoPanel.add(authorLabel);

        previewPanel.add(coverLabel, BorderLayout.CENTER);
        previewPanel.add(infoPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, previewPanel);
        splitPane.setResizeWeight(0.80);
        splitPane.setDividerSize(5);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // --- BUTONLAR ---
        JPanel bottomContainer = new JPanel(new BorderLayout(5, 8));
        bottomContainer.setBackground(BG);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(BG);

        JButton addBtn    = createButton("+ Kitap Ekle", GREEN);
        JButton updateBtn = createButton("Güncelle",     new Color(110, 140, 90));
        JButton deleteBtn = createButton("Sil",          RED_BROWN);

        addBtn.addActionListener(e -> showAddDialog());
        updateBtn.addActionListener(e -> showUpdateDialog());
        deleteBtn.addActionListener(e -> deleteBook());

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

        // --- FİLTRELEME ---
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        categoryCombo.addActionListener(e -> filterTable());

        // --- KAPAK GÜNCELLEME ---
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = bookTable.getSelectedRow();
            if (row == -1) return;
            int modelRow = bookTable.convertRowIndexToModel(row);
            String title  = tableModel.getValueAt(modelRow, 1).toString();
            String author = tableModel.getValueAt(modelRow, 2).toString();
            updateCover(title, author);
        });
    }

    private void loadCoverImage(String imageName) {
        try {
            java.net.URL url = getClass().getResource("/images/" + imageName);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(160, 220, Image.SCALE_SMOOTH);
                coverLabel.setIcon(new ImageIcon(img));
            } else {
                coverLabel.setIcon(null);
                coverLabel.setText("Kapak Yok");
            }
        } catch (Exception ex) {
            coverLabel.setIcon(null);
            coverLabel.setText("Kapak Yok");
        }
    }

    private void updateCover(String title, String author) {
    String imageName;
    switch (title.toLowerCase().trim()) {
        case "küçük prens"              -> imageName = "kucukprens.png";
        case "sefiller"                 -> imageName = "sefiller.png";
        case "bilimin kısa tarihi"      -> imageName = "bilimin_kisa_tarihi.png";
        case "harry potter"             -> imageName = "harrypotter.png";
        case "kürk mantolu madonna"     -> imageName = "kurkmantolu.png";
        case "charlie'nin çikolata fabrikası" -> imageName = "charlie.png";
        case "çalıkuşu"                 -> imageName = "calikusu.png";
        default                         -> imageName = "default.png";
    }
    loadCoverImage(imageName);
    bookNameLabel.setText(title);
    authorLabel.setText(author);
}

    private void filterTable() {
        String searchText = searchField.getText().trim();
        String category   = (String) categoryCombo.getSelectedItem();

        RowFilter<DefaultTableModel, Object> searchFilter   = null;
        RowFilter<DefaultTableModel, Object> categoryFilter = null;

        if (!searchText.isEmpty()) {
            searchFilter = RowFilter.regexFilter("(?i)" + searchText, 1, 2);
        }
        if (!"Tümü".equals(category)) {
            categoryFilter = RowFilter.regexFilter("(?i)" + category, 3);
        }

        if (searchFilter != null && categoryFilter != null) {
            sorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(searchFilter, categoryFilter)));
        } else if (searchFilter != null) {
            sorter.setRowFilter(searchFilter);
        } else if (categoryFilter != null) {
            sorter.setRowFilter(categoryFilter);
        } else {
            sorter.setRowFilter(null);
        }
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, title, author, category, status FROM books")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog(this, "Kitap Ekle", true);
        dialog.setSize(370, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        form.setBackground(CARD);

        JTextField titleField  = new JTextField();
        JTextField authorField = new JTextField();
        JComboBox<String> catBox = new JComboBox<>(new String[]{
            "Roman", "Bilim", "Tarih", "Çocuk", "Şiir", "Diğer"
        });

        form.add(new JLabel("Başlık:"));   form.add(titleField);
        form.add(new JLabel("Yazar:"));    form.add(authorField);
        form.add(new JLabel("Kategori:")); form.add(catBox);

        JButton saveBtn = createButton("Kaydet", GREEN);
        saveBtn.addActionListener(e -> {
            String title    = titleField.getText().trim();
            String author   = authorField.getText().trim();
            String category = (String) catBox.getSelectedItem();

            if (title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Başlık ve yazar boş bırakılamaz.");
                return;
            }

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO books (title, author, category, status) VALUES (?, ?, ?, 'available')")) {
                ps.setString(1, title);
                ps.setString(2, author);
                ps.setString(3, category);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Kitap eklendi.");
                dialog.dispose();
                loadBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Hata: " + ex.getMessage());
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(saveBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showUpdateDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen güncellenecek kitabı seçin.");
            return;
        }

        int modelRow    = bookTable.convertRowIndexToModel(selectedRow);
        int id          = (int)    tableModel.getValueAt(modelRow, 0);
        String title    = (String) tableModel.getValueAt(modelRow, 1);
        String author   = (String) tableModel.getValueAt(modelRow, 2);
        String category = (String) tableModel.getValueAt(modelRow, 3);

        JDialog dialog = new JDialog(this, "Kitap Güncelle", true);
        dialog.setSize(370, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        form.setBackground(CARD);

        JTextField titleField  = new JTextField(title);
        JTextField authorField = new JTextField(author);
        JComboBox<String> catBox = new JComboBox<>(new String[]{
            "Roman", "Bilim", "Tarih", "Çocuk", "Şiir", "Diğer"
        });
        catBox.setSelectedItem(category);

        form.add(new JLabel("Başlık:"));   form.add(titleField);
        form.add(new JLabel("Yazar:"));    form.add(authorField);
        form.add(new JLabel("Kategori:")); form.add(catBox);

        JButton saveBtn = createButton("Güncelle", GREEN);
        saveBtn.addActionListener(e -> {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                    "UPDATE books SET title=?, author=?, category=? WHERE id=?")) {
                ps.setString(1, titleField.getText().trim());
                ps.setString(2, authorField.getText().trim());
                ps.setString(3, (String) catBox.getSelectedItem());
                ps.setInt(4, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Kitap güncellendi.");
                dialog.dispose();
                loadBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Hata: " + ex.getMessage());
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(saveBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek kitabı seçin.");
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        int id       = (int)    tableModel.getValueAt(modelRow, 0);
        String title = (String) tableModel.getValueAt(modelRow, 1);

        Object[] options = {"Evet", "Hayır"};
        int confirm = JOptionPane.showOptionDialog(this,
            "\"" + title + "\" kitabını silmek istediğinize emin misiniz?",
            "Silme Onayı", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[1]);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Kitap silindi.");
                loadBooks();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
            }
        }
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