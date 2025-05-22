package com.cpl.reconciliation.web.api;

import com.cpl.core.api.response.ApiResponse;
import com.cpl.reconciliation.core.request.SaveRecoLogicRequest;
import com.cpl.reconciliation.core.request.UpdateRecoLogicRequest;
import com.cpl.reconciliation.domain.entity.RecoLogicsEntity;
import com.cpl.reconciliation.web.service.RecoLogicService;
import com.cpl.reconciliation.web.service.beans.TenderRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
/*
 * @author Abhishek N
 */
@Data
@Slf4j
@RestController
@RequestMapping("/api/v1/recologics")
public class CustomRecoLogicController {

    private final RecoLogicService recoLogicservice;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ApiResponse<String> addRecoLogics(HttpServletRequest request, @RequestBody SaveRecoLogicRequest recoJson) throws JsonProcessingException {
        log.info("reco data is uploading");
        ApiResponse apiResponse;
        int code;
        String response;
        if (recoJson.getTenders() != null && !recoJson.getTenders().isEmpty()) {
            response = recoLogicservice.saveRecoLogic(recoJson);
            if (!response.contains("already available")) {
                code = 200;
            } else {
                code = 500;
            }
        } else {
            code = 500;
            response = "tenders are null/empty and not valid. Please check.";
        }
        apiResponse = new ApiResponse<>(code, response);
        return apiResponse;
    }

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public ApiResponse<List<RecoLogicsEntity>> getRecoLogicsByTenders(HttpServletRequest request, @RequestBody TenderRequest tenders) throws JsonProcessingException {
        ApiResponse apiResponse = new ApiResponse<>(recoLogicservice.findLogicsByTender(tenders.getTenders()));
        return apiResponse;
    }

    @RequestMapping(value = "/findOldestEffectiveDate", method = RequestMethod.GET)
    public ApiResponse<List<RecoLogicsEntity>> getRecoLogicsByTenders(HttpServletRequest request) throws JsonProcessingException {
        String response = recoLogicservice.findOldestEffectiveDate();
        ApiResponse apiResponse;
        int code;
        if (!response.isEmpty()) {
            code = 200;
        } else {
            code = 500;
            response = "recoLogics are not uploaded !";
        }
        apiResponse = new ApiResponse<>(code, response);
        return apiResponse;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ApiResponse<String> getRecoLogics(HttpServletRequest request, @RequestBody UpdateRecoLogicRequest recoData) throws JsonProcessingException {
        log.info("reco data is updating");
        ApiResponse apiResponse;
        int code;
        String response = recoLogicservice.updateRecoLogic(recoData);
        if (response.contains("success")) {
            code = 200;
        } else {
            code = 500;
        }
        apiResponse = new ApiResponse<>(code, response);
        return apiResponse;
    }
}
