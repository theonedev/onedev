package io.onedev.server.web.component.issue.primary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueService;
import io.onedev.server.model.Issue;
import io.onedev.server.web.component.issue.choice.IssueChoiceProvider;
import io.onedev.server.web.component.issue.choice.IssueMultiChoice;

abstract class SelectIssuesPanel extends FormComponentPanel<Collection<Issue>> {

    private Collection<Long> issueIds = new ArrayList<>();

    private IssueMultiChoice choice;

    public SelectIssuesPanel(String id) {
        super(id, new IModel<Collection<Issue>>() {

            private Collection<Issue> collection = new ArrayList<Issue>();

            @Override
            public void detach() {
            }
            @Override
            public Collection<Issue> getObject() {
                return collection;
            }
            @Override
            public void setObject(Collection<Issue> object) {
                collection = object;
            }
        });
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new FencedFeedbackPanel("feedback"));
        choice = new IssueMultiChoice("choice", new IModel<Collection<Issue>>() {

            @Override
            public void detach() {
            }

            @Override
            public Collection<Issue> getObject() {
                return issueIds.stream().map(it->getIssueService().load(it)).collect(Collectors.toList());
            }

            @Override
            public void setObject(Collection<Issue> object) {
                issueIds = object.stream().map(it->it.getId()).collect(Collectors.toList());
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

    private IssueService getIssueService() {
        return OneDev.getInstance(IssueService.class);
    }

}
