package io.onedev.server.plugin.mailservice.sendgrid;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

interface MessageManager {

    void process(HttpServletRequest request, HttpServletResponse response);

    void register(MessageTarget target);

    void unregister(MessageTarget target);

}
