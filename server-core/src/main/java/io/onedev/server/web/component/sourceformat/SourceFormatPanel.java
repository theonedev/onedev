package io.onedev.server.web.component.sourceformat;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class SourceFormatPanel extends Panel {

	private static final String COOKIE_INDENT_TYPE = "sourceFormat.identType";
	
	private static final String COOKIE_TAB_SIZE = "sourceFormat.tabSize";
	
	private static final String COOKIE_LINE_WRAP_MODE = "sourceFormat.lineWrapMode";
	
	private final OptionChangeCallback indentTypeChangeCallback;
	
	private final OptionChangeCallback tabSizeChangeCallback;
	
	private final OptionChangeCallback lineWrapModeChangeCallback;
	
	private String indentType;
	
	private String tabSize;
	
	private String lineWrapMode;
	
	public SourceFormatPanel(String id, 
			@Nullable OptionChangeCallback indentTypeChangeCallback, 
			@Nullable OptionChangeCallback tabSizeChangeCallback, 
			@Nullable OptionChangeCallback lineWrapModeChangeCallback) {
		super(id);
	
		this.indentTypeChangeCallback = indentTypeChangeCallback;
		this.tabSizeChangeCallback = tabSizeChangeCallback;
		this.lineWrapModeChangeCallback = lineWrapModeChangeCallback;
		
		WebRequest request = (WebRequest) RequestCycle.get().getRequest();
		Cookie cookie = request.getCookie(COOKIE_INDENT_TYPE);
		if (cookie != null)
			indentType = cookie.getValue();
		else
			indentType = "Tabs";
		
		cookie = request.getCookie(COOKIE_TAB_SIZE);
		if (cookie != null)
			tabSize = cookie.getValue();
		else
			tabSize = "4";
		
		cookie = request.getCookie(COOKIE_LINE_WRAP_MODE);
		if (cookie != null)
			lineWrapMode = cookie.getValue().replace('-', ' ');
		else
			lineWrapMode = "Soft wrap";
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new DropDownChoice<String>("indentType", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return indentType;
			}

			@Override
			public void setObject(String object) {
				indentType = object;
			}
			
		}, Lists.newArrayList("Spaces", "Tabs")).add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				WebResponse response = (WebResponse) RequestCycle.get().getResponse();
				Cookie cookie = new Cookie(COOKIE_INDENT_TYPE, indentType);
				cookie.setPath("/");
				cookie.setMaxAge(Integer.MAX_VALUE);
				response.addCookie(cookie);
				indentTypeChangeCallback.onOptioneChange(target);
			}
			
		}).setVisible(indentTypeChangeCallback != null));
		
		add(new DropDownChoice<String>("tabSize", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return tabSize;
			}

			@Override
			public void setObject(String object) {
				tabSize = object;
			}
			
		}, Lists.newArrayList("2", "4", "8")).add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				WebResponse response = (WebResponse) RequestCycle.get().getResponse();
				Cookie cookie = new Cookie(COOKIE_TAB_SIZE, tabSize);
				cookie.setPath("/");
				cookie.setMaxAge(Integer.MAX_VALUE);
				response.addCookie(cookie);
				tabSizeChangeCallback.onOptioneChange(target);
			}
			
		}).setVisible(tabSizeChangeCallback != null));
		
		add(new DropDownChoice<String>("lineWrapMode", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return lineWrapMode;
			}

			@Override
			public void setObject(String object) {
				lineWrapMode = object;
			}
			
		}, Lists.newArrayList("No wrap", "Soft wrap")).add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				WebResponse response = (WebResponse) RequestCycle.get().getResponse();
				Cookie cookie = new Cookie(COOKIE_LINE_WRAP_MODE, lineWrapMode.replace(' ', '-'));				
				cookie.setPath("/");
				cookie.setMaxAge(Integer.MAX_VALUE);
				response.addCookie(cookie);
				lineWrapModeChangeCallback.onOptioneChange(target);
			}
			
		}).setVisible(lineWrapModeChangeCallback != null));
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptReferenceHeaderItem.forReference(new SourceFormatResourceReference()));
		
		String script = String.format("onedev.server.sourceFormat.init('%s');", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	public String getIndentType() {
		return indentType;
	}

	public String getTabSize() {
		return tabSize;
	}

	public String getLineWrapMode() {
		return lineWrapMode;
	}

}
