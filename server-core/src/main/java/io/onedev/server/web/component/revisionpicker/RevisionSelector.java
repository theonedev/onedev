package io.onedev.server.web.component.revisionpicker;

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
import org.eclipse.jgit.lib.Ref;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.InputChangeBehavior;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.component.createtag.CreateTagPanel;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;

@SuppressWarnings("serial")
public abstract class RevisionSelector extends Panel {

	private static final int PAGE_SIZE = 25;
	
	private final IModel<Project> projectModel;
	
	private static final String COMMIT_FLAG = "*";
	
	private static final String ADD_FLAG = "~";
	
	private final String revision;
	
	private final boolean canCreateBranch;
	
	private final boolean canCreateTag;

	private boolean branchesActive;
	
	private Label feedback;
	
	private String feedbackMessage;
	
	private TextField<String> revField;
	
	private String revInput;
	
	private List<String> refs;
	
	private List<String> findRefs() {
		List<String> names = new ArrayList<>();
		
		if (branchesActive) {
			for (RefInfo ref: projectModel.getObject().getBranchRefInfos())
				names.add(GitUtils.ref2branch(ref.getRef().getName()));
		} else {
			for (RefInfo ref: projectModel.getObject().getTagRefInfos())
				names.add(GitUtils.ref2tag(ref.getRef().getName()));
		}
		return names;
	}
	
	private void onSelectTab(AjaxRequestTarget target) {
		refs = findRefs();
		revField.setModel(Model.of(""));
		revInput = null;
		target.add(revField);
		newItemsView(target);
		String script = String.format("onedev.server.revisionSelector.bindInputKeys('%s');", getMarkupId(true));
		target.appendJavaScript(script);
		target.focusComponent(revField);
	}

	public RevisionSelector(String id, IModel<Project> projectModel, @Nullable String revision, boolean canCreateRef) {
		super(id);
		
		Preconditions.checkArgument(revision!=null || !canCreateRef);
	
		this.projectModel = projectModel;
		this.revision = revision;		
		if (canCreateRef) {
			Project project = projectModel.getObject();
			canCreateBranch = SecurityUtils.canCreateBranch(project, Constants.R_HEADS);						
			canCreateTag = SecurityUtils.canCreateTag(project, Constants.R_TAGS);						
		} else {
			canCreateBranch = false;
			canCreateTag = false;
		}
		if (revision != null) {
			Ref ref = projectModel.getObject().getRef(revision);
			branchesActive = ref == null || GitUtils.ref2tag(ref.getName()) == null;
		} else {
			branchesActive = true;
		}
		
		refs = findRefs();
	}
	
	public RevisionSelector(String id, IModel<Project> projectModel, @Nullable String revision) {
		this(id, projectModel, revision, false);
	}

	public RevisionSelector(String id, IModel<Project> projectModel) {
		this(id, projectModel, null, false);
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
				} else if (StringUtils.isNotBlank(revInput)) { 
					selectRevision(target, revInput.trim());
				}
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				String script = String.format("onedev.server.revisionSelector.init('%s', %s);", 
						getMarkupId(true), getCallbackFunction(explicit("value")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		});
		
		revField.add(new InputChangeBehavior() {
			
			@Override
			protected void onInputChange(AjaxRequestTarget target) {
				revInput = revField.getInput();
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
		
		newItemsView(null);
		
		setOutputMarkupId(true);
	}
	
	@Nullable
	protected String getRevisionUrl(String revision) {
		return null;
	}
	
	private List<String> getItemValues(String revInput, int count) {
		List<String> itemValues = new ArrayList<>();
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
				Project project = projectModel.getObject();
				try {
					if (project.getRepository().resolve(revInput) != null) {
						itemValues.add(COMMIT_FLAG + revInput);
					} else if (branchesActive) {
						if (canCreateBranch) {
							if (SecurityUtils.canCreateBranch(project, revInput))
								itemValues.add(ADD_FLAG + revInput);
						}
					} else {
						if (canCreateTag) {
							if (SecurityUtils.canCreateTag(project, revInput)) { 
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
			projectModel.getObject().createBranch(refName, revision);
			selectRevision(target, refName);
		} else {
			ModalPanel modal = new ModalPanel(target) {

				@Override
				protected Component newContent(String id) {
					return new CreateTagPanel(id, projectModel, refName, revision) {

						@Override
						protected void onCreate(AjaxRequestTarget target, String tagName) {
							close();
							onSelect(target, tagName);
						}

						@Override
						protected void onCancel(AjaxRequestTarget target) {
							close();
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
		
		AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

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
		
		String icon;
		if (itemValue.startsWith(COMMIT_FLAG)) {
			link.add(new Label("label", ref));
			icon = "commit";
		} else if (itemValue.startsWith(ADD_FLAG)) {
			String label;
			if (branchesActive)
				label = "Create branch <b>" + HtmlEscape.escapeHtml5(ref) + "</b>";
			else
				label = "Create tag <b>" + HtmlEscape.escapeHtml5(ref) + "</b>";
			label += " from " + HtmlEscape.escapeHtml5(revision);
			link.add(new Label("label", label).setEscapeModelStrings(false));
			icon = "plus";
		} else if (ref.equals(revision)) {
			link.add(new Label("label", ref));
			icon = "tick";
		} else {
			link.add(new Label("label", ref));
			icon = null;
		}
		if (icon != null)
			link.add(new SpriteImage("icon", icon));
		else
			link.add(new WebMarkupContainer("icon"));
		WebMarkupContainer item = new WebMarkupContainer(itemId);
		item.setOutputMarkupId(true);
		item.add(AttributeAppender.append("data-value", HtmlEscape.escapeHtml5(itemValue)));
		item.add(link);
		
		return item;
	}
	
	private void newItemsView(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer itemsContainer = new WebMarkupContainer("items");
		itemsContainer.setOutputMarkupId(true);
		RepeatingView itemsView;
		itemsContainer.add(itemsView = new RepeatingView("items"));
		List<String> itemValues = getItemValues(revInput, PAGE_SIZE);
		for (int i=0; i<itemValues.size(); i++) {
			Component item = newItem(itemsView.newChildId(), itemValues.get(i));
			if (i == 0)
				item.add(AttributeAppender.append("class", "active"));
			itemsView.add(item);
		}
		itemsContainer.add(new Label("noItems", branchesActive? "No branches found": "No tags found") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(itemsView.size() == 0);
			}
			
		});
		
		itemsContainer.add(new InfiniteScrollBehavior(PAGE_SIZE) {

			@Override
			protected void appendMore(AjaxRequestTarget target, int offset, int count) {
				List<String> itemValues = getItemValues(revInput, offset+count);
				if (itemValues.size() > offset) {
					List<String> newItemValues = itemValues.subList(offset, itemValues.size());
					for (String itemValue: newItemValues) {
						Component item = newItem(itemsView.newChildId(), itemValue);
						itemsView.add(item);
						String script = String.format("$('#%s').append('<li id=\"%s\"></li>');", 
								itemsContainer.getMarkupId(), item.getMarkupId());
						target.prependJavaScript(script);
						target.add(item);
					}
				}
			}
			
		});
		
		if (target != null) {
			replace(itemsContainer);
			target.add(itemsContainer);
		} else {
			add(itemsContainer);
		}
	}
	
	private void selectRevision(AjaxRequestTarget target, String revision) {
		try {
			if (projectModel.getObject().getRevCommit(revision, false) != null) {
				onSelect(target, revision);
			} else {
				feedbackMessage = "Can not find commit of revision " + revision + "";
				target.add(feedback);
			}
		} catch (Exception e) {
			// revision selector might be closed in onSelect handler
			if (findPage() != null) {
				feedbackMessage = Throwables.getRootCause(e).getMessage();
				target.add(feedback);
			} else {
				throw ExceptionUtils.unchecked(e);
			}
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
		projectModel.detach();
		
		super.onDetach();
	}
	
}
