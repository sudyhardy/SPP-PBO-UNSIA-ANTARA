package spp;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javax.swing.table.TableModel;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Laporan {
    private Laporan() { }
    public static void pdf(TableModel model, String user, Path target) throws Exception {
        Document document = new Document(PageSize.A4.rotate());
        try (OutputStream output = Files.newOutputStream(target)) {
            PdfWriter.getInstance(document, output);
            try {
                document.open();
                document.add(new Paragraph("LAPORAN PEMBAYARAN SPP SMP JAKENAN"));
                document.add(new Paragraph("Petugas: " + user + " | Dibuat: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
                document.add(new Paragraph(" "));
                PdfPTable table = new PdfPTable(model.getColumnCount());
                table.setWidthPercentage(100); table.setHeaderRows(1);
                for (int col = 0; col < model.getColumnCount(); col++) table.addCell(model.getColumnName(col));
                BigDecimal total = BigDecimal.ZERO;
                for (int row = 0; row < model.getRowCount(); row++) {
                    for (int col = 0; col < model.getColumnCount(); col++)
                        table.addCell(String.valueOf(model.getValueAt(row, col)));
                    total = total.add(new BigDecimal(model.getValueAt(row, 6).toString()));
                }
                document.add(table);
                document.add(new Paragraph("Total pembayaran: Rp " + total.toPlainString()));
            } finally { document.close(); }
        }
    }
}
