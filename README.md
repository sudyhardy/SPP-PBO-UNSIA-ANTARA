# Project Siswa v2 — sumber lengkap
Java Swing, JDBC, database dbProjectSiswa, login, CRUD, PDF dan cetak.
Ini versi sumber baru, bukan source persis JAR v1. Belum dikompilasi atau diuji
pada Codespace pengguna. Gunakan sebagai contoh pembelajaran dan ikuti aturan
penggunaan AI dari dosen. Tidak ada klaim bahwa kode ini ditulis sendiri oleh pengguna.

## Kebutuhan
JDK 11+, Maven, MySQL atau MariaDB kompatibel, desktop grafis untuk Swing.
Dependency dikunci ke versi contoh, bukan klaim versi paling baru.

## Codespaces
Ekstrak folder ProjectSiswaSource di root workspace. Semua command berikut
berjalan di terminal Codespaces, bukan Terminal Mac.

cd /workspaces/codespaces-blank/ProjectSiswaSource
sudo service mariadb start
sudo mariadb < database.sql

SQL memakai IF NOT EXISTS; tidak menghapus transaksi yang sudah ada. Untuk
schema lama yang berbeda, migrasi mungkin diperlukan.

Berikan hak akun demo, termasuk ke tabel users:

sudo mariadb <<'SQL'
CREATE USER IF NOT EXISTS 'sppapp'@'localhost' IDENTIFIED BY 'SppDemo2026!';
GRANT SELECT, INSERT, UPDATE, DELETE ON dbProjectSiswa.* TO 'sppapp'@'localhost';
SQL

export DB_USER='sppapp'
export DB_PASSWORD='SppDemo2026!'
export DB_URL='jdbc:mysql://127.0.0.1:3306/dbProjectSiswa?serverTimezone=Asia/Jakarta&connectTimeout=5000&socketTimeout=10000'
export DISPLAY=:1

Jika mvn tidak tersedia:
sudo apt-get update && sudo apt-get install -y maven

Build:
mvn clean package

Buat akun aplikasi sekali, dari terminal interaktif:
java -jar target/ProjectSiswa.jar --create-admin

Masukkan username baru dan password minimal 8 karakter. Tidak ada akun login
bawaan. Password tidak tampil saat diketik. Jika username sudah dipakai, INSERT
gagal tanpa menimpa akun lama.

Jalankan:
java -jar target/ProjectSiswa.jar

Buka browser desktop port 6080. Login dengan akun yang baru dibuat.
DB_USER/DB_PASSWORD adalah akun koneksi database, bukan akun login aplikasi.

## PDF dan cetak
Laporan PDF menghasilkan file tanpa membutuhkan printer. Pada dialog simpan,
pilih folder root proyek agar PDF terlihat di Explorer Codespaces. Klik kanan
file PDF di Explorer lalu Download untuk mendapatkannya di komputer Anda.
Cetak memakai layanan printer sistem; Codespace mungkin tidak memiliki printer.
Gunakan Laporan PDF untuk bukti laporan jika tidak ada printer.
PDF memuat seluruh model tabel saat itu, bukan hanya baris terpilih; urutan PDF
mengikuti model database, bukan urutan visual setelah pengguna mengurutkan tabel.

## Skenario uji
- Login salah lalu benar.
- Isi data Budi, Simpan, periksa database.
- Pilih Budi, ubah jumlah 150000 menjadi 175000, klik Ubah.
- Tambah data Siti, pilih lalu Hapus dan konfirmasi.
- Ekspor laporan PDF dan buka hasilnya.
- Restart aplikasi, login dan pastikan data Budi tetap ada.
- Logout dan pastikan halaman login muncul kembali.

## Batasan
- Contoh akademik; bukan sistem pembayaran produksi.
- Tidak ada tahun ajaran, tanggal bayar, role, audit, atau pencegahan transaksi ganda.
- Login memakai PBKDF2-HMAC-SHA256, salt acak, 600000 iterasi.
- Login database/hash berjalan di SwingWorker; CRUD/PDF masih di thread UI demi
  kesederhanaan. Untuk penggunaan nyata pindahkan operasi lambat ke worker.
- Login desktop bukan batas keamanan database: klien tetap memegang kredensial DB.
- Akun admin yang dibuat tidak memiliki role khusus; semua akun setara.
- Contoh password DB hanya untuk demo privat; ganti untuk penggunaan nyata.
- File database.sql dan sumber disertakan dalam ZIP; JAR dibangun dari pom.xml.
