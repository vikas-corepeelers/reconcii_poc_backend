DROP FUNCTION IF EXISTS `CALCULATE_CHARGES_GST`;
DELIMITER //
CREATE FUNCTION CALCULATE_CHARGES_GST(
    billSubTotal DOUBLE,
    freebies DOUBLE,
    actual_packaging_charge DOUBLE,
     refund_disputed_amount DOUBLE,
    threePO VARCHAR(255),
    order_status VARCHAR(100)
    
)
RETURNS DOUBLE 
READS SQL DATA

BEGIN
	RETURN ROUND((CALCULATE_PG_CHARGE(billSubTotal,freebies,actual_packaging_charge,threePO,order_status)
    + CALCULATE_COMMISSION_AMOUNT(billSubTotal,freebies,actual_packaging_charge,refund_disputed_amount,threePO,order_status))*0.18,2);
END //

DELIMITER ;
