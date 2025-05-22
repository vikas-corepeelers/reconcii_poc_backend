SELECT
    m.settlement_date AS settlementDate,
    m.bank AS bank,
	m.payment_type AS payment_type,
    m.mprCount AS mprCount,
    COALESCE(m.mprAmount, 0) AS mprAmount,
    COALESCE(b.bankAmount, 0) AS bankAmount,
    ABS(ROUND(m.mprAmount - COALESCE(b.bankAmount, 0), 2)) AS diff
FROM
    (SELECT
        DATE(settled_date) AS settlement_date,
            payment_type,
            bank,
			count(*) AS mprCount,
            SUM(settled_amount) AS mprAmount
    FROM
        mcd.mpr
    WHERE
        bank = 'HDFC' AND payment_type = 'UPI'
    GROUP BY DATE(settled_date)) AS m
        LEFT JOIN
    (SELECT
        DATE(expected_actual_transaction_date) AS settlement_date,
            payment_type,
			source_bank as bank,
            SUM(deposit_amt) AS bankAmount
    FROM
        mcd.bank_statements
    WHERE
        source_bank = 'HDFC' AND payment_type = 'UPI'
    GROUP BY DATE(expected_actual_transaction_date)) b ON (m.settlement_date = b.settlement_date);

#To update bs_matched in mpr
use mcd;
SET SQL_SAFE_UPDATES = 0;
UPDATE mcd.mpr
SET bs_matched = TRUE
WHERE (date(settled_date), bank, payment_type) IN (
    SELECT
        m.settlement_date,
        m.bank,
        m.payment_type
    FROM
        (SELECT
            DATE(settled_date) AS settlement_date,
            payment_type,
            bank,
            SUM(settled_amount) AS mprAmount
        FROM
            mcd.mpr
        WHERE
            bank = 'HDFC' AND payment_type = 'UPI'
        GROUP BY DATE(settled_date)) AS m
    LEFT JOIN
        (SELECT
            DATE(expected_actual_transaction_date) AS settlement_date,
            payment_type,
            source_bank as bank,
            SUM(deposit_amt) AS bankAmount
        FROM
            mcd.bank_statements
        WHERE
            source_bank = 'HDFC' AND payment_type = 'UPI'
        GROUP BY DATE(expected_actual_transaction_date)) b ON (m.settlement_date = b.settlement_date)
    WHERE ABS(ROUND(m.mprAmount - COALESCE(b.bankAmount, 0), 2)) < 1 AND m.settlement_date IS NOT NULL
);

SELECT date(settled_date), SUM(settled_amount), count(*) from mcd.mpr WHERE bank="HDFC" AND payment_type="UPI" AND bs_matched=false group by date(settled_date);