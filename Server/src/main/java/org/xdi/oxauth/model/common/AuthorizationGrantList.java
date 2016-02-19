/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.xdi.oxauth.model.common;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.xdi.oxauth.model.registration.Client;

import java.util.Date;
import java.util.List;

/**
 * Component to hold in memory authorization grant objects.
 *
 * @author Javier Rojas Blum Date: 09.29.2011
 */
@Name("authorizationGrantList")
@Startup(depends = "appInitializer")
@Scope(ScopeType.APPLICATION)
public class AuthorizationGrantList implements IAuthorizationGrantList {

    private IAuthorizationGrantList grant;

    /**
     * Initializes the authorization grant list.
     */
    @Create
    public void init() {
        grant = AuthorizationGrantListLdap.instance();
    }

    @Override
    public List<AuthorizationGrant> getAuthorizationGrants() {
        return grant.getAuthorizationGrants();
    }

    @Override
    public void removeAuthorizationGrants(List<AuthorizationGrant> authorizationGrants) {
        grant.removeAuthorizationGrants(authorizationGrants);
    }

    @Override
    public void addAuthorizationGrant(AuthorizationGrant authorizationGrant) {
        grant.addAuthorizationGrant(authorizationGrant);
    }

    /**
     * Creates an {@link AuthorizationGrant}
     *
     * @param user               The resource owner.
     * @param client             An application making protected resource requests on behalf of the resource owner and
     *                           with its authorization.
     * @param authenticationTime The Claim Value is the number of seconds from 1970-01-01T0:0:0Z as measured in UTC
     *                           until the date/time that the End-User authentication occurred.
     * @return The authorization grant.
     */
    @Override
    public AuthorizationGrant createAuthorizationGrant(User user, Client client, Date authenticationTime) {
        return grant.createAuthorizationGrant(user, client, authenticationTime);
    }

    /**
     * Creates an {@link AuthorizationCodeGrant}.
     *
     * @param user               The resource owner.
     * @param client             An application making protected resource requests on behalf of
     *                           the resource owner and with its authorization.
     * @param authenticationTime The Claim Value is the number of seconds from 1970-01-01T0:0:0Z as measured in UTC
     *                           until the date/time that the End-User authentication occurred.
     * @return The authorization code grant.
     */
    @Override
    public AuthorizationCodeGrant createAuthorizationCodeGrant(User user, Client client, Date authenticationTime) {
        return grant.createAuthorizationCodeGrant(user, client, authenticationTime);
    }

    /**
     * Creates an {@link ImplicitGrant}.
     *
     * @param user               The resource owner.
     * @param client             An application making protected resource requests on behalf of the resource owner and
     *                           with its authorization.
     * @param authenticationTime The Claim Value is the number of seconds from 1970-01-01T0:0:0Z as measured in UTC
     *                           until the date/time that the End-User authentication occurred.
     * @return The implicit grant
     */
    @Override
    public ImplicitGrant createImplicitGrant(User user, Client client, Date authenticationTime) {
        return grant.createImplicitGrant(user, client, authenticationTime);
    }

    /**
     * Creates a {@link ClientCredentialsGrant}.
     *
     * @param user   The resource owner.
     * @param client An application making protected resource requests on behalf of
     *               the resource owner and with its authorization.
     * @return The client credentials grant.
     */
    @Override
    public ClientCredentialsGrant createClientCredentialsGrant(User user, Client client) {
        return grant.createClientCredentialsGrant(user, client);
    }

    /**
     * Creates a {@link ResourceOwnerPasswordCredentialsGrant}
     *
     * @param user   The resource owner.
     * @param client An application making protected resource requests on behalf of
     *               the resource owner and with its authorization.
     * @return The resource owner password credentials grant.
     */
    @Override
    public ResourceOwnerPasswordCredentialsGrant createResourceOwnerPasswordCredentialsGrant(User user, Client client) {
        return grant.createResourceOwnerPasswordCredentialsGrant(user, client);
    }

    /**
     * Search the authorization grant given an authorization code.
     * <p/>
     * The client MUST NOT use the authorization code more than once. If an
     * authorization code is used more than once, the authorization server MUST
     * deny the request and SHOULD attempt to revoke all tokens previously
     * issued based on that authorization code.
     *
     * @param clientId          An application making protected resource requests on behalf of
     *                          the resource owner and with its authorization.
     * @param authorizationCode authorization code
     * @return The authorization code grant, otherwise <code>null</code>.
     */
    @Override
    public AuthorizationCodeGrant getAuthorizationCodeGrant(String clientId, String authorizationCode) {
        return grant.getAuthorizationCodeGrant(clientId, authorizationCode);
    }

    /**
     * Search the authorization grant given a refresh token. The refresh token
     * must be valid, otherwise the function will return null.
     *
     * @param clientId         An application making protected resource requests on behalf of
     *                         the resource owner and with its authorization.
     * @param refreshTokenCode The refresh token code.
     * @return The authorization grant, otherwise <code>null</code>.
     */
    @Override
    public AuthorizationGrant getAuthorizationGrantByRefreshToken(String clientId, String refreshTokenCode) {
        return grant.getAuthorizationGrantByRefreshToken(clientId, refreshTokenCode);
    }

    @Override
    public List<AuthorizationGrant> getAuthorizationGrant(String clientId) {
        return grant.getAuthorizationGrant(clientId);
    }

    /**
     * Search the authorization grant given an access token.
     *
     * @param tokenCode The access token code.
     * @return The authorization grant, otherwise <code>null</code>.
     */
    @Override
    public AuthorizationGrant getAuthorizationGrantByAccessToken(String tokenCode) {
        return grant.getAuthorizationGrantByAccessToken(tokenCode);
    }

    @Override
    public AuthorizationGrant getAuthorizationGrantByIdToken(String idToken){
        return grant.getAuthorizationGrantByIdToken(idToken);
    }
}
