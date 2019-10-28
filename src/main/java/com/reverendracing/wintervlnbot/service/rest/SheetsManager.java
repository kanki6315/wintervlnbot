/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service.rest;

import static com.reverendracing.wintervlnbot.util.CsvInputs.BACKUP_CAR_NUMBER;
import static com.reverendracing.wintervlnbot.util.CsvInputs.CAR_NUMBER;
import static com.reverendracing.wintervlnbot.util.CsvInputs.CLASS;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIFTH_DRIVER_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIFTH_DRIVER_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIFTH_DRIVER_IR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIFTH_DRIVER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIFTH_DRIVER_SR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIRST_DRIVER_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIRST_DRIVER_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIRST_DRIVER_IR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIRST_DRIVER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIRST_DRIVER_SR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FOURTH_DRIVER_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FOURTH_DRIVER_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FOURTH_DRIVER_IR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FOURTH_DRIVER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FOURTH_DRIVER_SR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.SECOND_DRIVER_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.SECOND_DRIVER_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.SECOND_DRIVER_IR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.SECOND_DRIVER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.SECOND_DRIVER_SR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_CODEWORD;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_GT3_CAR_CHOICE;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_MANAGER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.THIRD_DRIVER_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.THIRD_DRIVER_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.THIRD_DRIVER_IR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.THIRD_DRIVER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.THIRD_DRIVER_SR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TIMESTAMP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.reverendracing.wintervlnbot.data.Entry;
import com.reverendracing.wintervlnbot.data.EntryRepository;

public class SheetsManager {

    private Logger logger;

    private final String spreadsheetId;
    private final String defaultRange;

    private Credential credential;

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final EntryRepository entryRepository;

    private Long lastUpdated;

    private static final int FIFTEEN_MINUTES = 15 * 60 * 1000;

    public SheetsManager(
            final EntryRepository entryRepository,
            final String spreadsheetId,
            final String defaultRange) {

        this.entryRepository = entryRepository;

        this.spreadsheetId = spreadsheetId;
        this.defaultRange = defaultRange;

        logger = LoggerFactory.getLogger(SheetsManager.class);

        connectAndCacheToken();
    }

    public EntryRepository getEntryRepository() throws IOException, GeneralSecurityException {

        long now = System.currentTimeMillis();
        if(lastUpdated == null || now - FIFTEEN_MINUTES > lastUpdated) {
            getEntriesAndCache();
        }
        return entryRepository;
    }

    private void connectAndCacheToken() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            credential = getCredentials(HTTP_TRANSPORT);
            logger.info("Cached token for GSheets");
        } catch (IOException | GeneralSecurityException ex) {
            logger.info("Unable to connect/authorize with sheets", ex);
        }
    }

    private Sheets getSheetService() throws GeneralSecurityException, IOException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("Winter-VLN Bot")
                .build();
    }

    private void getEntriesAndCache() throws IOException, GeneralSecurityException {

        logger.info("Updating database");
        Sheets service = getSheetService();

        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, defaultRange)
                .execute();

        List<Entry> entries = getEntriesFromSheetResponse(response);

        entryRepository.saveAll(entries);
        lastUpdated = System.currentTimeMillis();
        logger.info(String.format("Updated databse at %d", lastUpdated));
    }

    private List<Entry> getEntriesFromSheetResponse(ValueRange response) throws IOException {

        List<List<Object>> spreadsheetOutput = response.getValues();
        List<String> headers = spreadsheetOutput.get(0).stream()
                .map(object -> Objects.toString(object, null))
                .collect(Collectors.toList());

        String csv = getCsvFromValues(spreadsheetOutput.subList(1, spreadsheetOutput.size()));

        CSVParser csvParser = new CSVParser(new StringReader(csv),
                CSVFormat.DEFAULT
                        .withHeader(headers.toArray(new String[headers.size()]))
                        .withDelimiter(',')
                        .withIgnoreHeaderCase()
                        .withIgnoreEmptyLines()
                        .withTrim());

        List<Entry> entries = new ArrayList<>();
        for(CSVRecord record : csvParser) {
            entries.add(getEntryFromRecord(record));
        }

        return entries;
    }

    private String getCsvFromValues(List<List<Object>> subList) {

        StringJoiner csvBuilder = new StringJoiner("\n");
        for(List<Object> row : subList) {
            StringJoiner rowBuilder = new StringJoiner(",");
            for(Object item : row) {
                rowBuilder.add(String.format("\"%s\"", item.toString()));
            }
            csvBuilder.add(rowBuilder.toString());
        }

        return csvBuilder.toString();
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

        // Load client secrets.
        InputStream in = SheetsManager.class.getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        File file = new File("tokens");
        logger.info(file.getCanonicalPath());
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(file))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    private Entry getEntryFromRecord(CSVRecord csvRecord) {

        Map<String, String> record = csvRecord.toMap();
        Entry entry = new Entry();
        entry.setRegistrationTime(record.get(TIMESTAMP));
        entry.setCarClass(record.get(CLASS));
        entry.setCarNumber(record.get(CAR_NUMBER));
        entry.setBackupCarNumber(record.get(BACKUP_CAR_NUMBER));
        entry.setTeamName(record.get(TEAM_NAME));
        entry.setTeamId(record.get(TEAM_ID));
        entry.setTeamCountry(record.get(TEAM_COUNTRY));
        entry.setTeamManagerName(record.get(TEAM_MANAGER_NAME));
        entry.setTeamCodeword(record.get(TEAM_CODEWORD));
        entry.setGt3CarChoice(record.get(TEAM_GT3_CAR_CHOICE));
        entry.setFirstDriverName(record.get(FIRST_DRIVER_NAME));
        entry.setFirstDriverId(record.get(FIRST_DRIVER_ID));
        entry.setFirstDriverCountry(record.get(FIRST_DRIVER_COUNTRY));
        entry.setFirstDriverIR(record.get(FIRST_DRIVER_IR));
        entry.setFirstDriverSR(record.get(FIRST_DRIVER_SR));
        if(record.containsKey(SECOND_DRIVER_NAME))
            entry.setSecondDriverName(record.get(SECOND_DRIVER_NAME));
        if(record.containsKey(SECOND_DRIVER_ID))
            entry.setSecondDriverId(record.get(SECOND_DRIVER_ID));
        if(record.containsKey(SECOND_DRIVER_COUNTRY))
            entry.setSecondDriverCountry(record.get(SECOND_DRIVER_COUNTRY));
        if(record.containsKey(SECOND_DRIVER_IR))
            entry.setSecondDriverIR(record.get(SECOND_DRIVER_IR));
        if(record.containsKey(SECOND_DRIVER_SR))
            entry.setSecondDriverSR(record.get(SECOND_DRIVER_SR));
        if(record.containsKey(THIRD_DRIVER_NAME))
            entry.setThirdDriverName(record.get(THIRD_DRIVER_NAME));
        if(record.containsKey(THIRD_DRIVER_ID))
            entry.setThirdDriverId(record.get(THIRD_DRIVER_ID));
        if(record.containsKey(THIRD_DRIVER_COUNTRY))
            entry.setThirdDriverCountry(record.get(THIRD_DRIVER_COUNTRY));
        if(record.containsKey(THIRD_DRIVER_IR))
            entry.setThirdDriverIR(record.get(THIRD_DRIVER_IR));
        if(record.containsKey(THIRD_DRIVER_SR))
            entry.setThirdDriverSR(record.get(THIRD_DRIVER_SR));
        if(record.containsKey(FOURTH_DRIVER_NAME))
            entry.setFourthDriverName(record.get(FOURTH_DRIVER_NAME));
        if(record.containsKey(FOURTH_DRIVER_ID))
            entry.setFourthDriverId(record.get(FOURTH_DRIVER_ID));
        if(record.containsKey(FOURTH_DRIVER_COUNTRY))
            entry.setFourthDriverCountry(record.get(FOURTH_DRIVER_COUNTRY));
        if(record.containsKey(FOURTH_DRIVER_IR))
            entry.setFourthDriverIR(record.get(FOURTH_DRIVER_IR));
        if(record.containsKey(FOURTH_DRIVER_SR))
            entry.setFourthDriverSR(record.get(FOURTH_DRIVER_SR));
        if(record.containsKey(FIFTH_DRIVER_NAME))
            entry.setFifthDriverName(record.get(FIFTH_DRIVER_NAME));
        if(record.containsKey(FIFTH_DRIVER_ID))
            entry.setFifthDriverId(record.get(FIFTH_DRIVER_ID));
        if(record.containsKey(FIFTH_DRIVER_COUNTRY))
            entry.setFifthDriverCountry(record.get(FIFTH_DRIVER_COUNTRY));
        if(record.containsKey(FIFTH_DRIVER_IR))
            entry.setFifthDriverIR(record.get(FIFTH_DRIVER_IR));
        if(record.containsKey(FIFTH_DRIVER_SR))
            entry.setFifthDriverSR(record.get(FIFTH_DRIVER_SR));

        return entry;
    }
}
