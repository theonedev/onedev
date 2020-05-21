package io.onedev.server.web.component.codecomment.referencedfrom;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public class ReferencedFromCodeCommentPanel extends GenericPanel<CodeComment> {

	public ReferencedFromCodeCommentPanel(String id, Long commentId) {
		super(id, new LoadableDetachableModel<CodeComment>() {

			@Override
			protected CodeComment load() {
				return OneDev.getInstance(CodeCommentManager.class).load(commentId);
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		CodeComment comment = getModelObject();
		
		if (SecurityUtils.canReadCode(comment.getProject())) {
			PageParameters params = ProjectBlobPage.paramsOf(comment);
			String url = RequestCycle.get().urlFor(ProjectBlobPage.class, params).toString();
			String title = String.format("<a href='%s'>%s</a>", url, 
					HtmlEscape.escapeHtml5(comment.getMark().getPath()));
			add(new Label("title", title).setEscapeModelStrings(false));  
		} else {
			add(new Label("title", comment.getMark().getPath()));  
		}
	}

}
