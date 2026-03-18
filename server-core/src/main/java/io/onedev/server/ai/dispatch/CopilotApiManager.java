package io.onedev.server.ai.dispatch;

import java.util.function.Consumer;

import io.onedev.server.model.support.AiModelSetting;
import io.onedev.server.model.support.administration.CopilotApiSetting;

public interface CopilotApiManager {

	AiModelSetting prepareModelSetting(CopilotApiSetting setting, Consumer<String> logger);

}
