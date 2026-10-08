package jetest;

import java.time.LocalDate;

public record Entry(String id, LocalDate date, double amount, String account,
                    String preparedBy, String approvedBy, String description) {}
