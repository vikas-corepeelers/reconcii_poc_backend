DROP FUNCTION IF EXISTS `CALCULATE_UNRECONCILED_AMOUNT`;
DELIMITER //
CREATE FUNCTION CALCULATE_UNRECONCILED_AMOUNT(
    billSubTotal DOUBLE,
    finalAmount DOUBLE,
    freebies DOUBLE,
    gstCustomerBill DOUBLE,
    commission DOUBLE,
    merchantPackCharge DOUBLE,
    tdsCharge DOUBLE,
    taxesZomatoFee DOUBLE,
    pgCharge DOUBLE,
    posSales DOUBLE,
    actual_packaging_charge DOUBLE,
    refund_disputed_amount DOUBLE,
    threePO VARCHAR(255),
    order_status VARCHAR(255),
    pickup_status VARCHAR(255),
    cancellationRemark VARCHAR(255),
    dot_pe_order_cancelled_stage VARCHAR(255)

)
RETURNS DOUBLE 
READS SQL DATA

BEGIN
    DECLARE threePOSales DOUBLE;
	DECLARE threePOFreebies DOUBLE;
    
    DECLARE posReceivables DOUBLE;
    DECLARE posCommission DOUBLE;
    DECLARE posChargesGST DOUBLE;
    DECLARE posDiscounts DOUBLE;
    DECLARE posFreebies DOUBLE;
    DECLARE posPGCharges DOUBLE;
    DECLARE posConsumerGst DOUBLE;
    DECLARE posTds DOUBLE;
    
    DECLARE unreconciledAmount DOUBLE;
    DECLARE reconciledAmount DOUBLE;

    if (threePO='swiggy' and order_status='cancelled' and cancellationRemark='MERCHANT' ) then
            SET posSales=0;
    end if;

    if(threePO='zomato' AND (order_status in ('deduction','refund','cancel'))) then
            return 0;
    end if;

    -- ThreePOData calculations
    SET threePOSales = CALCULATE_THREE_PO_SALES(billSubTotal,freebies,actual_packaging_charge,gstCustomerBill,threePO,order_status);

    -- Ideal scenarios calculations
    SET posCommission = CALCULATE_COMMISSION_AMOUNT(billSubTotal,freebies,actual_packaging_charge,refund_disputed_amount,threePO,order_status);
    set posTds = CALCULATE_TDS(billSubTotal,freebies,actual_packaging_charge,refund_disputed_amount,threePO,order_status);
    set posPGCharges = CALCULATE_PG_CHARGE(billSubTotal,freebies,actual_packaging_charge,threePO,order_status)     ;
    set posChargesGST =CALCULATE_CHARGES_GST(billSubTotal,freebies,actual_packaging_charge,refund_disputed_amount,threePO,order_status);
	set posReceivables = CALCULATE_RECEIVABLES(billSubTotal,freebies,actual_packaging_charge,refund_disputed_amount,threePO,order_status,pickup_status,dot_pe_order_cancelled_stage);
	set posConsumerGst = CALCULATE_CONSUMER_GST(billSubTotal,freebies,actual_packaging_charge,threePO);
    -- RECO
   SET unreconciledAmount = IS_ORDER_RECONCILED(
          threePOSales ,
    finalAmount ,
    commission ,
    pgCharge ,
    taxesZomatoFee ,
    tdsCharge ,
    merchantPackCharge,
    gstCustomerBill,
    
    
    posSales ,
    posReceivables ,
    posCommission ,
    posPGCharges ,
    posChargesGST ,
    posTds ,
    actual_packaging_charge,
    posConsumerGst,
    order_status,
    pickup_status,
    threePO
    );
    
    RETURN ROUND(unreconciledAmount,2);
END //

DELIMITER ;
