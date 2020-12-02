//
//  ========================================================================
//  Copyright (c) 1995-2020 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.IllegalSelectorException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.function.Supplier;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.http.CookieCompliance;
import org.eclipse.jetty.http.DateGenerator;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpCookie.SameSite;
import org.eclipse.jetty.http.HttpCookie.SetCookieHttpField;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.AtomicBiInteger;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;

/**
 * <p>{@link Response} provides the implementation for {@link HttpServletResponse}.</p>
 */
public class Response implements HttpServletResponse
{
    private static final int __MIN_BUFFER_SIZE = 1;
    private static final HttpField __EXPIRES_01JAN1970 = new PreEncodedHttpField(HttpHeader.EXPIRES, DateGenerator.__01Jan1970);
    public static final int NO_CONTENT_LENGTH = -1;
    public static final int USE_KNOWN_CONTENT_LENGTH = -2;

    public enum OutputType
    {
        NONE, STREAM, WRITER
    }

    /**
     * If a header name starts with this string,  the header (stripped of the prefix)
     * can be set during include using only {@link #setHeader(String, String)} or
     * {@link #addHeader(String, String)}.
     */
    public static final String SET_INCLUDE_HEADER_PREFIX = "org.eclipse.jetty.server.include.";

    private final HttpChannel _channel;
    private final HttpFields _fields = new HttpFields();
    private final AtomicBiInteger _errorSentAndIncludes = new AtomicBiInteger(); // hi is errorSent flag, lo is include count
    private final HttpOutput _out;
    private int _status = HttpStatus.OK_200;
    private String _reason;
    private Locale _locale;
    private MimeTypes.Type _mimeType;
    private String _characterEncoding;
    private EncodingFrom _encodingFrom = EncodingFrom.NOT_SET;
    private String _contentType;
    private OutputType _outputType = OutputType.NONE;
    private ResponseWriter _writer;
    private long _contentLength = -1;
    private Supplier<HttpFields> _trailers;

    private enum EncodingFrom
    {
        NOT_SET, INFERRED, SET_LOCALE, SET_CONTENT_TYPE, SET_CHARACTER_ENCODING
    }

    private static final EnumSet<EncodingFrom> __localeOverride = EnumSet.of(EncodingFrom.NOT_SET, EncodingFrom.INFERRED);
    private static final EnumSet<EncodingFrom> __explicitCharset = EnumSet.of(EncodingFrom.SET_LOCALE, EncodingFrom.SET_CHARACTER_ENCODING);

    public Response(HttpChannel channel, HttpOutput out)
    {
        _channel = channel;
        _out = out;
    }

    public HttpChannel getHttpChannel()
    {
        return _channel;
    }

    protected void recycle()
    {
        // _channel need not be recycled
        _fields.clear();
        _errorSentAndIncludes.set(0);
        _out.recycle();
        _status = HttpStatus.OK_200;
        _reason = null;
        _locale = null;
        _mimeType = null;
        _characterEncoding = null;
        _encodingFrom = EncodingFrom.NOT_SET;
        _contentType = null;
        _outputType = OutputType.NONE;
        // _writer does not need to be recycled
        _contentLength = -1;
        _trailers = null;
    }

    public HttpOutput getHttpOutput()
    {
        return _out;
    }

    public void reopen()
    {
        // Make the response mutable and reopen output.
        setErrorSent(false);
        _out.reopen();
    }

    public void errorClose()
    {
        // Make the response immutable and soft close the output.
        setErrorSent(true);
        _out.softClose();
    }

    /**
     * @return true if the response is mutable, ie not errorSent and not included
     */
    private boolean isMutable()
    {
        return _errorSentAndIncludes.get() == 0;
    }

    private void setErrorSent(boolean errorSent)
    {
        _errorSentAndIncludes.getAndSetHi(errorSent ? 1 : 0);
    }

    public boolean isIncluding()
    {
        return _errorSentAndIncludes.getLo() > 0;
    }

    public void include()
    {
        _errorSentAndIncludes.add(0, 1);
    }

    public void included()
    {
        _errorSentAndIncludes.add(0, -1);
        if (_outputType == OutputType.WRITER)
        {
            _writer.reopen();
        }
        _out.reopen();
    }

    public void addCookie(HttpCookie cookie)
    {
        if (StringUtil.isBlank(cookie.getName()))
            throw new IllegalArgumentException("Cookie.name cannot be blank/null");
     
        // add the set cookie
        _fields.add(new SetCookieHttpField(checkSameSite(cookie), getHttpChannel().getHttpConfiguration().getResponseCookieCompliance()));

        // Expire responses with set-cookie headers so they do not get cached.
        _fields.put(__EXPIRES_01JAN1970);
    }
    
    /**
     * Check that samesite is set on the cookie. If not, use a 
     * context default value, if one has been set.
     * 
     * @param cookie the cookie to check
     * @return either the original cookie, or a new one that has the samesit default set
     */
    private HttpCookie checkSameSite(HttpCookie cookie)
    {
        if (cookie == null || cookie.getSameSite() != null)
            return cookie;

        //sameSite is not set, use the default configured for the context, if one exists
        SameSite contextDefault = HttpCookie.getSameSiteDefault(_channel.getRequest().getServletContext());
        if (contextDefault == null)
            return cookie; //no default set

        return new HttpCookie(cookie.getName(),
            cookie.getValue(),
            cookie.getDomain(),
            cookie.getPath(),
            cookie.getMaxAge(),
            cookie.isHttpOnly(),
            cookie.isSecure(),
            cookie.getComment(),
            cookie.getVersion(),
            contextDefault);
    }

    /**
     * Replace (or add) a cookie.
     * Using name, path and domain, look for a matching set-cookie header and replace it.
     *
     * @param cookie The cookie to add/replace
     */
    public void replaceCookie(HttpCookie cookie)
    {
        for (ListIterator<HttpField> i = _fields.listIterator(); i.hasNext(); )
        {
            HttpField field = i.next();

            if (field.getHeader() == HttpHeader.SET_COOKIE)
            {
                CookieCompliance compliance = getHttpChannel().getHttpConfiguration().getResponseCookieCompliance();

                HttpCookie oldCookie;
                if (field instanceof SetCookieHttpField)
                    oldCookie = ((SetCookieHttpField)field).getHttpCookie();
                else
                    oldCookie = new HttpCookie(field.getValue());

                if (!cookie.getName().equals(oldCookie.getName()))
                    continue;

                if (cookie.getDomain() == null)
                {
                    if (oldCookie.getDomain() != null)
                        continue;
                }
                else if (!cookie.getDomain().equalsIgnoreCase(oldCookie.getDomain()))
                    continue;

                if (cookie.getPath() == null)
                {
                    if (oldCookie.getPath() != null)
                        continue;
                }
                else if (!cookie.getPath().equals(oldCookie.getPath()))
                    continue;

                i.set(new SetCookieHttpField(checkSameSite(cookie), compliance));
                return;
            }
        }

        // Not replaced, so add normally
        addCookie(cookie);
    }

    @Override
    public void addCookie(Cookie cookie)
    { 
        //Servlet Spec 9.3 Include method: cannot set a cookie if handling an include
        if (isMutable())
        {
            if (StringUtil.isBlank(cookie.getName()))
                throw new IllegalArgumentException("Cookie.name cannot be blank/null");

            String comment = cookie.getComment();
            // HttpOnly was supported as a comment in cookie flags before the java.net.HttpCookie implementation so need to check that
            boolean httpOnly = cookie.isHttpOnly() || HttpCookie.isHttpOnlyInComment(comment);
            SameSite sameSite = HttpCookie.getSameSiteFromComment(comment);
            comment = HttpCookie.getCommentWithoutAttributes(comment);

            addCookie(new HttpCookie(
                cookie.getName(),
                cookie.getValue(),
                cookie.getDomain(),
                cookie.getPath(),
                (long)cookie.getMaxAge(),
                httpOnly,
                cookie.getSecure(),
                comment,
                cookie.getVersion(),
                sameSite));
        }
    }

    @Override
    public boolean containsHeader(String name)
    {
        return _fields.containsKey(name);
    }

    @Override
    public String encodeURL(String url)
    {
        final Request request = _channel.getRequest();
        SessionHandler sessionManager = request.getSessionHandler();

        if (sessionManager == null)
            return url;

        HttpURI uri = null;
        if (sessionManager.isCheckingRemoteSessionIdEncoding() && URIUtil.hasScheme(url))
        {
            uri = new HttpURI(url);
            String path = uri.getPath();
            path = (path == null ? "" : path);
            int port = uri.getPort();
            if (port < 0)
                port = HttpScheme.HTTPS.asString().equalsIgnoreCase(uri.getScheme()) ? 443 : 80;

            // Is it the same server?
            if (!request.getServerName().equalsIgnoreCase(uri.getHost()))
                return url;
            if (request.getServerPort() != port)
                return url;
            if (!path.startsWith(request.getContextPath())) //TODO the root context path is "", with which every non null string starts
                return url;
        }

        String sessionURLPrefix = sessionManager.getSessionIdPathParameterNamePrefix();
        if (sessionURLPrefix == null)
            return url;

        if (url == null)
            return null;

        // should not encode if cookies in evidence
        if ((sessionManager.isUsingCookies() && request.isRequestedSessionIdFromCookie()) || !sessionManager.isUsingURLs())
        {
            int prefix = url.indexOf(sessionURLPrefix);
            if (prefix != -1)
            {
                int suffix = url.indexOf("?", prefix);
                if (suffix < 0)
                    suffix = url.indexOf("#", prefix);

                if (suffix <= prefix)
                    return url.substring(0, prefix);
                return url.substring(0, prefix) + url.substring(suffix);
            }
            return url;
        }

        // get session;
        HttpSession session = request.getSession(false);

        // no session
        if (session == null)
            return url;

        // invalid session
        if (!sessionManager.isValid(session))
            return url;

        String id = sessionManager.getExtendedId(session);

        if (uri == null)
            uri = new HttpURI(url);

        // Already encoded
        int prefix = url.indexOf(sessionURLPrefix);
        if (prefix != -1)
        {
            int suffix = url.indexOf("?", prefix);
            if (suffix < 0)
                suffix = url.indexOf("#", prefix);

            if (suffix <= prefix)
                return url.substring(0, prefix + sessionURLPrefix.length()) + id;
            return url.substring(0, prefix + sessionURLPrefix.length()) + id +
                url.substring(suffix);
        }

        // edit the session
        int suffix = url.indexOf('?');
        if (suffix < 0)
            suffix = url.indexOf('#');
        if (suffix < 0)
        {
            return url +
                ((HttpScheme.HTTPS.is(uri.getScheme()) || HttpScheme.HTTP.is(uri.getScheme())) && uri.getPath() == null ? "/" : "") + //if no path, insert the root path
                sessionURLPrefix + id;
        }

        return url.substring(0, suffix) +
            ((HttpScheme.HTTPS.is(uri.getScheme()) || HttpScheme.HTTP.is(uri.getScheme())) && uri.getPath() == null ? "/" : "") + //if no path so insert the root path
            sessionURLPrefix + id + url.substring(suffix);
    }

    @Override
    public String encodeRedirectURL(String url)
    {
        return encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeUrl(String url)
    {
        return encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String url)
    {
        return encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        sendError(sc, null);
    }

    /**
     * Send an error response.
     * <p>In addition to the servlet standard handling, this method supports some additional codes:</p>
     * <dl>
     * <dt>102</dt><dd>Send a partial PROCESSING response and allow additional responses</dd>
     * <dt>-1</dt><dd>Abort the HttpChannel and close the connection/stream</dd>
     * </dl>
     * @param code The error code
     * @param message The message
     * @throws IOException If an IO problem occurred sending the error response.
     */
    @Override
    public void sendError(int code, String message) throws IOException
    {
        if (isIncluding())
            return;

        switch (code)
        {
            case -1:
                _channel.abort(new IOException(message));
                break;
            case HttpStatus.PROCESSING_102:
                sendProcessing();
                break;
            default:
                _channel.getState().sendError(code, message);
                break;
        }
    }

    /**
     * Sends a 102-Processing response.
     * If the connection is an HTTP connection, the version is 1.1 and the
     * request has a Expect header starting with 102, then a 102 response is
     * sent. This indicates that the request still be processed and real response
     * can still be sent.   This method is called by sendError if it is passed 102.
     *
     * @throws IOException if unable to send the 102 response
     * @see javax.servlet.http.HttpServletResponse#sendError(int)
     */
    public void sendProcessing() throws IOException
    {
        if (_channel.isExpecting102Processing() && !isCommitted())
        {
            _channel.sendResponse(HttpGenerator.PROGRESS_102_INFO, null, true);
        }
    }

    /**
     * Sends a response with one of the 300 series redirection codes.
     *
     * @param code the redirect status code
     * @param location the location to send in {@code Location} headers
     * @throws IOException if unable to send the redirect
     */
    public void sendRedirect(int code, String location) throws IOException
    {
        sendRedirect(code, location, false);
    }

    /**
     * Sends a response with a HTTP version appropriate 30x redirection.
     *
     * @param location the location to send in {@code Location} headers
     * @param consumeAll if True, consume any HTTP/1 request input before doing the redirection. If the input cannot
     * be consumed without blocking, then add a `Connection: close` header to the response.
     * @throws IOException if unable to send the redirect
     */
    public void sendRedirect(String location, boolean consumeAll) throws IOException
    {
        sendRedirect(getHttpChannel().getRequest().getHttpVersion().getVersion() < HttpVersion.HTTP_1_1.getVersion()
            ? HttpServletResponse.SC_MOVED_TEMPORARILY : HttpServletResponse.SC_SEE_OTHER, location, consumeAll);
    }

    /**
     * Sends a response with a given redirection code.
     *
     * @param code the redirect status code
     * @param location the location to send in {@code Location} headers
     * @param consumeAll if True, consume any HTTP/1 request input before doing the redirection. If the input cannot
     * be consumed without blocking, then add a `Connection: close` header to the response.
     * @throws IOException if unable to send the redirect
     */
    public void sendRedirect(int code, String location, boolean consumeAll) throws IOException
    {
        if (consumeAll)
            getHttpChannel().ensureConsumeAllOrNotPersistent();
        if (!HttpStatus.isRedirection(code))
            throw new IllegalArgumentException("Not a 3xx redirect code");

        if (!isMutable())
            return;

        if (location == null)
            throw new IllegalArgumentException();

        /*
         *  The new http spec now allows to redirect to relative urls. So we comment
         *  out below section to avoid wrongly prepending http scheme to the url if
         *  OneDev running with http protocol is exposed to outside as https prococol
         *  via reverse proxy
         */
        
        /*
        if (!URIUtil.hasScheme(location))
        {
            StringBuilder buf = _channel.getHttpConfiguration().isRelativeRedirectAllowed()
                ? new StringBuilder()
                : _channel.getRequest().getRootURL();
            if (location.startsWith("/"))
            {
                // absolute in context
                location = URIUtil.canonicalEncodedPath(location);
            }
            else
            {
                // relative to request
                String path = _channel.getRequest().getRequestURI();
                String parent = (path.endsWith("/")) ? path : URIUtil.parentPath(path);
                location = URIUtil.canonicalEncodedPath(URIUtil.addEncodedPaths(parent, location));
                if (location != null && !location.startsWith("/"))
                    buf.append('/');
            }

            if (location == null)
                throw new IllegalStateException("path cannot be above root");
            buf.append(location);

            location = buf.toString();
        }
        */

        resetBuffer();
        setHeader(HttpHeader.LOCATION, location);
        setStatus(code);
        closeOutput();
    }

    @Override
    public void sendRedirect(String location) throws IOException
    {
        sendRedirect(HttpServletResponse.SC_MOVED_TEMPORARILY, location);
    }

    @Override
    public void setDateHeader(String name, long date)
    {
        if (isMutable())
            _fields.putDateField(name, date);
    }

    @Override
    public void addDateHeader(String name, long date)
    {
        if (isMutable())
            _fields.addDateField(name, date);
    }

    public void setHeader(HttpHeader name, String value)
    {
        if (isMutable())
        {
            if (HttpHeader.CONTENT_TYPE == name)
                setContentType(value);
            else
            {
                _fields.put(name, value);

                if (HttpHeader.CONTENT_LENGTH == name)
                {
                    if (value == null)
                        _contentLength = -1L;
                    else
                        _contentLength = Long.parseLong(value);
                }
            }
        }
    }

    @Override
    public void setHeader(String name, String value)
    {
        long biInt = _errorSentAndIncludes.get();
        if (biInt != 0)
        {
            boolean errorSent = AtomicBiInteger.getHi(biInt) != 0;
            boolean including = AtomicBiInteger.getLo(biInt) > 0;
            if (!errorSent && including && name.startsWith(SET_INCLUDE_HEADER_PREFIX))
                name = name.substring(SET_INCLUDE_HEADER_PREFIX.length());
            else
                return;
        }

        if (HttpHeader.CONTENT_TYPE.is(name))
            setContentType(value);
        else
        {
            _fields.put(name, value);
            if (HttpHeader.CONTENT_LENGTH.is(name))
            {
                if (value == null)
                    _contentLength = -1L;
                else
                    _contentLength = Long.parseLong(value);
            }
        }
    }

    @Override
    public Collection<String> getHeaderNames()
    {
        return _fields.getFieldNamesCollection();
    }

    @Override
    public String getHeader(String name)
    {
        return _fields.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name)
    {
        Collection<String> i = _fields.getValuesList(name);
        if (i == null)
            return Collections.emptyList();
        return i;
    }

    @Override
    public void addHeader(String name, String value)
    {
        long biInt = _errorSentAndIncludes.get();
        if (biInt != 0)
        {
            boolean errorSent = AtomicBiInteger.getHi(biInt) != 0;
            boolean including = AtomicBiInteger.getLo(biInt) > 0;
            if (!errorSent && including && name.startsWith(SET_INCLUDE_HEADER_PREFIX))
                name = name.substring(SET_INCLUDE_HEADER_PREFIX.length());
            else
                return;
        }

        if (HttpHeader.CONTENT_TYPE.is(name))
        {
            setContentType(value);
            return;
        }

        if (HttpHeader.CONTENT_LENGTH.is(name))
        {
            setHeader(name, value);
            return;
        }

        _fields.add(name, value);
    }

    @Override
    public void setIntHeader(String name, int value)
    {
        if (isMutable())
        {
            _fields.putLongField(name, value);
            if (HttpHeader.CONTENT_LENGTH.is(name))
                _contentLength = value;
        }
    }

    @Override
    public void addIntHeader(String name, int value)
    {
        if (isMutable())
        {
            _fields.add(name, Integer.toString(value));
            if (HttpHeader.CONTENT_LENGTH.is(name))
                _contentLength = value;
        }
    }

    @Override
    public void setStatus(int sc)
    {
        if (sc <= 0)
            throw new IllegalArgumentException();
        if (isMutable())
        {
            // Null the reason only if the status is different. This allows
            // a specific reason to be sent with setStatusWithReason followed by sendError.
            if (_status != sc)
                _reason = null;
            _status = sc;
        }
    }

    @Override
    @Deprecated
    public void setStatus(int sc, String sm)
    {
        setStatusWithReason(sc, sm);
    }

    public void setStatusWithReason(int sc, String sm)
    {
        if (sc <= 0)
            throw new IllegalArgumentException();
        if (isMutable())
        {
            _status = sc;
            _reason = sm;
        }
    }

    @Override
    public String getCharacterEncoding()
    {
        if (_characterEncoding == null)
        {
            String encoding = MimeTypes.getCharsetAssumedFromContentType(_contentType);
            if (encoding != null)
                return encoding;
            encoding = MimeTypes.getCharsetInferredFromContentType(_contentType);
            if (encoding != null)
                return encoding;
            return StringUtil.__ISO_8859_1;
        }
        return _characterEncoding;
    }

    @Override
    public String getContentType()
    {
        return _contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        if (_outputType == OutputType.WRITER)
            throw new IllegalStateException("WRITER");
        _outputType = OutputType.STREAM;
        return _out;
    }

    public boolean isWriting()
    {
        return _outputType == OutputType.WRITER;
    }

    public boolean isStreaming()
    {
        return _outputType == OutputType.STREAM;
    }

    public boolean isWritingOrStreaming()
    {
        return isWriting() || isStreaming();
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        if (_outputType == OutputType.STREAM)
            throw new IllegalStateException("STREAM");

        if (_outputType == OutputType.NONE)
        {
            /* get encoding from Content-Type header */
            String encoding = _characterEncoding;
            if (encoding == null)
            {
                if (_mimeType != null && _mimeType.isCharsetAssumed())
                    encoding = _mimeType.getCharsetString();
                else
                {
                    encoding = MimeTypes.getCharsetAssumedFromContentType(_contentType);
                    if (encoding == null)
                    {
                        encoding = MimeTypes.getCharsetInferredFromContentType(_contentType);
                        if (encoding == null)
                            encoding = StringUtil.__ISO_8859_1;
                        setCharacterEncoding(encoding, EncodingFrom.INFERRED);
                    }
                }
            }

            Locale locale = getLocale();

            if (_writer != null && _writer.isFor(locale, encoding))
                _writer.reopen();
            else
            {
                if (StringUtil.__ISO_8859_1.equalsIgnoreCase(encoding))
                    _writer = new ResponseWriter(new Iso88591HttpWriter(_out), locale, encoding);
                else if (StringUtil.__UTF8.equalsIgnoreCase(encoding))
                    _writer = new ResponseWriter(new Utf8HttpWriter(_out), locale, encoding);
                else
                    _writer = new ResponseWriter(new EncodingHttpWriter(_out, encoding), locale, encoding);
            }

            // Set the output type at the end, because setCharacterEncoding() checks for it
            _outputType = OutputType.WRITER;
        }
        return _writer;
    }

    @Override
    public void setContentLength(int len)
    {
        // Protect from setting after committed as default handling
        // of a servlet HEAD request ALWAYS sets _content length, even
        // if the getHandling committed the response!
        if (isCommitted() || !isMutable())
            return;

        if (len > 0)
        {
            long written = _out.getWritten();
            if (written > len)
                throw new IllegalArgumentException("setContentLength(" + len + ") when already written " + written);

            _contentLength = len;
            _fields.putLongField(HttpHeader.CONTENT_LENGTH, len);
            if (isAllContentWritten(written))
            {
                try
                {
                    closeOutput();
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        }
        else if (len == 0)
        {
            long written = _out.getWritten();
            if (written > 0)
                throw new IllegalArgumentException("setContentLength(0) when already written " + written);
            _contentLength = len;
            _fields.put(HttpHeader.CONTENT_LENGTH, "0");
        }
        else
        {
            _contentLength = len;
            _fields.remove(HttpHeader.CONTENT_LENGTH);
        }
    }

    public long getContentLength()
    {
        return _contentLength;
    }

    public boolean isAllContentWritten(long written)
    {
        return (_contentLength >= 0 && written >= _contentLength);
    }

    public boolean isContentComplete(long written)
    {
        return (_contentLength < 0 || written >= _contentLength);
    }

    public void closeOutput() throws IOException
    {
        if (_outputType == OutputType.WRITER)
            _writer.close();
        else
            _out.close();
    }

    /**
     * close the output
     *
     * @deprecated Use {@link #closeOutput()}
     */
    @Deprecated
    public void completeOutput() throws IOException
    {
        closeOutput();
    }

    public void completeOutput(Callback callback)
    {
        if (_outputType == OutputType.WRITER)
            _writer.complete(callback);
        else
            _out.complete(callback);
    }

    public long getLongContentLength()
    {
        return _contentLength;
    }

    public void setLongContentLength(long len)
    {
        // Protect from setting after committed as default handling
        // of a servlet HEAD request ALWAYS sets _content length, even
        // if the getHandling committed the response!
        if (isCommitted() || !isMutable())
            return;
        _contentLength = len;
        _fields.putLongField(HttpHeader.CONTENT_LENGTH.toString(), len);
    }

    @Override
    public void setContentLengthLong(long length)
    {
        setLongContentLength(length);
    }

    @Override
    public void setCharacterEncoding(String encoding)
    {
        setCharacterEncoding(encoding, EncodingFrom.SET_CHARACTER_ENCODING);
    }

    private void setCharacterEncoding(String encoding, EncodingFrom from)
    {
        if (!isMutable() || isWriting())
            return;

        if (_outputType != OutputType.WRITER && !isCommitted())
        {
            if (encoding == null)
            {
                _encodingFrom = EncodingFrom.NOT_SET;

                // Clear any encoding.
                if (_characterEncoding != null)
                {
                    _characterEncoding = null;

                    if (_mimeType != null)
                    {
                        _mimeType = _mimeType.getBaseType();
                        _contentType = _mimeType.asString();
                        _fields.put(_mimeType.getContentTypeField());
                    }
                    else if (_contentType != null)
                    {
                        _contentType = MimeTypes.getContentTypeWithoutCharset(_contentType);
                        _fields.put(HttpHeader.CONTENT_TYPE, _contentType);
                    }
                }
            }
            else
            {
                // No, so just add this one to the mimetype
                _encodingFrom = from;
                _characterEncoding = HttpGenerator.__STRICT ? encoding : StringUtil.normalizeCharset(encoding);
                if (_mimeType != null)
                {
                    _contentType = _mimeType.getBaseType().asString() + ";charset=" + _characterEncoding;
                    _mimeType = MimeTypes.CACHE.get(_contentType);
                    if (_mimeType == null || HttpGenerator.__STRICT)
                        _fields.put(HttpHeader.CONTENT_TYPE, _contentType);
                    else
                        _fields.put(_mimeType.getContentTypeField());
                }
                else if (_contentType != null)
                {
                    _contentType = MimeTypes.getContentTypeWithoutCharset(_contentType) + ";charset=" + _characterEncoding;
                    _fields.put(HttpHeader.CONTENT_TYPE, _contentType);
                }
            }
        }
    }

    @Override
    public void setContentType(String contentType)
    {
        if (isCommitted() || !isMutable())
            return;

        if (contentType == null)
        {
            if (isWriting() && _characterEncoding != null)
                throw new IllegalSelectorException();

            if (_locale == null)
                _characterEncoding = null;
            _mimeType = null;
            _contentType = null;
            _fields.remove(HttpHeader.CONTENT_TYPE);
        }
        else
        {
            _contentType = contentType;
            _mimeType = MimeTypes.CACHE.get(contentType);

            String charset;
            if (_mimeType != null && _mimeType.getCharset() != null && !_mimeType.isCharsetAssumed())
                charset = _mimeType.getCharsetString();
            else
                charset = MimeTypes.getCharsetFromContentType(contentType);

            if (charset == null)
            {
                switch (_encodingFrom)
                {
                    case NOT_SET:
                        break;
                    case INFERRED:
                    case SET_CONTENT_TYPE:
                        if (isWriting())
                        {
                            _mimeType = null;
                            _contentType = _contentType + ";charset=" + _characterEncoding;
                        }
                        else
                        {
                            _encodingFrom = EncodingFrom.NOT_SET;
                            _characterEncoding = null;
                        }
                        break;
                    case SET_LOCALE:
                    case SET_CHARACTER_ENCODING:
                    {
                        _contentType = contentType + ";charset=" + _characterEncoding;
                        _mimeType = null;
                    }
                }
            }
            else if (isWriting() && !charset.equalsIgnoreCase(_characterEncoding))
            {
                // too late to change the character encoding;
                _mimeType = null;
                _contentType = MimeTypes.getContentTypeWithoutCharset(_contentType);
                if (_characterEncoding != null)
                    _contentType = _contentType + ";charset=" + _characterEncoding;
            }
            else
            {
                _characterEncoding = charset;
                _encodingFrom = EncodingFrom.SET_CONTENT_TYPE;
            }

            if (HttpGenerator.__STRICT || _mimeType == null)
                _fields.put(HttpHeader.CONTENT_TYPE, _contentType);
            else
            {
                _contentType = _mimeType.asString();
                _fields.put(_mimeType.getContentTypeField());
            }
        }
    }

    @Override
    public void setBufferSize(int size)
    {
        if (isCommitted())
            throw new IllegalStateException("cannot set buffer size after response is in committed state");
        if (getContentCount() > 0)
            throw new IllegalStateException("cannot set buffer size after response has " + getContentCount() + " bytes already written");
        if (size < __MIN_BUFFER_SIZE)
            size = __MIN_BUFFER_SIZE;
        _out.setBufferSize(size);
    }

    @Override
    public int getBufferSize()
    {
        return _out.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException
    {
        if (!_out.isClosed())
            _out.flush();
    }

    @Override
    public void reset()
    {
        _status = 200;
        _reason = null;
        _out.resetBuffer();
        _outputType = OutputType.NONE;
        _contentLength = -1;
        _contentType = null;
        _mimeType = null;
        _characterEncoding = null;
        _encodingFrom = EncodingFrom.NOT_SET;
        _trailers = null;

        // Clear all response headers
        _fields.clear();

        // recreate necessary connection related fields
        for (String value : _channel.getRequest().getHttpFields().getCSV(HttpHeader.CONNECTION, false))
        {
            HttpHeaderValue cb = HttpHeaderValue.CACHE.get(value);
            if (cb != null)
            {
                switch (cb)
                {
                    case CLOSE:
                        _fields.put(HttpHeader.CONNECTION, HttpHeaderValue.CLOSE.toString());
                        break;
                    case KEEP_ALIVE:
                        if (HttpVersion.HTTP_1_0.is(_channel.getRequest().getProtocol()))
                            _fields.put(HttpHeader.CONNECTION, HttpHeaderValue.KEEP_ALIVE.toString());
                        break;
                    case TE:
                        _fields.put(HttpHeader.CONNECTION, HttpHeaderValue.TE.toString());
                        break;
                    default:
                }
            }
        }

        // recreate session cookies
        Request request = getHttpChannel().getRequest();
        HttpSession session = request.getSession(false);
        if (session != null && session.isNew())
        {
            SessionHandler sh = request.getSessionHandler();
            if (sh != null)
            {
                HttpCookie c = sh.getSessionCookie(session, request.getContextPath(), request.isSecure());
                if (c != null)
                    addCookie(c);
            }
        }
    }

    public void resetContent()
    {
        _out.resetBuffer();
        _outputType = OutputType.NONE;
        _contentLength = -1;
        _contentType = null;
        _mimeType = null;
        _characterEncoding = null;
        _encodingFrom = EncodingFrom.NOT_SET;

        // remove the content related response headers and keep all others
        for (Iterator<HttpField> i = getHttpFields().iterator(); i.hasNext(); )
        {
            HttpField field = i.next();
            if (field.getHeader() == null)
                continue;

            switch (field.getHeader())
            {
                case CONTENT_TYPE:
                case CONTENT_LENGTH:
                case CONTENT_ENCODING:
                case CONTENT_LANGUAGE:
                case CONTENT_RANGE:
                case CONTENT_MD5:
                case CONTENT_LOCATION:
                case TRANSFER_ENCODING:
                case CACHE_CONTROL:
                case LAST_MODIFIED:
                case EXPIRES:
                case ETAG:
                case DATE:
                case VARY:
                    i.remove();
                    continue;
                default:
            }
        }
    }

    public void resetForForward()
    {
        resetBuffer();
        _outputType = OutputType.NONE;
    }

    @Override
    public void resetBuffer()
    {
        _out.resetBuffer();
        _out.reopen();
    }

    public void setTrailers(Supplier<HttpFields> trailers)
    {
        this._trailers = trailers;
    }

    public Supplier<HttpFields> getTrailers()
    {
        return _trailers;
    }

    protected MetaData.Response newResponseMetaData()
    {
        MetaData.Response info = new MetaData.Response(_channel.getRequest().getHttpVersion(), getStatus(), getReason(), _fields, getLongContentLength());
        info.setTrailerSupplier(getTrailers());
        return info;
    }

    /**
     * Get the MetaData.Response committed for this response.
     * This may differ from the meta data in this response for
     * exceptional responses (eg 4xx and 5xx responses generated
     * by the container) and the committedMetaData should be used
     * for logging purposes.
     *
     * @return The committed MetaData or a {@link #newResponseMetaData()}
     * if not yet committed.
     */
    public MetaData.Response getCommittedMetaData()
    {
        MetaData.Response meta = _channel.getCommittedMetaData();
        if (meta == null)
            return newResponseMetaData();
        return meta;
    }

    @Override
    public boolean isCommitted()
    {
        // If we are in sendError state, we pretend to be committed
        if (_channel.isSendError())
            return true;
        return _channel.isCommitted();
    }

    @Override
    public void setLocale(Locale locale)
    {
        if (locale == null || isCommitted() || !isMutable())
            return;

        _locale = locale;
        _fields.put(HttpHeader.CONTENT_LANGUAGE, StringUtil.replace(locale.toString(), '_', '-'));

        if (_outputType != OutputType.NONE)
            return;

        if (_channel.getRequest().getContext() == null)
            return;

        String charset = _channel.getRequest().getContext().getContextHandler().getLocaleEncoding(locale);

        if (charset != null && charset.length() > 0 && __localeOverride.contains(_encodingFrom))
            setCharacterEncoding(charset, EncodingFrom.SET_LOCALE);
    }

    @Override
    public Locale getLocale()
    {
        if (_locale == null)
            return Locale.getDefault();
        return _locale;
    }

    @Override
    public int getStatus()
    {
        return _status;
    }

    public String getReason()
    {
        return _reason;
    }

    public HttpFields getHttpFields()
    {
        return _fields;
    }

    public long getContentCount()
    {
        return _out.getWritten();
    }

    @Override
    public String toString()
    {
        return String.format("%s %d %s%n%s", _channel.getRequest().getHttpVersion(), _status, _reason == null ? "" : _reason, _fields);
    }

    public void putHeaders(HttpContent content, long contentLength, boolean etag)
    {
        HttpField lm = content.getLastModified();
        if (lm != null)
            _fields.put(lm);

        if (contentLength == USE_KNOWN_CONTENT_LENGTH)
        {
            _fields.put(content.getContentLength());
            _contentLength = content.getContentLengthValue();
        }
        else if (contentLength > NO_CONTENT_LENGTH)
        {
            _fields.putLongField(HttpHeader.CONTENT_LENGTH, contentLength);
            _contentLength = contentLength;
        }

        HttpField ct = content.getContentType();
        if (ct != null)
        {
            if (_characterEncoding != null &&
                content.getCharacterEncoding() == null &&
                content.getContentTypeValue() != null &&
                __explicitCharset.contains(_encodingFrom))
            {
                setContentType(MimeTypes.getContentTypeWithoutCharset(content.getContentTypeValue()));
            }
            else
            {
                _fields.put(ct);
                _contentType = ct.getValue();
                _characterEncoding = content.getCharacterEncoding();
                _mimeType = content.getMimeType();
            }
        }

        HttpField ce = content.getContentEncoding();
        if (ce != null)
            _fields.put(ce);

        if (etag)
        {
            HttpField et = content.getETag();
            if (et != null)
                _fields.put(et);
        }
    }

    public static void putHeaders(HttpServletResponse response, HttpContent content, long contentLength, boolean etag)
    {
        long lml = content.getResource().lastModified();
        if (lml >= 0)
            response.setDateHeader(HttpHeader.LAST_MODIFIED.asString(), lml);

        if (contentLength == USE_KNOWN_CONTENT_LENGTH)
            contentLength = content.getContentLengthValue();
        if (contentLength > NO_CONTENT_LENGTH)
        {
            if (contentLength < Integer.MAX_VALUE)
                response.setContentLength((int)contentLength);
            else
                response.setHeader(HttpHeader.CONTENT_LENGTH.asString(), Long.toString(contentLength));
        }

        String ct = content.getContentTypeValue();
        if (ct != null && response.getContentType() == null)
            response.setContentType(ct);

        String ce = content.getContentEncodingValue();
        if (ce != null)
            response.setHeader(HttpHeader.CONTENT_ENCODING.asString(), ce);

        if (etag)
        {
            String et = content.getETagValue();
            if (et != null)
                response.setHeader(HttpHeader.ETAG.asString(), et);
        }
    }

    public static HttpServletResponse unwrap(ServletResponse servletResponse)
    {
        if (servletResponse instanceof HttpServletResponseWrapper)
        {
            return (HttpServletResponseWrapper)servletResponse;
        }
        if (servletResponse instanceof ServletResponseWrapper)
        {
            return unwrap(((ServletResponseWrapper)servletResponse).getResponse());
        }
        return (HttpServletResponse)servletResponse;
    }
}
