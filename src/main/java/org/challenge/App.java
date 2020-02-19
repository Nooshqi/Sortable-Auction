package org.challenge;

import java.io.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {
    public static void main(String[] args) throws IOException
    {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode input = mapper.readValue(new File("input.json"), JsonNode.class);
        JsonNode configg = mapper.readValue(new File("config.json"), JsonNode.class);


        ValidateAuction auction = new ValidateAuction();
        System.out.println(auction.validateEntry(input, configg));

    }
}
