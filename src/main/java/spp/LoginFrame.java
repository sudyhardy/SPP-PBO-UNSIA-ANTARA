package spp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Arrays;

public class LoginFrame extends JFrame {
    private final JTextField username = new JTextField(20);
    private final JPasswordField password = new JPasswordField(20);
    private final JButton masuk = new JButton("Masuk");

    public LoginFrame() {
        super("Login — Pembayaran SPP");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.add(new JLabel("LOGIN SISTEM PEMBAYARAN SPP", SwingConstants.CENTER));
        panel.add(new JLabel("Username")); panel.add(username);
        panel.add(new JLabel("Password")); panel.add(password); panel.add(masuk);
        add(panel); getRootPane().setDefaultButton(masuk);
        masuk.addActionListener(e -> login());
        pack(); setResizable(false); setLocationRelativeTo(null);
    }
    private void login() {
        String user = username.getText().trim();
        char[] pass = password.getPassword();
        if (user.isEmpty() || pass.length == 0) {
            Arrays.fill(pass, '\0');
            JOptionPane.showMessageDialog(this, "Isi username dan password."); return;
        }
        masuk.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                try (Connection c = Koneksi.buka(); PreparedStatement p = c.prepareStatement(
                        "SELECT password_hash,salt,iterations FROM users WHERE username=?")) {
                    p.setString(1, user);
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) return PasswordUtil.cocok(pass, r.getString(2), r.getString(1), r.getInt(3));
                        return false;
                    }
                } finally { Arrays.fill(pass, '\0'); }
            }
            protected void done() {
                masuk.setEnabled(true); password.setText("");
                try {
                    if (get()) { new PembayaranFrame(user).setVisible(true); dispose(); }
                    else JOptionPane.showMessageDialog(LoginFrame.this, "Username atau password salah.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Login gagal: " +
                        (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()));
                }
            }
        }.execute();
    }
}
