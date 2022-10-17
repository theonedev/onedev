package io.onedev.server.web.page.project.blob.render.folder;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.service.GitService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.FileExtension;
import io.onedev.server.util.FilterIterator;
import io.onedev.server.util.ProgrammingLanguageDetector;
import io.onedev.server.web.ajaxlistener.TrackViewStateListener;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.CtrlAwareOnClickAjaxBehavior;
import io.onedev.server.web.component.blob.BlobIcon;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.component.user.card.PersonCardPanel;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.util.EditParamsAware;

@SuppressWarnings("serial")
public class FolderViewPanel extends Panel {

	private static final String USER_CARD_ID = "userCard";
	
	private final BlobRenderContext context;
	
	private final IModel<List<BlobIdent>> childrenModel = new LoadableDetachableModel<List<BlobIdent>>() {

		@Override
		protected List<BlobIdent> load() {
			List<BlobIdent> children = getGitService().getChildren(context.getProject(), getCommitId(), 
					context.getBlobIdent().path, BlobIdentFilter.ALL, true);
			for (BlobIdent child: children)
				child.revision = context.getBlobIdent().revision;
			
			BlobIdent oldBuildSpecIdent = new BlobIdent(context.getBlobIdent().revision, 
					".onedev-buildspec", FileMode.REGULAR_FILE.getBits());
			BlobIdent buildSpecIdent = new BlobIdent(context.getBlobIdent().revision, 
					BuildSpec.BLOB_PATH, FileMode.REGULAR_FILE.getBits());
			if (children.contains(oldBuildSpecIdent)) {
				children.remove(oldBuildSpecIdent);
				children.add(0, oldBuildSpecIdent);
			}
			if (children.contains(buildSpecIdent)) {
				children.remove(buildSpecIdent);
				children.add(0, buildSpecIdent);
			}
			return children;
		}
		
	};
	
	private final IModel<BlobIdent> readmeModel = new LoadableDetachableModel<BlobIdent>() {

		@Override
		protected BlobIdent load() {
			Predicate<BlobIdent> isReadmeBlob = new Predicate<BlobIdent>() {
				@Override
				public boolean test(BlobIdent t) {
					String nameNoExt = FileExtension.getNameWithoutExtension(t.getName());
					return t.isFile() && nameNoExt != null && nameNoExt.equalsIgnoreCase("readme");
				}
			};
			
			FilterIterator<BlobIdent> readmeBlobIterator = new FilterIterator<BlobIdent>(childrenModel.getObject().iterator(), isReadmeBlob);
			for (BlobIdent blobIdent: readmeBlobIterator) {
				String language = ProgrammingLanguageDetector.getLanguageForExtension(FileExtension.getExtension(blobIdent.getName()));
				if(language != null && language.equals("Markdown")) {
					return blobIdent;
				}
			}
			return null;
		}	

	};
	
	private AbstractDefaultAjaxBehavior userCardBehavior;
	
	public FolderViewPanel(String id, BlobRenderContext context) {
		super(id);

		this.context = context;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer parent = new WebMarkupContainer("parent") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(context.getBlobIdent().path != null);
			}
			
		};
		
		final BlobIdent parentIdent;
		if (context.getBlobIdent().path == null) {
			parentIdent = null;
		} else if (context.getBlobIdent().path.indexOf('/') != -1) {
			parentIdent = new BlobIdent(
					context.getBlobIdent().revision, 
					StringUtils.substringBeforeLast(context.getBlobIdent().path, "/"), 
					FileMode.TREE.getBits());
		} else {
			parentIdent = new BlobIdent(context.getBlobIdent().revision, null, FileMode.TREE.getBits());
		}
		parent.add(new ViewStateAwareAjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				context.onSelect(target, parentIdent, null);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				ProjectBlobPage.State state = new ProjectBlobPage.State(parentIdent);
				PageParameters params = ProjectBlobPage.paramsOf(context.getProject(), state); 
				tag.put("href", urlFor(ProjectBlobPage.class, params));
			}
			
		});
		add(parent);
		
		add(new ListView<BlobIdent>("children", childrenModel) {

			@Override
			protected void populateItem(ListItem<BlobIdent> item) {
				BlobIdent blobIdent = item.getModelObject();
				
				WebMarkupContainer pathLink = new WebMarkupContainer("pathLink") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						
						ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
						PageParameters params = ProjectBlobPage.paramsOf(context.getProject(), state); 
						tag.put("href", urlFor(ProjectBlobPage.class, params));
					}

				}; 
				
				pathLink.add(new CtrlAwareOnClickAjaxBehavior() {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.setPreventDefault(true);
						attributes.getAjaxCallListeners().add(new TrackViewStateListener(false));
					}

					@Override
					protected void respond(AjaxRequestTarget target) {
						context.onSelect(target, blobIdent, null);
					}
					
				});

				pathLink.add(new BlobIcon("icon", Model.of(blobIdent)));
				
				if (context.getBlobIdent().path != null) 
					pathLink.add(new Label("label", blobIdent.path.substring(context.getBlobIdent().path.length()+1)));
				else if (blobIdent.path.equals(BuildSpec.BLOB_PATH) || blobIdent.path.equals(".onedev-buildspec"))
					pathLink.add(new Label("label", "<b>" + HtmlEscape.escapeHtml5(blobIdent.path) + "</b>").setEscapeModelStrings(false));
				else
					pathLink.add(new Label("label", blobIdent.path));
				item.add(pathLink);
				
				if (item.getIndex() == 0)
					item.add(new Label("lastCommit", "<span class='text-warning'>Loading last commit info...</span>").setEscapeModelStrings(false));
				else
					item.add(new Label("lastCommit"));
			}
			
		});
		
		WebMarkupContainer readmeContainer = new WebMarkupContainer("readme") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(readmeModel.getObject() != null);
			}
			
		};
		readmeContainer.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return readmeModel.getObject().getName();
			}
			
		}));
		readmeContainer.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				ProjectBlobPage.State state = new ProjectBlobPage.State();
				state.blobIdent = readmeModel.getObject();
				state.mode = Mode.EDIT;
				state.urlBeforeEdit = EditParamsAware.getUrlBeforeEdit(getPage());
				state.urlAfterEdit = EditParamsAware.getUrlAfterEdit(getPage());
				setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(context.getProject(), state));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				BlobIdent blobIdent = readmeModel.getObject();
				setVisible(context.isOnBranch()
						&& SecurityUtils.canModify(context.getProject(), blobIdent.revision, blobIdent.path));
			}
			
		});
		readmeContainer.add(new MarkdownViewer("body", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Blob blob = context.getProject().getBlob(readmeModel.getObject(), true);
				Blob.Text text = blob.getText();
				if (text != null)
					return text.getContent();
				else
					return "This seems like a binary file!";
			}
			
		}, null) {

			@Override
			protected BlobRenderContext getRenderContext() {
				return context;
			}

		});
		
		add(readmeContainer);
		
		add(new WebMarkupContainer(USER_CARD_ID).setOutputMarkupId(true));
		add(userCardBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				String name = RequestCycle.get().getRequest().getPostParameters()
						.getParameterValue("name").toString();
				String emailAddress = RequestCycle.get().getRequest().getPostParameters()
						.getParameterValue("emailAddress").toString();
				PersonIdent personIdent = new PersonIdent(name, emailAddress);
				Component userCard = new PersonCardPanel(USER_CARD_ID, personIdent, "Author");
				userCard.setOutputMarkupId(true);
				replace(userCard);
				target.add(userCard);
				target.appendJavaScript("onedev.server.folderView.onUserCardAvailable();");
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new FolderViewResourceReference()));

		PageParameters params = LastCommitsResource.paramsOf(context.getProject(), 
				context.getBlobIdent().revision, context.getBlobIdent().path); 
		String lastCommitsUrl = urlFor(new LastCommitsResourceReference(), params).toString();
		CharSequence callback = userCardBehavior.getCallbackFunction(
				CallbackParameter.explicit("name"), CallbackParameter.explicit("emailAddress"));
		String script = String.format("onedev.server.folderView.onDomReady('%s', '%s', %s)", 
				getMarkupId(), lastCommitsUrl, callback); 
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	private ObjectId getCommitId() {
		return context.getCommit();
	}

	@Override
	protected void onDetach() {
		childrenModel.detach();
		readmeModel.detach();		
		
		super.onDetach();
	}

}
