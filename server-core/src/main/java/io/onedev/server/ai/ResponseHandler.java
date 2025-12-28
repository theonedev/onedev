package io.onedev.server.ai;

import io.onedev.server.model.User;

public interface ResponseHandler {

    void onResponse(User ai, String response);

}