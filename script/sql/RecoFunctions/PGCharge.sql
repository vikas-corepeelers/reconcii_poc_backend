DROP FUNCTION IF EXISTS `CALCULATE_PG_CHARGE`;
DELIMITER //

CREATE FUNCTION CALCULATE_PG_CHARGE(
    billSubTotal DOUBLE,
    freebies DOUBLE,
    actual_packaging_charge DOUBLE,
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
      RETURN ROUND((billSubTotal + actual_packaging_charge - freebies+CALCULATE_CONSUMER_GST(billSubTotal,freebies,actual_packaging_charge,threePO)) * 0.0125,2);
	END IF;
    if(threePO='swiggy') THEN
      RETURN 0;
	END IF;
    if(threePO='magicpin') THEN
      RETURN 0 ;
	END IF;
	RETURN 0;
END //

DELIMITER ;
