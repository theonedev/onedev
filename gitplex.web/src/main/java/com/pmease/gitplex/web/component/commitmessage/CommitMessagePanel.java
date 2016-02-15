package com.pmease.gitplex.web.component.commitmessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.unbescape.html.HtmlEscape;

import com.pmease.commons.git.Commit;
import com.pmease.commons.util.Highlighter;
import com.pmease.commons.util.Transformer;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.page.repository.commit.CommitDetailPage;

@SuppressWarnings("serial")
public class CommitMessagePanel extends Panel {

	private final IModel<Depot> repoModel;
	
	private final IModel<Commit> commitModel;
	
	private final IModel<List<Pattern>> patternsModel;
	
	public CommitMessagePanel(String id, IModel<Depot> repoModel, 
			IModel<Commit> commitModel, IModel<List<Pattern>> patternsModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.commitModel = commitModel;
		this.patternsModel = patternsModel;
	}

	public CommitMessagePanel(String id, IModel<Depot> repoModel, IModel<Commit> commitModel) {
		this(id, repoModel, commitModel, new LoadableDetachableModel<List<Pattern>>() {

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
		
		AbstractLink link = new BookmarkablePageLink<Void>("link",
				CommitDetailPage.class,
				CommitDetailPage.paramsOf(repoModel.getObject(), commitModel.getObject().getHash()));
		
		add(link);
		link.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return highlight(commitModel.getObject().getSubject());
			}
			
		}).setEscapeModelStrings(false));

		add(new Label("detail", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return highlight(commitModel.getObject().getBody());
			}
			
		}) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(commitModel.getObject().getBody() != null);
			}
		}.setEscapeModelStrings(false));
		
		WebMarkupContainer detailedToggle = new WebMarkupContainer("toggle") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(commitModel.getObject().getBody() != null);
			}
		};
		add(detailedToggle);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(CommitMessagePanel.class, "commit-message.css")));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		commitModel.detach();
		patternsModel.detach();
		
		super.onDetach();
	}
	
}
