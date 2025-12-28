package io.onedev.server.ai;

import java.io.Serializable;
import java.util.List;

public interface ChatToolAware extends Serializable {

	List<ChatTool> getChatTools();

}
