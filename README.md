# je-analyzer

Offline command-line tool that runs standard audit tests over a general ledger journal entry export. Pure Java 17, no dependencies, no network access.

## Tests

| Test | What it flags |
|---|---|
| Duplicate entries | Same date, account and amount posted more than once |
| Round amounts | Amounts of 10,000 or more that are exact multiples of 1,000 |
| Weekend postings | Entries dated Saturday or Sunday |
| Segregation of duties | Missing approver, or preparer and approver are the same user |
| Just below approval limit | Amounts within 5% under a configurable limit |
| Benford's law | First-digit distribution compared to expected, with chi-square at 5% |

## Input format

CSV with a header row:

    entry_id,posting_date,amount,account,prepared_by,approved_by,description

Dates are ISO (`2026-03-14`). A sample file is in `sample/`.

## Build and run

    mvn package
    java -jar target/je-analyzer.jar sample/journal_entries.csv
    java -jar target/je-analyzer.jar sample/journal_entries.csv --threshold 50000 --out report.txt

Without Maven:

    mkdir out && javac -d out src/main/java/jetest/*.java
    java -cp out jetest.Main sample/journal_entries.csv

## Notes

Flags are indicators for follow-up, not conclusions. Benford's law suits wide-ranging, naturally occurring amounts and is unreliable on small samples or capped/fixed-fee data.
