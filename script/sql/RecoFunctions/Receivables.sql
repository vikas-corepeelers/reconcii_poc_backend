DROP FUNCTION IF EXISTS `CALCULATE_RECEIVABLES`;

DELIMITER //

CREATE FUNCTION CALCULATE_RECEIVABLES(
    billSubTotal DOUBLE,
    freebies DOUBLE,
    actual_packaging_charge DOUBLE,
    refund_disputed_order DOUBLE,
    threePO VARCHAR(255),
    order_status VARCHAR(255),
    pickup_status VARCHAR(255),
    dot_pe_order_cancelled_stage VARCHAR(255)
    
)
RETURNS DOUBLE 
READS SQL DATA
BEGIN
	DECLARE tds DOUBLE;
    DECLARE pgCharge DOUBLE;
    DECLARE chargesGST DOUBLE;
    DECLARE commission DOUBLE;
    IF(threePO='zomato' and (order_status='refund' OR order_status='cancel')) then
            return 0;
        end if;
    IF(threePO='zomato' and (order_status='addition')) then
        if(dot_pe_order_cancelled_stage like "%Cancelled%before%Pickup%") then
            return ROUND(0.60 * (billSubTotal+actual_packaging_charge-freebies),2);
        end if;
        return ROUND(0.7835 * (billSubTotal+actual_packaging_charge-freebies),2);

     end if;

      IF(threePO='magicpin' and (order_status='cancelled')) then
         if(dot_pe_order_cancelled_stage like "%Cancelled%before%Pickup%") then
             return ROUND(0.60 * (billSubTotal+actual_packaging_charge-freebies),2);
         end if;
         return ROUND(0.8956 * (billSubTotal+actual_packaging_charge-freebies),2);
      end if;

    if(threePO='swiggy' and order_status='cancelled') THEN
        if(dot_pe_order_cancelled_stage like "%Cancelled%before%Pickup%") then
            return ROUND(0.60 * (billSubTotal+actual_packaging_charge-freebies),2);
          end if;

          if(pickup_status='not picked up') then
            return ROUND(0.7835 * (billSubTotal+actual_packaging_charge-freebies),2);
        end if;
    END IF;
    
    SET tds=CALCULATE_TDS(billSubTotal,freebies,actual_packaging_charge,refund_disputed_order,threePO,order_status);
    SET pgCharge=CALCULATE_PG_CHARGE(billSubTotal,freebies,actual_packaging_charge,threePO,order_status)  ;
    set chargesGST = CALCULATE_CHARGES_GST(billSubTotal,freebies,actual_packaging_charge,refund_disputed_order,threePO,order_status);
    set commission = CALCULATE_COMMISSION_AMOUNT(billSubTotal,freebies,actual_packaging_charge,refund_disputed_order,threePO,order_status);
    

    
	RETURN ROUND(billSubTotal + actual_packaging_charge- freebies-commission-tds-pgCharge-chargesGST-refund_disputed_order,2);
END //

DELIMITER ;
