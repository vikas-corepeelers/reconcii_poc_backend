//package com.cpl.reconciliation.web.instore;
//
//import com.cpl.core.api.response.ApiResponse;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.web.service.JobApiService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@Data
//@Slf4j
//@RestController
//@RequestMapping("/public/job")
//public class JobApiController {
//
//    private final JobApiService jobApiService;
//
//    @RequestMapping(value = "/runJob", method = RequestMethod.GET)
//    public ApiResponse<String> runJobHandler(@RequestParam("jobName") DataSource jobName) {
//        jobApiService.runJob(jobName);
//        return new ApiResponse<>("Job Queued");
//    }
//}
