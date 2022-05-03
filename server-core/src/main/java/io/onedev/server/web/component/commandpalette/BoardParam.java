package io.onedev.server.web.component.commandpalette;

import java.util.LinkedHashMap;
import java.util.Map;

import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.web.page.project.issues.boards.IssueBoardsPage;

public class BoardParam extends ParamSegment {

	private static final long serialVersionUID = 1L;
	
	public BoardParam(boolean optional) {
		super(IssueBoardsPage.PARAM_BOARD, optional);
	}
	
	@Override
	public Map<String, String> suggest(String matchWith, 
			Map<String, String> paramValues, int count) {
		Map<String, String> suggestions = new LinkedHashMap<>();
		for (BoardSpec board: ParsedUrl.getProject(paramValues).getHierarchyBoards()) {
			if (board.getName().toLowerCase().contains(matchWith)) {
				suggestions.put(board.getName(), board.getName());
				if (--count == 0)
					break;
			}
		}
		return suggestions;
	}

	@Override
	public boolean isExactMatch(String matchWith, Map<String, String> paramValues) {
		return ParsedUrl.getProject(paramValues).getHierarchyBoards()
				.stream().anyMatch(it->it.getName().equals(matchWith));
	}
		
}
