/*
 * Janssen Project software is available under the Apache License (2004). See http://www.apache.org/licenses/ for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.as.client;

import io.jans.as.model.common.AuthenticationMethod;
import io.jans.as.model.common.AuthorizationMethod;
import io.jans.as.model.common.HasParamName;
import io.jans.as.model.config.Constants;
import io.jans.as.model.util.Util;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows to retrieve HTTP requests to the authorization server and responses from it for display purposes.
 *
 * @author Javier Rojas Blum
 * @version October 5, 2021
 */
public abstract class BaseClient<T extends BaseRequest, V extends BaseResponse> {

    private static final Logger LOG = Logger.getLogger(BaseClient.class);

    private String url;

    protected T request;
    protected V response;
    protected ResteasyClient resteasyClient = null;
    protected WebTarget webTarget = null;
    protected Form requestForm = new Form();
    protected Response clientResponse = null;
    private final List<Cookie> cookies = new ArrayList<>();
    private final Map<String, String> headers = new HashMap<>();

    protected ClientHttpEngine executor = null;

    protected BaseClient() {
    }

    protected BaseClient(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }

    public V getResponse() {
        return response;
    }

    public void setResponse(V response) {
        this.response = response;
    }

    public ClientHttpEngine getExecutor() {
        return executor;
    }

    public void setExecutor(ClientHttpEngine executor) {
        this.executor = executor;
    }

    protected void addReqParam(String p_key, HasParamName p_value) {
        if (p_value != null) {
            addReqParam(p_key, p_value.getParamName());
        }
    }

    protected void addReqParam(String p_key, String p_value) {
        if (Util.allNotBlank(p_key, p_value)) {
            if (request.getAuthorizationMethod() == AuthorizationMethod.FORM_ENCODED_BODY_PARAMETER) {
            	requestForm.param(p_key, p_value);
            } else {
            	webTarget = webTarget.queryParam(p_key, p_value);
            }
        }
    }
/*
    @SuppressWarnings("java:S1874")
    public static void putAllFormParameters(ClientRequest clientRequest, BaseRequest request) {
        if (clientRequest != null && request != null) {
            final Map<String, String> parameters = request.getParameters();
            if (parameters != null && !parameters.isEmpty()) {
                for (Map.Entry<String, String> e : parameters.entrySet()) {
                    p_requestForm.param(e.getKey(), e.getValue());
                }
            }
        }
    }
*/
    public String getRequestAsString() {
        StringBuilder sb = new StringBuilder();

        try {
            URL theUrl = new URL(url);

            if (getHttpMethod().equals(HttpMethod.POST) || getHttpMethod().equals(HttpMethod.PUT) || getHttpMethod().equals(HttpMethod.DELETE)) {
                sb.append(getHttpMethod()).append(" ").append(theUrl.getPath()).append(Constants.SPACE_HTTP_11);
                sb.append("\n");
                sb.append("Host: ").append(theUrl.getHost());
                if (StringUtils.isNotBlank(request.getContentType())) {
                    sb.append("\n");
                    sb.append("Content-Type: ").append(request.getContentType());
                }
                if (StringUtils.isNotBlank(request.getMediaType())) {
                    sb.append("\n");
                    sb.append("Accept: ").append(request.getMediaType());
                }

                appendDpopHeader(sb);
                appendNoRedirectHeader(sb);

                if (request.getAuthorizationMethod() == null) {
                    if ((request.getAuthenticationMethod() == null || request.getAuthenticationMethod() == AuthenticationMethod.CLIENT_SECRET_BASIC) && request.hasCredentials()) {
                        String encodedCredentials = request.getEncodedCredentials();
                        sb.append("\n");
                        sb.append(Constants.AUTHORIZATION_BASIC).append(encodedCredentials);
                    }
                } else if (request.getAuthorizationMethod() == AuthorizationMethod.AUTHORIZATION_REQUEST_HEADER_FIELD && request instanceof UserInfoRequest) {
                    String accessToken = ((UserInfoRequest) request).getAccessToken();
                    sb.append("\n");
                    sb.append(Constants.AUTHORIZATION_BEARER).append(accessToken);
                }

                sb.append("\n");
                sb.append("\n");
                if (request instanceof RegisterRequest && ((RegisterRequest) request).hasJwtRequestAsString()) {
                    sb.append(((RegisterRequest) request).getJwtRequestAsString());
                } else {
                    sb.append(request.getQueryString());
                }
            } else if (getHttpMethod().equals(HttpMethod.GET)) {
                sb.append(getHttpMethod()).append(" ").append(theUrl.getPath()).append(Constants.SPACE_HTTP_11);
                if (StringUtils.isNotBlank(request.getQueryString())) {
                    sb.append("?").append(request.getQueryString());
                }
                sb.append(Constants.SPACE_HTTP_11);
                sb.append("\n");
                sb.append("Host: ").append(theUrl.getHost());
                if (StringUtils.isNotBlank(request.getContentType())) {
                    sb.append("\n");
                    sb.append("Content-Type: ").append(request.getContentType());
                }

                appendDpopHeader(sb);
                appendNoRedirectHeader(sb);

                if (request.getAuthorizationMethod() == null) {
                    if (request.hasCredentials()) {
                        String encodedCredentials = request.getEncodedCredentials();
                        sb.append("\n");
                        sb.append(Constants.AUTHORIZATION_BASIC).append(encodedCredentials);
                    } else if (request instanceof RegisterRequest) {
                        RegisterRequest r = (RegisterRequest) request;
                        String registrationAccessToken = r.getRegistrationAccessToken();
                        sb.append("\n");
                        sb.append(Constants.AUTHORIZATION_BEARER).append(registrationAccessToken);
                    }
                } else if (request.getAuthorizationMethod() == AuthorizationMethod.AUTHORIZATION_REQUEST_HEADER_FIELD && request instanceof UserInfoRequest) {
                    String accessToken = ((UserInfoRequest) request).getAccessToken();
                    sb.append("\n");
                    sb.append(Constants.AUTHORIZATION_BEARER).append(accessToken);
                }
            }
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
        }

        return sb.toString();
    }

    private void appendDpopHeader(StringBuilder sb) {
        if (request instanceof TokenRequest) {
            TokenRequest tokenRequest = (TokenRequest) request;
            if (tokenRequest.getDpop() != null) {
                sb.append("\n");
                sb.append("DPoP: ").append(tokenRequest.getDpop().toString());
            }
        }
    }

    private void appendNoRedirectHeader(StringBuilder sb) {
        if (request instanceof AuthorizationRequest) {
            AuthorizationRequest authorizationRequest = (AuthorizationRequest) request;
            if (authorizationRequest.isUseNoRedirectHeader()) {
                sb.append("\n");
                sb.append(AuthorizationRequest.NO_REDIRECT_HEADER + ": true");
            }
        }
    }

    public String getResponseAsString() {
        StringBuilder sb = new StringBuilder();

        if (response != null) {
            sb.append("HTTP/1.1 ").append(response.getStatus());
            if (response.getHeaders() != null) {
                for (String key : response.getHeaders().keySet()) {
                    sb.append("\n")
                            .append(key)
                            .append(": ")
                            .append(response.getHeaders().get(key).get(0));
                }
            }
            if (response.getEntity() != null) {
                sb.append("\n");
                sb.append("\n");
                sb.append(response.getEntity());
            }
        }
        return sb.toString();
    }

    // TODO: Rename method
    protected void initClientRequest() {
        if (this.executor == null) {
        	resteasyClient = (ResteasyClient) ResteasyClientBuilder.newClient();
        } else {
        	resteasyClient = ((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder()).httpEngine(executor).build();
        }

        webTarget = resteasyClient.target(getUrl());
    }

    protected void applyCookies(Builder clientRequest) {
		for (Cookie cookie : cookies) {
            clientRequest.cookie(cookie);
        }
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            clientRequest.header(headerEntry.getKey(), headerEntry.getValue());
        }
    }

    public void closeConnection() {
        try {
            if (clientResponse != null) {
                clientResponse.close();
            }
            // Why we should close engine after processing response?
//            if (resteasyClient != null && resteasyClient.httpEngine() != null) {
//            	resteasyClient.httpEngine().close();
//            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public abstract String getHttpMethod();

    public List<Cookie> getCookies() {
        return cookies;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
