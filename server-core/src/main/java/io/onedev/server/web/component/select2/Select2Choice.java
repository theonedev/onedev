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

import java.util.Collection;
import java.util.Collections;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.json.JSONException;

import io.onedev.server.web.component.select2.json.JsonBuilder;

/**
 * Single-select Select2 component. Should be attached to a
 * {@code <input type='hidden'/>} element.
 * 
 * @author igor
 * 
 * @param <T>
 *            type of choice object
 */
public class Select2Choice<T> extends AbstractSelect2Choice<T, T> {

	private static final long serialVersionUID = 1L;

	public Select2Choice(String id, IModel<T> model, ChoiceProvider<T> provider) {
		super(id, model, provider);
	}

	public Select2Choice(String id, IModel<T> model) {
		super(id, model);
	}

	public Select2Choice(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder("Select below...");
		else
			getSettings().setPlaceholder("");
	}

	@Override
	public void convertInput() {

		String input = getWebRequest().getRequestParameters().getParameterValue(getInputName()).toString();
		
		if (Strings.isEmpty(input)) {
			setConvertedInput(null);
		} else {
			Collection<T> choices = getProvider().toChoices(Collections.singleton(input));
			if (choices.isEmpty())
				setConvertedInput(null);
			else
				setConvertedInput(choices.iterator().next());
		}
	}

	@Override
	protected void renderInitializationScript(IHeaderResponse response) {
		T value;
		if (hasRawInput()) {
			convertInput();
			value = getConvertedInput();
		} else {
			value = getModelObject();
		}
		if (value != null) {

			JsonBuilder selection = new JsonBuilder();

			try {
				selection.object();
				getProvider().toJson(value, selection);
				selection.endObject();
			} catch (JSONException e) {
				throw new RuntimeException("Error converting model object to Json", e);
			}
			response.render(OnLoadHeaderItem.forScript(
					JQuery.execute("$('#%s').select2('data', %s);", getJquerySafeMarkupId(), selection.toJson())));
		} else {
			clearInput();
		}
	}
}
