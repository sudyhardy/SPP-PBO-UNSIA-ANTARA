package spp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.MessageFormat;
import java.nio.file.Path;

public class PembayaranFrame extends JFrame {
    private final String petugas;
    private final JTextField id = new JTextField(), nama = new JTextField();
    private final JTextField kelas = new JTextField(), jumlah = new JTextField();
    private final JComboBox<String> jurusan = new JComboBox<>(new String[]{"Pilih","Umum","IPA","IPS"});
    private final JComboBox<String> bayar = new JComboBox<>(new String[]{"Pilih","SPP Januari","SPP Februari",
        "SPP Maret","SPP April","SPP Mei","SPP Juni","SPP Juli","SPP Agustus","SPP September",
        "SPP Oktober","SPP November","SPP Desember"});
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID Pembayaran","ID Siswa",
        "Nama Siswa","Kelas","Jurusan","Pembayaran","Jumlah"}, 0) {
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tabel = new JTable(model);
    private Integer selected;

    public PembayaranFrame(String petugas) {
        super("Pembayaran SPP — " + petugas); this.petugas = petugas;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel top = new JPanel(new BorderLayout(10, 15));
        top.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        top.add(new JLabel("SISTEM PEMBAYARAN SPP SMP JAKENAN", SwingConstants.CENTER), BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(3, 4, 12, 12));
        form.add(new JLabel("ID Siswa :")); form.add(id);
        form.add(new JLabel("Nama Siswa :")); form.add(nama);
        form.add(new JLabel("Kelas :")); form.add(kelas);
        form.add(new JLabel("Jurusan :")); form.add(jurusan);
        form.add(new JLabel("Pembayaran :")); form.add(bayar);
        form.add(new JLabel("Jumlah :")); form.add(jumlah);
        top.add(form, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        tombol(buttons, "Simpan", () -> simpan(false));
        tombol(buttons, "Hapus", this::hapus);
        tombol(buttons, "Ubah", () -> simpan(true));
        tombol(buttons, "Cetak", this::cetak);
        tombol(buttons, "Laporan PDF", this::pdf);
        tombol(buttons, "Bersihkan", this::bersihkan);
        tombol(buttons, "Muat Ulang", this::muat);
        tombol(buttons, "Logout", () -> { dispose(); new LoginFrame().setVisible(true); });
        top.add(buttons, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH); add(new JScrollPane(tabel), BorderLayout.CENTER);
        tabel.setRowHeight(25); tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setAutoCreateRowSorter(true);
        tabel.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) pilih(); });
        setSize(1100, 550); setMinimumSize(new Dimension(1000, 450)); setLocationRelativeTo(null);
        muat();
    }
    private void tombol(JPanel panel, String text, Runnable action) {
        JButton button = new JButton(text); panel.add(button);
        button.addActionListener(e -> action.run());
    }
    private void pesan(String text) { JOptionPane.showMessageDialog(this, text); }
    private void gagal(Exception e) { JOptionPane.showMessageDialog(this, e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE); }

    private void muat() {
        try (Connection c = Koneksi.buka(); PreparedStatement p = c.prepareStatement(
                "SELECT id_pembayaran,id_siswa,nama_siswa,kelas,jurusan,pembayaran,jumlah FROM pembayaran_spp ORDER BY id_pembayaran DESC");
                ResultSet r = p.executeQuery()) {
            bersihkan(); model.setRowCount(0);
            while (r.next()) model.addRow(new Object[]{r.getInt(1),r.getString(2),r.getString(3),
                r.getString(4),r.getString(5),r.getString(6),r.getBigDecimal(7)});
        } catch (SQLException e) { gagal(e); }
    }
    private void pilih() {
        int row = tabel.getSelectedRow();
        if (row < 0) { selected = null; return; }
        row = tabel.convertRowIndexToModel(row);
        selected = (Integer) model.getValueAt(row, 0);
        id.setText(model.getValueAt(row, 1).toString()); nama.setText(model.getValueAt(row, 2).toString());
        kelas.setText(model.getValueAt(row, 3).toString()); jurusan.setSelectedItem(model.getValueAt(row, 4));
        bayar.setSelectedItem(model.getValueAt(row, 5)); jumlah.setText(model.getValueAt(row, 6).toString());
    }
    private BigDecimal validasi() {
        if (id.getText().trim().isEmpty() || nama.getText().trim().isEmpty() || kelas.getText().trim().isEmpty()
                || jurusan.getSelectedIndex() == 0 || bayar.getSelectedIndex() == 0)
            throw new IllegalArgumentException("Lengkapi semua isian.");
        if (id.getText().trim().length() > 20 || nama.getText().trim().length() > 100 || kelas.getText().trim().length() > 20)
            throw new IllegalArgumentException("Batas karakter: ID 20, nama 100, kelas 20.");
        BigDecimal n;
        try { n = new BigDecimal(jumlah.getText().trim()).setScale(2, RoundingMode.UNNECESSARY); }
        catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("Jumlah berupa angka, maksimal 2 desimal. Contoh: 150000 atau 150000.50.");
        }
        if (n.signum() <= 0 || n.compareTo(new BigDecimal("9999999999999.99")) > 0)
            throw new IllegalArgumentException("Nominal harus positif, maksimal 9999999999999.99.");
        return n;
    }
    private void simpan(boolean edit) {
        if (edit && selected == null) { pesan("Pilih transaksi yang akan diubah."); return; }
        if (!edit && selected != null) { pesan("Klik Bersihkan untuk data baru, atau Ubah untuk transaksi terpilih."); return; }
        try {
            BigDecimal nominal = validasi();
            String sql = edit
                ? "UPDATE pembayaran_spp SET id_siswa=?,nama_siswa=?,kelas=?,jurusan=?,pembayaran=?,jumlah=? WHERE id_pembayaran=?"
                : "INSERT INTO pembayaran_spp(id_siswa,nama_siswa,kelas,jurusan,pembayaran,jumlah) VALUES(?,?,?,?,?,?)";
            try (Connection c = Koneksi.buka(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, id.getText().trim()); p.setString(2, nama.getText().trim());
                p.setString(3, kelas.getText().trim()); p.setString(4, (String) jurusan.getSelectedItem());
                p.setString(5, (String) bayar.getSelectedItem()); p.setBigDecimal(6, nominal);
                if (edit) p.setInt(7, selected);
                if (p.executeUpdate() == 0) { pesan("Transaksi tidak ditemukan. Klik Muat Ulang."); return; }
            }
            pesan(edit ? "Data berhasil diubah." : "Data berhasil disimpan."); muat();
        } catch (SQLException | IllegalArgumentException e) { gagal(e); }
    }
    private void hapus() {
        if (selected == null) { pesan("Pilih transaksi yang akan dihapus."); return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus transaksi terpilih?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = Koneksi.buka(); PreparedStatement p = c.prepareStatement(
                "DELETE FROM pembayaran_spp WHERE id_pembayaran=?")) {
            p.setInt(1, selected); int n = p.executeUpdate();
            pesan(n > 0 ? "Data berhasil dihapus." : "Transaksi sudah tidak ditemukan."); muat();
        } catch (SQLException e) { gagal(e); }
    }
    private void bersihkan() {
        tabel.clearSelection(); selected = null;
        id.setText(""); nama.setText(""); kelas.setText(""); jumlah.setText("");
        jurusan.setSelectedIndex(0); bayar.setSelectedIndex(0);
    }
    private void cetak() {
        if (model.getRowCount() == 0) { pesan("Belum ada data untuk dicetak."); return; }
        try {
            boolean done = tabel.print(JTable.PrintMode.FIT_WIDTH,
                new MessageFormat("Laporan Pembayaran SPP SMP Jakenan"), new MessageFormat("Halaman {0}"));
            if (done) pesan("Laporan dikirim ke layanan pencetakan.");
        } catch (java.awt.print.PrinterException e) { gagal(e); }
    }
    private void pdf() {
        if (model.getRowCount() == 0) { pesan("Belum ada data untuk laporan."); return; }
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.setSelectedFile(new java.io.File("Laporan_SPP.pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String name = chooser.getSelectedFile().getAbsolutePath();
        if (!name.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) name += ".pdf";
        Path target = Path.of(name);
        if (java.nio.file.Files.exists(target) && JOptionPane.showConfirmDialog(this,
                "File sudah ada. Timpa?", "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { Laporan.pdf(model, petugas, target); pesan("PDF tersimpan:\n" + target); }
        catch (Exception e) { gagal(e); }
    }
}
