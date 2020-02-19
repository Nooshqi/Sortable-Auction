package org.challenge;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ValidateAuction {
    ObjectMapper mapper = new ObjectMapper();

    //Go through each input JSON entry and perform validations before running auction
    public String validateEntry(JsonNode input, JsonNode configg) throws IOException {
        List<List<String>> list = new ArrayList<List<String>>();
        Iterator iterator = input.elements();

        while (iterator.hasNext()) {
            JsonNode nextInput = (JsonNode) iterator.next();
            list.add(validateSite(nextInput, configg));
        }
        JsonNode ans = mapper.readTree(list.toString());
        String output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ans);
        return output;

    }


    //Ensure input Site is registered in Config file before running the auction
    public List<String> validateSite(JsonNode input, JsonNode configg) throws IOException {
        JsonNode getSites = configg.get("sites");
        Iterator iterator = getSites.elements();
        List<String> list = new ArrayList<String>();

        while (iterator.hasNext()) {
            JsonNode element = (JsonNode) iterator.next();
            if (element.get("name").asText().equals(input.get("site").asText())) {
                Double floor = element.get("floor").asDouble();
                return generateRank(input, element, configg, floor);
            }

        }
        return list;
    }


    // Ensure bidder is permitted to bid on this site
    public Boolean validateBidder(JsonNode input, JsonNode site) {
        Iterator iterator = site.get("bidders").elements();

        while (iterator.hasNext()) {
            JsonNode element = (JsonNode) iterator.next();
            if (element.asText().equals(input.get("bidder").asText())) {
                return true;
            }
        }
        return false;
    }


    //Compare adjusted bid to the Site floor
    public Boolean validateFloor(Double floor, Double adjusted){
        return (adjusted >= floor);
    }


    //Adjust bid as defined by the config file
    public Double adjustBid(Double adjustment, Double bid){
        return (adjustment+1)*bid;
    }

    
    //Collect bid adjustment values and store them in a hashmap
    public Map<String, Double> getBidderAdjustments(JsonNode configg){
        Map<String, Double> validBidders = new HashMap<>();
        Iterator iterator = configg.get("bidders").elements();
        while(iterator.hasNext()){
            JsonNode bidderNode = (JsonNode) iterator.next();
            String name = bidderNode.get("name").asText();
            Double adjustment = bidderNode.get("adjustment").asDouble();
            validBidders.put(name, adjustment);
            
        }
        return validBidders;
    }


    //Collect the ad units being auctioned into a list
    public List<String> getAuctionedUnits(JsonNode input){
        List<String> units = new ArrayList<String>();
        Iterator iterator = input.get("units").elements();
        while(iterator.hasNext()){
            String unitNode = iterator.next().toString();
            units.add(unitNode);
            
        }
        return units;
    }


    //Generate a list of the winning bids per ad unit per site 
    public List<String> generateRank(JsonNode input, JsonNode site, JsonNode configg, Double floor) throws IOException {
        Map<String, Double> adjustments = getBidderAdjustments(configg);
        List<String> units = getAuctionedUnits(input);

        //Iterate through bids processing both bidder and bid validity before auction
        String topbid = "";
        List<String>  winningbids = new ArrayList<String>();
        for(String currentunit : units){
            Double adjusted = 0.0;

            Iterator iterator = input.get("bids").elements();
            while(iterator.hasNext()){
                JsonNode element = (JsonNode) iterator.next();
                if(validateBidder(element, site)){
                    String unit = element.get("unit").toString();

                    if(currentunit.equals(unit)){
                        String bidder = element.get("bidder").asText().replaceAll("^\"|\"$", "");
                        Double adj = adjustments.get(bidder);
                        Double bid = element.get("bid").asDouble();
                        double currentadj = adjustBid(adj, bid);

                        if((adjusted <= currentadj) && validateFloor(floor, currentadj)){
                            topbid = element.toString();
                            adjusted = currentadj;
                        }
                    }
                }
            }
            winningbids.add(topbid);

        }
        return winningbids;
    }
}