package com.pmease.gitplex.web.component.revisionpicker;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.RefInfo;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.behavior.AbstractPostAjaxBehavior;
import com.pmease.commons.wicket.behavior.InputChangeBehavior;
import com.pmease.commons.wicket.component.PreventDefaultAjaxLink;
import com.pmease.commons.wicket.component.modal.ModalPanel;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.security.SecurityUtils;

@SuppressWarnings("serial")
public abstract class RevisionSelector extends Panel {

	private static final int PAGE_SIZE = 25;
	
	private final IModel<Depot> depotModel;
	
	private static final String COMMIT_FLAG = "*";
	
	private static final String ADD_FLAG = "~";
	
	private final String revision;
	
	private final boolean canCreateBranch;
	
	private final boolean canCreateTag;

	private boolean branchesActive;
	
	private Label feedback;
	
	private String feedbackMessage;
	
	private TextField<String> revField;
	
	private WebMarkupContainer itemsContainer;
	
	private RepeatingView itemsView;

	private List<String> refs;
	
	private List<String> itemValues;
	
	private int page = 1;
	
	private List<String> findRefs() {
		List<String> names = new ArrayList<>();
		
		if (branchesActive) {
			for (RefInfo ref: depotModel.getObject().getBranches())
				names.add(GitUtils.ref2branch(ref.getRef().getName()));
		} else {
			for (RefInfo ref: depotModel.getObject().getTags())
				names.add(GitUtils.ref2tag(ref.getRef().getName()));
		}
		return names;
	}
	
	private void onSelectTab(AjaxRequestTarget target) {
		refs = findRefs();
		page = 1;
		itemValues = getItemValues(null);
		revField.setModel(Model.of(""));
		target.add(revField);
		newItemsView(target);
		String script = String.format("gitplex.revisionSelector.bindInputKeys('%s');", getMarkupId(true));
		target.appendJavaScript(script);
		target.focusComponent(revField);
	}

	public RevisionSelector(String id, IModel<Depot> depotModel, @Nullable String revision, boolean canCreateRef) {
		super(id);
		
		Preconditions.checkArgument(revision!=null || !canCreateRef);
	
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
		if (revision != null) {
			Ref ref = depotModel.getObject().getRef(revision);
			branchesActive = ref == null || GitUtils.ref2tag(ref.getName()) == null;
		} else {
			branchesActive = true;
		}
		
		refs = findRefs();
		itemValues = getItemValues(null);
	}
	
	public RevisionSelector(String id, IModel<Depot> depotModel, @Nullable String revision) {
		this(id, depotModel, revision, false);
	}

	public RevisionSelector(String id, IModel<Depot> depotModel) {
		this(id, depotModel, null, false);
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
		
		add(new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
				String key = params.getParameterValue("key").toString();
				
				if (key.equals("return")) {
					String value = params.getParameterValue("value").toString();
					if (StringUtils.isNotBlank(value)) {
						if (value.startsWith(COMMIT_FLAG)) {
							selectRevision(target, value.substring(COMMIT_FLAG.length()));
						} else if (value.startsWith(ADD_FLAG)) {
							value = value.substring(ADD_FLAG.length());
							onCreateRef(target, value);
						} else {
							selectRevision(target, value);
						}
					} else if (StringUtils.isNotBlank(revField.getInput())) { 
						selectRevision(target, revField.getInput().trim());
					}
				} else if (key.equals("load")) {
					page++;
					List<String> itemValues = getItemValues(revField.getInput());
					if (itemValues.size() > RevisionSelector.this.itemValues.size()) {
						List<String> additionalItemValues = 
								itemValues.subList(RevisionSelector.this.itemValues.size(), itemValues.size());
						for (String itemValue: additionalItemValues) {
							Component item = newItem(itemsView.newChildId(), itemValue);
							itemsView.add(item);
							String script = String.format("$('#%s').append('<li id=\"%s\"></li>');", 
									itemsContainer.getMarkupId(), item.getMarkupId());
							target.prependJavaScript(script);
							target.add(item);
						}
						RevisionSelector.this.itemValues = itemValues;
					}
					
				} else {
					throw new IllegalStateException("Unrecognized key: " + key);
				}
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				String script = String.format("gitplex.revisionSelector.init('%s', %s);", 
						getMarkupId(true), getCallbackFunction(explicit("key"), explicit("value")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		revField.add(new InputChangeBehavior() {
			
			@Override
			protected void onInputChange(AjaxRequestTarget target) {
				page = 1;
				itemValues = getItemValues(revField.getInput());
				newItemsView(target);
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
		
		itemsContainer = new WebMarkupContainer("items");
		itemsContainer.setOutputMarkupId(true);
		add(itemsContainer);
		newItemsView(null);
		
		setOutputMarkupId(true);
	}
	
	@Nullable
	protected String getRevisionUrl(String revision) {
		return null;
	}
	
	private List<String> getItemValues(String revInput) {
		List<String> itemValues = new ArrayList<>();
		int count = page*PAGE_SIZE;
		if (StringUtils.isNotBlank(revInput)) {
			revInput = revInput.trim().toLowerCase();
			boolean found = false;
			for (String ref: refs) {
				if (ref.equalsIgnoreCase(revInput))
					found = true;
				if (itemValues.size() < count && ref.toLowerCase().contains(revInput))
					itemValues.add(ref);
			}
			if (itemValues.size() < count && !found) {
				Depot depot = depotModel.getObject();
				try {
					if (depot.getRepository().resolve(revInput) != null) {
						itemValues.add(COMMIT_FLAG + revInput);
					} else if (branchesActive) {
						if (canCreateBranch) {
							String refName = Constants.R_HEADS + revInput;
							if (SecurityUtils.canPushRef(depot, refName, ObjectId.zeroId(), depot.getRevCommit(revision))) { 
								itemValues.add(ADD_FLAG + revInput);
							}
						}
					} else {
						if (canCreateTag) {
							String refName = Constants.R_TAGS + revInput;
							if (SecurityUtils.canPushRef(depot, refName, ObjectId.zeroId(), depot.getRevCommit(revision))) { 
								itemValues.add(ADD_FLAG + revInput);
							}
						}
					}
				} catch (RevisionSyntaxException | AmbiguousObjectException | IncorrectObjectTypeException e) {
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} else {
			if (refs.size() > count)
				itemValues.addAll(refs.subList(0, count));
			else
				itemValues.addAll(refs);
		}
		return itemValues;
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
	
	private Component newItem(String itemId, String itemValue) {
		String ref;
		if (itemValue.startsWith(COMMIT_FLAG))
			ref = itemValue.substring(COMMIT_FLAG.length());
		else if (itemValue.startsWith(ADD_FLAG))
			ref = itemValue.substring(ADD_FLAG.length());
		else
			ref = itemValue;
		
		AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("link") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				if (itemValue.startsWith(ADD_FLAG)) {
					onCreateRef(target, ref);
				} else {
					selectRevision(target, ref);
				}
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (!itemValue.startsWith(ADD_FLAG)) {
					String url = getRevisionUrl(ref);
					if (url != null)
						tag.put("href", url);
				}
			}
			
		};
		if (itemValue.startsWith(COMMIT_FLAG)) {
			link.add(new Label("label", ref));
			link.add(AttributeAppender.append("class", "icon commit"));
		} else if (itemValue.startsWith(ADD_FLAG)) {
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
		WebMarkupContainer item = new WebMarkupContainer(itemId);
		item.setOutputMarkupId(true);
		item.add(AttributeAppender.append("data-value", HtmlEscape.escapeHtml5(itemValue)));
		item.add(link);
		
		return item;
	}
	
	private void newItemsView(@Nullable AjaxRequestTarget target) {
		itemsView = new RepeatingView("items");
		for (int i=0; i<itemValues.size(); i++) {
			Component item = newItem(itemsView.newChildId(), itemValues.get(i));
			if (i == 0)
				item.add(AttributeAppender.append("class", "active"));
			itemsView.add(item);
		}
		if (target != null) {
			itemsContainer.replace(itemsView);
			target.add(itemsContainer);
			String script = String.format("gitplex.revisionSelector.setupInfiniteScroll('%s');", 
					getMarkupId(true));
			target.appendJavaScript(script);
		} else {
			itemsContainer.add(itemsView);
		}
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

		response.render(JavaScriptHeaderItem.forReference(new RevisionSelectorResourceReference()));
	}

	protected abstract void onSelect(AjaxRequestTarget target, String revision);

	@Override
	protected void onDetach() {
		depotModel.detach();
		
		super.onDetach();
	}
	
}
