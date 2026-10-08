package jetest;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java -jar je-analyzer.jar <journal.csv> [--threshold 100000] [--out report.txt]");
            System.exit(1);
        }
        Path file = Path.of(args[0]);
        double threshold = 100_000;
        Path outFile = null;
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--threshold")) threshold = Double.parseDouble(args[++i]);
            else if (args[i].equals("--out")) outFile = Path.of(args[++i]);
        }

        List<Entry> entries = CsvLoader.load(file);
        List<Finding> findings = new ArrayList<>();
        findings.addAll(Tests.duplicates(entries));
        findings.addAll(Tests.roundAmounts(entries));
        findings.addAll(Tests.weekendPostings(entries));
        findings.addAll(Tests.segregationOfDuties(entries));
        findings.addAll(Tests.belowThreshold(entries, threshold));

        int[] obs = new int[10];
        double chi = Tests.benford(entries, obs);

        try (PrintStream out = outFile == null ? System.out : new PrintStream(Files.newOutputStream(outFile))) {
            out.println("Journal Entry Analysis: " + file.getFileName());
            out.println("Entries tested: " + entries.size() + "   Approval limit: " + threshold);
            out.println();

            out.println("Findings by test");
            Map<String, List<Finding>> byTest = new LinkedHashMap<>();
            for (Finding f : findings) byTest.computeIfAbsent(f.test(), k -> new ArrayList<>()).add(f);
            if (byTest.isEmpty()) out.println("  none");
            byTest.forEach((t, fs) -> {
                out.printf("  %-28s %d%n", t, fs.size());
            });
            out.println();

            out.println("Benford's law (first digit)");
            int n = Arrays.stream(obs).sum();
            for (int d = 1; d <= 9; d++) {
                double expPct = Math.log10(1 + 1.0 / d) * 100;
                double obsPct = n == 0 ? 0 : obs[d] * 100.0 / n;
                out.printf("  %d  observed %5.1f%%  expected %5.1f%%  %s%n", d, obsPct, expPct,
                        "#".repeat((int) Math.round(obsPct / 2)));
            }
            out.printf("  chi-square %.2f (5%% critical value %.3f): %s%n%n", chi, Tests.CHI_CRITICAL_5PCT,
                    chi > Tests.CHI_CRITICAL_5PCT ? "DEVIATES from expected distribution" : "consistent");

            out.println("Detail");
            for (Finding f : findings)
                out.printf("  [%s] %s: %s%n", f.test(), f.entryId(), f.detail());
        }
    }
}
