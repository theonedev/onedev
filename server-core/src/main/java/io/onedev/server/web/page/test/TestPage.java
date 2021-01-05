package io.onedev.server.web.page.test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.JestTestMetric;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.dao.Dao;
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
				Build.Status[] statuses = new Build.Status[] {Build.Status.CANCELLED, Build.Status.FAILED, Build.Status.SUCCESSFUL, Build.Status.TIMED_OUT};
				String[] branches = new String[] {"master", "dev"};
				AtomicLong number = new AtomicLong(1L);
				for (int year=2010; year<=2020; year++) {
					System.out.println(year);
					int aYear = year;
					for (int month=1; month<=12; month++) {
						System.out.println(month);
						int aMonth = month;
						OneDev.getInstance(TransactionManager.class).call(new Callable<Void>() {

							@Override
							public Void call() throws Exception {
								Project project = OneDev.getInstance(ProjectManager.class).load(1L);
								User user = OneDev.getInstance(UserManager.class).load(1L);
								Dao dao = OneDev.getInstance(Dao.class);
								
								for (int day=1; day<=7; day++) {
									for (int hour=1; hour<=4; hour++) {
										DateTime dateTime = new DateTime(aYear, aMonth, day, hour, 0);
										for (int job=1; job<=4; job++) {
											for (int branch=1; branch<=2; branch++) {
												for (int paramName=1; paramName<=2; paramName++) {
													for (int paramValue=1; paramValue<=2; paramValue++) {
														Build build = new Build();
														build.setCommitHash("24b2fad8533f2cc59ae4d4dad9bcf44db7ba7d84");
														build.setFinishDate(dateTime.toDate());
														build.setJobName(String.valueOf(job));
														build.setPendingDate(dateTime.toDate());
														build.setProject(project);
														build.setRefName("refs/heads/" + branches[branch-1]);
														build.setRunningDate(dateTime.toDate());
														
														build.setStatus(statuses[hour-1]);
														build.setSubmitDate(dateTime.toDate());
														build.setSubmitReason("faked");
														build.setSubmitter(user);
														build.setNumber(number.getAndIncrement());
														build.setNumberScope(project);
														dao.persist(build);
														
														BuildParam param = new BuildParam();
														param.setBuild(build);
														param.setName(String.valueOf(paramName));
														param.setValue(String.valueOf(paramValue));
														param.setType("Text");
														dao.persist(param);
														
														for (int report=1; report<=8; report++) {
															JestTestMetric metric = new JestTestMetric();
															metric.setBuild(build);
															metric.setReportName(String.valueOf(report));
															metric.setNumOfTestSuites(job);
															metric.setNumOfTestCases(report);
															if (build.isSuccessful())
																metric.setTestSuiteSuccessRate(100);
															else
																metric.setTestSuiteSuccessRate(50);
															metric.setTestCaseSuccessRate(paramValue*10);
															metric.setTotalTestDuration(branch*day);
															dao.persist(metric);
														}
													}
												}
											}
										}
									}
								}
								return null;
							}
						});
					}
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
