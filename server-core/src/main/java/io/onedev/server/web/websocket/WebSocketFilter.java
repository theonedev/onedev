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

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.protocol.ws.AbstractUpgradeFilter;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServerContainer;

/** Upgrades Wicket connections through Jetty's Jakarta Servlet WebSocket container. */
public class WebSocketFilter extends AbstractUpgradeFilter {

    public static final String SHIRO_SUBJECT = "shiro_subject";

    private JettyWebSocketServerContainer webSocketContainer;

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
        request.setAttribute(SHIRO_SUBJECT, SecurityUtils.getSubject());
        return super.acceptWebSocket(request, response)
                && webSocketContainer.upgrade((req, resp) -> new WebSocketProcessor(req, resp, getApplication()),
                        request, response);
    }
}
