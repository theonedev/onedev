package io.onedev.server.web.component.stringchoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
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
	
	private final IModel<List<String>> choicesModel;

	private final IModel<Map<String, String>> displayNamesModel;

	private final IModel<Map<String, String>> descriptionsModel;

	private final boolean tagsMode;
	
	public StringChoiceProvider(
			IModel<List<String>> choicesModel, 
			IModel<Map<String, String>> displayNamesModel, 
			IModel<Map<String, String>> descriptionsModel, 
			boolean tagsMode) {
		this.choicesModel = choicesModel;
		this.displayNamesModel = displayNamesModel;
		this.descriptionsModel = descriptionsModel;
		this.tagsMode = tagsMode;
	}

	public StringChoiceProvider(
			IModel<List<String>> choicesModel,
			IModel<Map<String, String>> displayNamesModel,
			boolean tagsMode) {
		this(choicesModel, displayNamesModel, Model.ofMap(new HashMap<>()), tagsMode);
	}

	public StringChoiceProvider(IModel<List<String>> choicesModel, boolean tagsMode) {
		this(choicesModel, Model.ofMap(new HashMap<>()), Model.ofMap(new HashMap<>()), tagsMode);
	}
	
	@Override
	public void detach() {
		choicesModel.detach();
		displayNamesModel.detach();
		descriptionsModel.detach();
		super.detach();
	}

	@Override
	public void toJson(String choice, JSONWriter writer) throws JSONException {
		var name = displayNamesModel.getObject().get(choice);
		if (name == null)
			name = choice;
		writer.key("id").value(choice).key("name").value(name);
		var description = descriptionsModel.getObject().get(choice);
		if (description != null)
			writer.key("description").value(description);
	}

	@Override
	public Collection<String> toChoices(Collection<String> ids) {
		Collection<String> choices = new ArrayList<>(ids);
		if (!tagsMode)
			choices.retainAll(choicesModel.getObject());
		return choices;
	}

	@Override
	public void query(String term, int page, Response<String> response) {
		List<String> similarities;
		if (StringUtils.isNotBlank(term)) {
			List<String> choices = choicesModel.getObject();
			similarities =  new Similarities<String>(choices) {

				private static final long serialVersionUID = 1L;

				@Override
				public double getSimilarScore(String object) {
					return Similarities.getSimilarScore(object, term);
				}
				
			};
			
			if (tagsMode && !similarities.contains(term))
				similarities.add(term);
		} else {
			similarities = new ArrayList<>(choicesModel.getObject());
		}
		new ResponseFiller<String>(response).fill(similarities, page, WebConstants.PAGE_SIZE);
	}
	
}