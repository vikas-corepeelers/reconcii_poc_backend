DROP FUNCTION IF EXISTS `IS_ORDER_RECONCILED`;
DELIMITER //
CREATE FUNCTION IS_ORDER_RECONCILED(
    threePOSales DOUBLE,
    threePOReceivables DOUBLE,
    threePOCommission DOUBLE,
    threePOPGCharges DOUBLE,
    threePOChargesGST DOUBLE,
    threePOTds DOUBLE,
     threePOPackagingCharge DOUBLE,
     threePOConsumerGST DOUBLE,
    
    posSales DOUBLE,
    posReceivables DOUBLE,
    posCommission DOUBLE,
    posPGCharges DOUBLE,
    posChargesGST DOUBLE,
    posTds DOUBLE,
     posPackagingCharge DOUBLE,
     posConsumerGST DOUBLE,
     order_status VARCHAR(255),
    pickup_status VARCHAR(255),
    threePO VARCHAR(255)
)
RETURNS DOUBLE
READS SQL DATA
BEGIN
    DECLARE threshold DOUBLE;
    DECLARE salesDiff DOUBLE;
    DECLARE receivablesDiff DOUBLE;
    DECLARE commissionDiff DOUBLE;
    DECLARE pgChargesDiff DOUBLE;
    DECLARE chargesGSTDiff DOUBLE;
    DECLARE tdsDiff DOUBLE;
    DECLARE packagingDiff DOUBLE;
    DECLARE consumerGSTDiff Double;

    -- Set the threshold value
    SET threshold = 0.1;

    IF (posPackagingCharge=37.14) then
        SET threshold = 0.2;
    END IF;

    -- Calculate absolute differences between corresponding values
    SET salesDiff = ABS(threePOSales - posSales);
    SET receivablesDiff = ABS(threePOReceivables - posReceivables);
    SET commissionDiff = ABS(threePOCommission - posCommission);
    SET pgChargesDiff = ABS(threePOPGCharges - posPGCharges);
    SET chargesGSTDiff = ABS(threePOChargesGST - posChargesGST);
    SET tdsDiff = ABS(threePOTds - posTds);
    SET packagingDiff = ABS(posPackagingCharge-threePOPackagingCharge);
    SET consumerGSTDiff = ABS(threePOConsumerGST-posConsumerGST);
    
   if (order_status='cancelled' and threePO='swiggy') then
        IF salesDiff >= threshold THEN
            return salesDiff;
        END IF;
		if receivablesDiff >= threshold then
			return receivablesDiff;
        end if;
		return 0;
    end if;

    IF packagingDiff > 0 THEN
    		return packagingDiff;
	END IF;

    IF salesDiff >= threshold THEN
		return salesDiff;
	END IF;

	IF commissionDiff >= threshold AND pgChargesDiff < threshold  THEN
		return commissionDiff;
	END IF;

    IF pgChargesDiff >= threshold AND commissionDiff<threshold  THEN
		return pgChargesDiff;
    END IF;

    IF pgChargesDiff >= threshold AND commissionDiff>=threshold  THEN
        return commissionDiff+pgChargesDiff;
    END IF;

    IF tdsDiff >= threshold THEN
		return tdsDiff;
    END IF;

   IF chargesGSTDiff >= threshold THEN
   		return chargesGSTDiff;
   	END IF;

   IF consumerGSTDiff >= threshold THEN
        return consumerGSTDiff;
   END IF;

    IF receivablesDiff >= threshold THEN
		return receivablesDiff;
	END IF;
    return 0;

END //

DELIMITER ;
