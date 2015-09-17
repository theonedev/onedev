package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.attachments;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.io.IOUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.lang.Bytes;

import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.behavior.ConfirmBehavior;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.resource.AttachmentResource;
import com.pmease.gitplex.web.resource.AttachmentResourceReference;
import com.pmease.gitplex.web.utils.DateUtils;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.BootstrapFileInputField;

@SuppressWarnings("serial")
public class RequestAttachmentsPage extends RequestDetailPage {

	public static final int MAX_FILE_SIZE = 50; 
	
	private static final int BUFFER_SIZE = 1024*64;
			
	public RequestAttachmentsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final BootstrapFileInputField uploadField = new BootstrapFileInputField("fileUpload");
		
		final File attachmentsDir = GitPlex.getInstance(StorageManager.class).getAttachmentsDir(getPullRequest());

		final WebMarkupContainer available = new WebMarkupContainer("available") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(attachmentsDir.listFiles().length != 0);
			}
			
		};
		available.setOutputMarkupPlaceholderTag(true);
		add(available);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				FileUpload upload = uploadField.getFileUpload();
				
				File file = new File(attachmentsDir, AttachmentResource.getFileName(upload.getClientFileName()));
				try (	InputStream is = upload.getInputStream();
						OutputStream os = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE)) {
					IOUtils.copy(is, os);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} 

				success("File has been uploaded.");
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				target.add(this);
				target.add(available);
			}
			
			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		form.setMaxSize(Bytes.megabytes(MAX_FILE_SIZE));
		form.setMultiPart(true);

		form.add(new FeedbackPanel("feedback", form));
		form.add(uploadField);
		
		form.setOutputMarkupId(true);
		
		add(form);
		
		available.add(new ListView<String>("attachments", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				List<String> attachments = new ArrayList<>();
				for (File file: attachmentsDir.listFiles())
					attachments.add(file.getName());
				return attachments;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String attachment = item.getModelObject();
				final File attachmentFile = new File(attachmentsDir, attachment);
				final String downloadName = AttachmentResource.getDownloadName(attachment);
				Link<Void> link = new ResourceLink<Void>("link", new AttachmentResourceReference(), 
						AttachmentResource.paramsOf(getPullRequest(), attachment));
				link.add(new Label("label", downloadName));
				item.add(link);
				
				item.add(new Label("size", FileUtils.byteCountToDisplaySize(attachmentFile.length())));
				item.add(new Label("date", DateUtils.formatAge(AttachmentResource.getUploadDate(attachment))));
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						FileUtils.deleteFile(attachmentFile);
						target.add(available);
					}
					
				}.add(new ConfirmBehavior("Do you really want to delete attachment " + downloadName + "?")));
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RequestAttachmentsPage.class, "request-attachments.css")));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RequestListPage.class, paramsOf(repository));
	}

}
