package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_Signature_Version")
public class TRXSignatureVersion {

    @JacksonXmlProperty(localName = "CryptographyAlgorithm")
    private String cryptographyAlgorithm;
    @JacksonXmlProperty(localName = "PrivateKeyVersion")
    private String privateKeyVersion;
}
