DROP FUNCTION IF EXISTS `CALCULATE_THREE_PO_SALES`;

DELIMITER //
CREATE FUNCTION CALCULATE_THREE_PO_SALES(
    billSubTotal DOUBLE,
    freebies DOUBLE,
    actual_packaging_charge DOUBLE,
    gstCustomerBill DOUBLE,
    threePO VARCHAR(100),
    order_status VARCHAR(100)
)
RETURNS DOUBLE 
READS SQL DATA

BEGIN
    IF(threePO='zomato' and (order_status='refund' OR order_status='cancel')) then
        return 0;
    end if;
	IF(threePO='magicpin') then 
    return ROUND(billSubTotal + actual_packaging_charge - freebies,2);
    end if;
	RETURN ROUND(billSubTotal + actual_packaging_charge - freebies,2) ;
END //

DELIMITER ;
