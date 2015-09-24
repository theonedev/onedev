package com.pmease.gitplex.web.component.comment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.io.IOUtils;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.resource.AttachmentResource;
import com.pmease.gitplex.web.resource.AttachmentResourceReference;

public abstract class CommentAttachmentSupport implements AttachmentSupport {

	private static final long serialVersionUID = 1L;

	private static final int MAX_FILE_SIZE = 50; 
	
	private static final int BUFFER_SIZE = 1024*64;
			
	@Override
	public String getAttachmentUrl(String attachment) {
		PageParameters params = AttachmentResource.paramsOf(getRequest(), attachment);
		return RequestCycle.get().urlFor(new AttachmentResourceReference(), params).toString();
	}
	
	@Override
	public List<String> getAttachments() {
		List<String> attachments = new ArrayList<>();
		File attachmentsDir = GitPlex.getInstance(StorageManager.class)
				.getAttachmentsDir(getRequest());
		if (attachmentsDir.exists()) {
			for (File file: attachmentsDir.listFiles())
				attachments.add(file.getName());
		}
		return attachments;
	}

	@Override
	public String saveAttachment(FileUpload upload) {
		String clientFileName = upload.getClientFileName();
		String fileName = clientFileName;
		File attachmentsDir = getAttachmentsDir();
		int index = 2;
		while (new File(attachmentsDir, fileName).exists()) {
			if (clientFileName.contains(".")) {
				String nameBeforeExt = StringUtils.substringBeforeLast(clientFileName, ".");
				String ext = StringUtils.substringAfterLast(clientFileName, ".");
				fileName = nameBeforeExt + "-" + index + "." + ext;
			} else {
				fileName = clientFileName + "-" + index;
			}
			index++;
		}
		
		File file = new File(attachmentsDir, fileName);
		try (	InputStream is = upload.getInputStream();
				OutputStream os = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE)) {
			IOUtils.copy(is, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
		return file.getName();
	}

	private File getAttachmentsDir() {
		return GitPlex.getInstance(StorageManager.class).getAttachmentsDir(getRequest());
	}
	
	@Override
	public void deleteAttachemnt(String attachment) {
		File attachmentsDir = GitPlex.getInstance(StorageManager.class)
				.getAttachmentsDir(getRequest());
		FileUtils.deleteFile(new File(attachmentsDir, attachment));
	}

	@Override
	public int getAttachmentMaxSize() {
		return MAX_FILE_SIZE;
	}

	protected abstract PullRequest getRequest();
}
