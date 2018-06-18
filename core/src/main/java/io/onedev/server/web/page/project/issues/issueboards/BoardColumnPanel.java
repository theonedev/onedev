package io.onedev.server.web.page.project.issues.issueboards;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueBoard;
import io.onedev.server.model.support.issue.query.ChoiceFieldCriteria;
import io.onedev.server.model.support.issue.query.FieldUnaryCriteria;
import io.onedev.server.model.support.issue.query.IssueCriteria;
import io.onedev.server.model.support.issue.query.IssueQuery;
import io.onedev.server.model.support.issue.query.IssueQueryLexer;
import io.onedev.server.model.support.issue.query.MilestoneCriteria;
import io.onedev.server.model.support.issue.query.StateCriteria;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.util.ComponentContext;

@SuppressWarnings("serial")
abstract class BoardColumnPanel extends Panel implements EditContext {

	private final IModel<IssueQuery> queryModel = new LoadableDetachableModel<IssueQuery>() {

		@Override
		protected IssueQuery load() {
			IssueQuery boardQuery = getBoardQuery();
			if (boardQuery != null) {
				List<IssueCriteria> criterias = new ArrayList<>();
				if (boardQuery.getCriteria() != null)
					criterias.add(boardQuery.getCriteria());
				if (getMilestone() != null)
					criterias.add(new MilestoneCriteria(getMilestone().getName(), IssueQueryLexer.Is));
				String identifyField = getBoard().getIdentifyField();
				if (identifyField.equals(Issue.STATE)) {
					criterias.add(new StateCriteria(getColumn(), IssueQueryLexer.Is));
				} else {
					if (getColumn() != null) {
						criterias.add(new ChoiceFieldCriteria(identifyField, 
								getColumn(), -1, IssueQueryLexer.Is, false));
					} else {
						criterias.add(new FieldUnaryCriteria(identifyField, IssueQueryLexer.IsEmpty));
					}
				}
				return new IssueQuery(IssueCriteria.of(criterias), boardQuery.getSorts());
			} else {
				return null;
			}
		}
		
	};
	
	public BoardColumnPanel(String id) {
		super(id);
	}

	@Override
	protected void onDetach() {
		queryModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer content = new WebMarkupContainer("content");
		add(content);
		
		String title;
		String color = null;
		User user = null;
		IssueWorkflow workflow = getProject().getIssueWorkflow();
		String identifyField = getBoard().getIdentifyField();
		if (getColumn() != null) {
			title = getColumn();
			if (identifyField.equals(Issue.STATE)) {
				color = workflow.getStateSpec(getColumn()).getColor();
			} else {
				InputSpec field = workflow.getFieldSpec(identifyField);
				if (field instanceof ChoiceInput) {
					ChoiceProvider choiceProvider = ((ChoiceInput)field).getChoiceProvider();
					OneContext.push(new ComponentContext(this));
					try {
						color = choiceProvider.getChoices(true).get(getColumn());
					} finally {
						OneContext.pop();
					}
				} else if (field instanceof UserChoiceInput) {
					user = OneDev.getInstance(UserManager.class).findByName(getColumn());
				}
			}
		} else {
			title = workflow.getFieldSpec(identifyField).getNameOfEmptyValue();
		}

		WebMarkupContainer head = new WebMarkupContainer("head");
		if (user != null) {
			head.add(new WebMarkupContainer("title").setVisible(false));
			head.add(new AvatarLink("avatarLink", user));
			head.add(new UserLink("userLink", user));
		} else {
			head.add(new Label("title", title));
			head.add(new WebMarkupContainer("avatarLink").setVisible(false));
			head.add(new WebMarkupContainer("userLink").setVisible(false));
		}
		
		head.add(AttributeAppender.append("title", getBoard().getIdentifyField()));
		content.add(head);
		if (color != null) {
			head.add(AttributeAppender.append("style", "border-top-color:" + color + ";"));
			content.add(AttributeAppender.append("style", "border-color:" + color + ";"));
		}
		
		head.add(new Label("count", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return String.valueOf(OneDev.getInstance(IssueManager.class).count(getProject(), queryModel.getObject().getCriteria()));
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(queryModel.getObject() != null);
			}
			
		});
		
		content.add(new CardListPanel("body") {

			@Override
			protected List<Issue> queryIssues(int page) {
				IssueQuery query = queryModel.getObject();
				if (query != null) {
					return OneDev.getInstance(IssueManager.class).query(getProject(), query, 
							page*WebConstants.PAGE_SIZE, WebConstants.PAGE_SIZE);
				} else {
					return new ArrayList<>();
				}
			}
			
		});
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		String script = String.format("onedev.server.issueBoards.onColumnDomReady('%s');", 
				getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	public Object getInputValue(String name) {
		return null;
	}

	protected abstract Project getProject();

	protected abstract IssueBoard getBoard();

	@Nullable
	protected abstract Milestone getMilestone();
	
	protected abstract String getColumn();
	
	@Nullable
	protected abstract IssueQuery getBoardQuery();
	
}
