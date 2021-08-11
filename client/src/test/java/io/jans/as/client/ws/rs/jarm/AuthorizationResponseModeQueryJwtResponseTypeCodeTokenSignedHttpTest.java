/*
 * Janssen Project software is available under the Apache License (2004). See http://www.apache.org/licenses/ for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */
package io.jans.as.client.ws.rs.jarm;

import io.jans.as.client.BaseTest;
import io.jans.as.client.RegisterResponse;
import io.jans.as.model.common.ResponseMode;
import io.jans.as.model.common.ResponseType;
import io.jans.as.model.crypto.signature.SignatureAlgorithm;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Javier Rojas Blum
 * @version July 28, 2021
 */
public class AuthorizationResponseModeQueryJwtResponseTypeCodeTokenSignedHttpTest extends BaseTest {

    @Parameters({"userId", "userSecret", "redirectUris", "redirectUri", "sectorIdentifierUri"})
    @Test
    public void testDefault(
            final String userId, final String userSecret, final String redirectUris, final String redirectUri,
            final String sectorIdentifierUri) throws Exception {
        showTitle("testDefault");

        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE, ResponseType.TOKEN);

        // 1. Register client
        RegisterResponse registerResponse = registerClient(redirectUris, responseTypes, sectorIdentifierUri, null,
                null, null, null);

        String clientId = registerResponse.getClientId();

        // 2. Request authorization
        List<String> scopes = Arrays.asList("openid", "profile", "address", "email");
        String state = UUID.randomUUID().toString();

        authorizationRequest(responseTypes, ResponseMode.QUERY_JWT, null, clientId, scopes, redirectUri, null,
                state, userId, userSecret);
    }

    @Parameters({"userId", "userSecret", "redirectUris", "redirectUri", "sectorIdentifierUri"})
    @Test
    public void testHS256(
            final String userId, final String userSecret, final String redirectUris, final String redirectUri,
            final String sectorIdentifierUri) throws Exception {
        showTitle("testHS256");

        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE, ResponseType.TOKEN);

        // 1. Register client
        RegisterResponse registerResponse = registerClient(redirectUris, responseTypes, sectorIdentifierUri, null,
                SignatureAlgorithm.HS256, null, null);

        String clientId = registerResponse.getClientId();
        sharedKey = registerResponse.getClientSecret();

        // 2. Request authorization
        List<String> scopes = Arrays.asList("openid", "profile", "address", "email");
        String state = UUID.randomUUID().toString();

        authorizationRequest(responseTypes, ResponseMode.QUERY_JWT, null, clientId, scopes, redirectUri, null,
                state, userId, userSecret);
    }

    @Parameters({"userId", "userSecret", "redirectUris", "redirectUri", "sectorIdentifierUri"})
    @Test
    public void testHS384(
            final String userId, final String userSecret, final String redirectUris, final String redirectUri,
            final String sectorIdentifierUri) throws Exception {
        showTitle("testHS384");

        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE, ResponseType.TOKEN);

        // 1. Register client
        RegisterResponse registerResponse = registerClient(redirectUris, responseTypes, sectorIdentifierUri, null,
                SignatureAlgorithm.HS384, null, null);

        String clientId = registerResponse.getClientId();
        sharedKey = registerResponse.getClientSecret();

        // 2. Request authorization
        List<String> scopes = Arrays.asList("openid", "profile", "address", "email");
        String state = UUID.randomUUID().toString();

        authorizationRequest(responseTypes, ResponseMode.QUERY_JWT, null, clientId, scopes, redirectUri, null,
                state, userId, userSecret);
    }

    @Parameters({"userId", "userSecret", "redirectUris", "redirectUri", "sectorIdentifierUri"})
    @Test
    public void testHS512(
            final String userId, final String userSecret, final String redirectUris, final String redirectUri,
            final String sectorIdentifierUri) throws Exception {
        showTitle("testHS512");

        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE, ResponseType.TOKEN);

        // 1. Register client
        RegisterResponse registerResponse = registerClient(redirectUris, responseTypes, sectorIdentifierUri, null,
                SignatureAlgorithm.HS512, null, null);

        String clientId = registerResponse.getClientId();
        sharedKey = registerResponse.getClientSecret();

        // 2. Request authorization
        List<String> scopes = Arrays.asList("openid", "profile", "address", "email");
        String state = UUID.randomUUID().toString();

        authorizationRequest(responseTypes, ResponseMode.QUERY_JWT, null, clientId, scopes, redirectUri, null,
                state, userId, userSecret);
    }

    @Parameters({"userId", "userSecret", "redirectUris", "redirectUri", "sectorIdentifierUri"})
    @Test
    public void testRS256(
            final String userId, final String userSecret, final String redirectUris, final String redirectUri,
            final String sectorIdentifierUri) throws Exception {
        showTitle("testRS256");

        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE, ResponseType.TOKEN);

        // 1. Register client
        RegisterResponse registerResponse = registerClient(redirectUris, responseTypes, sectorIdentifierUri, null,
                SignatureAlgorithm.RS256, null, null);

        String clientId = registerResponse.getClientId();

        // 2. Request authorization
        List<String> scopes = Arrays.asList("openid", "profile", "address", "email");
        String state = UUID.randomUUID().toString();

        authorizationRequest(responseTypes, ResponseMode.QUERY_JWT, null, clientId, scopes, redirectUri, null,
                state, userId, userSecret);
    }

    @Parameters({"userId", "userSecret", "redirectUris", "redirectUri", "sectorIdentifierUri"})
    @Test
    public void testRS384(
            final String userId, final String userSecret, final String redirectUris, final String redirectUri,
            final String sectorIdentifierUri) throws Exception {
        showTitle("testRS384");

        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE, ResponseType.TOKEN);

        // 1. Register client
        RegisterResponse registerResponse = registerClient(redirectUris, responseTypes, sectorIdentifierUri, null,
                SignatureAlgorithm.RS384, null, null);

        String clientId = registerResponse.getClientId();

        // 2. Request authorization
        List<String> scopes = Arrays.asList("openid", "profile", "address", "email");
        String state = UUID.randomUUID().toString();

        authorizationRequest(responseTypes, ResponseMode.QUERY_JWT, null, clientId, scopes, redirectUri, null,
                state, userId, userSecret);
    }

    @Parameters({"userId", "userSecret", "redirectUris", "redirectUri", "sectorIdentifierUri"})
    @Test
    public void testRS512(
            final String userId, final String userSecret, final String redirectUris, final String redirectUri,
            final String sectorIdentifierUri) throws Exception {
        showTitle("testRS512");

        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE, ResponseType.TOKEN);

        // 1. Register client
        RegisterResponse registerResponse = registerClient(redirectUris, responseTypes, sectorIdentifierUri, null,
                SignatureAlgorithm.RS512, null, null);

        String clientId = registerResponse.getClientId();

        // 2. Request authorization
        List<String> scopes = Arrays.asList("openid", "profile", "address", "email");
        String state = UUID.randomUUID().toString();

        authorizationRequest(responseTypes, ResponseMode.QUERY_JWT, null, clientId, scopes, redirectUri, null,
                state, userId, userSecret);
    }

    @Parameters({"userId", "userSecret", "redirectUris", "redirectUri", "sectorIdentifierUri"})
    @Test
    public void testES256(
            final String userId, final String userSecret, final String redirectUris, final String redirectUri,
            final String sectorIdentifierUri) throws Exception {
        showTitle("testES256");

        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE, ResponseType.TOKEN);

        // 1. Register client
        RegisterResponse registerResponse = registerClient(redirectUris, responseTypes, sectorIdentifierUri, null,
                SignatureAlgorithm.ES256, null, null);

        String clientId = registerResponse.getClientId();

        // 2. Request authorization
        List<String> scopes = Arrays.asList("openid", "profile", "address", "email");
        String state = UUID.randomUUID().toString();

        authorizationRequest(responseTypes, ResponseMode.QUERY_JWT, null, clientId, scopes, redirectUri, null,
                state, userId, userSecret);
    }

    @Parameters({"userId", "userSecret", "redirectUris", "redirectUri", "sectorIdentifierUri"})
    @Test
    public void testES384(
            final String userId, final String userSecret, final String redirectUris, final String redirectUri,
            final String sectorIdentifierUri) throws Exception {
        showTitle("testES384");

        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE, ResponseType.TOKEN);

        // 1. Register client
        RegisterResponse registerResponse = registerClient(redirectUris, responseTypes, sectorIdentifierUri, null,
                SignatureAlgorithm.ES384, null, null);

        String clientId = registerResponse.getClientId();

        // 2. Request authorization
        List<String> scopes = Arrays.asList("openid", "profile", "address", "email");
        String state = UUID.randomUUID().toString();

        authorizationRequest(responseTypes, ResponseMode.QUERY_JWT, null, clientId, scopes, redirectUri, null,
                state, userId, userSecret);
    }

    @Parameters({"userId", "userSecret", "redirectUris", "redirectUri", "sectorIdentifierUri"})
    @Test
    public void testES512(
            final String userId, final String userSecret, final String redirectUris, final String redirectUri,
            final String sectorIdentifierUri) throws Exception {
        showTitle("testES512");

        List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE, ResponseType.TOKEN);

        // 1. Register client
        RegisterResponse registerResponse = registerClient(redirectUris, responseTypes, sectorIdentifierUri, null,
                SignatureAlgorithm.ES512, null, null);

        String clientId = registerResponse.getClientId();

        // 2. Request authorization
        List<String> scopes = Arrays.asList("openid", "profile", "address", "email");
        String state = UUID.randomUUID().toString();

        authorizationRequest(responseTypes, ResponseMode.QUERY_JWT, null, clientId, scopes, redirectUri, null,
                state, userId, userSecret);
    }
}
