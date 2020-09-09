package io.onedev.server.web.component.branch.picker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Project;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.InputChangeBehavior;

@SuppressWarnings("serial")
public abstract class BranchSelector extends Panel {
	
	private final IModel<Project> projectModel;
	
	private final String branch;
	
	private int activeBranchIndex;
	
	private String branchInput;
	
	private AbstractPostAjaxBehavior keyBehavior;
	
	private TextField<String> branchField;

	private final List<String> branches = new ArrayList<>();
	
	private final List<String> filteredBranches = new ArrayList<>();
	
	public BranchSelector(String id, IModel<Project> projectModel, String branch) {
		super(id);
		
		this.projectModel = projectModel;
		this.branch = branch;		
		
		for (RefInfo ref: projectModel.getObject().getBranchRefInfos())
			branches.add(GitUtils.ref2branch(ref.getRef().getName()));
		
		filteredBranches.addAll(branches);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		branchField = new TextField<String>("branch", Model.of(""));
		branchField.setOutputMarkupId(true);
		add(branchField);
		
		keyBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String key = params.getParameterValue("key").toString();
				
				if (key.equals("return")) {
					if (!filteredBranches.isEmpty()) 
						onSelect(target, filteredBranches.get(activeBranchIndex));
				} else if (key.equals("up")) {
					activeBranchIndex--;
				} else if (key.equals("down")) {
					activeBranchIndex++;
				} else {
					throw new IllegalStateException("Unrecognized key: " + key);
				}
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				String script = String.format("onedev.server.branchSelector.init('%s', %s);", 
						getMarkupId(true), getCallbackFunction(CallbackParameter.explicit("key")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		};
		add(keyBehavior);
		
		WebMarkupContainer noBranchesContainer = new WebMarkupContainer("noBranches") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(filteredBranches.isEmpty());
			}
			
		};
		noBranchesContainer.setOutputMarkupPlaceholderTag(true);
		add(noBranchesContainer);
		
		WebMarkupContainer branchesContainer = new WebMarkupContainer("branches") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!filteredBranches.isEmpty());
			}
			
		};
		
		branchField.add(new InputChangeBehavior() {
			
			@Override
			protected void onInputChange(AjaxRequestTarget target) {
				branchInput = branchField.getInput();
				filteredBranches.clear();
				if (StringUtils.isNotBlank(branchInput)) {
					branchInput = branchInput.trim();
					for (String branch: branches) {
						if (branch.contains(branchInput))
							filteredBranches.add(branch);
					}
				} else {
					branchInput = null;
					filteredBranches.addAll(branches);
				}
				
				if (activeBranchIndex >= filteredBranches.size())
					activeBranchIndex = 0;
				target.add(branchesContainer);
				target.add(noBranchesContainer);
			}
			
		});
		
		branchesContainer.add(new ListView<String>("branches", filteredBranches) {

			@Override
			protected void populateItem(final ListItem<String> item) {
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, item.getModelObject());
					}

				};
				link.add(new Label("label", item.getModelObject()));
				if (item.getModelObject().equals(branch))
					link.add(AttributeAppender.append("class", " current"));
				item.add(link);
				
				if (activeBranchIndex == item.getIndex())
					item.add(AttributeAppender.append("class", " active"));
			}
			
		});
		
		branchesContainer.setOutputMarkupPlaceholderTag(true);
		add(branchesContainer);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new BranchSelectorResourceReference()));
	}

	protected abstract void onSelect(AjaxRequestTarget target, String branch);

	@Override
	protected void onDetach() {
		projectModel.detach();
		
		super.onDetach();
	}
	
}
