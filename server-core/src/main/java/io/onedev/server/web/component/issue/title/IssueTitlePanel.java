package io.onedev.server.web.component.issue.title;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.issue.fieldspec.DateField;
import io.onedev.server.issue.fieldspec.FieldSpec;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.util.Input;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.behavior.ReferenceInputBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import io.onedev.server.web.util.ReferenceTransformer;

@SuppressWarnings("serial")
public abstract class IssueTitlePanel extends Panel {

	private static final String CONTENT_ID = "content";
	
	private final boolean withIssueCreation;
	
	public IssueTitlePanel(String id, boolean withIssueCreation) {
		super(id);
		this.withIssueCreation = withIssueCreation;
	}

	private Fragment newTitleEditor() {
		Fragment titleEditor = new Fragment(CONTENT_ID, "titleEditFrag", this);
		Form<?> form = new Form<Void>("form");
		TextField<String> titleInput = new TextField<String>("title", Model.of(getIssue().getTitle()));
		titleInput.add(new ReferenceInputBehavior(false) {

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}
			
		});
		titleInput.setRequired(true);
		titleInput.setLabel(Model.of("Title"));
		
		form.add(titleInput);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				OneDev.getInstance(IssueChangeManager.class).changeTitle(getIssue(), titleInput.getModelObject());
				
				Fragment titleViewer = newTitleViewer();
				titleEditor.replaceWith(titleViewer);
				target.add(titleViewer);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(titleEditor);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment titleViewer = newTitleViewer();
				titleEditor.replaceWith(titleViewer);
				target.add(titleViewer);
			}
			
		});		
		
		titleEditor.add(form);
		
		form.add(new NotificationPanel("feedback", form));
		titleEditor.setOutputMarkupId(true);
		
		return titleEditor;
	}
	
	private Fragment newTitleViewer() {
		Fragment titleViewer = new Fragment(CONTENT_ID, "titleViewFrag", this);
		titleViewer.add(new Label("title", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				ReferenceTransformer transformer = new ReferenceTransformer(getIssue().getProject(), null);
				return "#" + getIssue().getNumber() + "&nbsp;&nbsp;" + transformer.apply(getIssue().getTitle());
			}
			
		}).setEscapeModelStrings(false));
		
		titleViewer.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment titleEditor = newTitleEditor();
				titleViewer.replaceWith(titleEditor);
				target.add(titleEditor);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(SecurityUtils.canModify(getIssue()));
			}
			
		});
		String copyContent = "#" + getIssue().getNumber() + ": " + getIssue().getTitle();
		titleViewer.add(new WebMarkupContainer("copy").add(new CopyClipboardBehavior(Model.of(copyContent))));
		
		if (withIssueCreation) {
			titleViewer.add(new BookmarkablePageLink<Void>("create", NewIssuePage.class) {

				@Override
				public PageParameters getPageParameters() {
					GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
					List<String> criterias = new ArrayList<>();
					if (getIssue().getMilestone() != null) {
						criterias.add(Criteria.quote(Issue.FIELD_MILESTONE) + " " 
								+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
								+ Criteria.quote(getIssue().getMilestoneName()));
					}
					for (Map.Entry<String, Input> entry: getIssue().getFieldInputs().entrySet()) {
						if (getIssue().isFieldVisible(entry.getKey())) {
							List<String> strings = entry.getValue().getValues();
							if (strings.isEmpty()) {
								criterias.add(Criteria.quote(entry.getKey()) + " " + IssueQuery.getRuleName(IssueQueryLexer.IsEmpty));
							} else { 
								FieldSpec field = issueSetting.getFieldSpec(entry.getKey());
								if (field.isAllowMultiple()) {
									for (String string: strings) {
										criterias.add(Criteria.quote(entry.getKey()) + " " 
												+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
												+ Criteria.quote(string));
									}
								} else if (!(field instanceof DateField)) { 
									criterias.add(Criteria.quote(entry.getKey()) + " " 
											+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
											+ Criteria.quote(strings.iterator().next()));
								}
							}
						}
					}

					String query;
					if (!criterias.isEmpty())
						query = StringUtils.join(criterias, " and ");
					else
						query = null;
					
					return NewIssuePage.paramsOf(getIssue().getProject(), query);
				}
				
			}.add(new WebSocketObserver() {
				
				@Override
				public void onObservableChanged(IPartialPageRequestHandler handler) {
					handler.add(component);
				}
				
				@Override
				public Collection<String> getObservables() {
					return Sets.newHashSet(Issue.getWebSocketObservable(getIssue().getId()));
				}
				
			}).setOutputMarkupId(true));
		} else {
			titleViewer.add(new WebMarkupContainer("create").setVisible(false));
		}
		
		titleViewer.setOutputMarkupId(true);
		
		return titleViewer;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newTitleViewer());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueTitleCssResourceReference()));
	}

	protected abstract Issue getIssue();
	
}
