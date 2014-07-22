package com.pmease.gitplex.web.page.repository.info.code.commit.diff.renderer.image;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.google.common.base.Strings;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.info.code.blob.renderer.FileBlobImage;
import com.pmease.gitplex.web.page.repository.info.code.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class BlendPanel extends AbstractImageDiffPanel {

	public BlendPanel(String id, IModel<FileHeader> fileModel,
			IModel<Repository> repositoryModel, IModel<String> sinceModel,
			IModel<String> untilModel) {
		super(id, fileModel, repositoryModel, sinceModel, untilModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		FileHeader file = getFile();
		String since = getSince();
		String until = getUntil();
		
		if (file.getChangeType() == ChangeType.ADD || Strings.isNullOrEmpty(since)) {
			add(new WebMarkupContainer("old").setVisibilityAllowed(false));
		} else {
			add(new FileBlobImage("old", 
					getRepository(), 
					since,
					file.getOldPath()));
		}
		
		if (file.getChangeType() == ChangeType.DELETE) {
			add(new WebMarkupContainer("new").setVisibilityAllowed(false));
		} else {
			add(new FileBlobImage("new",
					getRepository(),
					until,
					file.getNewPath()));
		}
		
		add(new WebMarkupContainer("slider").setOutputMarkupId(true));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(OnDomReadyHeaderItem.forScript(getInitScript()));
	}
	
	private String getInitScript() {
		String containerId = "#" + getMarkupId(true);
		String oldImageId = containerId + " .oldimg";
		String newImageId = containerId + " .newimg";
		String imageWrapper = containerId + " .blend-view";
		
		String sliderId = get("slider").getMarkupId();
		
		String func = String.format(
				"$('#%s').on('change', function() {\n"
						+ "$('%s').css('opacity', $(this).val() / 100);});",
				sliderId, newImageId);

		String str =
				String.format(
					"gitplex.utils.onImageLoad('%s', function() {\n"
							+ "	gitplex.utils.onImageLoad('%s', function() {\n"
							+ "		$('%s').css('height', Math.max($('%s').height(), $('%s').height()));\n"
							+ "		%s"
							+ "}); \n});",
					oldImageId,
					newImageId,
					imageWrapper, 
					oldImageId, 
					newImageId,
					func);
		return str;
	}
}
