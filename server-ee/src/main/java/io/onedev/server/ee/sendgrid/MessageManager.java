package io.onedev.server.ee.sendgrid;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface MessageManager {

    void process(HttpServletRequest request, HttpServletResponse response);

    void register(MessageTarget target);

    void unregister(MessageTarget target);

}
