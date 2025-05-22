package com.cpl.reconciliation.core.response.threepo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DataResponse extends Data {

    private List<ThreePOData> threePOData;

}
