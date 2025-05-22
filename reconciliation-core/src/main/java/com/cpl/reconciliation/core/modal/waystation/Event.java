package com.cpl.reconciliation.core.modal.waystation;

import com.cpl.reconciliation.core.modal.waystation.events.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
//@JsonIgnoreProperties({})
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "Event")
public class Event {

    @JacksonXmlProperty(localName = "RegId", isAttribute = true)
    private int regId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMddHHmmss")
    @JacksonXmlProperty(localName = "Time", isAttribute = true)
    private LocalDateTime time;
    @JacksonXmlProperty(localName = "Type", isAttribute = true)
    private String type;
    //
    @JacksonXmlProperty(localName = "TRX_Sale")
    private TRXSale sale;
    @JacksonXmlProperty(localName = "Ev_SaleCustom")
    private EvSaleCustom saleCustom;
    @JacksonXmlProperty(localName = "TRX_Refund")
    private Object refund;
    @JacksonXmlProperty(localName = "TRX_Waste")
    private Object waste;
    @JacksonXmlProperty(localName = "Ev_CancelSpecialTrx")
    private EvCancelSpecialTrx cancelSpecialTrx;
    //
    @JacksonXmlProperty(localName = "TRX_SetSMState")
    private TRXSetSMState setSMState;
    @JacksonXmlProperty(localName = "TRX_BaseConfig")
    private TRXBaseConfig baseConfig;
    @JacksonXmlProperty(localName = "TRX_DayParts")
    private TRXDayParts dayParts;
    @JacksonXmlProperty(localName = "TRX_TenderTable")
    private TRXTenderTable tenderTable;
    @JacksonXmlProperty(localName = "TRX_TaxTable")
    private TRXTaxTable taxTable;
    //
    @JacksonXmlProperty(localName = "TRX_Signature_Version")
    private TRXSignatureVersion signatureVersion;
    @JacksonXmlProperty(localName = "TRX_Show_Prices")
    private TRXShowPrices showPrices;
    @JacksonXmlProperty(localName = "TRX_SvdItem")
    private TRXSvdItem svdItem;
    @JacksonXmlProperty(localName = "TRX_SetPOD")
    private TRXSetPOD setPOD;
    @JacksonXmlProperty(localName = "EV_Custom")
    private EVCustom evCustom;
    @JacksonXmlProperty(localName = "Ev_Custom")
    private EVvCustom evvCustom;
    @JacksonXmlProperty(localName = "TRX_OrderNotRecalled")
    private TRXOrderNotRecalled orderNotRecalled;
    @JacksonXmlProperty(localName = "TRX_UpdateCustomCounter")
    private TRXUpdateCustomCounter updateCustomCounter;
    @JacksonXmlProperty(localName = "Ev_SetCOD")
    private EvSetCOD setCOD;
    @JacksonXmlProperty(localName = "TRX_ClearOrderInProgress")
    private Object clearOrderInProgress;
    //
    @JacksonXmlProperty(localName = "TRX_GetAuthorization")
    private TRXGetAuthorization getAuthorization;
    @JacksonXmlProperty(localName = "TRX_Authorized_Cash_Drawer_Open")
    private TRXAuthorizedCashDrawerOpen authorizedCashDrawerOpen;
    @JacksonXmlProperty(localName = "TRX_UnaDrawerOpening")
    private TRXUnaDrawerOpening unaDrawerOpening;
    @JacksonXmlProperty(localName = "Ev_DrawerClose")
    private EvDrawerClose drawerClose;
    @JacksonXmlProperty(localName = "TRX_LogToggleRunnerBox")
    private TRXLogToggleRunnerBox logToggleRunnerBox;
    //
    @JacksonXmlProperty(localName = "TRX_DayOpen")
    private TRXDayOpen dayOpen;
    @JacksonXmlProperty(localName = "TRX_DayClose")
    private TRXDayClose dayClose;
    //
    @JacksonXmlProperty(localName = "TRX_OperLogin")
    private TRXOperLogin operLogin;
    @JacksonXmlProperty(localName = "TRX_OperLogout")
    private TRXOperLogout operLogout;
    //
    @JacksonXmlProperty(localName = "TRX_InitGTotal")
    private TRXInitGTotal initGTotal;
    @JacksonXmlProperty(localName = "Ev_POSShutDown")
    private EvPOSShutDown posShutDown;
    @JacksonXmlProperty(localName = "TRX_RegTransfer")
    private TRXRegTransfer regTransfer;
    @JacksonXmlProperty(localName = "TRX_Price_Changed")
    private TRXPriceChanged priceChanged;
    @JacksonXmlProperty(localName = "TRX_ModifyExchangeRate")
    private TRXModifyExchangeRate modifyExchangeRate;

    @Override
    public String toString() {
        return "Event{" +
                "regId=" + regId +
                ", time='" + time + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

