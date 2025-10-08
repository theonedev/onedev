package io.onedev.server.web.page.project.issues.iteration;

import static io.onedev.server.web.component.iteration.burndown.BurndownIndicators.getChoices;
import static io.onedev.server.web.component.iteration.burndown.BurndownIndicators.getDefault;

import java.io.Serializable;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.Iteration;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.iteration.burndown.BurndownIndicators;
import io.onedev.server.web.component.iteration.burndown.IterationBurndownPanel;

public class IterationBurndownPage extends IterationDetailPage {

	private static final String PARAM_INDICATOR = "indicator";
	
	private String indicator;
	
	private DropDownChoice<String>  indicatorChoice;
	
	public IterationBurndownPage(PageParameters params) {
		super(params);
		indicator = params.get(PARAM_INDICATOR).toOptionalString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var choices = getChoices(getProject());
		indicatorChoice = new DropDownChoice<>("by", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return getIndicator();
			}

			@Override
			public void setObject(String object) {
				indicator = object;
			}

		}, choices, new IChoiceRenderer<>() {

			@Override
			public Object getDisplayValue(String object) {
				return BurndownIndicators.getDisplayName(object);
			}

			@Override
			public String getIdValue(String object, int index) {
				return object;
			}

			@Override
			public String getObject(String id, IModel<? extends List<? extends String>> choices) {
				return id;
			}

		});
		indicatorChoice.add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				newBurndown(target);
				var url = urlFor(IterationBurndownPage.class, paramsOf(getProject(), getIteration(), indicator));
				pushState(target, url.toString(), indicator);
			}

		});
		indicatorChoice.setOutputMarkupId(true);
		add(indicatorChoice);

		newBurndown(null);
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		indicator = (String) data;
		target.add(indicatorChoice);
		newBurndown(target);
	}

	private void newBurndown(@Nullable AjaxRequestTarget target) {
		var burndown = new IterationBurndownPanel("burndown", new AbstractReadOnlyModel<>() {
			@Override
			public Iteration getObject() {
				return getIteration();
			}
		}, indicator);
		
		if (target != null) {
			replace(burndown);
			target.add(burndown);
		} else {
			add(burndown);
		}
	}

	private String getIndicator() {
		if (indicator != null)
			return indicator;
		else
			return getDefault(getProject());
	}
	
	private static PageParameters paramsOf(Project project, Iteration iteration, @Nullable String indicator) {
		var params = paramsOf(project, iteration);
		if (indicator != null)
			params.add(PARAM_INDICATOR, indicator);
		return params;
	}
}
