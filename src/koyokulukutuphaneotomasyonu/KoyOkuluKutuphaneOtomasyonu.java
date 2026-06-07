package koyokulukutuphaneotomasyonu;

import javax.swing.SwingUtilities;

public class KoyOkuluKutuphaneOtomasyonu {

    public static void main(String[] args) {

        DatabaseManager.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}