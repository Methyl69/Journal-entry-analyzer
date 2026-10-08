package jetest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reads a GL journal export. Columns: entry_id,posting_date,amount,account,prepared_by,approved_by,description */
public final class CsvLoader {

    public static List<Entry> load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<Entry> out = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;
            String[] c = split(lines.get(i));
            if (c.length < 7) throw new IOException("Line " + (i + 1) + ": expected 7 columns, got " + c.length);
            out.add(new Entry(c[0], LocalDate.parse(c[1]), Double.parseDouble(c[2]),
                    c[3], c[4], c[5], c[6]));
        }
        return out;
    }

    private static String[] split(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean quoted = false;
        for (char ch : line.toCharArray()) {
            if (ch == '"') quoted = !quoted;
            else if (ch == ',' && !quoted) { parts.add(sb.toString().trim()); sb.setLength(0); }
            else sb.append(ch);
        }
        parts.add(sb.toString().trim());
        return parts.toArray(new String[0]);
    }
}
