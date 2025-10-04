package io.onedev.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.Map;

import io.onedev.server.OneDev;
import io.onedev.server.service.IterationService;
import io.onedev.server.model.Iteration;
import io.onedev.server.web.page.project.issues.iteration.IterationDetailPage;

public class IterationParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public IterationParam(boolean optional) {
		super(IterationDetailPage.PARAM_ITERATION, optional);
	}

	@Override
	public Map<String, String> suggest(String matchWith, Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		for (Iteration iteration: ParsedUrl.getProject(paramValues).getSortedHierarchyIterations()) {
			if (iteration.getName().toLowerCase().contains(matchWith)) {
				suggestions.put(iteration.getName(), String.valueOf(iteration.getId()));
				if (--count == 0)
					break;
			}
		}
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		try {
			Long iterationId = Long.valueOf(matchWith);
			if (OneDev.getInstance(IterationService.class).get(iterationId) != null)
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

}
