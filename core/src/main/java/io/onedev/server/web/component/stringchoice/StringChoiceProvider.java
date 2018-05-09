package io.onedev.server.web.component.stringchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONWriter;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class StringChoiceProvider extends ChoiceProvider<String> {

	private static final long serialVersionUID = 1L;
	
	private final List<String> values;
	
	public StringChoiceProvider(List<String> values) {
		this.values = values;
	}
	
	@Override
	public void toJson(String choice, JSONWriter writer) throws JSONException {
		String escapedValue = HtmlEscape.escapeHtml5(choice);
		writer.key("id").value(escapedValue).key("name").value(escapedValue);
	}

	@Override
	public Collection<String> toChoices(Collection<String> ids) {
		Collection<String> choices = new ArrayList<>();
		for (String id: ids) 
			choices.add(HtmlEscape.unescapeHtml(id));
		return choices;
	}

	@Override
	public void query(String term, int page, Response<String> response) {
		List<String> matched;
		if (StringUtils.isNotBlank(term)) {
			matched = new ArrayList<>();
			for (String each: values) {
				if (each.toLowerCase().startsWith(term))
					matched.add(each);
			}
		} else {
			matched = values;
		}
		new ResponseFiller<String>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}
	
}