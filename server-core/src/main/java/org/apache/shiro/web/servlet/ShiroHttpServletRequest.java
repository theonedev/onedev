/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shiro.web.servlet;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DisabledSessionException;
import org.apache.shiro.web.util.WebUtils;
import org.apache.wicket.request.cycle.RequestCycle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.security.Principal;


/**
 * A {@code ShiroHttpServletRequest} wraps the Servlet container's original {@code ServletRequest} instance, but ensures
 * that all {@link HttpServletRequest} invocations that require Shiro's support ({@link #getRemoteUser getRemoteUser},
 * {@link #getSession getSession}, etc) can be executed first by Shiro as necessary before allowing the underlying
 * Servlet container instance's method to be invoked.
 *
 * @since 0.2
 */
public class ShiroHttpServletRequest extends HttpServletRequestWrapper {

    //The following 7 constants support the Shiro's implementation of the Servlet Specification
    public static final String COOKIE_SESSION_ID_SOURCE = "cookie";
    public static final String URL_SESSION_ID_SOURCE = "url";
    public static final String REFERENCED_SESSION_ID = ShiroHttpServletRequest.class.getName() + "_REQUESTED_SESSION_ID";
    public static final String REFERENCED_SESSION_ID_IS_VALID = ShiroHttpServletRequest.class.getName() + "_REQUESTED_SESSION_ID_VALID";
    public static final String REFERENCED_SESSION_IS_NEW = ShiroHttpServletRequest.class.getName() + "_REFERENCED_SESSION_IS_NEW";
    public static final String REFERENCED_SESSION_ID_SOURCE = ShiroHttpServletRequest.class.getName() + "REFERENCED_SESSION_ID_SOURCE";
    public static final String IDENTITY_REMOVED_KEY = ShiroHttpServletRequest.class.getName() + "_IDENTITY_REMOVED_KEY";
    public static final String SESSION_ID_URL_REWRITING_ENABLED = ShiroHttpServletRequest.class.getName() + "_SESSION_ID_URL_REWRITING_ENABLED";

    protected ServletContext servletContext = null;

    protected HttpSession session = null;
    protected boolean httpSessions = true;

    public ShiroHttpServletRequest(HttpServletRequest wrapped, ServletContext servletContext, boolean httpSessions) {
        super(wrapped);
        this.servletContext = servletContext;
        this.httpSessions = httpSessions;
    }

    public boolean isHttpSessions() {
        return httpSessions;
    }

    public String getRemoteUser() {
        String remoteUser;
        Object scPrincipal = getSubjectPrincipal();
        if (scPrincipal != null) {
            if (scPrincipal instanceof String) {
                return (String) scPrincipal;
            } else if (scPrincipal instanceof Principal) {
                remoteUser = ((Principal) scPrincipal).getName();
            } else {
                remoteUser = scPrincipal.toString();
            }
        } else {
            remoteUser = super.getRemoteUser();
        }
        return remoteUser;
    }

    protected Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    protected Object getSubjectPrincipal() {
        Object userPrincipal = null;
        Subject subject = getSubject();
        if (subject != null) {
            userPrincipal = subject.getPrincipal();
        }
        return userPrincipal;
    }

    public boolean isUserInRole(String s) {
        Subject subject = getSubject();
        boolean inRole = (subject != null && subject.hasRole(s));
        if (!inRole) {
            inRole = super.isUserInRole(s);
        }
        return inRole;
    }

    public Principal getUserPrincipal() {
        Principal userPrincipal;
        Object scPrincipal = getSubjectPrincipal();
        if (scPrincipal != null) {
            if (scPrincipal instanceof Principal) {
                userPrincipal = (Principal) scPrincipal;
            } else {
                userPrincipal = new ObjectPrincipal(scPrincipal);
            }
        } else {
            userPrincipal = super.getUserPrincipal();
        }
        return userPrincipal;
    }

    public String getRequestedSessionId() {
        String requestedSessionId = null;
        if (isHttpSessions()) {
            requestedSessionId = super.getRequestedSessionId();
        } else {
            Object sessionId = getAttribute(REFERENCED_SESSION_ID);
            if (sessionId != null) {
                requestedSessionId = sessionId.toString();
            }
        }

        return requestedSessionId;
    }

    public HttpSession getSession(boolean create) {

        HttpSession httpSession;

        if (isHttpSessions()) {
            httpSession = super.getSession(false);
            if (httpSession == null && create) {
                //Shiro 1.2: assert that creation is enabled (SHIRO-266):
                if (RequestCycle.get() != null || WebUtils._isSessionCreationEnabled(this)) {
                    httpSession = super.getSession(create);
                } else {
                    throw newNoSessionCreationException();
                }
            }
        } else {
            boolean existing = getSubject().getSession(false) != null;
            
            if (this.session == null || !existing) {
                Session shiroSession = getSubject().getSession(create);
                if (shiroSession != null) {
                    this.session = new ShiroHttpSession(shiroSession, this, this.servletContext);
                    if (!existing) {
                        setAttribute(REFERENCED_SESSION_IS_NEW, Boolean.TRUE);
                    }
                } else if (this.session != null) {
                    this.session = null;
                }
            }
            httpSession = this.session;
        }

        return httpSession;
    }

    /**
     * Constructs and returns a {@link DisabledSessionException} with an appropriate message explaining why
     * session creation has been disabled.
     *
     * @return a new DisabledSessionException with appropriate no creation message
     * @since 1.2
     */
    private DisabledSessionException newNoSessionCreationException() {
        String msg = "Session creation has been disabled for the current request.  This exception indicates " +
                "that there is either a programming error (using a session when it should never be " +
                "used) or that Shiro's configuration needs to be adjusted to allow Sessions to be created " +
                "for the current request.  See the " + DisabledSessionException.class.getName() + " JavaDoc " +
                "for more.";
        return new DisabledSessionException(msg);
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public boolean isRequestedSessionIdValid() {
        if (isHttpSessions()) {
            return super.isRequestedSessionIdValid();
        } else {
            Boolean value = (Boolean) getAttribute(REFERENCED_SESSION_ID_IS_VALID);
            return (value != null && value.equals(Boolean.TRUE));
        }
    }

    public boolean isRequestedSessionIdFromCookie() {
        if (isHttpSessions()) {
            return super.isRequestedSessionIdFromCookie();
        } else {
            String value = (String) getAttribute(REFERENCED_SESSION_ID_SOURCE);
            return value != null && value.equals(COOKIE_SESSION_ID_SOURCE);
        }
    }

    public boolean isRequestedSessionIdFromURL() {
        if (isHttpSessions()) {
            return super.isRequestedSessionIdFromURL();
        } else {
            String value = (String) getAttribute(REFERENCED_SESSION_ID_SOURCE);
            return value != null && value.equals(URL_SESSION_ID_SOURCE);
        }
    }

    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    private class ObjectPrincipal implements java.security.Principal {
        private Object object = null;

        public ObjectPrincipal(Object object) {
            this.object = object;
        }

        public Object getObject() {
            return object;
        }

        public String getName() {
            return getObject().toString();
        }

        public int hashCode() {
            return object.hashCode();
        }

        public boolean equals(Object o) {
            if (o instanceof ObjectPrincipal) {
                ObjectPrincipal op = (ObjectPrincipal) o;
                return getObject().equals(op.getObject());
            }
            return false;
        }

        public String toString() {
            return object.toString();
        }
    }
}
