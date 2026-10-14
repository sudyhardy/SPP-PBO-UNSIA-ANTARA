package spp;

import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Base64;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--create-admin".equals(args[0])) {
            try { buatAdmin(); }
            catch (Exception e) { System.err.println("Gagal membuat akun: " + e.getMessage()); System.exit(1); }
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new LoginFrame().setVisible(true);
        });
    }
    private static void buatAdmin() throws Exception {
        Console console = System.console();
        if (console == null) throw new IllegalStateException("Jalankan java -jar dari terminal interaktif.");
        String username = console.readLine("Username akun baru: ");
        if (username == null || username.trim().isEmpty() || username.trim().length() > 50)
            throw new IllegalArgumentException("Username wajib diisi, maksimal 50 karakter.");
        char[] password = console.readPassword("Password (minimal 8 karakter): ");
        if (password == null) throw new IllegalArgumentException("Password dibatalkan.");
        try {
            if (password.length < 8) throw new IllegalArgumentException("Password minimal 8 karakter.");
            byte[] salt = PasswordUtil.saltBaru();
            String hash = Base64.getEncoder().encodeToString(PasswordUtil.hash(password, salt, PasswordUtil.ITERATIONS));
            try (Connection c = Koneksi.buka(); PreparedStatement p = c.prepareStatement(
                    "INSERT INTO users(username,password_hash,salt,iterations) VALUES(?,?,?,?)")) {
                p.setString(1, username.trim()); p.setString(2, hash);
                p.setString(3, Base64.getEncoder().encodeToString(salt));
                p.setInt(4, PasswordUtil.ITERATIONS); p.executeUpdate();
            }
            console.printf("Akun berhasil dibuat. Jalankan JAR tanpa --create-admin untuk login.%n");
        } finally { Arrays.fill(password, '\0'); }
    }
}
