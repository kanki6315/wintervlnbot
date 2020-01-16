/**
 * Copyright (C) 2020 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.service.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reverendracing.wintervlnbot.data.Driver;
import com.reverendracing.wintervlnbot.data.Entry;
import com.reverendracing.wintervlnbot.util.model.DriverDTO;
import com.reverendracing.wintervlnbot.util.model.EntryDTO;

public class RequestBuilder {

    OkHttpClient client;
    ObjectMapper mapper;

    public RequestBuilder() {
        client = new OkHttpClient();
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<Entry> getEntries(String leagueId) {
        try {
            String response = makeRequest(
                String.format(
                    "https://www.wintervln.com/api/leagues/%s/entries?includeDrivers=true",
                    "91c743dc-6905-4e9d-99a3-64142cb019e9"));

            List<EntryDTO> dtos = mapper.readValue(response, new TypeReference<List<EntryDTO>>() {
            });
            List<Entry> returnVal = new ArrayList<>();
            for(EntryDTO dto : dtos) {
                Entry entry = new Entry();
                entry.setId(dto.getId());
                entry.setCarClass(dto.getClassDto().getName());
                entry.setCarName(dto.getCarName());
                entry.setTeamId(dto.getTeamId());
                entry.setTeamName(dto.getTeamName());
                entry.setTeamManagerName(dto.getTeamManagerName());
                entry.setCarNumber(dto.getCarNumber());
                for(DriverDTO driverDTO : dto.getDrivers()) {
                    Driver driver = new Driver();
                    driver.setId(driverDTO.getId());
                    driver.setEntryId(driverDTO.getEntryId());
                    driver.setSafetyRating(driverDTO.getSafetyRating());
                    driver.setLicenseLevel(driverDTO.getLicenseLevel());
                    driver.setIrating(driverDTO.getIrating());
                    driver.setDriverName(driverDTO.getDriverName());
                    driver.setDriverId(driverDTO.getDriverId());
                    entry.addDriver(driver);
                }
                returnVal.add(entry);
            }
            return returnVal;
        } catch(Exception ex) {
            throw new RuntimeException("Unable to parse json response into entries");
        }
    }

    private String makeRequest(String url) {
        Request request = new Request.Builder()
            .url(url)
            .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
        catch(IOException ioException) {
            throw new RuntimeException("Unable to read response");
        }
    }
}