package io.onedev.server.web.page.project.codecomments;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.ConfirmClickModifier;

@SuppressWarnings("serial")
public class InvalidCodeCommentPage extends ProjectPage {

	public static final String PARAM_COEDE_COMMENT = "code-comment";
	
	private IModel<CodeComment> codeCommentModel;
	
	public InvalidCodeCommentPage(PageParameters params) {
		super(params);
		
		codeCommentModel = new LoadableDetachableModel<CodeComment>() {

			@Override
			protected CodeComment load() {
				Long codeCommentId = params.get(PARAM_COEDE_COMMENT).toLong();
				CodeComment codeComment = OneDev.getInstance(CodeCommentManager.class).load(codeCommentId);
				Preconditions.checkState(!codeComment.isValid());
				return codeComment;
			}

		};
	}

	private CodeComment getCodeComment() {
		return codeCommentModel.getObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				OneDev.getInstance(CodeCommentManager.class).delete(getCodeComment());
				
				Session.get().success("Code comment #" + getCodeComment().getId() + " deleted");
				
				String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(CodeComment.class);
				if (redirectUrlAfterDelete != null)
					throw new RedirectToUrlException(redirectUrlAfterDelete);
				else
					setResponsePage(ProjectCodeCommentsPage.class, ProjectCodeCommentsPage.paramsOf(getProject()));
				
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManageCodeComments(getCodeComment().getProject()));
			}
			
		}.add(new ConfirmClickModifier("Really want to delete this code comment?")));
	}
	
	public static PageParameters paramsOf(CodeComment codeComment) {
		PageParameters params = ProjectPage.paramsOf(codeComment.getProject());
		params.add(PARAM_COEDE_COMMENT, codeComment.getId());
		return params;
	}
	
	@Override
	protected void onDetach() {
		codeCommentModel.detach();
		super.onDetach();
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectCodeCommentsCssResourceReference()));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("codeComments", ProjectCodeCommentsPage.class, 
				ProjectCodeCommentsPage.paramsOf(getProject())));
		fragment.add(new Label("codeCommentNumber", "#" + getCodeComment().getId()));
		return fragment;
	}

}
