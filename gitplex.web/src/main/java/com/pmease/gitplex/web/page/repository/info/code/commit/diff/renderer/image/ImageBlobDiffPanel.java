package com.pmease.gitplex.web.page.repository.info.code.commit.diff.renderer.image;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.pmease.gitplex.web.page.repository.info.code.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class ImageBlobDiffPanel extends AbstractImageDiffPanel {

	public ImageBlobDiffPanel(String id, 
			IModel<FileHeader> model, 
			IModel<String> sinceModel,
			IModel<String> untilModel) {
		
		super(id, model, sinceModel, untilModel);
		
		setOutputMarkupId(true);
	}
	
	static enum CompareType {
		SIDE_BY_SIDE("Side by side"),
		SWIPE("Swipe"),
		BLEND("Blend");
//		DIFFERENCE("Difference");
		
		final String displayName;
		CompareType(String displayName) {
			this.displayName = displayName;
		}
	}
	
	private CompareType compareType = CompareType.SIDE_BY_SIDE;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(createBtns());
		add(createImageContainer());
	}
	
	protected Component createBtns() {
		WebMarkupContainer btnsContainer = new WebMarkupContainer("btns") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				this.setVisibilityAllowed(getFile().getChangeType() == ChangeType.MODIFY);
			}
		};
		
		Loop btnLoop = new Loop("types", CompareType.values().length) {

			@Override
			protected void populateItem(LoopItem item) {
				final int index = item.getIndex();
				final CompareType type = CompareType.values()[index];
				AjaxLink<Void> link = new AjaxLink<Void>("typeBtn") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (type == compareType) {
							return;
						}
						
						compareType = type;
						onCompareTypeChanged(target);
					}
				};
				link.add(new Label("name", type.displayName));
				link.add(AttributeAppender.append("class", 
						type == compareType ? "active" : ""));
				item.add(link);
			}
		};
		
		btnsContainer.add(btnLoop);
		
		return btnsContainer;
	}
	
	protected Component createImageContainer() {
		switch (compareType) {
		case SIDE_BY_SIDE:
			return new SideBySidePanel("image", fileModel, sinceModel, untilModel);
			
		case SWIPE:
			return new SwipePanel("image", fileModel, sinceModel, untilModel);
			
		case BLEND:
			return new BlendPanel("image", fileModel, sinceModel, untilModel);
			
		default:
			return new WebMarkupContainer("image").setVisibilityAllowed(false);
		}
	}
	
	private void onCompareTypeChanged(AjaxRequestTarget target) {
		this.addOrReplace(createBtns());
		this.addOrReplace(createImageContainer());
		
		target.add(this);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
//		
//		response.render(JQuery.$(this, ".compares .btn").on("click", 
//				new JavaScriptInlineFunction(
//						"$('#" + getMarkupId(true) + " .btn-group .btn').removeClass('active'); \n"
//						+ "$(this).addClass('active'); \n"
//						+ "var cssClass = $(this).data('class'); \n"
//						+ "$('#" + imageContainer.getMarkupId(true) + "').removeClass().addClass('image ' + cssClass);"))
//						
//					.asDomReadyScript());
	}
}
