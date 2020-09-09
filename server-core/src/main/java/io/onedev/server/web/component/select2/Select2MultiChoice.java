/*
 * Copyright 2012 Igor Vaynberg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.onedev.server.web.component.select2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.json.JSONException;

import io.onedev.server.web.component.select2.json.JsonBuilder;

/**
 * Multi-select Select2 component. Should be attached to a
 * {@code <input type='hidden'/>} element.
 * 
 * @author igor
 * 
 * @param <T>
 *            type of choice object
 */
public class Select2MultiChoice<T> extends AbstractSelect2Choice<T, Collection<T>> {

	private static final long serialVersionUID = 1L;

	public Select2MultiChoice(String id, IModel<Collection<T>> model, ChoiceProvider<T> provider) {
		super(id, model, provider);
	}

	public Select2MultiChoice(String id, IModel<Collection<T>> model) {
		super(id, model);
	}

	public Select2MultiChoice(String id) {
		super(id);
	}

	@Override
	public void convertInput() {

		String input = getWebRequest().getRequestParameters().getParameterValue(getInputName()).toString();

		final Collection<T> choices;
		if (Strings.isEmpty(input)) {
			choices = new ArrayList<T>();
		} else {
			choices = getProvider().toChoices(Arrays.asList(input.split(",")));
		}

		setConvertedInput(choices);
	}

	@Override
	public void updateModel() {
		Collection<T> choices = getModelObject();
		Collection<T> selection = getConvertedInput();

		if (choices == null) {
			getModel().setObject(selection);
		} else {
			choices = new ArrayList<>();
			choices.addAll(selection);
			getModel().setObject(choices);
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setMultiple(true);
		if (isRequired())
			getSettings().setPlaceholder("Select below...");
		else
			getSettings().setPlaceholder("");
	}

	@Override
	protected String getModelValue() {
		Collection<T> values = getModelObject();

		// if values is null or empty set value attribute to an empty string
		// rather then '[]' which does not make sense
		if (values == null || values.isEmpty()) {
			return "";
		}

		return super.getModelValue();
	}

	@Override
	protected void renderInitializationScript(IHeaderResponse response) {
		response.render(JavaScriptHeaderItem.forReference(new DragSortResourceReference()));
		
		Collection<? extends T> choices;
		
        if (hasRawInput()) { // Add this as otherwise cleared options will occur again after validation if the field is required 
            convertInput();
            choices = getConvertedInput();
        } else {
            choices = getModelObject();
        }
		
		if (choices != null && !choices.isEmpty()) {

			JsonBuilder selection = new JsonBuilder();

			try {
				selection.array();
				for (T choice : choices) {
					selection.object();
					getProvider().toJson(choice, selection);
					selection.endObject();
				}
				selection.endArray();
			} catch (JSONException e) {
				throw new RuntimeException("Error converting model object to Json", e);
			}

			response.render(OnLoadHeaderItem.forScript(
					JQuery.execute("$('#%s').select2('data', %s);", getJquerySafeMarkupId(), selection.toJson())));
		} else {
			clearInput();
		}
		String script = String.format("onedev.server.select2DragSort.onWindowLoad('%s');", getMarkupId());
		response.render(OnLoadHeaderItem.forScript(script));
	}

}
