package io.onedev.server.web.component.build;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.util.Input;

@SuppressWarnings("serial")
public class ParamValuesLabel extends Label {

	public ParamValuesLabel(String id, Input param) {
		super(id);

		if (param.getType().equals(ParamSpec.SECRET)) {
			setDefaultModel(Model.of(SecretInput.MASK));
		} else if (!param.getValues().isEmpty()) {
			setDefaultModel(Model.of(StringUtils.join(param.getValues(), ",")));
		} else {
			setDefaultModel(Model.of("<i>Unspecified</i>"));
			setEscapeModelStrings(false);
		}
	}
	
}
