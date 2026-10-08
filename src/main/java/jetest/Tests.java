package jetest;

import java.time.DayOfWeek;
import java.util.*;

/** Standard journal entry tests used in IT/financial audit. */
public final class Tests {

    public static final double CHI_CRITICAL_5PCT = 15.507; // 8 degrees of freedom

    public static List<Finding> duplicates(List<Entry> es) {
        Map<String, List<Entry>> groups = new LinkedHashMap<>();
        for (Entry e : es)
            groups.computeIfAbsent(e.date() + "|" + e.account() + "|" + e.amount(), k -> new ArrayList<>()).add(e);
        List<Finding> out = new ArrayList<>();
        groups.values().stream().filter(g -> g.size() > 1).forEach(g -> {
            String ids = g.stream().map(Entry::id).reduce((a, b) -> a + ", " + b).get();
            out.add(new Finding("Duplicate entries", g.get(0).id(),
                    "Same date/account/amount (" + g.get(0).amount() + ") in: " + ids));
        });
        return out;
    }

    public static List<Finding> roundAmounts(List<Entry> es) {
        List<Finding> out = new ArrayList<>();
        for (Entry e : es)
            if (Math.abs(e.amount()) >= 10_000 && Math.abs(e.amount()) % 1000 == 0)
                out.add(new Finding("Round amounts", e.id(), "Amount " + e.amount() + " is a round thousand"));
        return out;
    }

    public static List<Finding> weekendPostings(List<Entry> es) {
        List<Finding> out = new ArrayList<>();
        for (Entry e : es) {
            DayOfWeek d = e.date().getDayOfWeek();
            if (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY)
                out.add(new Finding("Weekend postings", e.id(), "Posted on " + e.date() + " (" + d + ")"));
        }
        return out;
    }

    public static List<Finding> segregationOfDuties(List<Entry> es) {
        List<Finding> out = new ArrayList<>();
        for (Entry e : es) {
            if (e.approvedBy().isBlank())
                out.add(new Finding("Segregation of duties", e.id(), "No approver recorded"));
            else if (e.approvedBy().equalsIgnoreCase(e.preparedBy()))
                out.add(new Finding("Segregation of duties", e.id(), "Prepared and approved by " + e.preparedBy()));
        }
        return out;
    }

    public static List<Finding> belowThreshold(List<Entry> es, double limit) {
        List<Finding> out = new ArrayList<>();
        for (Entry e : es) {
            double a = Math.abs(e.amount());
            if (a >= limit * 0.95 && a < limit)
                out.add(new Finding("Just below approval limit", e.id(),
                        "Amount " + e.amount() + " is within 5% under limit " + limit));
        }
        return out;
    }

    /** Returns {chiSquare, n}; fills observed[1..9] with first-digit counts. */
    public static double benford(List<Entry> es, int[] observed) {
        int n = 0;
        for (Entry e : es) {
            double a = Math.abs(e.amount());
            if (a < 1) continue;
            String s = String.valueOf((long) a);
            observed[s.charAt(0) - '0']++;
            n++;
        }
        double chi = 0;
        for (int d = 1; d <= 9; d++) {
            double exp = n * Math.log10(1 + 1.0 / d);
            chi += Math.pow(observed[d] - exp, 2) / exp;
        }
        return chi;
    }
}
