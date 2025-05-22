package com.cpl.reconciliation.web.api;

import com.cpl.core.api.exception.ApiException;
import com.cpl.core.api.response.ApiResponse;
import com.cpl.core.common.annotations.Action;
import com.cpl.core.common.annotations.Resource;
import com.cpl.reconciliation.core.enums.UploadConfig;
import com.cpl.reconciliation.core.request.StoreByCitiesRequest;
import com.cpl.reconciliation.core.request.StoreByStatesRequest;
import com.cpl.reconciliation.core.response.SelectBoxResponse;
import com.cpl.reconciliation.core.response.StoreCodeResponse;
import com.cpl.reconciliation.core.response.StoreResponse;
import com.cpl.reconciliation.web.service.StoreApiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

@Data
@Slf4j
@RestController
@Resource(name = "Store")
@RequestMapping("/api/ve1/store")
public class StoreApiController {

    private final StoreApiService storeApiService;
    @Value("${manualUpload.template.path:/tmp}")
    private String templatePath;

//    @Action(name = "GetStore")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ApiResponse<StoreResponse> getStore(@PathVariable("id") String storeCode) {
        StoreResponse response = storeApiService.getStore(storeCode);
        return new ApiResponse<>(response);
    }

//    @Action(name = "GetStore")
    @RequestMapping(method = RequestMethod.GET, value = "/get/{id}")
    public ApiResponse<StoreResponse> getStore(@PathVariable("id") Long id) {
        StoreResponse response = storeApiService.getStore(id);
        return new ApiResponse<>(response);
    }

//    @Action(name = "GetStoreList")
    @RequestMapping(method = RequestMethod.GET, value = "/list")
    public ApiResponse<List<StoreResponse>> getStoreList(@RequestParam(required = false) String state) {
        List<StoreResponse> response = storeApiService.getStoreList(state);
        return new ApiResponse<>(response);
    }

//    @Action(name = "GetStateList")
    @RequestMapping(method = RequestMethod.GET, value = "/stateList")
    public ApiResponse<List<String>> getStateList() {
        List<String> response = storeApiService.getStateList();
        return new ApiResponse<>(response);
    }

    //    @Action(name = "GetCityList")
    @RequestMapping(method = RequestMethod.GET, value = "/cityList")
    public ApiResponse<List<String>> getCityList(HttpServletRequest request) throws JsonProcessingException {
        List<String> response = storeApiService.getCityList();
        ApiResponse apiResponse = new ApiResponse<>(response);
        return apiResponse;
    }

    //    @Action(name = "GetStoreList")
    @RequestMapping(method = RequestMethod.GET, value = "/storeList")
    public ApiResponse<List<StoreCodeResponse>> getStoresByCity(@RequestParam(required = false) String city) {
        List<StoreCodeResponse> response = storeApiService.getStoresByCity(city);
        return new ApiResponse<>(response);
    }

    //    @Action(name = "GetStoreList")
    @RequestMapping(method = RequestMethod.POST, value = "/storeList")
    public ApiResponse<List<StoreCodeResponse>> getStoresBymultipleCities(@Valid @RequestBody StoreByCitiesRequest storeByCitiesRequest) {
        List<StoreCodeResponse> response = storeApiService.getStoreListByMultipleCities(storeByCitiesRequest);
        return new ApiResponse<>(response);
    }

//    @Action(name = "GetStoreList")
    @RequestMapping(method = RequestMethod.GET, value = "/codeList")
    public ApiResponse<List<StoreCodeResponse>> getStoreCodeList(@RequestParam(required = false) String state) {
        List<StoreCodeResponse> response = storeApiService.getStoreCodeList(state);
        return new ApiResponse<>(response);
    }

//    @Action(name = "GetStoreList")
    @RequestMapping(method = RequestMethod.POST, value = "/codeList")
    public ApiResponse<List<StoreCodeResponse>> getStoreCodeListByStates(HttpServletRequest request, @Valid @RequestBody StoreByStatesRequest storebyStatesRequest) throws JsonProcessingException {
        List<StoreCodeResponse> response = storeApiService.getStoreListByMultipleStates(storebyStatesRequest);
        ApiResponse apiResponse = new ApiResponse<>(response);
        return apiResponse;
    }

//    @Action(name = "CreateStore")
//    @RequestMapping(method = RequestMethod.POST, value = "/create")
//    public ApiResponse<StoreResponse> createStoreHandler(StoreRequest storeRequest) {
//        StoreResponse response = storeApiService.createStore(storeRequest);
//        return new ApiResponse<>(response);
//    }
//
//    @Action(name = "UpdateStore")
//    @RequestMapping(method = RequestMethod.PUT, value = "/update")
//    public ApiResponse<StoreResponse> updateStoreHandler(StoreRequest storeRequest) {
//        StoreResponse response = storeApiService.updateStore(storeRequest);
//        return new ApiResponse<>(response);
//    }
//
//    @Action(name = "DeleteStore")
//    @RequestMapping(method = RequestMethod.DELETE, value = "/delete/{id}")
//    public ApiResponse<String> deleteStoreHandler(@PathVariable("id") Long id) {
//        storeApiService.deleteStore(id);
//        return new ApiResponse<>("Store successfully Deleted!");
//    }
    @Action(name = "UploadConfig")
    @RequestMapping(method = RequestMethod.GET, value = "/listUploadConfig")
    public ApiResponse<List<SelectBoxResponse>> listConfigUploaderHandler() {
        List<SelectBoxResponse> response = Arrays.stream(UploadConfig.values()).map(c -> new SelectBoxResponse(c.getUiValue(), c.name())).collect(Collectors.toList());
        return new ApiResponse<>(response);
    }

    @Action(name = "UploadConfig")
    @RequestMapping(method = RequestMethod.GET, value = "/downloadConfig")
    public ResponseEntity<org.springframework.core.io.Resource> downloadStoreConfigHandler(@RequestParam("uploadConfig") UploadConfig uploadConfig) {
        String filePath = templatePath + uploadConfig + ".xlsx";
        try {
            org.springframework.core.io.Resource resource = new FileUrlResource(filePath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", uploadConfig + ".xlsx");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new ApiException("Something went wrong in getting config file");
        }
    }

//    @ActivityLog(name = "DATA_UPLOAD")
//    @Action(name = "UploadConfig")
//    @RequestMapping(method = RequestMethod.POST, value = "/uploadConfig")
//    public ApiResponse<String> uploadStoreFileHandler(@RequestParam("uploadConfig") UploadConfig uploadConfig, @RequestParam("files") MultipartFile file) {
//        String response = null;
//        switch (uploadConfig) {
//            case StoreMaster -> {
//                response = storeApiService.uploadStoreMasterFile(file);
//            }
//            case StoreTIDMapping -> {
//                response = storeApiService.uploadStoreTIDMapping(file);
//            }
//            case AmexStoreNameMapping -> {
//                response = storeApiService.uploadStoreAmexStoreNameMapping(file);
//            }
//            case SwiggyStoreMapping -> {
//                response = storeApiService.uploadStoreSwiggyStoreMapping(file);
//            }
//            case ZomatoStoreMapping -> {
//                response = storeApiService.uploadStoreZomatoStoreMapping(file);
//            }
//            case MagicPinStoreMapping -> {
//                response = storeApiService.uploadStoreMagicPinStoreMapping(file);
//            }
//            case SBICardCharges -> {
//                response = storeApiService.uploadSBICardCharges(file);
//            }
//            case HDFCardCharges -> {
//                response = storeApiService.uploadHDFCCardCharges(file);
//            }
//            case AMEXCardCharges -> {
//                response = storeApiService.uploadAMEXCardCharges(file);
//            }
//            case ICICICardCharges -> {
//                response = storeApiService.uploadICICICardCharges(file);
//            }
//            case VOUCHERCONFIG -> {
//                response = storeApiService.uploadVoucherConfig(file);
//            }
//        }
//        return new ApiResponse<>(response);
//    }
}
