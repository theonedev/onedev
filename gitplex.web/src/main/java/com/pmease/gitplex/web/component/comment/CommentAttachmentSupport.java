package com.pmease.gitplex.web.component.comment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.resource.AttachmentResource;
import com.pmease.gitplex.web.resource.AttachmentResourceReference;

import jersey.repackaged.com.google.common.base.Throwables;

public class CommentAttachmentSupport implements AttachmentSupport {

	private static final long serialVersionUID = 1L;

	private static final int MAX_FILE_SIZE = 50*1024*1024; // mega bytes
	
	private static final int BUFFER_SIZE = 1024*64;
	
	private final Long requestId;
	
	public CommentAttachmentSupport(Long requestId) {
		this.requestId = requestId;
	}
	
	@Override
	public String getAttachmentUrl(String attachment) {
		PageParameters params = AttachmentResource.paramsOf(getRequest(), attachment);
		String url = RequestCycle.get().urlFor(new AttachmentResourceReference(), params).toString();
		return RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
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
	public long getAttachmentMaxSize() {
		return MAX_FILE_SIZE;
	}

	@Override
	public long getAttachmentSize(String attachment) {
		return new File(getAttachmentsDir(), attachment).length();
	}

	private PullRequest getRequest() {
		return GitPlex.getInstance(Dao.class).load(PullRequest.class, requestId);
	}

	@Override
	public String saveAttachment(String suggestedAttachmentName, InputStream attachmentStream) {
		Preconditions.checkState(SecurityUtils.canPull(getRequest().getTargetDepot()));
		
		String attachmentName = suggestedAttachmentName;
		File attachmentsDir = getAttachmentsDir();
		int index = 2;
		while (new File(attachmentsDir, attachmentName).exists()) {
			if (suggestedAttachmentName.contains(".")) {
				String nameBeforeExt = StringUtils.substringBeforeLast(suggestedAttachmentName, ".");
				String ext = StringUtils.substringAfterLast(suggestedAttachmentName, ".");
				attachmentName = nameBeforeExt + "_" + index + "." + ext;
			} else {
				attachmentName = suggestedAttachmentName + "_" + index;
			}
			index++;
		}
		
		Exception ex = null;
		File file = new File(attachmentsDir, attachmentName);
		try (OutputStream os = new FileOutputStream(file)) {
			byte[] buffer = new byte[BUFFER_SIZE];
	        long count = 0;
	        int n = 0;
	        while (-1 != (n = attachmentStream.read(buffer))) {
	            count += n;
		        if (count > getAttachmentMaxSize()) {
		        	throw new RuntimeException("Upload must be less than " 
		        			+ FileUtils.byteCountToDisplaySize(getAttachmentMaxSize()));
		        }
	            os.write(buffer, 0, n);
	        }
		} catch (Exception e) {
			ex = e;
		} 
		if (ex != null) {
			if (file.exists())
				FileUtils.deleteFile(file);
			throw Throwables.propagate(ex);
		} else {
			return file.getName();
		}
	}
}
