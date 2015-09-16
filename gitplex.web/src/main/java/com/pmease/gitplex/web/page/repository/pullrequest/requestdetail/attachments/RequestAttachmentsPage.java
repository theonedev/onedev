package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.attachments;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tika.io.IOUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.lang.Bytes;

import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.repository.pullrequest.requestlist.RequestListPage;

import de.agilecoders.wicket.extensions.javascript.jasny.FileUploadField;

@SuppressWarnings("serial")
public class RequestAttachmentsPage extends RequestDetailPage {

	private static final int MAX_FILE_SIZE = 50; 
	
	private static final int BUFFER_SIZE = 1024*64;
			
	public RequestAttachmentsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final FileUploadField uploadField = new FileUploadField("fileUpload");
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				FileUpload upload = uploadField.getFileUpload();
				
				File attachmentsDir = GitPlex.getInstance(StorageManager.class).getAttachmentsDir(getPullRequest());
				String fileName = upload.getClientFileName();
				File file;
				if (fileName.contains(".")) {
					String name = StringUtils.substringBeforeLast(fileName, ".");
					String ext = StringUtils.substringAfterLast(fileName, ".");
					file = new File(attachmentsDir, name + "-" + System.currentTimeMillis() + "." + ext);
				} else {
					file = new File(attachmentsDir, fileName + "-" + System.currentTimeMillis());
				}
				try (	InputStream is = upload.getInputStream();
						OutputStream os = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE)) {
					IOUtils.copy(is, os);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} 
			}
			
		};
		form.setMaxSize(Bytes.megabytes(MAX_FILE_SIZE));
		form.setMultiPart(true);
		
		form.add(uploadField);
		add(form);
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
