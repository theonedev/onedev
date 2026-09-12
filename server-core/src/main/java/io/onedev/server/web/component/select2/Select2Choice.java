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

import static io.onedev.server.web.translation.Translation._T;

import java.util.Collection;
import java.util.Collections;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;

/**
 * Single-select Select2 component. Should be attached to a
 * {@code <select></select>} element.
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
			getSettings().setPlaceholder(_T("Select below..."));
		else
			getSettings().setPlaceholder("");
	}

	@Override
	public void convertInput() {

		String[] inputs = getInputAsArray();
		String input = inputs != null && inputs.length != 0 ? inputs[0] : null;
		
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
	protected Collection<T> getSelections() {
		T value;
		if (hasRawInput()) {
			convertInput();
			value = getConvertedInput();
		} else {
			value = getModelObject();
		}
		return value != null ? Collections.singletonList(value) : Collections.emptyList();
	}
}
