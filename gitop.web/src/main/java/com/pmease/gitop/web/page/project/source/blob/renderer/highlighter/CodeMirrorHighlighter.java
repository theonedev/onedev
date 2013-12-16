package com.pmease.gitop.web.page.project.source.blob.renderer.highlighter;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;

public class CodeMirrorHighlighter extends Behavior {

	private static final long serialVersionUID = 1L;

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
//		response.render(JavaScriptReferenceHeaderItem.forReference(new GoogleCodePrettifyResourceReference()));
//		response.render(OnDomReadyHeaderItem.forScript("prettyPrint()"));

//		String mime = langModel.getObject();
//		Syntax syntax = Syntax.findByMime(mime);
//		if (syntax == null) {
//			return;
//		}
//		
//		response.render(JavaScriptHeaderItem.forReference(CodeMirrorResourceReference.getInstance()));
//		response.render(JavaScriptHeaderItem.forReference(CM_RUNMODE));
//		for (JavaScriptResourceReference each : getDependencies(syntax)) {
//			response.render(JavaScriptHeaderItem.forReference(each));
//		}
//		
//		response.render(JavaScriptHeaderItem.forReference(syntax.getResourceReference()));
//		
//		response.render(OnDomReadyHeaderItem.forScript(
//				String.format("\nCodeMirror.runMode($('#%s').data('text'), '%s', document.getElementById('%s'));",
//					component.getMarkupId(true),
//					mime,
//					component.getMarkupId(true))));
	}
	
//	private List<JavaScriptResourceReference> getDependencies(Syntax syntax) {
//	Syntax[] dependencies = syntax.getDependencies();
//	List<JavaScriptResourceReference> list = Lists.newArrayList();
//	for (Syntax each : dependencies) {
//		list.add(each.getResourceReference());
//	}
//	
//	return list;
//}
//
//private static final JavaScriptResourceReference CM_RUNMODE = 
//		new JavaScriptResourceReference(HighlightBehavior.class, 
//										"res/codemirror/addon/runmode/runmode.js");
//

}
