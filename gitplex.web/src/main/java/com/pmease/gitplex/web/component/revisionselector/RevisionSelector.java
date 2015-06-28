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

import com.google.common.base.Throwables;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.behavior.FormComponentInputBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public abstract class RevisionSelector extends Panel {
	
	private final IModel<Repository> repoModel;
	
	private final String revision;
	
	private boolean branchesActive = true;
	
	private int activeRefIndex;
	
	private DropdownPanel dropdown;
	
	private String revInput;
	
	private Label feedback;
	
	private String feedbackMessage;
	
	public RevisionSelector(String id, IModel<Repository> repoModel, String revision) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer button = new WebMarkupContainer("button");
		add(button);
		
		button.add(new WebMarkupContainer("icon").add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				try (FileRepository jgitRepo = repoModel.getObject().openAsJGitRepo()) {
					if (jgitRepo.getRefDatabase().getRef(Git.REFS_HEADS + revision) != null)
						return "fa fa-ext fa-branch";
					else if (jgitRepo.getRefDatabase().getRef(Git.REFS_TAGS + revision) != null)
						return "fa fa-tag";
					else
						return "fa fa-ext fa-commit";
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		})));
		button.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (GitUtils.isHash(revision))
					return GitUtils.abbreviateSHA(revision);
				else
					return revision;
			}
			
		}));
		
		dropdown = new DropdownPanel("dropdown", true) {

			private Fragment fragment;
			
			private AbstractDefaultAjaxBehavior keyBehavior;
			
			private TextField<String> revField;

			private List<String> refs = findRefs();
			
			private List<String> filteredRefs = new ArrayList<>(refs);
			
			private List<String> findRefs() {
				List<String> refs = new ArrayList<>();
				
				if (branchesActive)
					refs.addAll(repoModel.getObject().getRefs(Git.REFS_HEADS).keySet());
				else
					refs.addAll(repoModel.getObject().getRefs(Git.REFS_TAGS).keySet());
				Collections.sort(refs);
				return refs;
			}
			
			private void onSelectTab(AjaxRequestTarget target) {
				refs.clear();
				refs.addAll(findRefs());
				filteredRefs.clear();
				filteredRefs.addAll(refs);
				revField.setModel(Model.of(""));
				activeRefIndex = 0;
				Component revisionList = newRefList(this, filteredRefs);
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
						response.render(JavaScriptHeaderItem.forReference(HotkeysResourceReference.INSTANCE));

						response.render(JavaScriptHeaderItem.forReference(
								new JavaScriptResourceReference(RevisionSelector.class, "revision-selector.js")));
						response.render(CssHeaderItem.forReference(
								new CssResourceReference(RevisionSelector.class, "revision-selector.css")));
						
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
				tabs.add(new AjaxActionTab(Model.of("branches")) {
					
					@Override
					protected void onSelect(AjaxRequestTarget target, Component tabLink) {
						branchesActive = true;
						onSelectTab(target);
					}
					
				});
				tabs.add(new AjaxActionTab(Model.of("tags")) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Component tabLink) {
						branchesActive = false;
						onSelectTab(target);
					}
					
				});
				
				fragment.add(new Tabbable("tabs", tabs));
				fragment.add(newRefList(this, filteredRefs));
				
				return fragment;
			}
			
		};
		add(dropdown);
		
		button.add(new DropdownBehavior(dropdown).alignWithTrigger(0, 100, 0, 0, 6, true));
		
		setOutputMarkupId(true);
	}
	
	private Component newRefList(final DropdownPanel dropdown, List<String> refs) {
		WebMarkupContainer refsContainer = new WebMarkupContainer("refs");
		refsContainer.add(new ListView<String>("refs", refs) {

			@Override
			protected void populateItem(final ListItem<String> item) {
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						selectRevision(target, item.getModelObject());
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						PageParameters params = RepoFilePage.paramsOf(repoModel.getObject(), item.getModelObject(), null);
						tag.put("href", urlFor(RepoFilePage.class, params));
					}
					
				};
				link.add(new Label("label", item.getModelObject()));
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
				dropdown.hide(target);
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
	
	protected abstract void onSelect(AjaxRequestTarget target, String revision);

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}
	
}
