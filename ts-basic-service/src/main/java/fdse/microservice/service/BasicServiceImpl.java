package fdse.microservice.service;

import fdse.microservice.config.MockLog;
import fdse.microservice.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;

@Service
public class BasicServiceImpl implements BasicService{

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    MockLog mockLog;

    @Override
    public ResultForTravel queryForTravel(QueryForTravel info, HttpHeaders headers){

        ResultForTravel result = new ResultForTravel();
        result.setStatus(true);
        //车站站名服务
//        boolean startingPlaceExist = restTemplate.postForObject(
//                "http://ts-station-service:12345/station/exist", new QueryStation(info.getStartingPlace()), Boolean.class);
//        boolean endPlaceExist = restTemplate.postForObject(
//                "http://ts-station-service:12345/station/exist", new QueryStation(info.getEndPlace()),  Boolean.class);
        boolean startingPlaceExist = checkStationExists(info.getStartingPlace(), headers);
        boolean endPlaceExist = checkStationExists(info.getEndPlace(), headers);
        if(!startingPlaceExist || !endPlaceExist){
            result.setStatus(false);
        }

//        String startingPlaceId = restTemplate.postForObject(
//                "http://ts-station-service:12345/station/queryForId", new QueryStation(info.getStartingPlace()), String.class);
//        String endPlaceId = restTemplate.postForObject(
//                "http://ts-station-service:12345/station/queryForId", new QueryStation(info.getEndPlace()),  String.class);



        //配置
        //查询车票配比，以车站ABC为例，A是始发站，B是途径的车站，C是终点站，分配AC 50%，如果总票数100，那么AC有50张票，AB和BC也各有
        //50张票，因为AB和AC拼起来正好是一张AC。
//        String proportion = restTemplate.postForObject("http://ts-config-service:15679/config/query",
//                new QueryConfig("DirectTicketAllocationProportion"), String.class
//        );
//        double percent = 1.0;
//        if(proportion.contains("%")) {
//            proportion = proportion.replaceAll("%", "");
//            percent = Double.valueOf(proportion)/100;
//            result.setPercent(percent);
//        }else{
//            result.setStatus(false);
//        }

        //车服务
//        TrainType trainType = restTemplate.postForObject(
//                "http://ts-train-service:14567/train/retrieve", new QueryTrainType(info.getTrip().getTrainTypeId()), TrainType.class
//        );
        TrainType trainType = queryTrainType(info.getTrip().getTrainTypeId(), headers);
        if(trainType == null){
            mockLog.printLog("traintype doesn't exist");
            result.setStatus(false);
        }else{
            result.setTrainType(trainType);
        }

        //票价服务
//        QueryPriceInfo queryPriceInfo = new QueryPriceInfo();
//        queryPriceInfo.setStartingPlaceId(startingPlaceId);
//        queryPriceInfo.setEndPlaceId(endPlaceId);
//        queryPriceInfo.setTrainTypeId(trainType.getId());
//        queryPriceInfo.setSeatClass("economyClass");
//        String priceForEconomyClass = restTemplate.postForObject(
//                "http://ts-price-service:16579/price/query",queryPriceInfo , String.class
//        );
//
//        queryPriceInfo.setSeatClass("confortClass");
//        String priceForConfortClass = restTemplate.postForObject(
//                "http://ts-price-service:16579/price/query", queryPriceInfo, String.class
//        );

        String routeId = info.getTrip().getRouteId();
        String trainTypeString = trainType.getId();
        Route route = getRouteByRouteId(routeId, headers);
        PriceConfig priceConfig = queryPriceConfigByRouteIdAndTrainType(routeId,trainTypeString, headers);

        String startingPlaceId = queryForStationId(new QueryStation(info.getStartingPlace()), headers);
        String endPlaceId = queryForStationId(new QueryStation(info.getEndPlace()), headers);
        int indexStart = route.getStations().indexOf(startingPlaceId);
        int indexEnd = route.getStations().indexOf(endPlaceId);

        int distance = route.getDistances().get(indexEnd) - route.getDistances().get(indexStart);

        double priceForEconomyClass = distance * priceConfig.getBasicPriceRate();//需要price Rate和距离（起始站）
        double priceForConfortClass= distance * priceConfig.getFirstClassPriceRate();

        HashMap<String,String> prices = new HashMap<String,String>();
        prices.put("economyClass","" + priceForEconomyClass);
        prices.put("confortClass","" + priceForConfortClass);
        result.setPrices(prices);

        result.setPercent(1.0);

        return result;
    }

    @Override
    public String queryForStationId(QueryStation info, HttpHeaders headers){
        mockLog.printLog("[Basic Information Service][Query For Station Id] Station Id:" + info.getName());
        HttpEntity requestEntity = new HttpEntity(info, headers);
        ResponseEntity<String> re = restTemplate.exchange(
                "http://ts-station-service:12345/station/queryForId",
                HttpMethod.POST,
                requestEntity,
                String.class);
        String id = re.getBody();
//        String id = restTemplate.postForObject(
//                "http://ts-station-service:12345/station/queryForId", info, String.class);
        return id;
    }

    public boolean checkStationExists(String stationName, HttpHeaders headers){
        mockLog.printLog("[Basic Information Service][Check Station Exists] Station Name:" + stationName);
        HttpEntity requestEntity = new HttpEntity(new QueryStation(stationName), headers);
        ResponseEntity<Boolean> re = restTemplate.exchange(
                "http://ts-station-service:12345/station/exist",
                HttpMethod.POST,
                requestEntity,
                Boolean.class);
        Boolean exist = re.getBody();
//        Boolean exist = restTemplate.postForObject(
//                "http://ts-station-service:12345/station/exist", new QueryStation(stationName), Boolean.class);
        return exist.booleanValue();
    }

    public TrainType queryTrainType(String trainTypeId, HttpHeaders headers){
        mockLog.printLog("[Basic Information Service][Query Train Type] Train Type:" + trainTypeId);
        HttpEntity requestEntity = new HttpEntity(new QueryTrainType(trainTypeId), headers);
        ResponseEntity<TrainType> re = restTemplate.exchange(
                "http://ts-train-service:14567/train/retrieve",
                HttpMethod.POST,
                requestEntity,
                TrainType.class);
        TrainType trainType = re.getBody();
//        TrainType trainType = restTemplate.postForObject(
//                "http://ts-train-service:14567/train/retrieve", new QueryTrainType(trainTypeId), TrainType.class
//        );
        return trainType;
    }

    private Route getRouteByRouteId(String routeId, HttpHeaders headers){
        mockLog.printLog("[Basic Information Service][Get Route By Id] Route ID：" + routeId);
        HttpEntity requestEntity = new HttpEntity(headers);
        ResponseEntity<GetRouteByIdResult> re = restTemplate.exchange(
                "http://ts-route-service:11178/route/queryById/"+ routeId,
                HttpMethod.GET,
                requestEntity,
                GetRouteByIdResult.class);
        GetRouteByIdResult result = re.getBody();
//        GetRouteByIdResult result = restTemplate.getForObject(
//                "http://ts-route-service:11178/route/queryById/" + routeId,
//                GetRouteByIdResult.class);
        if(result.isStatus() == false){
            mockLog.printLog("[Basic Information Service][Get Route By Id] Fail." + result.getMessage());
            return null;
        }else{
            mockLog.printLog("[Basic Information Service][Get Route By Id] Success.");
            return result.getRoute();
        }
    }

    private PriceConfig queryPriceConfigByRouteIdAndTrainType(String routeId,String trainType, HttpHeaders headers){
        mockLog.printLog("[Basic Information Service][Query For Price Config] RouteId:"
                + routeId + "TrainType:" + trainType);
        QueryPriceConfigByTrainAndRoute info = new QueryPriceConfigByTrainAndRoute();
        info.setRouteId(routeId);
        info.setTrainType(trainType);
        HttpEntity requestEntity = new HttpEntity(info, headers);
        ResponseEntity<ReturnSinglePriceConfigResult> re = restTemplate.exchange(
                "http://ts-price-service:16579/price/query",
                HttpMethod.POST,
                requestEntity,
                ReturnSinglePriceConfigResult.class);
        ReturnSinglePriceConfigResult result = re.getBody();
//        ReturnSinglePriceConfigResult result = restTemplate.postForObject(
//                "http://ts-price-service:16579/price/query",
//                info,
//                ReturnSinglePriceConfigResult.class
//        );
        return result.getPriceConfig();
    }

}
