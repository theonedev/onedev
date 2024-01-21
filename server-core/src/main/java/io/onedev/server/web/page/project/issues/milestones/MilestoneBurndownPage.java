package io.onedev.server.web.page.project.issues.milestones;

import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.milestone.burndown.MilestoneBurndownPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

import static io.onedev.server.web.component.milestone.burndown.BurndownIndicators.getChoices;
import static io.onedev.server.web.component.milestone.burndown.BurndownIndicators.getDefault;

@SuppressWarnings("serial")
public class MilestoneBurndownPage extends MilestoneDetailPage {

	private static final String PARAM_INDICATOR = "indicator";
	
	private String indicator;
	
	private DropDownChoice<String>  indicatorChoice;
	
	public MilestoneBurndownPage(PageParameters params) {
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
				return object;
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
				var url = urlFor(MilestoneBurndownPage.class, paramsOf(getProject(), getMilestone(), indicator));
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
		var burndown = new MilestoneBurndownPanel("burndown", new AbstractReadOnlyModel<>() {
			@Override
			public Milestone getObject() {
				return getMilestone();
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
	
	private static PageParameters paramsOf(Project project, Milestone milestone, @Nullable String indicator) {
		var params = paramsOf(project, milestone);
		if (indicator != null)
			params.add(PARAM_INDICATOR, indicator);
		return params;
	}
}
