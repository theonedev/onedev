package io.onedev.server.util.oauth;

public interface OAuthTokenService {
    String getAccessToken(String tokenEndpoint, String clientId, String clientSecret,
                                 RefreshTokenAccessor refreshTokenAccessor);

}
