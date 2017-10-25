package com.gitplex.server.web.component.commitmessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.revwalk.RevCommit;
import org.unbescape.html.HtmlEscape;

import com.gitplex.server.git.GitUtils;
import com.gitplex.server.model.Project;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.project.commit.CommitDetailPage;
import com.gitplex.utils.Highlighter;
import com.gitplex.utils.Transformer;

@SuppressWarnings("serial")
public class ExpandableCommitMessagePanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final IModel<RevCommit> commitModel;
	
	private final IModel<List<Pattern>> patternsModel;
	
	public ExpandableCommitMessagePanel(String id, IModel<Project> projectModel, 
			IModel<RevCommit> commitModel, IModel<List<Pattern>> patternsModel) {
		super(id);
		
		this.projectModel = projectModel;
		this.commitModel = commitModel;
		this.patternsModel = patternsModel;
	}

	public ExpandableCommitMessagePanel(String id, IModel<Project> projectModel, IModel<RevCommit> commitModel) {
		this(id, projectModel, commitModel, new LoadableDetachableModel<List<Pattern>>() {

			@Override
			protected List<Pattern> load() {
				return new ArrayList<>();
			}
			
		});
	}
	
	private String highlight(String text) {
		return Highlighter.highlightPatterns(text, patternsModel.getObject(), new Transformer<String>() {

			@Override
			public String transform(String text) {
				return "<span class='highlight'>" + HtmlEscape.escapeHtml5(text) + "</span>";
			}
			
		}, new Transformer<String>() {

			@Override
			public String transform(String text) {
				return HtmlEscape.escapeHtml5(text);
			}
			
		});
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AbstractLink link = new ViewStateAwarePageLink<Void>("link",
				CommitDetailPage.class,
				CommitDetailPage.paramsOf(projectModel.getObject(), commitModel.getObject().name()));
		
		add(link);
		link.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return highlight(commitModel.getObject().getShortMessage());
			}
			
		}).setEscapeModelStrings(false));

		add(new Label("detail", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return highlight(GitUtils.getDetailMessage(commitModel.getObject()));
			}
			
		}) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitUtils.getDetailMessage(commitModel.getObject()) != null);
			}
		}.setEscapeModelStrings(false));
		
		WebMarkupContainer detailedToggle = new WebMarkupContainer("toggle") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(GitUtils.getDetailMessage(commitModel.getObject()) != null);
			}
		};
		add(detailedToggle);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CommitMessageResourceReference()));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		commitModel.detach();
		patternsModel.detach();
		
		super.onDetach();
	}
	
}
