package com.gitplex.server.web.component.comment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AttachmentManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.web.component.markdown.AttachmentSupport;
import com.gitplex.server.web.util.resource.AttachmentResource;
import com.gitplex.server.web.util.resource.AttachmentResourceReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class ProjectAttachmentSupport implements AttachmentSupport {

	private static final long serialVersionUID = 1L;

	private static final int MAX_FILE_SIZE = 20*1024*1024; // mega bytes
	
	private static final int BUFFER_SIZE = 1024*64;
	
	private final Long projectId;
	
	private final String attachmentDirUUID;
	
	public ProjectAttachmentSupport(Project project, String attachmentDirUUID) {
		projectId = project.getId();
		this.attachmentDirUUID = attachmentDirUUID;
	}
	
	@Override
	public String getAttachmentUrl(String attachment) {
		PageParameters params = AttachmentResource.paramsOf(getProject(), attachmentDirUUID, attachment);
		return RequestCycle.get().urlFor(new AttachmentResourceReference(), params).toString();
	}
	
	@Override
	public List<String> getAttachments() {
		List<String> attachments = new ArrayList<>();
		File attachmentDir = GitPlex.getInstance(AttachmentManager.class).getAttachmentDir(getProject(), attachmentDirUUID);
		if (attachmentDir.exists()) {
			for (File file: attachmentDir.listFiles())
				attachments.add(file.getName());
		}
		return attachments;
	}

	private File getAttachmentDir() {
		return GitPlex.getInstance(AttachmentManager.class).getAttachmentDir(getProject(), attachmentDirUUID);
	}
	
	@Override
	public void deleteAttachemnt(String attachment) {
		File attachmentDir = GitPlex.getInstance(AttachmentManager.class).getAttachmentDir(getProject(), attachmentDirUUID);
		FileUtils.deleteFile(new File(attachmentDir, attachment));
	}

	@Override
	public long getAttachmentMaxSize() {
		return MAX_FILE_SIZE;
	}

	private Project getProject() {
		return GitPlex.getInstance(ProjectManager.class).load(projectId);
	}

	@Override
	public String saveAttachment(String suggestedAttachmentName, InputStream attachmentStream) {
		Preconditions.checkState(SecurityUtils.canRead(getProject()));
		
		String attachmentName = suggestedAttachmentName;
		File attachmentDir = getAttachmentDir();
		FileUtils.createDir(attachmentDir);
		int index = 2;
		while (new File(attachmentDir, attachmentName).exists()) {
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
		File file = new File(attachmentDir, attachmentName);
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
