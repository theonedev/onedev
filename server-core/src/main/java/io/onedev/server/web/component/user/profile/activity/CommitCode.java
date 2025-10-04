package io.onedev.server.web.component.user.profile.activity;

import static io.onedev.server.web.translation.Translation._T;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.commits.CommitDetailPage;

public class CommitCode extends UserActivity {

    private final Long projectId;

    private final ObjectId commitId;

    public CommitCode(Date date, Long projectId, ObjectId commitId) {
        super(date);
        this.projectId = projectId;
        this.commitId = commitId;
    }

    public Project getProject() {
        return OneDev.getInstance(ProjectService.class).load(projectId);
    }

    public ObjectId getCommitId() {
        return commitId;
    }

    @Override
    public boolean isAccessible() {
        return SecurityUtils.canReadCode(getProject());
    }

    @Override
    public Type getType() {
        return Type.CODE_COMMIT;
    }

    @Override
    public Component render(String id) {
        var url = RequestCycle.get().urlFor(CommitDetailPage.class, CommitDetailPage.paramsOf(getProject(), getCommitId().getName()));
        var commit = getProject().getRevCommit(getCommitId(), false);
        String label;
        if (commit != null) {        
            label = MessageFormat.format(_T("Added commit \"{0}\" ({1})"), "<a href=\"" + url + "\">" + getProject().getPath() + ":" + GitUtils.abbreviateSHA(getCommitId().getName()) + "</a>", escapeHtml5(commit.getShortMessage()));
        } else {
            label = MessageFormat.format(_T("Added commit \"{0}\" (<i class='text-danger'>missing in repository</i>)"), "<a href=\"" + url + "\">" + getProject().getPath() + ":" + GitUtils.abbreviateSHA(getCommitId().getName()) + "</a>");
        }
        return new Label(id, label).setEscapeModelStrings(false);
    }
    
}
