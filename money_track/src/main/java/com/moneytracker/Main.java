package com.moneytracker;

import com.moneytracker.database.DatabaseSetup;
import com.moneytracker.ui.frmLoginScreen;

public class Main {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(frmLoginScreen.class.getName());

    public static void main(String[] args) {
        com.moneytracker.common.Logger.configure();
        logger.info("Starting MyWallet with database at " + com.moneytracker.database.DatabaseManager.getDatabaseFile());
        DatabaseSetup.initialize();
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new frmLoginScreen().setVisible(true));
    }
}
