package io.onedev.server.web.page.project.issues.issueboards;

import static io.onedev.server.model.support.issue.IssueConstants.FIELD_COMMENT_COUNT;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_MILESTONE;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_STATE;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_SUBMITTER;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_SUBMIT_DATE;
import static io.onedev.server.model.support.issue.IssueConstants.FIELD_VOTE_COUNT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.issuestate.IssueStateLabel;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.page.project.issues.fieldvalues.FieldValuesPanel;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDetailPage;
import jersey.repackaged.com.google.common.collect.Lists;

@SuppressWarnings("serial")
abstract class CardDetailPanel extends GenericPanel<Issue> {

	public CardDetailPanel(String id, IModel<Issue> model) {
		super(id, model);
	}

	private Issue getIssue() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("number", "#" + getIssue().getNumber()));
		
		add(new Label("title", getIssue().getTitle()));

		List<String> fieldNames = Lists.newArrayList(FIELD_STATE);
		fieldNames.add(FIELD_SUBMITTER);
		fieldNames.add(FIELD_SUBMIT_DATE);
		for (String fieldName: getIssue().getFieldNames()) {
			if (getIssue().isFieldVisible(fieldName))
				fieldNames.add(fieldName);
		}
		fieldNames.add(FIELD_MILESTONE);
		fieldNames.add(FIELD_COMMENT_COUNT);
		fieldNames.add(FIELD_VOTE_COUNT);

		if (fieldNames.size() % 2 != 0)
			fieldNames.add(null);
		
		List<FieldPair> fieldPairs = new ArrayList<>();
		for (int i=0; i<fieldNames.size()/2; i++) 
			fieldPairs.add(new FieldPair(fieldNames.get(2*i), fieldNames.get(2*i+1)));
		
		add(new ListView<FieldPair>("fieldPairs", fieldPairs) {

			@Override
			protected void populateItem(ListItem<FieldPair> item) {
				FieldPair pair = item.getModelObject();
				if (pair.getField1() != null) {
					item.add(new Label("name1", pair.getField1()));
					item.add(renderField("value1", pair.getField1()));
				} else {
					item.add(new WebMarkupContainer("name1"));
					item.add(new WebMarkupContainer("value1"));
				}
				if (pair.getField2() != null) {
					item.add(new Label("name2", pair.getField2()));
					item.add(renderField("value2", pair.getField2()));
				} else {
					item.add(new WebMarkupContainer("name2"));
					item.add(new WebMarkupContainer("value2"));
				}
			}

			private Component renderField(String componentId, String fieldName) {
				switch (fieldName) {
				case FIELD_STATE:
					return new IssueStateLabel(componentId, CardDetailPanel.this.getModel());
				case FIELD_SUBMITTER:
					Fragment fragment = new Fragment(componentId, "userFrag", CardDetailPanel.this);
					fragment.add(new UserLink("name", User.getForDisplay(getIssue().getSubmitter(), getIssue().getSubmitterName())));
					fragment.add(new AvatarLink("avatar", getIssue().getSubmitter()));
					return fragment;
				case FIELD_SUBMIT_DATE:
					return new Label(componentId, DateUtils.formatAge(getIssue().getSubmitDate()));
				case FIELD_MILESTONE:
					if (getIssue().getMilestone() != null) {
						return new BookmarkablePageLink<Void>(componentId, MilestoneDetailPage.class, 
								MilestoneDetailPage.paramsOf(getIssue().getMilestone(), null)) {

							@Override
							public IModel<?> getBody() {
								return Model.of(getIssue().getMilestone().getName());
							}
							
						};
					} else {
						return new Label(componentId, "<i>No milestone</i>").setEscapeModelStrings(false);
					}
				case FIELD_COMMENT_COUNT:
					return new Label(componentId, getIssue().getCommentCount());
				case FIELD_VOTE_COUNT:
					return new Label(componentId, getIssue().getVoteCount());
				default:
					return new FieldValuesPanel(componentId) {

						@Override
						protected Issue getIssue() {
							return CardDetailPanel.this.getIssue();
						}

						@Override
						protected IssueField getField() {
							return getIssue().getFields().get(fieldName);
						}
						
					};
				}
			}
			
		});
		ContentVersionSupport contentVersionSupport;
		if (SecurityUtils.canModify(getIssue())) {
			contentVersionSupport = new ContentVersionSupport() {

				@Override
				public long getVersion() {
					return getIssue().getVersion();
				}
				
			};
		} else {
			contentVersionSupport = null;
		}
		
		add(new MarkdownViewer("description", new IModel<String>() {

			@Override
			public String getObject() {
				return getIssue().getDescription();
			}

			@Override
			public void detach() {
			}

			@Override
			public void setObject(String object) {
				getIssue().setDescription(object);
				OneDev.getInstance(IssueManager.class).save(getIssue());				
			}
			
		}, contentVersionSupport).setVisible(getIssue().getDescription() != null));
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
	}
	
	protected abstract void onClose(AjaxRequestTarget target);
	
	private static class FieldPair implements Serializable {
		
		private final String field1;
		
		private final String field2;
		
		public FieldPair(String field1, String field2) {
			this.field1 = field1;
			this.field2 = field2;
		}

		public String getField1() {
			return field1;
		}

		public String getField2() {
			return field2;
		}
		
	}
}
