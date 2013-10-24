package com.pmease.gitop.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.CalcMergeBaseCommand;
import com.pmease.commons.git.CheckAncestorCommand;
import com.pmease.commons.git.FindChangedFilesCommand;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.gatekeeper.GateKeeper;
import com.pmease.gitop.core.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.core.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.gatekeeper.checkresult.Pending;
import com.pmease.gitop.core.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.core.manager.MergeRequestManager;
import com.pmease.gitop.core.manager.StorageManager;
import com.pmease.gitop.core.manager.VoteInvitationManager;

@SuppressWarnings("serial")
@Entity
public class MergeRequest extends AbstractEntity {

    public enum Status {
        PENDING_APPROVAL("Pending Approval"), PENDING_UPDATE("Pending Update"), PENDING_MERGE(
                "Pending Merge"), MERGED("Merged"), CLOSED("Closed");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

    }

    private String title;

    private boolean autoMerge;

    private boolean autoCreated;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User submitter;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Branch target;

    @ManyToOne
    @JoinColumn(nullable = true)
    private Branch source;

    @Column(nullable = false)
    private CheckResult lastCheckResult;

    @Column(nullable = false)
    private Status status = Status.PENDING_APPROVAL;

    private transient Boolean merged;

    private transient String mergeBase;

    private transient List<MergeRequestUpdate> sortedUpdates;

    private transient List<MergeRequestUpdate> effectiveUpdates;

    private transient MergeRequestUpdate baseUpdate;

    @OneToMany(mappedBy = "request", cascade = CascadeType.REMOVE)
    private Collection<MergeRequestUpdate> updates = new ArrayList<MergeRequestUpdate>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.REMOVE)
    private Collection<VoteInvitation> voteInvitations = new ArrayList<VoteInvitation>();

    /**
     * Get title of this merge request.
     * 
     * @return user specified title of this merge request, <tt>null</tt> for
     *         auto-created merge request.
     */
    public @Nullable
    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAutoMerge() {
        return autoMerge;
    }

    public void setAutoMerge(boolean autoMerge) {
        this.autoMerge = autoMerge;
    }

    /**
     * Get title of this merge request, and use title of latest merge request
     * update for auto-created merge request.
     * 
     * @return title of this merge request, or title of latest merge request
     *         update for auto-created merge request.
     */
    public String findTitle() {
        if (getTitle() != null)
            return getTitle();
        else
            return getLatestUpdate().getSubject();
    }

    public boolean isAutoCreated() {
        return autoCreated;
    }

    public void setAutoCreated(boolean autoCreated) {
        this.autoCreated = autoCreated;
    }

    public User getSubmitter() {
        return submitter;
    }

    public void setSubmitter(User submitter) {
        this.submitter = submitter;
    }

    public Branch getTarget() {
        return target;
    }

    public void setTarget(Branch target) {
        this.target = target;
    }

    public Branch getSource() {
        return source;
    }

    public void setSource(Branch source) {
        this.source = source;
    }

    public Collection<MergeRequestUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(Collection<MergeRequestUpdate> updates) {
        this.updates = updates;
    }

    public Collection<VoteInvitation> getVoteInvitations() {
        return voteInvitations;
    }

    public void setVoteInvitations(Collection<VoteInvitation> voteInvitations) {
        this.voteInvitations = voteInvitations;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public MergeRequestUpdate getBaseUpdate() {
        if (baseUpdate != null) {
            return baseUpdate;
        } else {
            return getEffectiveUpdates().iterator().next();
        }
    }

    public void setBaseUpdate(MergeRequestUpdate baseUpdate) {
        this.baseUpdate = baseUpdate;
    }

    public CheckResult getLastCheckResult() {
        return lastCheckResult;
    }

    /**
     * Get list of sorted updates.
     * <p>
     * 
     * @return list of sorted updates ordered by update id reversely
     */
    public List<MergeRequestUpdate> getSortedUpdates() {
        if (sortedUpdates == null) {
            sortedUpdates = new ArrayList<MergeRequestUpdate>(getUpdates());
            Preconditions.checkState(!sortedUpdates.isEmpty());

            Collections.sort(sortedUpdates);
            Collections.reverse(sortedUpdates);
        }
        return sortedUpdates;
    }

    /**
     * Get list of effective updates in reverse order. Update is considered
     * effective if it is descendant of merge base of latest update and target
     * branch head.
     * 
     * @return list of effective updates in reverse order.
     */
    public List<MergeRequestUpdate> getEffectiveUpdates() {
        if (effectiveUpdates == null) {
            effectiveUpdates = new ArrayList<MergeRequestUpdate>();

            File repoDir =
                    Gitop.getInstance(StorageManager.class).getStorage(getTarget().getProject())
                            .ofCode();
            Git git = new Git(repoDir);
            CheckAncestorCommand command = git.checkAncestor();
            command.ancestor(getMergeBase());

            for (Iterator<MergeRequestUpdate> it = getSortedUpdates().iterator(); it.hasNext();) {
                MergeRequestUpdate update = it.next();
                command.descendant(update.getRefName());
                if (command.call()) {
                    effectiveUpdates.add(update);
                } else {
                    break;
                }
            }
        }
        return effectiveUpdates;
    }

    public MergeRequestUpdate getLatestUpdate() {
        return getSortedUpdates().iterator().next();
    }

    public boolean isFastForward() {
        File repoDir =
                Gitop.getInstance(StorageManager.class).getStorage(getTarget().getProject())
                        .ofCode();
        CheckAncestorCommand command = new Git(repoDir).checkAncestor();
        command.ancestor(getTarget().getName());
        command.descendant(getLatestUpdate().getRefName());
        return command.call();
    }

    public Collection<String> findTouchedFiles() {
        StorageManager storageManager = Gitop.getInstance(StorageManager.class);
        File repoDir = storageManager.getStorage(getTarget().getProject()).ofCode();
        MergeRequestUpdate update = getLatestUpdate();
        if (update != null) {
            FindChangedFilesCommand command = new Git(repoDir).findChangedFiles();
            command.fromRev(getTarget().getName());
            command.toRev(update.getRefName());
            return command.call();
        } else {
            return new ArrayList<String>();
        }
    }

    public boolean isMerged() {
        if (merged == null) {
            StorageManager storageManager = Gitop.getInstance(StorageManager.class);
            File repoDir = storageManager.getStorage(getTarget().getProject()).ofCode();
            CheckAncestorCommand command = new Git(repoDir).checkAncestor();
            command.ancestor(getLatestUpdate().getRefName());
            command.descendant(getTarget().getName());
            merged = command.call();
        }
        return merged;
    }

    public String getMergeBase() {
        if (mergeBase == null) {
            StorageManager storageManager = Gitop.getInstance(StorageManager.class);
            File repoDir = storageManager.getStorage(getTarget().getProject()).ofCode();
            CalcMergeBaseCommand command = new Git(repoDir).calcMergeBase();
            command.rev1(getLatestUpdate().getRefName());
            command.rev2(getTarget().getName());
            mergeBase = command.call();
        }
        return mergeBase;
    }

    /**
     * Invite specified number of users in candicates to vote for this request.
     * <p>
     * 
     * @param candidates a collection of users to invite users from
     * @param count number of users to invite
     */
    public void inviteToVote(Collection<User> candidates, int count) {
        Collection<User> copyOfCandidates = new HashSet<User>(candidates);

        // submitter is not allowed to vote for this request
        copyOfCandidates.remove(getSubmitter());

        /*
         * users already voted since base update should be excluded from
         * invitation list as their votes are still valid
         */
        for (Vote vote : getBaseUpdate().listVotesOnwards()) {
            copyOfCandidates.remove(vote.getVoter());
        }

        Set<User> invited = new HashSet<User>();
        for (VoteInvitation each : getVoteInvitations())
            invited.add(each.getVoter());

        invited.retainAll(copyOfCandidates);

        for (int i = 0; i < count - invited.size(); i++) {
            User selected = Collections.min(copyOfCandidates, new Comparator<User>() {

                @Override
                public int compare(User user1, User user2) {
                    return user1.getVotes().size() - user2.getVotes().size();
                }

            });

            copyOfCandidates.remove(selected);

            VoteInvitation invitation = new VoteInvitation();
            invitation.setRequest(this);
            invitation.setVoter(selected);
            invitation.getRequest().getVoteInvitations().add(invitation);
            invitation.getVoter().getVoteInvitations().add(invitation);

            Gitop.getInstance(VoteInvitationManager.class).save(invitation);
        }

    }

    public CheckResult check() {
        GateKeeper gateKeeper = getTarget().getProject().getGateKeeper();
        lastCheckResult = gateKeeper.check(this);

        for (VoteInvitation invitation : getVoteInvitations()) {
            if (!lastCheckResult.canVote(invitation.getVoter(), this))
                Gitop.getInstance(VoteInvitationManager.class).delete(invitation);
        }

        if (lastCheckResult instanceof Pending || lastCheckResult instanceof Blocked) {
            setStatus(Status.PENDING_APPROVAL);
        } else if (lastCheckResult instanceof Rejected) {
            setStatus(Status.PENDING_UPDATE);
        } else {
            setStatus(Status.PENDING_MERGE);
        }
        
        return lastCheckResult;
    }

    public void close() {
        setStatus(Status.CLOSED);
        Gitop.getInstance(MergeRequestManager.class).save(this);
    }

    public void merge() {
        Preconditions.checkState(getLastCheckResult() instanceof Accepted,
                "Can only merge when gate keeper check is passed.");

        // TODO: implement this
        
        Git git = new Git(Gitop.getInstance(StorageManager.class)
                .getStorage(getTarget().getProject()).ofCode());
        git.updateRef()
            .refName(getTarget().getName())
            .revision(getLatestUpdate().getCommitHash())
            .call();

        setStatus(Status.MERGED);
        Gitop.getInstance(MergeRequestManager.class).save(this);
    }

    public boolean canMergeWithoutConflicts() {
        // TODO: implement this
        return true;
    }

}
