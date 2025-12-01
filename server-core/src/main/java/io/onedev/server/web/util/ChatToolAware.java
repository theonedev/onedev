package io.onedev.server.web.util;

import java.util.List;

import io.onedev.server.service.support.ChatTool;

public interface ChatToolAware {

    List<ChatTool> getChatTools();
    
}
