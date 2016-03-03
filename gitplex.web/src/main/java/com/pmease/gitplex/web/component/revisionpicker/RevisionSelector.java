package com.pmease.gitplex.web.component.revisionpicker;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
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
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Throwables;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.behavior.FormComponentInputBehavior;
import com.pmease.commons.wicket.component.modal.ModalPanel;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.security.SecurityUtils;

@SuppressWarnings("serial")
public abstract class RevisionSelector extends Panel {
	
	private final IModel<Depot> depotModel;
	
	private static final String COMMIT_FLAG = "*";
	
	private static final String ADD_FLAG = "~";
	
	private final String revision;
	
	private final boolean canCreateBranch;
	
	private final boolean canCreateTag;

	private boolean branchesActive;
	
	private int activeRefIndex;
	
	private String revInput;
	
	private Label feedback;
	
	private String feedbackMessage;
	
	private AbstractDefaultAjaxBehavior keyBehavior;
	
	private TextField<String> revField;

	private List<String> refs;
	
	private List<String> filteredRefs;
	
	private List<String> findRefs() {
		List<String> names = new ArrayList<>();
		
		if (branchesActive) {
			for (Ref ref: depotModel.getObject().getBranchRefs())
				names.add(GitUtils.ref2branch(ref.getName()));
		} else {
			for (Ref ref: depotModel.getObject().getTagRefs())
				names.add(GitUtils.ref2tag(ref.getName()));
		}
		return names;
	}
	
	private void onSelectTab(AjaxRequestTarget target) {
		refs.clear();
		refs.addAll(findRefs());
		filteredRefs.clear();
		filteredRefs.addAll(refs);
		revField.setModel(Model.of(""));
		activeRefIndex = 0;
		Component revisionList = newRefList(filteredRefs);
		replace(revisionList);
		target.add(revisionList);
		target.add(revField);
		String script = String.format("gitplex.revisionSelector.bindInputKeys('%s', %s);", 
				getMarkupId(true), keyBehavior.getCallbackFunction(CallbackParameter.explicit("key")));
		target.appendJavaScript(script);
		target.focusComponent(revField);
	}

	public RevisionSelector(String id, IModel<Depot> depotModel, String revision, boolean canCreateRef) {
		super(id);
		
		this.depotModel = depotModel;
		this.revision = revision;		
		if (canCreateRef) {
			Depot depot = depotModel.getObject();
			ObjectId commitId = depot.getRevCommit(revision);
			canCreateBranch = SecurityUtils.canPushRef(depot, Constants.R_HEADS, ObjectId.zeroId(), commitId);						
			canCreateTag = SecurityUtils.canPushRef(depot, Constants.R_TAGS, ObjectId.zeroId(), commitId);						
		} else {
			canCreateBranch = false;
			canCreateTag = false;
		}
		Ref ref = depotModel.getObject().getRef(revision);
		branchesActive = ref == null || GitUtils.ref2tag(ref.getName()) == null;
		
		refs = findRefs();
		filteredRefs = new ArrayList<>(refs);
	}
	
	public RevisionSelector(String id, IModel<Depot> depotModel, String revision) {
		this(id, depotModel, revision, false);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		revField = new TextField<String>("revision", Model.of(""));
		revField.add(AttributeModifier.replace("placeholder", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (branchesActive) {
					if (canCreateBranch) {
						return "Find or create branch";
					} else {
						return "Find branch";
					}
				} else {
					if (canCreateTag) {
						return "Find or create tag";
					} else {
						return "Find tag";
					}
				}
			}
			
		}));
		revField.setOutputMarkupId(true);
		add(revField);
		
		feedback = new Label("feedback", new PropertyModel<String>(RevisionSelector.this, "feedbackMessage")) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(feedbackMessage != null);
			}
			
		};
		feedback.setOutputMarkupPlaceholderTag(true);
		add(feedback);
		
		keyBehavior = new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
				String key = params.getParameterValue("key").toString();
				
				if (key.equals("return")) {
					if (!filteredRefs.isEmpty()) {
						String activeRef = filteredRefs.get(activeRefIndex);
						if (activeRef.startsWith(COMMIT_FLAG)) {
							selectRevision(target, activeRef.substring(COMMIT_FLAG.length()));
						} else if (activeRef.startsWith(ADD_FLAG)) {
							activeRef = activeRef.substring(ADD_FLAG.length());
							onCreateRef(target, activeRef);
						} else {
							selectRevision(target, activeRef);
						}
					} else if (revInput != null) { 
						selectRevision(target, revInput);
					}
				} else if (key.equals("up")) {
					activeRefIndex--;
				} else if (key.equals("down")) {
					activeRefIndex++;
				} else {
					throw new IllegalStateException("Unrecognized key: " + key);
				}
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				String script = String.format("gitplex.revisionSelector.init('%s', %s);", 
						getMarkupId(true), getCallbackFunction(CallbackParameter.explicit("key")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		};
		add(keyBehavior);
		
		revField.add(new FormComponentInputBehavior() {
			
			@Override
			protected void onInput(AjaxRequestTarget target) {
				revInput = revField.getInput();
				filteredRefs.clear();
				if (StringUtils.isNotBlank(revInput)) {
					revInput = revInput.trim().toLowerCase();
					boolean found = false;
					for (String ref: refs) {
						if (ref.equalsIgnoreCase(revInput))
							found = true;
						if (ref.toLowerCase().contains(revInput))
							filteredRefs.add(ref);
					}
					if (!found) {
						Depot depot = depotModel.getObject();
						if (depot.getRevCommit(revInput, false) != null) {
							filteredRefs.add(COMMIT_FLAG + revInput);
						} else if (branchesActive) {
							String refName = Constants.R_HEADS + revInput;
							if (Repository.isValidRefName(refName) 
									&& SecurityUtils.canPushRef(depot, refName, ObjectId.zeroId(), depot.getRevCommit(revision))) { 
								filteredRefs.add(ADD_FLAG + revInput);
							}
						} else {
							String refName = Constants.R_TAGS + revInput;
							if (Repository.isValidRefName(refName) 
									&& SecurityUtils.canPushRef(depot, refName, ObjectId.zeroId(), depot.getRevCommit(revision))) { 
								filteredRefs.add(ADD_FLAG + revInput);
							}
						}
					}
				} else {
					revInput = null;
					filteredRefs.addAll(refs);
				}
				
				if (activeRefIndex >= filteredRefs.size())
					activeRefIndex = 0;
				target.add(get("refs"));
			}
			
		});
		
		List<Tab> tabs = new ArrayList<>();
		AjaxActionTab branchesTab;
		tabs.add(branchesTab = new AjaxActionTab(Model.of("branches")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				branchesActive = true;
				onSelectTab(target);
			}
			
		});
		AjaxActionTab tagsTab;
		tabs.add(tagsTab = new AjaxActionTab(Model.of("tags")) {

			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				branchesActive = false;
				onSelectTab(target);
			}
			
		});
		if (branchesActive)
			branchesTab.setSelected(true);
		else
			tagsTab.setSelected(true);
		
		add(new Tabbable("tabs", tabs));
		add(newRefList(filteredRefs));
		
		setOutputMarkupId(true);
	}
	
	@Nullable
	protected String getRevisionUrl(String revision) {
		return null;
	}
	
	private void onCreateRef(AjaxRequestTarget target, final String refName) {
		if (branchesActive) {
			depotModel.getObject().createBranch(refName, revision);
			selectRevision(target, refName);
		} else {
			ModalPanel modal = new ModalPanel(target) {

				@Override
				protected Component newContent(String id) {
					return new CreateTagPanel(id, depotModel, refName, revision) {

						@Override
						protected void onCreate(AjaxRequestTarget target, String tagName) {
							close(target);
							onSelect(target, tagName);
						}

						@Override
						protected void onCancel(AjaxRequestTarget target) {
							close(target);
						}
						
					};
				}
				
			};
			onModalOpened(target, modal);
		}		
	}

	protected void onModalOpened(AjaxRequestTarget target, ModalPanel modal) {
		
	}
	
	private Component newRefList(List<String> refs) {
		WebMarkupContainer refsContainer = new WebMarkupContainer("refs");
		refsContainer.add(new ListView<String>("refs", refs) {

			@Override
			protected void populateItem(final ListItem<String> item) {
				final String ref;
				if (item.getModelObject().startsWith(COMMIT_FLAG))
					ref = item.getModelObject().substring(COMMIT_FLAG.length());
				else if (item.getModelObject().startsWith(ADD_FLAG))
					ref = item.getModelObject().substring(ADD_FLAG.length());
				else
					ref = item.getModelObject();
				
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						if (item.getModelObject().startsWith(ADD_FLAG)) {
							onCreateRef(target, ref);
						} else {
							selectRevision(target, ref);
						}
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						if (!item.getModelObject().startsWith(ADD_FLAG)) {
							String url = getRevisionUrl(ref);
							if (url != null)
								tag.put("href", url);
						}
					}
					
				};
				if (item.getModelObject().startsWith(COMMIT_FLAG)) {
					link.add(new Label("label", ref));
					link.add(AttributeAppender.append("class", "icon commit"));
				} else if (item.getModelObject().startsWith(ADD_FLAG)) {
					String label;
					if (branchesActive)
						label = "<div class='name'>Create branch <b>" + HtmlEscape.escapeHtml5(ref) + "</b></div>";
					else
						label = "<div class='name'>Create tag <b>" + HtmlEscape.escapeHtml5(ref) + "</b></div>";
					label += "<div class='revision'>from " + HtmlEscape.escapeHtml5(revision) + "</div>";
					link.add(new Label("label", label).setEscapeModelStrings(false));
					link.add(AttributeAppender.append("class", "icon add"));
				} else if (ref.equals(revision)) {
					link.add(new Label("label", ref));
					link.add(AttributeAppender.append("class", "icon current"));
				} else {
					link.add(new Label("label", ref));
				}
				item.add(link);
				
				if (activeRefIndex == item.getIndex())
					item.add(AttributeAppender.append("class", " active"));
			}
			
		});
		refsContainer.setOutputMarkupId(true);
		return refsContainer;
	}
	
	private void selectRevision(AjaxRequestTarget target, String revision) {
		try {
			if (depotModel.getObject().getRevCommit(revision, false) != null) {
				onSelect(target, revision);
			} else {
				feedbackMessage = "Can not find commit of revision " + revision + "";
				target.add(feedback);
			}
		} catch (Exception e) {
			feedbackMessage = Throwables.getRootCause(e).getMessage();
			target.add(feedback);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));

		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(RevisionSelector.class, "revision-selector.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RevisionSelector.class, "revision-selector.css")));
	}

	protected abstract void onSelect(AjaxRequestTarget target, String revision);

	@Override
	protected void onDetach() {
		depotModel.detach();
		
		super.onDetach();
	}
	
}
