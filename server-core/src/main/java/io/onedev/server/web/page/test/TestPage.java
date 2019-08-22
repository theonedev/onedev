package io.onedev.server.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	static Thread thread;
	
	public TestPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Link<Void>("run") {

			@Override
			public void onClick() {
				thread = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Commandline cmd = new Commandline("sh");
							cmd.addArgs("/home/robin/temp/test.sh");
							cmd.execute(new LineConsumer() {
	
								@Override
								public void consume(String line) {
									System.out.println(line);
								}
								
							}, new LineConsumer() {
	
								@Override
								public void consume(String line) {
									System.err.println(line);
								}
									
							}).checkReturnCode();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				});
				thread.start();
			}
			
		});
		
		add(new Link<Void>("stop") {

			@Override
			public void onClick() {
				thread.interrupt();
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new TestResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.test.onDomReady();"));
	}

}
