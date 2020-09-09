package io.onedev.server.web.page.project.stats;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.util.Day;

@SuppressWarnings("serial")
public class SourceLinesPage extends ProjectStatsPage {

	public SourceLinesPage(PageParameters params) {
		super(params);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		Map<Day, Map<String, Integer>> lineIncrements = OneDev.getInstance(CommitInfoManager.class).getLineIncrements(getProject());
		Map<Integer, Map<String, Integer>> data = new HashMap<>();
		for (Map.Entry<Day, Map<String, Integer>> entry: lineIncrements.entrySet()) 
			data.put(entry.getKey().getValue(), entry.getValue());
		try {
			ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
			String jsonOfData = mapper.writeValueAsString(data);
			String jsonOfDefaultBranch = mapper.writeValueAsString(getProject().getDefaultBranch());
			String script = String.format("onedev.server.stats.sourceLines.onDomReady(%s, %s);", 
					jsonOfData, jsonOfDefaultBranch);
			response.render(OnDomReadyHeaderItem.forScript(script));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Line Statistics");
	}

}
