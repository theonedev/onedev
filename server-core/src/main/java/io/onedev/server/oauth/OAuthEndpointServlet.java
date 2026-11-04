package io.onedev.server.oauth;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenRevocationRequest;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;

import io.onedev.server.persistence.SessionService;
import net.minidev.json.JSONObject;

@Singleton
public class OAuthEndpointServlet extends HttpServlet {

	@Inject
	private OAuthProviderService providerService;
	@Inject
	private SessionService sessionService;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("X-Content-Type-Options", "nosniff");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType("application/json");
		var result = sessionService.call(() -> handle(request));
		response.setStatus(result.getStatusCode());
		if (result.getStatusCode() == 401)
			response.setHeader("WWW-Authenticate", request.getServletPath().endsWith("userinfo") ? "Bearer" : "Basic realm=\"OAuth\"");
		if (result.getStatusCode() == 405) {
			String path = request.getServletPath();
			response.setHeader("Allow", path.endsWith("userinfo") ? "GET, POST"
					: path.endsWith("token") || path.endsWith("revoke") ? "POST" : "GET");
		}
		if (result.getContent() != null)
			response.getWriter().write(result.getContent());
	}

	private HTTPResponse json(Map<String, ?> value) {
		var response = new HTTPResponse(200);
		response.setContent(JSONObject.toJSONString(value));
		return response;
	}

	private HTTPResponse handle(HttpServletRequest request) throws IOException {
		String path = request.getServletPath();
		try {
			if (path.endsWith("/token") || path.endsWith("/revoke")) {
				if (!"POST".equals(request.getMethod()))
					return new HTTPResponse(405);
				if (request.getQueryString() != null || request.getContentType() == null
						|| !request.getContentType().split(";", 2)[0].equalsIgnoreCase("application/x-www-form-urlencoded"))
					return new TokenErrorResponse(OAuth2Error.INVALID_REQUEST).toHTTPResponse();
				if (request.getContentLengthLong() > 16384)
					return new TokenErrorResponse(OAuth2Error.INVALID_REQUEST).toHTTPResponse();
				// Filters may already have asked the servlet container to parse the form body.
				// Use that parsed form instead of reading the (possibly consumed) input stream.
				var parameters = new java.util.LinkedHashMap<String, java.util.List<String>>();
				request.getParameterMap().forEach((key, values) -> parameters.put(key, java.util.Arrays.asList(values)));
				OAuthProtocol.checkSingleParameters(parameters);
				var body = com.nimbusds.oauth2.sdk.util.URLUtils.serializeParameters(parameters);
				if (body.length() > 16384)
					return new TokenErrorResponse(OAuth2Error.INVALID_REQUEST).toHTTPResponse();
				var http = new HTTPRequest(HTTPRequest.Method.POST, URI.create(request.getRequestURL().toString()));
				http.setContentType("application/x-www-form-urlencoded");
				http.setBody(body);
				var headers = Collections.list(request.getHeaders("Authorization"));
				if (headers.size() > 1)
					throw new ParseException("Repeated Authorization header", OAuth2Error.INVALID_REQUEST);
				if (!headers.isEmpty())
					http.setAuthorization(headers.get(0));
				if (!headers.isEmpty() && (parameters.containsKey("client_secret") || parameters.containsKey("client_id")))
					throw new ParseException("Use one client authentication method", OAuth2Error.INVALID_REQUEST);
				return path.endsWith("/token") ? providerService.exchange(TokenRequest.parse(http)).toHTTPResponse()
						: providerService.revoke(TokenRevocationRequest.parse(http));
			}
			if (!"GET".equals(request.getMethod()) && !(path.endsWith("/userinfo") && "POST".equals(request.getMethod())))
				return new HTTPResponse(405);
			if (path.startsWith("/.well-known/"))
				return json(providerService.getMetadata());
			if (path.endsWith("/jwks"))
				return json(providerService.getJwks());
			if (path.endsWith("/userinfo")) {
				var headers = Collections.list(request.getHeaders("Authorization"));
				if (headers.size() != 1)
					return new HTTPResponse(401);
				var token = BearerAccessToken.parse(headers.get(0));
				var claims = providerService.getUserInfo(token.getValue());
				return claims != null ? json(claims) : new HTTPResponse(401);
			}
			return new HTTPResponse(404);
		} catch (ParseException e) {
			if (path.endsWith("/userinfo"))
				return new HTTPResponse(401);
			return new TokenErrorResponse(e.getErrorObject() != null ? e.getErrorObject() : OAuth2Error.INVALID_REQUEST).toHTTPResponse();
		}
	}
}
