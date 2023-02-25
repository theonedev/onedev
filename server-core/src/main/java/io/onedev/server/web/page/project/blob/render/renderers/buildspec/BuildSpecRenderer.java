package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.NamedElement;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;
import io.onedev.server.web.page.project.blob.render.source.SourceEditPanel;
import io.onedev.server.web.page.project.blob.render.source.SourceViewPanel;
import org.apache.wicket.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

public class BuildSpecRenderer implements BlobRenderer {

	private static final long serialVersionUID = 1L;

	private static final String POSITION_PREFIX = "buildspec-";
	
	private boolean isBuildSpec(@Nullable String blobPath) {
		return BuildSpec.BLOB_PATH.equals(blobPath) || ".onedev-buildspec".equals(blobPath);
	}
	
	@Nullable
	public static String getPosition(@Nullable String selection) {
		return selection!=null? POSITION_PREFIX + selection: null;
	}
	
	@Nullable
	public static String getSelection(@Nullable String position) {
		if (position != null && position.startsWith(POSITION_PREFIX))
			return position.substring(POSITION_PREFIX.length());
		else
			return null;
	}
	
	public static String getUrlSegment(Class<?> namedElementClass) {
		return EditableUtils.getDisplayName(namedElementClass).replace(' ', '-').toLowerCase();
	}
	
	public static <T extends NamedElement> int getActiveElementIndex(BlobRenderContext context, 
			Class<T> namedElementClass,  List<T> elements, int defaultIndex) {
		String selection = getSelection(context.getPosition());
		String urlSegment = getUrlSegment(namedElementClass) + "s/";
		
		if (selection != null && selection.startsWith(urlSegment)) {
			String activeElementName = selection.substring(urlSegment.length());
			return IntStream.range(0, elements.size())
				     .filter(i -> activeElementName.equals(elements.get(i).getName()))
				     .findFirst()
				     .orElse(defaultIndex);										
		} else {
			return defaultIndex;
		}
	}
	
	@Override
	public Component render(String componentId, BlobRenderContext context) {
		if (context.getMode() == Mode.ADD && isBuildSpec(context.getNewPath()) 
				|| context.getMode() == Mode.EDIT && isBuildSpec(context.getBlobIdent().path)) {
			if (BlobRenderer.getSourceRange(context.getPosition()) != null) 
				return new SourceEditPanel(componentId, context);
			else 
				return new BuildSpecBlobEditPanel(componentId, context);
		} else if ((context.getMode() == Mode.VIEW || context.getMode() == Mode.BLAME) 
				&& context.getBlobIdent().isFile() 
				&& isBuildSpec(context.getBlobIdent().path)) {
			if (BlobRenderer.getSourceRange(context.getPosition()) != null || context.getMode() == Mode.BLAME)
				return new SourceViewPanel(componentId, context, false);
			else if (context.isViewPlain()) 
				return new SourceViewPanel(componentId, context, true);
			else
				return new BuildSpecBlobViewPanel(componentId, context);
		} else {
			return null;
		}
	}

}
