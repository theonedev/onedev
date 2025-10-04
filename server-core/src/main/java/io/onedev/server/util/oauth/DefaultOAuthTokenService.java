package io.onedev.server.util.oauth;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import org.joda.time.DateTime;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.onedev.server.util.oauth.OAuthUtils.getErrorMessage;

@Singleton
public class DefaultOAuthTokenService implements OAuthTokenService, SchedulableTask {

    @Inject
    private TaskScheduler taskScheduler;

    private final Map<String, CacheEntry> accessTokenCache = new ConcurrentHashMap<>();

    private volatile String taskId;

    @Override
    public String getAccessToken(String tokenEndpoint, String clientId, String clientSecret,
                                        RefreshTokenAccessor refreshTokenAccessor) {
        var refreshToken = refreshTokenAccessor.getRefreshToken();
        var cacheEntry = accessTokenCache.get(clientId);
        if (cacheEntry == null || cacheEntry.isExpired()) {
            var tokens = requestTokens(tokenEndpoint, clientId, clientSecret, refreshToken);
            cacheEntry = new CacheEntry(tokens.getAccessToken());
            accessTokenCache.put(clientId, cacheEntry);
            refreshTokenAccessor.setRefreshToken(tokens.getRefreshToken().getValue());
        }
        return cacheEntry.accessToken.getValue();
    }

    private Tokens requestTokens(String tokenEndpoint, String clientId,
                                       String clientSecret, String refreshTokenValue) {
        com.nimbusds.oauth2.sdk.token.RefreshToken refreshToken =
                new com.nimbusds.oauth2.sdk.token.RefreshToken(refreshTokenValue);
        AuthorizationGrant refreshTokenGrant = new RefreshTokenGrant(refreshToken);

        ClientAuthentication clientAuth = new ClientSecretBasic(
                new ClientID(clientId), new Secret(clientSecret));

        TokenResponse response;
        try {
            TokenRequest request = new TokenRequest(new URI(tokenEndpoint), clientAuth, refreshTokenGrant, null);
            response = TokenResponse.parse(request.toHTTPRequest().send());
        } catch (ParseException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        if (response.indicatesSuccess()) {
            return response.toSuccessResponse().getTokens();
        } else {
            TokenErrorResponse errorResponse = response.toErrorResponse();
            throw new ExplicitException(getErrorMessage(errorResponse.getErrorObject()));
        }

    }

    @Listen
    public void on(SystemStarted event) {
        taskId = taskScheduler.schedule(this);
    }

    @Listen
    public void on(SystemStopping event) {
        if (taskId != null)
            taskScheduler.unschedule(taskId);
    }

    @Override
    public void execute() {
        accessTokenCache.entrySet().removeIf(it -> it.getValue().isExpired());
    }

    @Override
    public ScheduleBuilder<?> getScheduleBuilder() {
        return SimpleScheduleBuilder.repeatHourlyForever();
    }

    private static class CacheEntry {

        final AccessToken accessToken;

        final Date generateDate = new Date();

        CacheEntry(AccessToken accessToken) {
            this.accessToken = accessToken;
        }

        boolean isExpired() {
            var lifetime = accessToken.getLifetime();
            return lifetime != 0 && new DateTime(generateDate).plusSeconds((int)lifetime).isBeforeNow();
        }

    }
}
