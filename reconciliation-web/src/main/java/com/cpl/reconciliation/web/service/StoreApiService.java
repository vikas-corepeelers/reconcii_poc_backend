package com.cpl.reconciliation.web.service;

import com.cpl.reconciliation.core.request.StoreByCitiesRequest;
import com.cpl.reconciliation.core.request.StoreByStatesRequest;
import com.cpl.reconciliation.core.request.StoreRequest;
import com.cpl.reconciliation.core.response.StoreCodeResponse;
import com.cpl.reconciliation.core.response.StoreResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

public interface StoreApiService {

    StoreResponse getStore(Long id);

    StoreResponse getStore(String storeCode);

    List<String> getStateList();

    List<StoreResponse> getStoreList(String state);

    List<StoreCodeResponse> getStoreListByMultipleStates(StoreByStatesRequest storebyStatesRequest);

    List<StoreCodeResponse> getStoreCodeList(String state);

 //   StoreResponse createStore(StoreRequest storeRequest);

 //   StoreResponse updateStore(StoreRequest storeRequest);

  //  void deleteStore(Long id);

   // String uploadStoreMasterFile(MultipartFile multipartFile);

   // String uploadStoreTIDMapping(MultipartFile multipartFile);

   // String uploadStoreAmexStoreNameMapping(MultipartFile file);


  //  String uploadStoreSwiggyStoreMapping(MultipartFile file);

 //   String uploadStoreZomatoStoreMapping(MultipartFile file);

   // String uploadStoreMagicPinStoreMapping(MultipartFile file);

  //  String uploadSBICardCharges(MultipartFile file);

  //  String uploadHDFCCardCharges(MultipartFile file);

 //   String uploadAMEXCardCharges(MultipartFile file);

 //   String uploadICICICardCharges(MultipartFile file);

  //  String uploadVoucherConfig(MultipartFile multipartFile);


    List<String> getCityList();

    List<StoreCodeResponse> getStoresByCity(String city);

    List<StoreCodeResponse> getStoreListByMultipleCities(StoreByCitiesRequest storeByCitiesRequest);
}
