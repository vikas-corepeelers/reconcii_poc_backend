DROP FUNCTION IF EXISTS `CALCULATE_CONSUMER_GST`;
DELIMITER //


CREATE FUNCTION CALCULATE_CONSUMER_GST(
    billSubTotal DOUBLE,
    freebies DOUBLE,
    actual_packaging_charge DOUBLE,
    threePO VARCHAR(100)
)
RETURNS DOUBLE 
READS SQL DATA

BEGIN
	DECLARE commission DOUBLE;
	if(threePO='zomato' OR threePO='swiggy' or threePO='magicpin') THEN
      RETURN ROUND((billSubTotal + actual_packaging_charge - freebies) * 0.05,2);
	END IF;
	RETURN 0;
END //

DELIMITER ;
