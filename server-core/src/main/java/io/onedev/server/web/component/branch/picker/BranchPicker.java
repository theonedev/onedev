package io.onedev.server.web.component.branch.picker;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.model.Project;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public abstract class BranchPicker extends DropdownLink {

	private final IModel<Project> projectModel;
	
	private String branch;
	
	public BranchPicker(String id, IModel<Project> projectModel, String branch) {
		super(id);
		
		this.projectModel = projectModel;
		this.branch = branch;
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new BranchSelector(id, projectModel, branch) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String branch) {
				dropdown.close();
				BranchPicker.this.branch = branch;
				target.add(BranchPicker.this);
				
				BranchPicker.this.onSelect(target, branch);
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setEscapeModelStrings(false);
	}

	@Override
	public IModel<?> getBody() {
		return Model.of(String.format(""
				+ "<span class='branch-picker'>"
				+ "  <svg class='icon'><use xlink:href='%s'/></svg>"
				+ "  <span>%s</span> "
				+ "  <svg class='icon rotate-90'><use xlink:href='%s'/></svg>"
				+ "</span>", 
				SpriteImage.getVersionedHref(IconScope.class, "branch"),
				branch!=null?HtmlEscape.escapeHtml5(branch):"<i>choose</i>", 
				SpriteImage.getVersionedHref(IconScope.class, "arrow")));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	protected abstract void onSelect(AjaxRequestTarget target, String branch);
	
}
