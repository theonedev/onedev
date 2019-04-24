package io.onedev.server.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	public TestPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
				
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				BuildManager buildManager = OneDev.getInstance(BuildManager.class);
				BuildParamManager buildParamManager = OneDev.getInstance(BuildParamManager.class);
				for (int i=1; i<=250; i++) {
					int i2 = i;
					OneDev.getInstance(TransactionManager.class).run(new Runnable() {

						@Override
						public void run() {
							for (int j=1; j<=1000; j++) {
								Long buildId = (i2-1)*1000L + j;
								Build build = buildManager.load(buildId);
								BuildParam param = new BuildParam();
								param.setBuild(build);
								param.setName("param1");
								param.setValue("param1.value1");
								buildParamManager.save(param);
								
								param = new BuildParam();
								param.setBuild(build);
								param.setName("param1");
								param.setValue("param1.value2");
								buildParamManager.save(param);
								
								param = new BuildParam();
								param.setBuild(build);
								param.setName("param2");
								param.setValue("param2.value1");
								buildParamManager.save(param);
								
								param = new BuildParam();
								param.setBuild(build);
								param.setName("param2");
								param.setValue("param2.value2");
								buildParamManager.save(param);
							}
						}
						
					});
					System.out.println(i2);
				}
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
