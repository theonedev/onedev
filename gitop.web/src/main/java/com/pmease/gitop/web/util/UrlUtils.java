package com.pmease.gitop.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class UrlUtils {

	private UrlUtils() {
	}

	public static long getLastModified(Set<URL> fileset) {
		long lastModified = 0;
		for (URL each : fileset) {
			lastModified = Math.max(lastModified, getLastModified(each));
		}

		return lastModified;
	}

	/**
	 * Copied from dropwizard
	 * 
	 * Returns the last modified time for file:// and jar:// URLs. This is
	 * slightly tricky for a couple of reasons: 1) calling getConnection on a
	 * {@link URLConnection} to a file opens an {@link InputStream} to that file
	 * that must then be closed ‚Äî though this is not true for
	 * {@code URLConnection}s to jar resources 2) calling getLastModified on
	 * {@link JarURLConnection}s returns the last modified time of the jar file,
	 * rather than the file within
	 * 
	 * @param resourceURL
	 *            the URL to return the last modified time for
	 * @return the last modified time of the resource, expressed as the number
	 *         of milliseconds since the epoch, or 0 if there was a problem
	 */
	public static long getLastModified(URL resourceURL) {
		final String protocol = resourceURL.getProtocol();
		switch (protocol) {
		case "jar":
			try {
				final JarURLConnection jarConnection = (JarURLConnection) resourceURL
						.openConnection();
				final JarEntry entry = jarConnection.getJarEntry();
				return entry.getTime();
			} catch (IOException ignored) {
				return 0;
			}
		case "file":
			URLConnection connection = null;
			try {
				connection = resourceURL.openConnection();
				return connection.getLastModified();
			} catch (IOException ignored) {
				return 0;
			} finally {
				if (connection != null) {
					try {
						connection.getInputStream().close();
					} catch (IOException ignored) {
						// do nothing.
					}
				}
			}
		default:
			throw new IllegalArgumentException("Unsupported protocol "
					+ resourceURL.getProtocol() + " for resource "
					+ resourceURL);
		}
	}
	
	public static String encodeUrl(final String segment) {
		try {
			return URLEncoder.encode(segment, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw Throwables.propagate(e);
		}
	}
	
	/**
	 * Concatenate url segments into one
	 *  
	 * @param segments
	 * @return 
	 */
	public static String concatSegments(String[] segments) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < segments.length; i++) {
			if (i > 0) {
				sb.append("/");
			}
			
			sb.append(StringUtils.stripEnd(segments[i], "/"));
		}
		
		return sb.toString();
	}

	public static String concatSegments(String base, String... segments) {
		List<String> list = Lists.newArrayList();
		list.add(base);
		for (String each : segments) {
			list.add(each);
		}
		return concatSegments(Iterables.toArray(list, String.class));
	}
	
	private static final Pattern SLASHES = Pattern.compile("//+");
	
	/**
	 * Reduces redundant slashes to a single slash.
	 *  
	 * @param path
	 * @return null or the reduced string
	 */
	public static @Nullable String removeRedundantSlashes(@Nullable String path) {
		if (path == null) {
			return null;
		}
		
		return SLASHES.matcher(path).replaceAll("/");
	}
	
	/**
     * Trims any trailing slashes from a URL path (see {@link URI#getPath()}.
     *
     * @param uri a URI with a path component
     * @return the URI with any trailing slashes from the path removed
     * @throws IllegalArgumentException if the value is no longer a valid URI after the slashes have been trimmed
     */
	public static URI trimTrailingSlashesFromPath(URI uri) {
		String path = uri.getPath();
		if (path.endsWith("/")) {
			while (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			
			try {
				return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(), uri.getFragment());
			} catch (URISyntaxException e) {
				throw Throwables.propagate(e);
			}
		}
		
		return uri;
	}
}
