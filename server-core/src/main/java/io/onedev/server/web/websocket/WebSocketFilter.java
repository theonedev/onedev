/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.onedev.server.web.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import jakarta.inject.Inject;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.protocol.ws.AbstractUpgradeFilter;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServerContainer;

import io.onedev.server.service.SettingService;

/** Upgrades Wicket connections through Jetty's Jakarta Servlet WebSocket container. */
public class WebSocketFilter extends AbstractUpgradeFilter {

    public static final String SHIRO_SUBJECT = "shiro_subject";

    private JettyWebSocketServerContainer webSocketContainer;

    @Inject
    private SettingService settingService;

    @Override
    public void init(boolean isServlet, FilterConfig filterConfig) throws ServletException {
        super.init(isServlet, filterConfig);
        webSocketContainer = JettyWebSocketServerContainer.getContainer(filterConfig.getServletContext());
        if (webSocketContainer == null)
            throw new ServletException("Jetty WebSocket container is not initialized");
    }

    @Override
    protected boolean acceptWebSocket(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!super.acceptWebSocket(request, response))
            return false;
        if (!isOriginAllowed(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        request.setAttribute(SHIRO_SUBJECT, SecurityUtils.getSubject());
        return webSocketContainer.upgrade((req, resp) -> new WebSocketProcessor(req, resp, getApplication()),
                request, response);
    }

    private boolean isOriginAllowed(HttpServletRequest request) {
        var origins = request.getHeaders("Origin");
        if (!origins.hasMoreElements())
            return false;
        var originHeader = origins.nextElement();
        if (origins.hasMoreElements())
            return false;
        try {
            var origin = new URI(originHeader);
            if ((!"http".equalsIgnoreCase(origin.getScheme()) && !"https".equalsIgnoreCase(origin.getScheme()))
                    || origin.getHost() == null || origin.getRawUserInfo() != null
                    || !origin.getRawPath().isEmpty() || origin.getRawQuery() != null
                    || origin.getRawFragment() != null || origin.getPort() > 65535)
                return false;

            // Reverse proxies may replace the public scheme, host, and port with backend values.
            var systemSetting = settingService.getSystemSetting();
            if (systemSetting != null && systemSetting.getServerUrl() != null
                    && isSameOrigin(origin, new URI(systemSetting.getServerUrl())))
                return true;

            // Preserve direct access via localhost, IP addresses, or alternate hostnames.
            var requestOrigin = new URI(request.getScheme(), null, request.getServerName(),
                    request.getServerPort(), null, null, null);
            return isSameOrigin(origin, requestOrigin);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private boolean isSameOrigin(URI origin, URI target) {
        return origin.getScheme().equalsIgnoreCase(target.getScheme())
                && origin.getHost().equalsIgnoreCase(target.getHost())
                && getPort(origin) == getPort(target);
    }

    private int getPort(URI uri) {
        if (uri.getPort() != -1)
            return uri.getPort();
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }
}
