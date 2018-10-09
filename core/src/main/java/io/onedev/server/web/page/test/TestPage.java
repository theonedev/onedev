package io.onedev.server.web.page.test;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.Link;

import io.onedev.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				/*
				for (int k=0; k<100; k++) {
					UnitOfWork unitOfWork = OneDev.getInstance(UnitOfWork.class);
					unitOfWork.begin();
					Session session = unitOfWork.getSession();
					session.beginTransaction();
					Project project = OneDev.getInstance(ProjectManager.class).load(1L);
					User user = OneDev.getInstance(UserManager.class).load(1L);
					Milestone milestone = OneDev.getInstance(MilestoneManager.class).load(1L); 
					try {
						for (int i=0; i<10000; i++) {
							long j = k*10000 +i;
							Issue issue = new Issue();
							issue.setProject(project);
							issue.setTitle("Issue " + j);
							issue.setNumber(j+2);
							if (j<10000)
								issue.setState("Open");
							else
								issue.setState("Closed");
							if (j<1000)
								issue.setMilestone(milestone);
							issue.setSubmitDate(new Date());
							issue.setSubmitter(user);
							issue.setUUID(UUID.randomUUID().toString());
							LastActivity lastActivity = new LastActivity();
							lastActivity.setDescription("submitted");
							lastActivity.setUser(issue.getSubmitter());
							lastActivity.setDate(issue.getSubmitDate());
							issue.setLastActivity(lastActivity);						
							session.save(issue);
							IssueFieldUnary unary = new IssueFieldUnary();
							unary.setIssue(issue);
							unary.setName("Type");
							unary.setType(InputSpec.ENUMERATION);
							if (i%4 == 0) {
								unary.setOrdinal(1L);
								unary.setValue("Bug");
							} else if (i%4 == 1) {
								unary.setOrdinal(2L);
								unary.setValue("Task");
							} else if (i%4 == 2) {
								unary.setOrdinal(3L);
								unary.setValue("New Feature");
							} else {
								unary.setOrdinal(4L);
								unary.setValue("Improvement");
							}
							session.save(unary);
						}
						session.flush();
						session.getTransaction().commit();
					} finally {
						unitOfWork.end();
					}
					System.out.println(k);
				}
				*/
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
