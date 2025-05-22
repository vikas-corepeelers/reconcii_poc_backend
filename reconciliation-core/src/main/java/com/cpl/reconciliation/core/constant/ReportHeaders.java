package com.cpl.reconciliation.core.constant;

public interface ReportHeaders {

    interface PosSale {
        String[] POS_HEADER = "Tender,BusinessDate,TransactionDate,PosId,StoreId,SaleType,InvoiceNumber,RRNNumber,SalesAmount,SalesTax,TenderAmount".split(",");
        String[] CASH_RECO_HEADER = "Date,Store Code & Date,Total Sale,Charges,Amt. Picked (Rs),Date of Pickup,CMS Slip No,Difference in Amt Pickedup (Rs),Net Diff-Short/Excess,SalesVsPickup,Receipts,Reconciled,UnReconciled,PickupVsReceipts,Reason".split(",");
        String[] SalesVsPickup_HEADER = "Date,Store Code & Date,Total Sale,Charges,Amt. Picked (Rs),Date of Pickup,CMS Slip No,Difference in Amt Pickedup (Rs),Net Diff-Short/Excess,SalesVsPickup,Reconciled,UnReconciled,Reason".split(",");
        String[] PickupVsReceipts_HEADER = "Date,Store Code & Date,Total Sale,Charges,Amt. Picked (Rs),Date of Pickup,CMS Slip No,Difference in Amt Pickedup (Rs),Net Diff-Short/Excess,SalesVsPickup,Reconciled,UnReconciled,Total Cash Collected,Total Receipts,PickupVsReceipts,Reason".split(",");
        String[] POSVSTRM_HEADERS = "Business Date, Order Date, Invoice Number, Store ID, POS ID, Tender Name, Total Amount, Sale Type, Tender Amount, Tender RRN, Transaction ID, Acquirer Bank, Auth Code, Card Number, Card Type, Network Type, Customer VPA, MID, Payment Type, TRM RRN, Source, TID, Transaction Status, Settlement Date, TRM Amount, Amount Difference, Remarks".split(",");
    }

    interface TrmSale {
        String TRM_Header[] = "Source,PaymentType,AcquirerBank,TID,MID,PosId,StoreId,SaleType,SaleAmount,InvoiceNumber,RRNNumber,TransactionId,TransactionDate,SettlementDate,Common Identifier,CustomerVPA,CardNo,CardType,CardNetwork,ApprovalCode".split(",");
        String UPI_Header[] = "Source,PaymentType,AcquirerBank,TID,MID,PosId,StoreId,SaleType,SaleAmount,InvoiceNumber,RRNNumber,TransactionId,TransactionDate,SettlementDate,Common Identifier,CustomerVPA".split(",");
        String CARD_Header[] = "Source,PaymentType,AcquirerBank,TID,MID,PosId,StoreId,SaleType,SaleAmount,InvoiceNumber,RRNNumber,TransactionId,TransactionDate,SettlementDate,Common Identifier,CardNo,CardType,CardNetwork,ApprovalCode".split(",");
        String[] TRMVSMPR_Header = "Source,PaymentType,AcquirerBank,TID,MID,PosId,StoreId,SaleType,SaleAmount,InvoiceNumber,RRNNumber,TransactionId,TransactionDate,SettlementDate,Common Identifier,CustomerVPA,CardNo,CardType,CardNetwork,ApprovalCode,MprPaymentType,MprBank,MprTID,MprMID,MprSoreId,MprAmount,MPRCommission,MPRSettleAmount,MprBankRRN,MPRTransactionDate,MPRSettlementDate,MprCustomerVPA,Remark,Amount Difference".split(",");
        String[] MPRVSTRM_Header = "Source,PaymentType,AcquirerBank,TID,MID,PosId,StoreId,SaleType,SaleAmount,InvoiceNumber,RRNNumber,TransactionId,TransactionDate,SettlementDate,Common Identifier,CustomerVPA,CardNo,CardType,CardNetwork,ApprovalCode,MprPaymentType,MprBank,MprTID,MprMID,MprSoreId,MprAmount,MPRCommission,MPRSettleAmount,MprBankRRN,MPRTransactionDate,MPRSettlementDate,MprCustomerVPA,Remark,Amount Difference".split(",");
    }

    interface MPR {
        String MPR_Header[] = "PaymentType,Bank,TID,MID,StoreId,MprAmount,Commission,SettledAmount,RRNNumber,TransactionDate,SettlementDate,MprCustomerVPA,CardNumber,CardType,AuthCode,Remark".split(",");
    }
    interface BankStatement {
        String Bank_Header[] = "PaymentType,Bank,MPRBank,DepositDate,SettlementDate,DepositAmount,ClosingBalance,Narration".split(",");
    }
}
