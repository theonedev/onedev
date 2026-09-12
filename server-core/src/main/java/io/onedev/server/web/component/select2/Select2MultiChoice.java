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
import java.util.Collections;

import org.apache.wicket.model.IModel;

/**
 * Multi-select Select2 component. Should be attached to a
 * {@code <select></select>} element.
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

		String[] input = getInputAsArray();

		final Collection<T> choices;
		if (input == null || input.length == 0) {
			choices = new ArrayList<T>();
		} else {
			choices = getProvider().toChoices(Arrays.asList(input));
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
	protected Collection<T> getSelections() {
		Collection<T> choices;
		if (hasRawInput()) {
			convertInput();
			choices = getConvertedInput();
		} else {
			choices = getModelObject();
		}
		return choices != null ? choices : Collections.emptyList();
	}
}
