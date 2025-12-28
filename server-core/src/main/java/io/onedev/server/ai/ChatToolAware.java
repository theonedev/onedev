package io.onedev.server.ai;

import java.io.Serializable;
import java.util.Collection;

public interface ChatToolAware extends Serializable {

	Collection<ChatTool> getChatTools();

}
