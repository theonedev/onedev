package io.onedev.server.util.oauth;

public interface OAuthTokenManager {
    String getAccessToken(String tokenEndpoint, String clientId, String clientSecret,
                                 RefreshTokenAccessor refreshTokenAccessor);

}
