package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.upload.FileUploadBase.SizeLimitExceededException;
import org.apache.wicket.util.upload.FileUploadException;

import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmListener;
import com.pmease.commons.wicket.behavior.markdown.AttachmentSupport;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.web.component.comment.CommentAttachmentSupport;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.RequestListPage;
import com.pmease.gitplex.web.resource.AttachmentResource;
import com.pmease.gitplex.web.resource.AttachmentResourceReference;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class RequestAttachmentsPage extends RequestDetailPage {

	private final AttachmentSupport attachmentSupport;
	
	public RequestAttachmentsPage(PageParameters params) {
		super(params);
		
		attachmentSupport = new CommentAttachmentSupport(getPullRequest().getId());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final FileUploadField uploadField = new FileUploadField("file");
		
		final WebMarkupContainer available = new WebMarkupContainer("available") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!attachmentSupport.getAttachments().isEmpty());
			}
			
		};
		available.setOutputMarkupPlaceholderTag(true);
		add(available);
		
		final Form<?> form = new Form<Void>("form") {

			@Override
			protected void onFileUploadException(FileUploadException e, Map<String, Object> model) {
				if (e instanceof SizeLimitExceededException)
				    error("Upload must be less than " + FileUtils.byteCountToDisplaySize(getMaxSize().bytes()));
			}
			
		};
		form.setMaxSize(Bytes.bytes(attachmentSupport.getAttachmentMaxSize()));
		form.setMultiPart(true);

		uploadField.add(new AjaxFormSubmitBehavior("change") {

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				FileUpload upload = uploadField.getFileUpload();
				if (upload != null) {
					try (InputStream is = upload.getInputStream()) {
						attachmentSupport.saveAttachment(upload.getClientFileName(), is);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					
					form.success("File has been uploaded.");
					target.add(form);
					target.add(available);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				super.onError(target);
				target.add(form);
			}
			
		});
		
		form.add(uploadField);
		form.add(new NotificationPanel("feedback", form));
		form.setOutputMarkupId(true);
		add(form);
		
		available.add(new ListView<String>("attachments", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return attachmentSupport.getAttachments();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				final String attachment = item.getModelObject();
				Link<Void> link = new ResourceLink<Void>("link", new AttachmentResourceReference(), 
						AttachmentResource.paramsOf(getPullRequest(), attachment));
				link.add(new Label("label", attachment));
				item.add(link);
				
				long attachmentSize = attachmentSupport.getAttachmentSize(attachment);
				item.add(new Label("size", FileUtils.byteCountToDisplaySize(attachmentSize)));
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete attachment " + attachment + "?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						attachmentSupport.deleteAttachemnt(attachment);
						target.add(available);
					}
					
				});
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
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RequestListPage.class, paramsOf(depot));
	}

}
