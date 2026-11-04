package io.onedev.server.oauth;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.shiro.subject.Subject;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.TokenRevocationRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;

import io.onedev.server.model.OAuthApplication;
import io.onedev.server.model.OAuthGrant;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;

/**
 * Methods accepting a Subject check that subject's permissions. Owner parameters
 * identify the target account, not the acting user. Methods without a Subject
 * leave authorization to callers unless explicitly documented otherwise.
 */
public interface OAuthProviderService {

	RSAKey getPublicSigningKey();

	void generateSigningKey();

	OAuthApplication getApplication(Long id);

	OAuthApplication findApplication(String clientId);

	List<OAuthApplication> getApplications(User owner);

	String saveApplication(OAuthApplication application);

	String rotateSecret(Long id);

	void deleteApplication(Long id);

	void revokeApplication(Long id);

	List<OAuthGrant> getGrants(User owner);

	void revokeGrant(Long id);

	void revokeGrants(User owner, Long applicationId);

	/**
	 * Internal operation without permission checks; callers are responsible for authorization.
	 */
	void revokeGrants(User owner);

	Map<String, String> getScopes();

	List<Role> getRoles(Scope scope) throws ParseException;

	/**
	 * Checks the subject's permission to manage each selected project. The caller
	 * must supply the subject of an enabled, signed-in user who approved consent.
	 */
	String authorize(Subject subject, AuthorizationRequest request, Collection<Long> projectIds) throws ParseException;

	/**
	 * Permission contract exception: validates OAuth client and grant credentials
	 * from the request instead of accepting a Subject.
	 */
	TokenResponse exchange(TokenRequest request);

	/**
	 * Permission contract exception: validates OAuth client credentials and token
	 * ownership from the request instead of accepting a Subject.
	 */
	HTTPResponse revoke(TokenRevocationRequest request);

	Map<String, Object> getMetadata();

	Map<String, Object> getJwks();

	/**
	 * Permission contract exception: validates the bearer token and limits claims
	 * to its granted scopes instead of accepting a Subject.
	 */
	Map<String, Object> getUserInfo(String accessToken);
}
