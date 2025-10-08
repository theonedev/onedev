package io.onedev.server.web.component.blob;

import static io.onedev.server.web.translation.Translation._T;

import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.svg.SpriteImage;

public abstract class BlobPicker extends DropdownLink {

	private final IModel<ProjectScopedCommit> commitModel;
	
	private String blobPath;
	
	public BlobPicker(String id, IModel<ProjectScopedCommit> commitModel, @Nullable String blobPath) {
		super(id);
		this.commitModel = commitModel;
		this.blobPath = blobPath;
	}
	
	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		if (commitModel.getObject() != null) {
			IModel<Project> projectModel = new AbstractReadOnlyModel<Project>() {

				@Override
				public Project getObject() {
					return commitModel.getObject().getProject();
				}
				
			}; 
			
			Set<BlobIdent> state = new HashSet<>();
			return new BlobSelector(id, projectModel, commitModel.getObject().getCommitId()) {

				@Override
				protected Set<BlobIdent> getState() {
					return state;
				}

				@Override
				protected void onSelect(AjaxRequestTarget target, String blobPath) {
					dropdown.close();
					BlobPicker.this.blobPath = blobPath;
					target.add(BlobPicker.this);
					
					BlobPicker.this.onSelect(target, blobPath);
				}

				@Override
				protected BlobIdentFilter getBlobIdentFilter() {
					return BlobPicker.this.getBlobIdentFilter();
				}
				
			};
		} else {
			return new Label(id, _T("Project or revision not specified yet")).add(AttributeAppender.append("class", "m-3 text-danger font-italic"));
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setEscapeModelStrings(false);
	}

	@Override
	public IModel<?> getBody() {
		if (commitModel.getObject() != null) {
			return Model.of(String.format(""
					+ "<span class='blob-picker text-nowrap'>"
					+ "  %s"
					+ "  <span>%s</span>"
					+ "  <svg class='icon rotate-90'><use xlink:href='%s'/></svg>"
					+ "</span>", 
					"<svg class='icon'><use xlink:href='" + SpriteImage.getVersionedHref(IconScope.class, "file") + "'/></svg>", 
					blobPath!=null?HtmlEscape.escapeHtml5(blobPath):_T("Choose file"), 
					SpriteImage.getVersionedHref(IconScope.class, "arrow")));
		} else {
			return Model.of("<i>" + _T("Select project and revision first") + "</i>");
		}
	}
	
	protected BlobIdentFilter getBlobIdentFilter() {
		return BlobIdentFilter.ALL;
	}

	@Override
	protected void onDetach() {
		commitModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, String blobPath);
	
}
