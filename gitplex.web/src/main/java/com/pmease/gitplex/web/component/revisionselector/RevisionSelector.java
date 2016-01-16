package com.pmease.gitplex.web.component.revisionselector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;

import com.google.common.base.Throwables;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.behavior.FormComponentInputBehavior;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.file.RepoFileState;

@SuppressWarnings("serial")
public abstract class RevisionSelector extends Panel {
	
	private final IModel<Repository> repoModel;
	
	private final String revision;
	
	private boolean branchesActive = true;
	
	private int activeRefIndex;
	
	protected DropdownLink dropdownLink;
	
	private String revInput;
	
	private Label feedback;
	
	private String feedbackMessage;
	
	public RevisionSelector(String id, IModel<Repository> repoModel, String revision) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;		
		Ref ref = repoModel.getObject().getRef(revision);
		if (ref != null && GitUtils.ref2tag(ref.getName()) != null)
			branchesActive = false;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		dropdownLink = new DropdownLink("button") {

			private Fragment fragment;
			
			private AbstractDefaultAjaxBehavior keyBehavior;
			
			private TextField<String> revField;

			private List<String> refs = findRefs();
			
			private List<String> filteredRefs = new ArrayList<>(refs);
			
			private List<String> findRefs() {
				List<String> names = new ArrayList<>();
				
				List<Ref> refs = new ArrayList<>();
				
				Repository repo = repoModel.getObject();
				if (branchesActive) {
					refs.addAll(repo.getRefs(Constants.R_HEADS).values());
					Collections.sort(refs, repo.newBranchDateComparator());
					for (Ref ref: refs)
						names.add(GitUtils.ref2branch(ref.getName()));
				} else {
					for (Ref ref: repo.getTagRefs()) {
						if (repo.getRevCommit(ref.getObjectId()) != null) 
							refs.add(ref);
					}
					Collections.sort(refs, repo.newTagDateComparator());
					for (Ref ref: refs)
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
				fragment.replace(revisionList);
				target.add(revisionList);
				target.add(revField);
				String script = String.format("gitplex.revisionSelector.bindInputKeys('%s', %s);", 
						fragment.getMarkupId(true), keyBehavior.getCallbackFunction(CallbackParameter.explicit("key")));
				target.appendJavaScript(script);
				target.focusComponent(revField);
			}

			@Override
			protected Component newContent(String id) {
				fragment = new Fragment(id, "dropdownContentFrag", RevisionSelector.this);
				
				revField = new TextField<String>("revision", Model.of(""));
				revField.add(AttributeModifier.replace("placeholder", new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						if (branchesActive)
							return "Input branches or commit hash";
						else
							return "Input tags or commit hash";
					}
					
				}));
				revField.setOutputMarkupId(true);
				fragment.add(revField);
				
				feedback = new Label("feedback", new PropertyModel<String>(RevisionSelector.this, "feedbackMessage")) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(feedbackMessage != null);
					}
					
				};
				feedback.setOutputMarkupPlaceholderTag(true);
				fragment.add(feedback);
				
				keyBehavior = new AbstractDefaultAjaxBehavior() {
					
					@Override
					protected void respond(AjaxRequestTarget target) {
						IRequestParameters params = RequestCycle.get().getRequest().getQueryParameters();
						String key = params.getParameterValue("key").toString();
						
						if (key.equals("return")) {
							if (!filteredRefs.isEmpty()) 
								selectRevision(target, filteredRefs.get(activeRefIndex));
							else if (revInput != null) 
								selectRevision(target, revInput);
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
								fragment.getMarkupId(true), getCallbackFunction(CallbackParameter.explicit("key")));
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				};
				fragment.add(keyBehavior);
				
				revField.add(new FormComponentInputBehavior() {
					
					@Override
					protected void onInput(AjaxRequestTarget target) {
						revInput = revField.getInput();
						filteredRefs.clear();
						if (StringUtils.isNotBlank(revInput)) {
							revInput = revInput.trim();
							for (String ref: refs) {
								if (ref.contains(revInput))
									filteredRefs.add(ref);
							}
						} else {
							revInput = null;
							filteredRefs.addAll(refs);
						}
						
						if (activeRefIndex >= filteredRefs.size())
							activeRefIndex = 0;
						target.add(fragment.get("refs"));
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
				
				fragment.add(new Tabbable("tabs", tabs));
				fragment.add(newRefList(filteredRefs));
				
				return fragment;
			}
			
		};
		add(dropdownLink);
		
		dropdownLink.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				try (FileRepository jgitRepo = repoModel.getObject().openAsJGitRepo()) {
					if (jgitRepo.getRefDatabase().getRef(Constants.R_HEADS + revision) != null)
						return "fa fa-ext fa-branch";
					else if (jgitRepo.getRefDatabase().getRef(Constants.R_TAGS + revision) != null)
						return "fa fa-tag";
					else
						return "fa fa-ext fa-commit";
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		})));
		dropdownLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (GitUtils.isHash(revision))
					return GitUtils.abbreviateSHA(revision);
				else
					return revision;
			}
			
		}));
		
		setOutputMarkupId(true);
	}
	
	private Component newRefList(List<String> refs) {
		WebMarkupContainer refsContainer = new WebMarkupContainer("refs");
		refsContainer.add(new ListView<String>("refs", refs) {

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
						selectRevision(target, item.getModelObject());
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						RepoFileState state = new RepoFileState();
						state.blobIdent.revision = item.getModelObject();
						PageParameters params = RepoFilePage.paramsOf(repoModel.getObject(), state);
						tag.put("href", urlFor(RepoFilePage.class, params));
					}
					
				};
				link.add(new Label("label", item.getModelObject()));
				if (item.getModelObject().equals(revision))
					link.add(AttributeAppender.append("class", " current"));
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
			if (repoModel.getObject().getObjectId(revision, false) != null) {
				dropdownLink.close(target);
				onSelect(target, revision);
			} else {
				feedbackMessage = "Can not find revision " + revision + "";
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
		repoModel.detach();
		
		super.onDetach();
	}
	
}
