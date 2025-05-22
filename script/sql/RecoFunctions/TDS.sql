DROP FUNCTION IF EXISTS `CALCULATE_TDS`;

DELIMITER //

CREATE FUNCTION CALCULATE_TDS(
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
      RETURN ROUND((billSubTotal - freebies + actual_packaging_charge) * 0.01,2) ;
	END IF;
    if(threePO='swiggy' or threePO='magicpin') THEN
      RETURN ROUND((billSubTotal + actual_packaging_charge - freebies)*0.01,2);
	END IF;
    
	RETURN 0;
END //

DELIMITER ;
