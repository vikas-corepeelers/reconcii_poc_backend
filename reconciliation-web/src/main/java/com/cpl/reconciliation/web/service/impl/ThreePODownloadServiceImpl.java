package com.cpl.reconciliation.web.service.impl;

import com.cpl.core.common.annotations.TrackExecutionTime;
import com.cpl.reconciliation.core.enums.ThreePO;
import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
import com.cpl.reconciliation.web.service.impl.ThreePo.SwiggyApiService;
import com.cpl.reconciliation.web.service.impl.ThreePo.ZomatoApiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

@Data
@Slf4j
@Service
public class ThreePODownloadServiceImpl {

//    private final MagicPinApiService magicPinApiService;
    private final SwiggyApiService swiggyApiService;
    private final ZomatoApiService zomatoApiService;

    @TrackExecutionTime
    public void reportDownload(ThreePODataRequest request, OutputStream outputStream) throws IOException, SQLException {
        long start = System.currentTimeMillis();
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(500)) {
            workbook.setCompressTempFiles(true); // temp files will be gzipped
            switch (request.getReportType()) {
                case ThreePOSales -> threePOSalesDownload(request, workbook);
                case ThreePOReceivables -> threePOReceivablesDownload(request, workbook); /*final_amount*/
                case ThreePOCommission -> threePOCommissionDownload(request, workbook);  /*commision_value*/
                case ThreePOCharges -> threePOChargeschargesDownload(request, workbook);
                case ThreePOFreebies -> freebieDownload(request, workbook);
                case ThreePODiscounts -> discountsDownload(request, workbook);
                case AllThreePoCharges -> allThreePOCharges(request, workbook);
               
                case POSSales -> posSalesDownload(request, workbook);
                case PosReceivables -> posReceivablesDownload(request, workbook);
                case PosCommission -> posCommissionDownload(request, workbook);
                case PosCharges -> posChargesDownload(request, workbook);
                case PosFreebies -> posFreebiesDownload(request, workbook);
                case PosDiscounts -> posDiscountsDownload(request, workbook);
                case AllPOSCharges -> allPOSCharges(request, workbook);
                case ReceivablesVsReceipts -> receivablesVsReceiptsDownload(request, workbook);
                case Reconciled -> reconciledDownload(request, workbook);
                case POSVsThreePO -> posVsThreePoDownload(request, workbook);
                case Promo -> promo(request,workbook);
                default -> {
                }
            }
            workbook.write(outputStream);
            workbook.dispose();
        }
        log.info("{} ms take for writing workbook to output stream", (System.currentTimeMillis() - start));
    }

    private void allPOSCharges(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.allPOSCharges(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.allPOSCharges(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.allPOSCharges(request, workbook);
//        }
    }

    private void promo(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        request.setStores(null);
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.promo(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.promo(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.allPOSCharges(request, workbook);
//        }
    }

    private void allThreePOCharges(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.allThreePOCharges(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.allThreePOCharges(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.allThreePOCharges(request, workbook);
//        }
    }

    private void threePOChargeschargesDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.threePOChargesDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.threePOChargesDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.threePOChargesDownload(request, workbook);
//        }
    }

    private void receivablesVsReceiptsDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.receivablesVsReceiptsDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.receivablesVsReceiptsDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.receivablesVsReceiptsDownload(request, workbook);
//        }
    }

    private void threePOCommissionDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.threePOCommissionDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.threePOCommissionDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.threePOCommissionDownload(request, workbook);
//        }
    }

    private void threePOReceivablesDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.threePOReceivablesDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.threePOReceivablesDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.threePOReceivablesDownload(request, workbook);
//        }
    }

    private void posReceivablesDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.posReceivablesDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.posReceivablesDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.posReceivablesDownload(request, workbook);
//        }
    }

    private void posChargesDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.posChargesDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.posChargesDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.posChargesDownload(request, workbook);
//        }
    }

    private void posDiscountsDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.posDiscountsDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.posDiscountsDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.posDiscountsDownload(request, workbook);
//        }
    }

    private void posFreebiesDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.posFreebiesDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.posFreebiesDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.posFreebiesDownload(request, workbook);
//        }
    }


    private void posCommissionDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.posCommissionDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.posCommissionDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.posCommissionDownload(request, workbook);
//        }
    }

    private void freebieDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.threePOFreebieDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.threePOFreebieDownload(request, workbook);
        }
    }


    private void posVsThreePoDownload(ThreePODataRequest request, Workbook workbook) throws SQLException {
        ThreePO threePO = request.getTender();
        boolean allThreePoDownload = threePO == null;
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.posVsThreePoDownload(request, workbook, allThreePoDownload);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.posVsThreePoDownload(request, workbook, allThreePoDownload);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.posVsThreePoDownload(request, workbook, allThreePoDownload);
//        }
    }

    private void reconciledDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.reconciledDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.reconciledDownload(request, workbook);
        }
    }

    private void discountsDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.discountsDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.discountsDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.discountsDownload(request, workbook);
//        }
    }

    private void posSalesDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.posSalesDownload(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.posSalesDownload(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.posSalesDownload(request, workbook);
//        }
    }

    @TrackExecutionTime
    private void threePOSalesDownload(ThreePODataRequest request, Workbook workbook) {
        ThreePO threePO = request.getTender();
        
        if (threePO == null || threePO.equals(ThreePO.ZOMATO)) {
            zomatoApiService.threePOSalesDownloadNew(request, workbook);
        }
        if (threePO == null || threePO.equals(ThreePO.SWIGGY)) {
            swiggyApiService.threePOSalesDownloadNew(request, workbook);
        }
//        if (threePO == null || threePO.equals(ThreePO.MAGICPIN)) {
//            magicPinApiService.threePOSalesDownload(request, workbook);
//        }
    }

}
