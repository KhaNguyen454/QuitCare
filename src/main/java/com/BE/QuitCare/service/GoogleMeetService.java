package com.BE.QuitCare.service;

import com.BE.QuitCare.entity.Account;
import com.BE.QuitCare.entity.SessionUser;
import com.BE.QuitCare.exception.BadRequestException;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Service
public class GoogleMeetService {

    private static final String TOKENS_DIRECTORY_PATH = "src/main/resources";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${google.calendar.client.id}")
    private String clientId;
    @Value("${google.calendar.client.secret}")
    private String clientSecret;
    @Value("${google.calendar.redirect.uri}")
    private String redirectUri;
    @Value("${google.calendar.application.name}")
    private String applicationName;
    @Value("${google.calendar.scope}")
    private String scope;
    @Value("${google.calendar.access.type}")
    private String accessType;
    @Value("${google.calendar.credentials.file}")
    private String credentialsFile;
    @Value("${google.calendar.user.root}")
    private String userRoot;
    @Value("${google.calendar.token.url}")
    private String tokenUrl;

    private Calendar googleCalendarService(Credential credential) throws GeneralSecurityException, IOException {
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(applicationName)
                .build();
    }

    public String createGoogleMeetLinkFromSlot(SessionUser sessionUser, Account coach) {
        try {
            Credential credential = getStoredCredential();
            if (credential == null) {
                throw new BadRequestException("Không tìm thấy token đăng nhập Google Calendar");
            }

            Calendar service = googleCalendarService(credential);

            Event event = new Event()
                    .setSummary("QuitCare Appointment")
                    .setDescription("Coach: " + coach.getFullName());

            // Chuyển LocalDate và LocalTime thành java.util.Date
            LocalDate date = sessionUser.getDate();
            LocalTime startTime = sessionUser.getStart();
            LocalTime endTime = sessionUser.getEnd();

            Date startDate = Date.from(date.atTime(startTime).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
            Date endDate = Date.from(date.atTime(endTime).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());

            EventDateTime start = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(startDate))
                    .setTimeZone("Asia/Ho_Chi_Minh");

            EventDateTime end = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(endDate))
                    .setTimeZone("Asia/Ho_Chi_Minh");

            event.setStart(start);
            event.setEnd(end);

            // Tạo link Google Meet
            String requestId = UUID.randomUUID().toString();
            ConferenceData conferenceData = new ConferenceData()
                    .setCreateRequest(new CreateConferenceRequest()
                            .setRequestId(requestId)
                            .setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet")));
            event.setConferenceData(conferenceData);

            Event createdEvent = service.events().insert("primary", event)
                    .setConferenceDataVersion(1)
                    .execute();

            return createdEvent.getConferenceData().getEntryPoints().get(0).getUri();

        } catch (GeneralSecurityException | IOException e) {
            throw new BadRequestException("Không thể tạo link Google Meet: " + e.getMessage());
        }
    }

    public String getAuthorizationUrl() throws GeneralSecurityException, IOException {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientId,
                clientSecret,
                Collections.singleton(scope))
                .setAccessType(accessType)
                .build();
        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .build();
    }

    public void exchangeCodeForToken(String code) {
        try {
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    clientId,
                    clientSecret,
                    Collections.singleton(scope))
                    .setAccessType(accessType)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .build();

            GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                    .setRedirectUri(redirectUri)
                    .execute();

            flow.createAndStoreCredential(tokenResponse, userRoot);
        } catch (IOException | GeneralSecurityException e) {
            throw new BadRequestException("Không thể đăng nhập Google Calendar");
        }
    }

    public Credential getStoredCredential() throws IOException, GeneralSecurityException {
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH));
        DataStore<com.google.api.client.auth.oauth2.StoredCredential> dataStore = dataStoreFactory.getDataStore(credentialsFile);

        if (dataStore.containsKey(userRoot)) {
            Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                    .setJsonFactory(JSON_FACTORY)
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret))
                    .setTokenServerUrl(new GenericUrl(tokenUrl))
                    .build();

            credential.setAccessToken(dataStore.get(userRoot).getAccessToken());
            credential.setRefreshToken(dataStore.get(userRoot).getRefreshToken());

            if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
                credential.refreshToken();
            }
            return credential;
        } else {
            return null;
        }
    }
}
