package io.onedev.server.web.component.issue.primary;

import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.web.component.issue.choice.IssueChoice;
import io.onedev.server.web.component.issue.choice.IssueChoiceProvider;

abstract class SelectIssuePanel extends FormComponentPanel<Issue> {

    private Long issueId;

    private IssueChoice choice;

    public SelectIssuePanel(String id) {
        super(id, Model.of((Issue)null));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new FencedFeedbackPanel("feedback"));
        choice = new IssueChoice("choice", new IModel<Issue>() {

            @Override
            public void detach() {
            }

            @Override
            public Issue getObject() {
                if (issueId != null)
                    return getIssueManager().load(issueId);
                else
                    return null;
            }

            @Override
            public void setObject(Issue object) {
                issueId = object.getId();
            }

        }, getChoiceProvider());        
        choice.setRequired(true);
        add(choice);

        setOutputMarkupId(true);
    }

    @Override
    public void convertInput() {
        super.convertInput();
        setConvertedInput(choice.getConvertedInput());
    }
    
    protected abstract IssueChoiceProvider getChoiceProvider();

    private IssueManager getIssueManager() {
        return OneDev.getInstance(IssueManager.class);
    }

}
