package koyokulukutuphaneotomasyonu;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class StudentCatalogFrame extends JFrame {

    private JTable catalogTable;
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
    private final Color BORDER   = new Color(190, 170, 130);

    public StudentCatalogFrame() {
        setTitle("Bir Kitap Bir Gelecek - Kitap Kataloğu");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 580);
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

        JLabel titleLabel = new JLabel("Kitap Kataloğu", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 26));
        titleLabel.setForeground(BROWN);

        JLabel subLabel = new JLabel("Raftaki ve ödünçteki kitapları inceleyin", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subLabel.setForeground(new Color(120, 100, 80));

        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);

        // --- FİLTRE PANELİ ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        topPanel.setBackground(CARD);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        searchField = new JTextField(22);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));

        categoryCombo = new JComboBox<>(new String[]{
            "Tümü", "Roman", "Bilim", "Tarih", "Çocuk", "Şiir", "Diğer"
        });
        categoryCombo.setFont(new Font("Arial", Font.PLAIN, 13));

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{
            "Tümü", "Rafta", "Ödünçte"
        });
        statusCombo.setFont(new Font("Arial", Font.PLAIN, 13));

        topPanel.add(createFormLabel("Ara:"));       topPanel.add(searchField);
        topPanel.add(createFormLabel("Kategori:"));  topPanel.add(categoryCombo);
        topPanel.add(createFormLabel("Durum:"));     topPanel.add(statusCombo);

        northContainer.add(titlePanel, BorderLayout.NORTH);
        northContainer.add(topPanel, BorderLayout.SOUTH);
        mainPanel.add(northContainer, BorderLayout.NORTH);

        // --- TABLO ---
        String[] columns = {"Başlık", "Yazar", "Kategori", "Durum"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        catalogTable = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                int modelRow = convertRowIndexToModel(row);
                String durum = (String) tableModel.getValueAt(modelRow, 3);
                if (!isRowSelected(row)) {
                    if ("Ödünçte".equals(durum)) {
                        c.setForeground(Color.RED);
                        c.setBackground(new Color(255, 230, 230));
                    } else {
                        c.setForeground(GREEN);
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        };

        catalogTable.setFont(new Font("Arial", Font.PLAIN, 13));
        catalogTable.setRowHeight(26);
        catalogTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        catalogTable.getTableHeader().setBackground(new Color(235, 225, 205));
        catalogTable.getTableHeader().setForeground(BROWN);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogTable.setGridColor(new Color(210, 195, 165));

        sorter = new TableRowSorter<>(tableModel);
        catalogTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(catalogTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        // --- KAPAK PANELİ ---
        JPanel previewPanel = new JPanel(new BorderLayout(10, 10));
        previewPanel.setPreferredSize(new Dimension(220, 0));
        previewPanel.setBackground(CARD);
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
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

        // --- ALT PANEL ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 8));
        bottomPanel.setBackground(BG);

        JLabel infoLabel = new JLabel("Kırmızı: Ödünçte  |  Yeşil: Rafta", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(120, 100, 80));

        JLabel footer = new JLabel("© 2026 Bir Kitap Bir Gelecek", SwingConstants.CENTER);
        footer.setFont(new Font("Arial", Font.PLAIN, 11));
        footer.setForeground(Color.GRAY);

        bottomPanel.add(infoLabel, BorderLayout.NORTH);
        bottomPanel.add(footer, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // --- FİLTRELEME ---
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(categoryCombo, statusCombo); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(categoryCombo, statusCombo); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(categoryCombo, statusCombo); }
        });

        categoryCombo.addActionListener(e -> filterTable(categoryCombo, statusCombo));
        statusCombo.addActionListener(e -> filterTable(categoryCombo, statusCombo));

        // --- KAPAK GÜNCELLEME ---
        catalogTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = catalogTable.getSelectedRow();
            if (row == -1) return;
            int modelRow  = catalogTable.convertRowIndexToModel(row);
            String title  = tableModel.getValueAt(modelRow, 0).toString();
            String author = tableModel.getValueAt(modelRow, 1).toString();
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

    private void filterTable(JComboBox<String> categoryCombo, JComboBox<String> statusCombo) {
        String searchText = searchField.getText().trim();
        String category   = (String) categoryCombo.getSelectedItem();
        String status     = (String) statusCombo.getSelectedItem();

        java.util.List<RowFilter<DefaultTableModel, Object>> filters = new java.util.ArrayList<>();

        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + searchText, 0, 1));
        }
        if (!"Tümü".equals(category)) {
            filters.add(RowFilter.regexFilter("(?i)" + category, 2));
        }
        if (!"Tümü".equals(status)) {
            filters.add(RowFilter.regexFilter("(?i)" + status, 3));
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT title, author, category, status FROM books ORDER BY title")) {
            while (rs.next()) {
                String durum = "available".equals(rs.getString("status")) ? "Rafta" : "Ödünçte";
                tableModel.addRow(new Object[]{
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    durum
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(BROWN);
        return lbl;
    }
}