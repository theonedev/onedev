//
//  ========================================================================
//  Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd.
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.http.DateGenerator;
import org.eclipse.jetty.http.HttpContent;
import org.eclipse.jetty.http.HttpCookie;
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
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.eclipse.jetty.util.QuotedStringTokenizer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * <p>{@link Response} provides the implementation for {@link HttpServletResponse}.</p>
 */
public class Response implements HttpServletResponse
{
    private static final Logger LOG = Log.getLogger(Response.class);    
    private static final String __COOKIE_DELIM="\",;\\ \t";
    private final static String __01Jan1970_COOKIE = DateGenerator.formatCookieDate(0).trim();
    private final static int __MIN_BUFFER_SIZE = 1;
    private final static HttpField __EXPIRES_01JAN1970 = new PreEncodedHttpField(HttpHeader.EXPIRES,DateGenerator.__01Jan1970);
    

    // Cookie building buffer. Reduce garbage for cookie using applications
    private static final ThreadLocal<StringBuilder> __cookieBuilder = new ThreadLocal<StringBuilder>()
    {
       @Override
       protected StringBuilder initialValue()
       {
          return new StringBuilder(128);
       }
    };
    
    public enum OutputType
    {
        NONE, STREAM, WRITER
    }

    /**
     * If a header name starts with this string,  the header (stripped of the prefix)
     * can be set during include using only {@link #setHeader(String, String)} or
     * {@link #addHeader(String, String)}.
     */
    public final static String SET_INCLUDE_HEADER_PREFIX = "org.eclipse.jetty.server.include.";

    /**
     * If this string is found within the comment of a cookie added with {@link #addCookie(Cookie)}, then the cookie
     * will be set as HTTP ONLY.
     */
    public final static String HTTP_ONLY_COMMENT = "__HTTP_ONLY__";

    private final HttpChannel _channel;
    private final HttpFields _fields = new HttpFields();
    private final AtomicInteger _include = new AtomicInteger();
    private final HttpOutput _out;
    private int _status = HttpStatus.OK_200;
    private String _reason;
    private Locale _locale;
    private MimeTypes.Type _mimeType;
    private String _characterEncoding;
    private EncodingFrom _encodingFrom=EncodingFrom.NOT_SET;
    private String _contentType;
    private OutputType _outputType = OutputType.NONE;
    private ResponseWriter _writer;
    private long _contentLength = -1;
    
    private enum EncodingFrom { NOT_SET, INFERRED, SET_LOCALE, SET_CONTENT_TYPE, SET_CHARACTER_ENCODING };
    private static final EnumSet<EncodingFrom> __localeOverride = EnumSet.of(EncodingFrom.NOT_SET,EncodingFrom.INFERRED);
    

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
        _status = HttpStatus.OK_200;
        _reason = null;
        _locale = null;
        _mimeType = null;
        _characterEncoding = null;
        _contentType = null;
        _outputType = OutputType.NONE;
        _contentLength = -1;
        _out.recycle();
        _fields.clear();
        _encodingFrom=EncodingFrom.NOT_SET;
    }
    
    public HttpOutput getHttpOutput()
    {
        return _out;
    }

    public boolean isIncluding()
    {
        return _include.get() > 0;
    }

    public void include()
    {
        _include.incrementAndGet();
    }

    public void included()
    {
        _include.decrementAndGet();
        if (_outputType == OutputType.WRITER)
        {
            _writer.reopen();
        }
        _out.reopen();
    }

    public void addCookie(HttpCookie cookie)
    {
        addSetCookie(
                cookie.getName(),
                cookie.getValue(),
                cookie.getDomain(),
                cookie.getPath(),
                cookie.getMaxAge(),
                cookie.getComment(),
                cookie.isSecure(),
                cookie.isHttpOnly(),
                cookie.getVersion());;
    }

    @Override
    public void addCookie(Cookie cookie)
    {
        String comment = cookie.getComment();
        boolean httpOnly = false;

        if (comment != null)
        {
            int i = comment.indexOf(HTTP_ONLY_COMMENT);
            if (i >= 0)
            {
                httpOnly = true;
                comment = comment.replace(HTTP_ONLY_COMMENT, "").trim();
                if (comment.length() == 0)
                    comment = null;
            }
        }
        addSetCookie(cookie.getName(),
                cookie.getValue(),
                cookie.getDomain(),
                cookie.getPath(),
                cookie.getMaxAge(),
                comment,
                cookie.getSecure(),
                httpOnly || cookie.isHttpOnly(),
                cookie.getVersion());
    }


    /**
     * Format a set cookie value
     *
     * @param name the name
     * @param value the value
     * @param domain the domain
     * @param path the path
     * @param maxAge the maximum age
     * @param comment the comment (only present on versions &gt; 0)
     * @param isSecure true if secure cookie
     * @param isHttpOnly true if for http only
     * @param version version of cookie logic to use (0 == default behavior)
     */
    public void addSetCookie(
            final String name,
            final String value,
            final String domain,
            final String path,
            final long maxAge,
            final String comment,
            final boolean isSecure,
            final boolean isHttpOnly,
            int version)
    {
        // Check arguments
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Bad cookie name");

        // Format value and params
        StringBuilder buf = __cookieBuilder.get();
        buf.setLength(0);
        
        // Name is checked for legality by servlet spec, but can also be passed directly so check again for quoting
        boolean quote_name=isQuoteNeededForCookie(name);
        quoteOnlyOrAppend(buf,name,quote_name);
        
        buf.append('=');
       
        // Append the value
        boolean quote_value=isQuoteNeededForCookie(value);
        quoteOnlyOrAppend(buf,value,quote_value);

        // Look for domain and path fields and check if they need to be quoted
        boolean has_domain = domain!=null && domain.length()>0;
        boolean quote_domain = has_domain && isQuoteNeededForCookie(domain);
        boolean has_path = path!=null && path.length()>0;
        boolean quote_path = has_path && isQuoteNeededForCookie(path);
        
        // Upgrade the version if we have a comment or we need to quote value/path/domain or if they were already quoted
        if (version==0 && ( comment!=null || quote_name || quote_value || quote_domain || quote_path ||
                QuotedStringTokenizer.isQuoted(name) || QuotedStringTokenizer.isQuoted(value) ||
                QuotedStringTokenizer.isQuoted(path) || QuotedStringTokenizer.isQuoted(domain)))
            version=1;

        // Append version
        if (version==1)
            buf.append (";Version=1");
        else if (version>1)
            buf.append (";Version=").append(version);
        
        // Append path
        if (has_path)
        {
            buf.append(";Path=");
            quoteOnlyOrAppend(buf,path,quote_path);
        }
        
        // Append domain
        if (has_domain)
        {
            buf.append(";Domain=");
            quoteOnlyOrAppend(buf,domain,quote_domain);
        }

        // Handle max-age and/or expires
        if (maxAge >= 0)
        {
            // Always use expires
            // This is required as some browser (M$ this means you!) don't handle max-age even with v1 cookies
            buf.append(";Expires=");
            if (maxAge == 0)
                buf.append(__01Jan1970_COOKIE);
            else
                DateGenerator.formatCookieDate(buf, System.currentTimeMillis() + 1000L * maxAge);
            
            // for v1 cookies, also send max-age
            if (version>=1)
            {
                buf.append(";Max-Age=");
                buf.append(maxAge);
            }
        }

        // add the other fields
        if (isSecure)
            buf.append(";Secure");
        if (isHttpOnly)
            buf.append(";HttpOnly");
        if (comment != null)
        {
            buf.append(";Comment=");
            quoteOnlyOrAppend(buf,comment,isQuoteNeededForCookie(comment));
        }
        
        // add the set cookie
        _fields.add(HttpHeader.SET_COOKIE, buf.toString());

        // Expire responses with set-cookie headers so they do not get cached.
        _fields.put(__EXPIRES_01JAN1970);
    }


    /* ------------------------------------------------------------ */
    /** Does a cookie value need to be quoted?
     * @param s value string
     * @return true if quoted;
     * @throws IllegalArgumentException If there a control characters in the string
     */
    private static boolean isQuoteNeededForCookie(String s)
    {
        if (s==null || s.length()==0)
            return true;
        
        if (QuotedStringTokenizer.isQuoted(s))
            return false;

        for (int i=0;i<s.length();i++)
        {
            char c = s.charAt(i);
            if (__COOKIE_DELIM.indexOf(c)>=0)
                return true;
            
            if (c<0x20 || c>=0x7f)
                throw new IllegalArgumentException("Illegal character in cookie value");
        }

        return false;
    }
    
    
    private static void quoteOnlyOrAppend(StringBuilder buf, String s, boolean quote)
    {
        if (quote)
            QuotedStringTokenizer.quoteOnly(buf,s);
        else
            buf.append(s);
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
        SessionManager sessionManager = request.getSessionManager();
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

        String id = sessionManager.getNodeId(session);

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

    @Override
    public void sendError(int code, String message) throws IOException
    {
        if (isIncluding())
            return;

        if (isCommitted())
        {
            if (LOG.isDebugEnabled())
                LOG.debug("Aborting on sendError on committed response {} {}",code,message);
            code=-1;
        }
        
        switch(code)
        {
            case -1:
                _channel.abort(new IOException());
                return;
            case 102:
                sendProcessing();
                return;
            default:
        }

        if (isCommitted())
            LOG.warn("cannot sendError("+code+", "+message+") response already committed");
        else
            resetBuffer();
        
        _characterEncoding=null;
        setHeader(HttpHeader.EXPIRES,null);
        setHeader(HttpHeader.LAST_MODIFIED,null);
        setHeader(HttpHeader.CACHE_CONTROL,null);
        setHeader(HttpHeader.CONTENT_TYPE,null);
        setHeader(HttpHeader.CONTENT_LENGTH,null);

        _outputType = OutputType.NONE;
        setStatus(code);
        _reason=message;

        Request request = _channel.getRequest();
        Throwable cause = (Throwable)request.getAttribute(Dispatcher.ERROR_EXCEPTION);
        if (message==null)
            message=cause==null?HttpStatus.getMessage(code):cause.toString();

        // If we are allowed to have a body
        if (code!=SC_NO_CONTENT &&
            code!=SC_NOT_MODIFIED &&
            code!=SC_PARTIAL_CONTENT &&
            code>=SC_OK)
        {
            ErrorHandler error_handler = ErrorHandler.getErrorHandler(_channel.getServer(),request.getContext()==null?null:request.getContext().getContextHandler());
            if (error_handler!=null)
            {
                request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE,new Integer(code));
                request.setAttribute(RequestDispatcher.ERROR_MESSAGE, message);
                request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, request.getRequestURI());
                request.setAttribute(RequestDispatcher.ERROR_SERVLET_NAME,request.getServletName());
                error_handler.handle(null,_channel.getRequest(),_channel.getRequest(),this );
            }
            else
            {
                setHeader(HttpHeader.CACHE_CONTROL, "must-revalidate,no-cache,no-store");
                setContentType(MimeTypes.Type.TEXT_HTML_8859_1.toString());
                try (ByteArrayISO8859Writer writer= new ByteArrayISO8859Writer(2048);)
                {
                    message=StringUtil.sanitizeXmlString(message);
                    String uri= request.getRequestURI();
                    uri=StringUtil.sanitizeXmlString(uri);

                    writer.write("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html;charset=ISO-8859-1\"/>\n");
                    writer.write("<title>Error ");
                    writer.write(Integer.toString(code));
                    writer.write(' ');
                    if (message==null)
                        writer.write(message);
                    writer.write("</title>\n</head>\n<body>\n<h2>HTTP ERROR: ");
                    writer.write(Integer.toString(code));
                    writer.write("</h2>\n<p>Problem accessing ");
                    writer.write(uri);
                    writer.write(". Reason:\n<pre>    ");
                    writer.write(message);
                    writer.write("</pre>");
                    writer.write("</p>\n<hr />");

                    getHttpChannel().getHttpConfiguration().writePoweredBy(writer,null,"<hr/>");
                    writer.write("\n</body>\n</html>\n");

                    writer.flush();
                    setContentLength(writer.size());
                    try (ServletOutputStream outputStream = getOutputStream())
                    {
                        writer.writeTo(outputStream);
                        writer.destroy();
                    }
                }
            }
        }
        else if (code!=SC_PARTIAL_CONTENT)
        {
            // TODO work out why this is required?
            _channel.getRequest().getHttpFields().remove(HttpHeader.CONTENT_TYPE);
            _channel.getRequest().getHttpFields().remove(HttpHeader.CONTENT_LENGTH);
            _characterEncoding=null;
            _mimeType=null;
        }

        closeOutput();
    }

    /**
     * Sends a 102-Processing response.
     * If the connection is a HTTP connection, the version is 1.1 and the
     * request has a Expect header starting with 102, then a 102 response is
     * sent. This indicates that the request still be processed and real response
     * can still be sent.   This method is called by sendError if it is passed 102.
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
     * @param code the redirect status code
     * @param location the location to send in <code>Location</code> headers
     * @throws IOException if unable to send the redirect
     */
    public void sendRedirect(int code, String location) throws IOException
    {
        if ((code < HttpServletResponse.SC_MULTIPLE_CHOICES) || (code >= HttpServletResponse.SC_BAD_REQUEST))
            throw new IllegalArgumentException("Not a 3xx redirect code");
        
        if (isIncluding())
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
            StringBuilder buf = _channel.getRequest().getRootURL();
            if (location.startsWith("/"))
            {
                // absolute in context
                location=URIUtil.canonicalPath(location);
            }
            else
            {
                // relative to request
                String path=_channel.getRequest().getRequestURI();
                String parent=(path.endsWith("/"))?path:URIUtil.parentPath(path);
                location=URIUtil.canonicalPath(URIUtil.addEncodedPaths(parent,location));
                if (!location.startsWith("/"))
                    buf.append('/');
            }
            
            if(location==null)
                throw new IllegalStateException("path cannot be above root");
            buf.append(location);
            
            location=buf.toString();
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
        if (!isIncluding())
            _fields.putDateField(name, date);
    }

    @Override
    public void addDateHeader(String name, long date)
    {
        if (!isIncluding())
            _fields.addDateField(name, date);
    }

    public void setHeader(HttpHeader name, String value)
    {
        if (HttpHeader.CONTENT_TYPE == name)
            setContentType(value);
        else
        {
            if (isIncluding())
                return;

            _fields.put(name, value);

            if (HttpHeader.CONTENT_LENGTH == name)
            {
                if (value == null)
                    _contentLength = -1l;
                else
                    _contentLength = Long.parseLong(value);
            }
        }
    }

    @Override
    public void setHeader(String name, String value)
    {
        if (HttpHeader.CONTENT_TYPE.is(name))
            setContentType(value);
        else
        {
            if (isIncluding())
            {
                if (name.startsWith(SET_INCLUDE_HEADER_PREFIX))
                    name = name.substring(SET_INCLUDE_HEADER_PREFIX.length());
                else
                    return;
            }
            _fields.put(name, value);
            if (HttpHeader.CONTENT_LENGTH.is(name))
            {
                if (value == null)
                    _contentLength = -1l;
                else
                    _contentLength = Long.parseLong(value);
            }
        }
    }

    @Override
    public Collection<String> getHeaderNames()
    {
        final HttpFields fields = _fields;
        return fields.getFieldNamesCollection();
    }

    @Override
    public String getHeader(String name)
    {
        return _fields.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name)
    {
        final HttpFields fields = _fields;
        Collection<String> i = fields.getValuesList(name);
        if (i == null)
            return Collections.emptyList();
        return i;
    }

    @Override
    public void addHeader(String name, String value)
    {
        if (isIncluding())
        {
            if (name.startsWith(SET_INCLUDE_HEADER_PREFIX))
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
            setHeader(name,value);
            return;
        }
        
        _fields.add(name, value);
    }

    @Override
    public void setIntHeader(String name, int value)
    {
        if (!isIncluding())
        {
            _fields.putLongField(name, value);
            if (HttpHeader.CONTENT_LENGTH.is(name))
                _contentLength = value;
        }
    }

    @Override
    public void addIntHeader(String name, int value)
    {
        if (!isIncluding())
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
        if (!isIncluding())
        {
            _status = sc;
            _reason = null;
        }
    }

    @Override
    @Deprecated
    public void setStatus(int sc, String sm)
    {
        setStatusWithReason(sc,sm);
    }
    
    public void setStatusWithReason(int sc, String sm)
    {
        if (sc <= 0)
            throw new IllegalArgumentException();
        if (!isIncluding())
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
            if (encoding!=null)
                return encoding;
            encoding = MimeTypes.getCharsetInferredFromContentType(_contentType);
            if (encoding!=null)
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
                if (_mimeType!=null && _mimeType.isCharsetAssumed())
                    encoding=_mimeType.getCharsetString();
                else
                {
                    encoding = MimeTypes.getCharsetAssumedFromContentType(_contentType);
                    if (encoding == null)
                    {
                        encoding = MimeTypes.getCharsetInferredFromContentType(_contentType);
                        if (encoding == null)
                            encoding = StringUtil.__ISO_8859_1;
                        setCharacterEncoding(encoding,EncodingFrom.INFERRED);
                    }
                }
            }
            
            Locale locale = getLocale();
            
            if (_writer != null && _writer.isFor(locale,encoding))
                _writer.reopen();
            else
            {
                if (StringUtil.__ISO_8859_1.equalsIgnoreCase(encoding))
                    _writer = new ResponseWriter(new Iso88591HttpWriter(_out),locale,encoding);
                else if (StringUtil.__UTF8.equalsIgnoreCase(encoding))
                    _writer = new ResponseWriter(new Utf8HttpWriter(_out),locale,encoding);
                else
                    _writer = new ResponseWriter(new EncodingHttpWriter(_out, encoding),locale,encoding);
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
        if (isCommitted() || isIncluding())
            return;

        _contentLength = len;
        if (_contentLength > 0)
        {
            long written = _out.getWritten();
            if (written > len)
                throw new IllegalArgumentException("setContentLength(" + len + ") when already written " + written);
            
            _fields.putLongField(HttpHeader.CONTENT_LENGTH, len);
            if (isAllContentWritten(written))
            {
                try
                {
                    closeOutput();
                }
                catch(IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        }
        else if (_contentLength==0)
        {
            long written = _out.getWritten();
            if (written > 0)
                throw new IllegalArgumentException("setContentLength(0) when already written " + written);
            _fields.put(HttpHeader.CONTENT_LENGTH, "0");
        }
        else
            _fields.remove(HttpHeader.CONTENT_LENGTH);
    }
    
    public long getContentLength()
    {
        return _contentLength;
    }

    public boolean isAllContentWritten(long written)
    {
        return (_contentLength >= 0 && written >= _contentLength);
    }

    public void closeOutput() throws IOException
    {
        switch (_outputType)
        {
            case WRITER:
                _writer.close();
                if (!_out.isClosed())
                    _out.close();
                break;
            case STREAM:
                getOutputStream().close();
                break;
            default:
                _out.close();
        }
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
        if (isCommitted() || isIncluding())
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
        setCharacterEncoding(encoding,EncodingFrom.SET_CHARACTER_ENCODING);
    }
    
    private void setCharacterEncoding(String encoding, EncodingFrom from)
    {
        if (isIncluding() || isWriting())
            return;

        if (_outputType != OutputType.WRITER && !isCommitted())
        {
            if (encoding == null)
            {
                _encodingFrom=EncodingFrom.NOT_SET;
                
                // Clear any encoding.
                if (_characterEncoding != null)
                {
                    _characterEncoding = null;
                    
                    if (_mimeType!=null)
                    {
                        _mimeType=_mimeType.getBaseType();
                        _contentType=_mimeType.asString();
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
                _characterEncoding = HttpGenerator.__STRICT?encoding:StringUtil.normalizeCharset(encoding);
                if (_mimeType!=null)
                {
                    _contentType=_mimeType.getBaseType().asString()+ ";charset=" + _characterEncoding;
                    _mimeType = MimeTypes.CACHE.get(_contentType);
                    if (_mimeType==null || HttpGenerator.__STRICT)
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
        if (isCommitted() || isIncluding())
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
            if (_mimeType!=null && _mimeType.getCharset()!=null && !_mimeType.isCharsetAssumed())
                charset=_mimeType.getCharsetString();
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
                            _mimeType=null;
                            _contentType = _contentType + ";charset=" + _characterEncoding;
                        }
                        else
                        {
                            _encodingFrom=EncodingFrom.NOT_SET;
                            _characterEncoding=null;
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

            if (HttpGenerator.__STRICT || _mimeType==null)
                _fields.put(HttpHeader.CONTENT_TYPE, _contentType);
            else
            {
                _contentType=_mimeType.asString();
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
        reset(false);
    }

    public void reset(boolean preserveCookies)
    { 
        resetForForward();
        _status = 200;
        _reason = null;
        _contentLength = -1;
        
        List<HttpField> cookies = preserveCookies
            ?_fields.stream()
            .filter(f->f.getHeader()==HttpHeader.SET_COOKIE)
            .collect(Collectors.toList()):null;
        
        _fields.clear();

        String connection = _channel.getRequest().getHeader(HttpHeader.CONNECTION.asString());  
        if (connection != null)
        {
            for (String value: StringUtil.csvSplit(null,connection,0,connection.length()))
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
        }

        if (preserveCookies)
            cookies.forEach(f->_fields.add(f));
        else
        {
            Request request = getHttpChannel().getRequest();
            HttpSession session = request.getSession(false);
            if (session!=null && session.isNew())
            {
                SessionManager sm = request.getSessionManager();
                if (sm!=null)
                {
                    HttpCookie c=sm.getSessionCookie(session,request.getContextPath(),request.isSecure());
                    if (c!=null)
                        addCookie(c);
                }
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
    }

    protected MetaData.Response newResponseMetaData()
    {
        return new MetaData.Response(_channel.getRequest().getHttpVersion(), getStatus(), getReason(), _fields, getLongContentLength());
    }
    
    /** Get the MetaData.Response committed for this response.
     * This may differ from the meta data in this response for 
     * exceptional responses (eg 4xx and 5xx responses generated
     * by the container) and the committedMetaData should be used 
     * for logging purposes.
     * @return The committed MetaData or a {@link #newResponseMetaData()}
     * if not yet committed.
     */
    public MetaData.Response getCommittedMetaData()
    {
        MetaData.Response meta = _channel.getCommittedMetaData();
        if (meta==null)
            return newResponseMetaData();
        return meta;
    }

    @Override
    public boolean isCommitted()
    {
        return _channel.isCommitted();
    }

    @Override
    public void setLocale(Locale locale)
    {
        if (locale == null || isCommitted() || isIncluding())
            return;

        _locale = locale;
        _fields.put(HttpHeader.CONTENT_LANGUAGE, locale.toString().replace('_', '-'));

        if (_outputType != OutputType.NONE)
            return;

        if (_channel.getRequest().getContext() == null)
            return;

        String charset = _channel.getRequest().getContext().getContextHandler().getLocaleEncoding(locale);

        if (charset != null && charset.length() > 0 && __localeOverride.contains(_encodingFrom))
            setCharacterEncoding(charset,EncodingFrom.SET_LOCALE);
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
    

    public void putHeaders(HttpContent content,long contentLength, boolean etag)
    {
        HttpField lm = content.getLastModified();
        if (lm!=null)
            _fields.put(lm);

        if (contentLength==0)
        {
            _fields.put(content.getContentLength());
            _contentLength=content.getContentLengthValue();
        }
        else if (contentLength>0)
        {
            _fields.putLongField(HttpHeader.CONTENT_LENGTH,contentLength);
            _contentLength=contentLength;
        }

        HttpField ct=content.getContentType();
        if (ct!=null)
        {
            _fields.put(ct);
            _contentType=ct.getValue();
            _characterEncoding=content.getCharacterEncoding();
            _mimeType=content.getMimeType();
        }
        
        HttpField ce=content.getContentEncoding();
        if (ce!=null)
            _fields.put(ce);
        
        if (etag)
        {
            HttpField et = content.getETag();
            if (et!=null)
                _fields.put(et);
        }
    }
    
    public static void putHeaders(HttpServletResponse response, HttpContent content, long contentLength, boolean etag)
    {   
        long lml=content.getResource().lastModified();
        if (lml>=0)
            response.setDateHeader(HttpHeader.LAST_MODIFIED.asString(),lml);

        if (contentLength==0)
            contentLength=content.getContentLengthValue();
        if (contentLength >=0)
        {
            if (contentLength<Integer.MAX_VALUE)
                response.setContentLength((int)contentLength);
            else
                response.setHeader(HttpHeader.CONTENT_LENGTH.asString(),Long.toString(contentLength));
        }

        String ct=content.getContentTypeValue();
        if (ct!=null && response.getContentType()==null)
            response.setContentType(ct);

        String ce=content.getContentEncodingValue();
        if (ce!=null)
            response.setHeader(HttpHeader.CONTENT_ENCODING.asString(),ce);
        
        if (etag)
        {
            String et=content.getETagValue();
            if (et!=null)
                response.setHeader(HttpHeader.ETAG.asString(),et);
        }
    }
}
