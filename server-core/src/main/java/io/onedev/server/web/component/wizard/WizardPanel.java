/*
 * Copyright OneDev Inc.,
 * Date: 2008-8-4
 * Time: ����09:00:25
 * All rights reserved.
 *
 * Revision: $Id$
 */
package io.onedev.server.web.component.wizard;

import static io.onedev.server.web.translation.Translation._T;
import static java.text.MessageFormat.format;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.persistence.TransactionService;

public abstract class WizardPanel extends Panel {

	private List<? extends WizardStep> steps;
	
	private int activeStepIndex;
	
	public WizardPanel(String id, List<? extends WizardStep> steps) {
		super(id);
		
		Preconditions.checkArgument(steps != null && !steps.isEmpty());
		this.steps = steps;
	}		

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return format(_T("Step {0} of {1}: "), activeStepIndex+1, steps.size()) + _T(getActiveStep().getTitle());
			}
			
		}));
		form.add(new Label("description", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return _T(getActiveStep().getDescription());
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getActiveStep().getDescription() != null);
			}
			
		});
		
		form.add(new FencedFeedbackPanel("feedback", form));
		getActiveStep().init();
		form.add(getActiveStep().render("content"));
		form.add(new Link<Void>("previous") {

			@Override
			public void onClick() {
				activeStepIndex--;
				form.replace(getActiveStep().render("content"));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(activeStepIndex > 0);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (activeStepIndex <= 0)
					tag.append("class", "disabled", " ");
			}
			
		});
		form.add(new Button("next") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				OneDev.getInstance(TransactionService.class).run(new Runnable() {

					@Override
					public void run() {
						getActiveStep().complete();
					}
					
				});
				activeStepIndex++;
				getActiveStep().init();
				form.replace(getActiveStep().render("content"));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(activeStepIndex < steps.size()-1);
			}

		});
		
		form.add(newEndActions("endActions").add(new Behavior() {

			@Override
			public void onConfigure(Component component) {
				super.onConfigure(component);
				component.setVisible(activeStepIndex == steps.size()-1);
			}
			
		}));
		add(form);
	}
	
	protected abstract WebMarkupContainer newEndActions(String componentId);
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WizardResourceReference()));
	}

	public WizardStep getActiveStep() {
		return steps.get(activeStepIndex);
	}

}
