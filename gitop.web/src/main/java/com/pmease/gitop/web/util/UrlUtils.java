package com.pmease.gitop.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.jar.JarEntry;

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
   * Returns the last modified time for file:// and jar:// URLs.  This is slightly tricky for a couple of reasons:
   * 1) calling getConnection on a {@link URLConnection} to a file opens an {@link InputStream} to that file that must
   * then be closed ‚Äî though this is not true for {@code URLConnection}s to jar resources
   * 2) calling getLastModified on {@link JarURLConnection}s returns the last modified time of the jar file, rather
   * than the file within
   *
   * @param resourceURL the URL to return the last modified time for
   * @return the last modified time of the resource, expressed as the number of milliseconds since the epoch, or 0 if there was a problem
   */
  public static long getLastModified(URL resourceURL) {
    final String protocol = resourceURL.getProtocol();
    switch (protocol) {
      case "jar":
        try {
          final JarURLConnection jarConnection = (JarURLConnection) resourceURL.openConnection();
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
        throw new IllegalArgumentException("Unsupported protocol " + resourceURL.getProtocol()
            + " for resource " + resourceURL);
    }
  }
}
