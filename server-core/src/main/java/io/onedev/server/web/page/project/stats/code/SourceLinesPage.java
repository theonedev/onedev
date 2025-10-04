package io.onedev.server.web.page.project.stats.code;

import static io.onedev.server.web.translation.Translation._T;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.OneDev;
import io.onedev.server.xodus.CommitInfoService;

public class SourceLinesPage extends CodeStatsPage {

	public SourceLinesPage(PageParameters params) {
		super(params);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		Map<Integer, Map<String, Integer>> lineIncrements = OneDev.getInstance(CommitInfoService.class)
				.getLineIncrements(getProject().getId());
		Map<Integer, Map<String, Integer>> data = new LinkedHashMap<>();
		for (var key: new TreeSet<>(lineIncrements.keySet())) 
			data.put(key, lineIncrements.get(key));
		var translations = new HashMap<String, String>();
		translations.put("noData", _T("No data"));
		translations.put("noDefaultBranch", _T("No default branch"));
		translations.put("sloc", _T("SLOC on {0}"));
		try {
			ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
			String jsonOfData = mapper.writeValueAsString(data);
			String jsonOfDefaultBranch = mapper.writeValueAsString(getProject().getDefaultBranch());
			String jsonOfTranslations = mapper.writeValueAsString(translations);
			String script = String.format("onedev.server.codeStats.sourceLines.onDomReady(%s, %s, %b, %s);", 
					jsonOfData, jsonOfDefaultBranch, isDarkMode(), jsonOfTranslations);
			response.render(OnDomReadyHeaderItem.forScript(script));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Code Line Statistics"));
	}

}
