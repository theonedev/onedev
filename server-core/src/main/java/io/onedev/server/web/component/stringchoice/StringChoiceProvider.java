package io.onedev.server.web.component.stringchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.IModel;
import org.json.JSONException;
import org.json.JSONWriter;

import io.onedev.server.util.Similarities;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.select2.ChoiceProvider;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;

public class StringChoiceProvider extends ChoiceProvider<String> {

	private static final long serialVersionUID = 1L;

	public static final String SPECIAL_CHOICE_PREFIX = "<$OneDevSpecialChoice$>";
	
	private final IModel<Map<String, String>> choicesModel;
	
	private final boolean tagsMode;
	
	public StringChoiceProvider(IModel<Map<String, String>> choicesModel, boolean tagsMode) {
		this.choicesModel = choicesModel;
		this.tagsMode = tagsMode;
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
		if (!tagsMode)
			choices.retainAll(choicesModel.getObject().keySet());
		return choices;
	}

	@Override
	public void query(String term, int page, Response<String> response) {
		List<String> similarities;
		if (StringUtils.isNotBlank(term)) {
			Map<String, String> choices = choicesModel.getObject();
			similarities =  new Similarities<String>(choices.keySet()) {

				private static final long serialVersionUID = 1L;

				@Override
				public double getSimilarScore(String object) {
					return Similarities.getSimilarScore(choices.get(object), term);
				}
				
			};
			
			if (tagsMode && !similarities.contains(term))
				similarities.add(term);
		} else {
			similarities = new ArrayList<>(choicesModel.getObject().keySet());
		}
		new ResponseFiller<String>(response).fill(similarities, page, WebConstants.PAGE_SIZE);
	}
	
}