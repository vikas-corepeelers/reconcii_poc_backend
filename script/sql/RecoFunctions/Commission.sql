DROP FUNCTION IF EXISTS `CALCULATE_COMMISSION_AMOUNT`;

DELIMITER //

CREATE FUNCTION CALCULATE_COMMISSION_AMOUNT(
    billSubTotal DOUBLE,
    freebies DOUBLE,
    actual_packaging_charge DOUBLE,
    refund_disputed_amount DOUBLE,
    threePO VARCHAR(100),
    order_status VARCHAR(100)
)
RETURNS DOUBLE 
READS SQL DATA

BEGIN
	DECLARE commission DOUBLE;
	if(threePO='zomato') THEN
	    if(order_status='addition') THEN
            RETURN 0;
        END IF;
      RETURN ROUND((billSubTotal  - freebies) * 0.18,2);
	END IF;
    if(threePO='swiggy') THEN
      RETURN ROUND((billSubTotal  +actual_packaging_charge-freebies-refund_disputed_amount) * 0.175,2);
	END IF;
    if(threePO='magicpin') THEN

      RETURN  ROUND((billSubTotal-freebies-refund_disputed_amount)*0.08,2) ;
	END IF;
	RETURN 0;
END //

DELIMITER ;
