package io.onedev.server.web.component.stringchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;

import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class StringChoiceProvider extends ChoiceProvider<String> {

	private static final long serialVersionUID = 1L;

	public static final String SPECIAL_CHOICE_PREFIX = "<$OneDevSpecialChoice$>";
	
	private final IModel<Map<String, String>> choicesModel;
	
	public StringChoiceProvider(IModel<Map<String, String>> choicesModel) {
		this.choicesModel = choicesModel;
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		super.detach();
	}

	@Override
	public void toJson(String choice, JSONWriter writer) throws JSONException {
		String name = choicesModel.getObject().get(choice);
		if (name == null)
			name = choice;
		writer.key("id").value(choice).key("name").value(name);
	}

	@Override
	public Collection<String> toChoices(Collection<String> ids) {
		Collection<String> choices = new ArrayList<>(ids);
		choices.retainAll(choicesModel.getObject().keySet());
		return choices;
	}

	@Override
	public void query(String term, int page, Response<String> response) {
		List<String> matched;
		if (StringUtils.isNotBlank(term)) {
			matched = new ArrayList<>();
			for (Map.Entry<String, String> entry: choicesModel.getObject().entrySet()) {
				if (entry.getValue().toLowerCase().contains(term.toLowerCase())) 
					matched.add(entry.getKey());
			}
		} else {
			matched = new ArrayList<>(choicesModel.getObject().keySet());
		}
		new ResponseFiller<String>(response).fill(matched, page, WebConstants.PAGE_SIZE);
	}
	
}