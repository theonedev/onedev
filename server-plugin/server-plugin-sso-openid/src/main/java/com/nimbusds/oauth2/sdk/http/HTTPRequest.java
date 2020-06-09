package com.nimbusds.oauth2.sdk.http;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.entity.ContentType;

import net.jcip.annotations.ThreadSafe;

import net.minidev.json.JSONObject;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.oauth2.sdk.util.URLUtils;


/**
 * HTTP request with support for the parameters required to construct an 
 * {@link com.nimbusds.oauth2.sdk.Request OAuth 2.0 request message}.
 *
 * <p>Supported HTTP methods:
 *
 * <ul>
 *     <li>{@link Method#GET HTTP GET}
 *     <li>{@link Method#POST HTTP POST}
 *     <li>{@link Method#POST HTTP PUT}
 *     <li>{@link Method#POST HTTP DELETE}
 * </ul>
 *
 * <p>Supported request headers:
 *
 * <ul>
 *     <li>Content-Type
 *     <li>Authorization
 * </ul>
 */
@ThreadSafe
public class HTTPRequest extends HTTPMessage {


	/**
	 * Enumeration of the HTTP methods used in OAuth 2.0 requests.
	 */
	public static enum Method {
	
		/**
		 * HTTP GET.
		 */
		GET,
		
		
		/**
		 * HTTP POST.
		 */
		POST,
		
		
		/**
		 * HTTP PUT.
		 */
		PUT,
		
		
		/**
		 * HTTP DELETE.
		 */
		DELETE
	}
	
	
	/**
	 * The request method.
	 */
	private final Method method;


	/**
	 * The request URL.
	 */
	private final URL url;
	
	
	/**
	 * Specifies an {@code Authorization} header value.
	 */
	private String authorization = null;
	
	
	/**
	 * The query string / post body.
	 */
	private String query = null;
	
	
	/**
	 * Creates a new minimally specified HTTP request.
	 *
	 * @param method The HTTP request method. Must not be {@code null}.
	 * @param url    The HTTP request URL. Must not be {@code null}.
	 */
	public HTTPRequest(final Method method, final URL url) {
	
		if (method == null)
			throw new IllegalArgumentException("The HTTP method must not be null");
		
		this.method = method;


		if (url == null)
			throw new IllegalArgumentException("The HTTP URL must not be null");

		this.url = url;
	}


	/**
	 * Reconstructs the request URL string for the specified servlet
	 * request. The host part is always the local IP address. The query
	 * string and fragment is always omitted.
	 *
	 * @param request The servlet request. Must not be {@code null}.
	 *
	 * @return The reconstructed request URL string.
	 */
	private static String reconstructRequestURLString(final HttpServletRequest request) {

		StringBuilder sb = new StringBuilder("http");

		if (request.isSecure())
			sb.append('s');

		sb.append("://");

		String localAddress = request.getLocalAddr();

		if (localAddress.contains(".")) {
			// IPv3 address
			sb.append(localAddress);
		} else if (localAddress.contains(":")) {
			// IPv6 address, see RFC 2732
			sb.append('[');
			sb.append(localAddress);
			sb.append(']');
		} else {
			// Don't know what to do
		}

		if (! request.isSecure() && request.getLocalPort() != 80) {
			// HTTP plain at port other than 80
			sb.append(':');
			sb.append(request.getLocalPort());
		}

		if (request.isSecure() && request.getLocalPort() != 443) {
			// HTTPS at port other than 443 (default TLS)
			sb.append(':');
			sb.append(request.getLocalPort());
		}

		String path = request.getRequestURI();

		if (path != null)
			sb.append(path);

		return sb.toString();
	}
	
	
	/**
	 * Creates a new HTTP request from the specified HTTP servlet request.
	 *
	 * @param sr The servlet request. Must not be {@code null}.
	 *
	 * @throws IllegalArgumentException The the servlet request method is
	 *                                  not GET, POST, PUT or DELETE or the
	 *                                  content type header value couldn't
	 *                                  be parsed.
	 * @throws IOException              For a POST or PUT body that
	 *                                  couldn't be read due to an I/O
	 *                                  exception.
	 */
	public HTTPRequest(final HttpServletRequest sr)
		throws IOException {
	
		method = HTTPRequest.Method.valueOf(sr.getMethod().toUpperCase());

		String urlString = reconstructRequestURLString(sr);

		try {
			url = new URL(urlString);

		} catch (MalformedURLException e) {

			throw new IllegalArgumentException("Invalid request URL: " + e.getMessage() + ": " + urlString, e);
		}
		
		try {
			setContentType(sr.getContentType());
		
		} catch (ParseException e) {
			
			throw new IllegalArgumentException("Invalid Content-Type header value: " + e.getMessage(), e);
		}
		
		setAuthorization(sr.getHeader("Authorization"));
		
		if (method.equals(Method.GET) || method.equals(Method.DELETE)) {
		
			setQuery(sr.getQueryString());

		} else if (method.equals(Method.POST) || method.equals(Method.PUT)) {
		
			// read body
			StringBuilder body = new StringBuilder(256);
			
			BufferedReader reader = sr.getReader();
			
			String line;
			
			boolean firstLine = true;
			
			while ((line = reader.readLine()) != null) {
			
				if (firstLine)
					firstLine = false;
				else
					body.append(System.getProperty("line.separator"));
				body.append(line);
			}
			
			reader.close();
			
			setQuery(body.toString());
		}
	}
	
	
	/**
	 * Gets the request method.
	 *
	 * @return The request method.
	 */
	public Method getMethod() {
	
		return method;
	}


	/**
	 * Gets the request URL.
	 *
	 * @return The request URL.
	 */
	public URL getURL() {

		return url;
	}
	
	
	/**
	 * Ensures this HTTP request has the specified method.
	 *
	 * @param expectedMethod The expected method. Must not be {@code null}.
	 *
	 * @throws ParseException If the method doesn't match the expected.
	 */
	public void ensureMethod(final Method expectedMethod)
		throws ParseException {
		
		if (method != expectedMethod)
			throw new ParseException("The HTTP request method must be " + expectedMethod);
	}
	
	
	/**
	 * Gets the {@code Authorization} header value.
	 *
	 * @return The {@code Authorization} header value, {@code null} if not 
	 *         specified.
	 */
	public String getAuthorization() {
	
		return authorization;
	}
	
	
	/**
	 * Sets the {@code Authorization} header value.
	 *
	 * @param authz The {@code Authorization} header value, {@code null} if 
	 *              not specified.
	 */
	public void setAuthorization(final String authz) {
	
		authorization = authz;
	}
	
	
	/**
	 * Gets the raw (undecoded) query string if the request is HTTP GET or
	 * the entity body if the request is HTTP POST.
	 *
	 * <p>Note that the '?' character preceding the query string in GET
	 * requests is not included in the returned string.
	 *
	 * <p>Example query string (line breaks for clarity):
	 *
	 * <pre>
	 * response_type=code
	 * &amp;client_id=s6BhdRkqt3
	 * &amp;state=xyz
	 * &amp;redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb
	 * </pre>
	 *
	 * @return For HTTP GET requests the URL query string, for HTTP POST 
	 *         requests the body. {@code null} if not specified.
	 */
	public String getQuery() {
	
		return query;
	}
	
	
	/**
	 * Sets the raw (undecoded) query string if the request is HTTP GET or
	 * the entity body if the request is HTTP POST.
	 *
	 * <p>Note that the '?' character preceding the query string in GET
	 * requests must not be included.
	 *
	 * <p>Example query string (line breaks for clarity):
	 *
	 * <pre>
	 * response_type=code
	 * &amp;client_id=s6BhdRkqt3
	 * &amp;state=xyz
	 * &amp;redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb
	 * </pre>
	 *
	 * @param query For HTTP GET requests the URL query string, for HTTP 
	 *              POST requests the body. {@code null} if not specified.
	 */
	public void setQuery(final String query) {
	
		this.query = query;
	}


	/**
	 * Ensures this HTTP response has a specified query string or entity
	 * body.
	 *
	 * @throws ParseException If the query string or entity body is missing
	 *                        or empty.
	 */
	private void ensureQuery()
		throws ParseException {
		
		if (query == null || query.trim().isEmpty())
			throw new ParseException("Missing or empty HTTP query string / entity body");
	}
	
	
	/**
	 * Gets the request query as a parameter map. The parameters are 
	 * decoded according to {@code application/x-www-form-urlencoded}.
	 *
	 * @return The request query parameters, decoded. If none the map will
	 *         be empty.
	 */
	public Map<String,String> getQueryParameters() {
	
		return URLUtils.parseParameters(query);
	}


	/**
	 * Gets the request query or entity body as a JSON Object.
	 *
	 * @return The request query or entity body as a JSON object.
	 *
	 * @throws ParseException If the Content-Type header isn't 
	 *                        {@code application/json}, the request query
	 *                        or entity body is {@code null}, empty or 
	 *                        couldn't be parsed to a valid JSON object.
	 */
	public JSONObject getQueryAsJSONObject()
		throws ParseException {

		ensureContentType(CommonContentTypes.APPLICATION_JSON);

		ensureQuery();

		return JSONObjectUtils.parseJSONObject(query);
	}


	/**
	 * Returns an established HTTP URL connection for this HTTP request.
	 *
	 * @return The HTTP URL connection, with the request sent and ready to
	 *         read the response.
	 *
	 * @throws IOException If the HTTP request couldn't be made, due to a
	 *                     network or other error.
	 */
	public HttpURLConnection toHttpURLConnection()
		throws IOException {

		URL finalURL = url;

		if (query != null && (method.equals(HTTPRequest.Method.GET) || method.equals(Method.DELETE))) {

			// Append query string
			StringBuilder sb = new StringBuilder(url.toString());
			sb.append('?');
			sb.append(query);

			try {
				finalURL = new URL(sb.toString());

			} catch (MalformedURLException e) {

				throw new IOException("Couldn't append query string: " + e.getMessage(), e);
			}
		}

		HttpURLConnection conn = (HttpURLConnection)finalURL.openConnection();

		if (authorization != null)
			conn.setRequestProperty("Authorization", authorization);
		
		conn.setRequestProperty("Accept", ContentType.APPLICATION_JSON.toString());

		conn.setRequestMethod(method.name());

		if (method.equals(HTTPRequest.Method.POST) || method.equals(Method.PUT)) {

			conn.setDoOutput(true);

			if (getContentType() != null)
				conn.setRequestProperty("Content-Type", getContentType().toString());

			if (query != null) {
				OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
				writer.write(query);
				writer.close();
			}
		}

		return conn;
	}


	/**
	 * Sends this HTTP request to the request URL and retrieves the 
	 * resulting HTTP response.
	 *
	 * @return The resulting HTTP response.
	 *
	 * @throws IOException If the HTTP request couldn't be made, due to a 
	 *                     network or other error.
	 */
	public HTTPResponse send()
		throws IOException {

		HttpURLConnection conn = toHttpURLConnection();

		int statusCode;

		BufferedReader reader;

		try {
			// Open a connection, then send method and headers
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			// The next step is to get the status
			statusCode = conn.getResponseCode();

		} catch (IOException e) {

			// HttpUrlConnection will throw an IOException if any
			// 4XX response is sent. If we request the status
			// again, this time the internal status will be
			// properly set, and we'll be able to retrieve it.
			statusCode = conn.getResponseCode();

			if (statusCode == -1) {
				// Rethrow IO exception
				throw e;
			} else {
				// HTTP status code indicates the response got
				// through, read the content but using error
				// stream
				reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			}
		}

		StringBuilder body = new StringBuilder();


		try {
			String line;

			while ((line = reader.readLine()) != null) {

				body.append(line);
				body.append(System.getProperty("line.separator"));
			}

			reader.close();

		} finally {
			conn.disconnect();
		}


		HTTPResponse response = new HTTPResponse(statusCode);

		String location = conn.getHeaderField("Location");

		if (location != null) {

			try {
				response.setLocation(new URL(location));

			} catch (MalformedURLException e) {

				throw new IOException("Couldn't parse Location header: " + e.getMessage(), e);
			}
		}


		try {
			response.setContentType(conn.getContentType());

		} catch (ParseException e) {

			throw new IOException("Couldn't parse Content-Type header: " + e.getMessage(), e);
		}


		response.setCacheControl(conn.getHeaderField("Cache-Control"));

		response.setPragma(conn.getHeaderField("Pragma"));

		response.setWWWAuthenticate(conn.getHeaderField("WWW-Authenticate"));

		String bodyContent = body.toString();

		if (! bodyContent.isEmpty())
			response.setContent(bodyContent);


		return response;
	}
}
