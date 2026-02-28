package io.onedev.server.terminal;

import io.onedev.agent.AgentUtils;

public interface Terminal {
		
	void onShellOutput(String base64Data);
		
	void onShellExit();

	default void onShellError(String error) {
		onShellOutput(AgentUtils.encodeBase64Error(error));
	}
	
}
