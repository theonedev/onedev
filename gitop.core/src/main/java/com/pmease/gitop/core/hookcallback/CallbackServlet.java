package com.pmease.gitop.core.hookcallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial")
public abstract class CallbackServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) clientIp = request.getRemoteAddr();

        if (!InetAddress.getByName(clientIp).isLoopbackAddress()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Git hook callbacks can only be accessed from localhost.");
            return;
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(request.getInputStream(), baos);

        Output output = new Output();
        callback(new String(baos.toByteArray()), output);
        
        response.getOutputStream().print(StringUtils.join(output.toString()));
    }

    protected abstract void callback(String callbackData, Output output);
    
}
