package travelplan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import travelplan.config.MockLog;
import travelplan.domain.QueryInfo;
import travelplan.domain.TransferTravelSearchInfo;
import travelplan.domain.TransferTravelSearchResult;
import travelplan.domain.TravelAdvanceResult;
import travelplan.service.TravelPlanService;

@RestController
public class TravelPlanController {

    @Autowired
    TravelPlanService travelPlanService;
    @Autowired
    MockLog mockLog;

    @RequestMapping(value="/travelPlan/getTransferResult", method= RequestMethod.POST)
    public TransferTravelSearchResult getTransferResult(@RequestBody TransferTravelSearchInfo info,@RequestHeader HttpHeaders headers) {
        mockLog.printLog("[Search Transit]");
        return travelPlanService.getTransferSearch(info, headers);
    }

    @RequestMapping(value="/travelPlan/getCheapest", method= RequestMethod.POST)
    public TravelAdvanceResult getByCheapest(@RequestBody QueryInfo queryInfo,@RequestHeader HttpHeaders headers) {
        mockLog.printLog("[Search Cheapest]");
        return travelPlanService.getCheapest(queryInfo, headers);
    }

    @RequestMapping(value="/travelPlan/getQuickest", method= RequestMethod.POST)
    public TravelAdvanceResult getByQuickest(@RequestBody QueryInfo queryInfo,@RequestHeader HttpHeaders headers) {
        mockLog.printLog("[Search Quickest]");
        return travelPlanService.getQuickest(queryInfo, headers);
    }

    @RequestMapping(value="/travelPlan/getMinStation", method= RequestMethod.POST)
    public TravelAdvanceResult getByMinStation(@RequestBody QueryInfo queryInfo,@RequestHeader HttpHeaders headers) {
        mockLog.printLog("[Search Min Station]");
        return travelPlanService.getMinStation(queryInfo, headers);
    }


}
