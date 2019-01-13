package io.onedev.server.web.component.stringchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONWriter;

import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class StringChoiceProvider extends ChoiceProvider<String> {

	private static final long serialVersionUID = 1L;

	public static final String SPECIAL_CHOICE_PREFIX = "<$OneDevSpecialChoice$>";
	
	private final Map<String, String> choices;
	
	public StringChoiceProvider(Map<String, String> choices) {
		this.choices = choices;
	}
	
	@Override
	public void toJson(String choice, JSONWriter writer) throws JSONException {
		String name = choices.get(choice);
		if (name == null)
			name = choice;
		writer.key("id").value(choice).key("name").value(name);
	}

	@Override
	public Collection<String> toChoices(Collection<String> ids) {
		return ids;
	}

	@Override
	public void query(String term, int page, Response<String> response) {
		List<String> matched;
		if (StringUtils.isNotBlank(term)) {
			matched = new ArrayList<>();
			for (Map.Entry<String, String> entry: choices.entrySet()) {
				if (entry.getValue().toLowerCase().startsWith(term)) 
					matched.add(entry.getKey());
			}
		} else {
			matched = new ArrayList<>(choices.keySet());
		}
		new ResponseFiller<String>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}
	
}