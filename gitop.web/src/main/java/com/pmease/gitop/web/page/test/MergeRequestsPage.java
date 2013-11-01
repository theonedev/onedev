package com.pmease.gitop.web.page.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.manager.VoteManager;
import com.pmease.gitop.core.model.Branch;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.MergeRequestUpdate;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.Vote;
import com.pmease.gitop.core.model.VoteInvitation;
import com.pmease.gitop.web.page.AbstractLayoutPage;

@SuppressWarnings("serial")
public class MergeRequestsPage extends AbstractLayoutPage {

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new ListView<MergeRequest>("mergeRequests", new LoadableDetachableModel<List<MergeRequest>>() {

            @Override
            protected List<MergeRequest> load() {
                List<MergeRequest> mergeRequests = new ArrayList<MergeRequest>();
                for (String branchName: getProject().listBranches()) {
                	Branch branch = Gitop.getInstance(BranchManager.class).find(getProject(), branchName, true);
                	for (MergeRequest request: branch.getIngoingRequests()) {
                		if (request.getStatus() != MergeRequest.Status.CLOSED && request.getLastCheckResult() != null)
                			mergeRequests.add(request);
                	}
                }
                
                return mergeRequests;
            }
		    
		}) {

            @Override
            protected void populateItem(final ListItem<MergeRequest> requestItem) {
                MergeRequest request = requestItem.getModelObject();
                requestItem.add(new Label("title", request.findTitle()));

                requestItem.add(new Label("status", request.getStatus().toString()));
                
                requestItem.add(new ListView<String>("reasons", request.getLastCheckResult().getReasons()) {

                    @Override
                    protected void populateItem(ListItem<String> item) {
                        item.add(new Label("reason", item.getModelObject()));
                    }
                    
                });
                
                Link<Void> mergeLink = new Link<Void>("merge") {

					@Override
					public void onClick() {
						MergeRequest request = requestItem.getModelObject();
						request.merge();
					}

                };
                
                if (request.getStatus() != MergeRequest.Status.PENDING_MERGE)
                	mergeLink.add(new AttributeAppender("class", " disabled"));
                
                requestItem.add(mergeLink);
                
                requestItem.add(new Link<Void>("close") {

					@Override
					public void onClick() {
						MergeRequest request = requestItem.getModelObject();
						request.close();
					}
                	
                });
                
                requestItem.add(new ListView<MergeRequestUpdate>("updates", new AbstractReadOnlyModel<List<MergeRequestUpdate>>() {

                    @Override
                    public List<MergeRequestUpdate> getObject() {
                        return (List<MergeRequestUpdate>) requestItem.getModelObject().getUpdates();
                    }
                    
                }) {

                    @Override
                    protected void populateItem(final ListItem<MergeRequestUpdate> updateItem) {
                        MergeRequestUpdate update = updateItem.getModelObject();
                        updateItem.add(new Label("title", update.getSubject()));
                        
                        updateItem.add(new Label("commitHash", update.getCommitHash()));

                        Collection<String> approvedUsers = new ArrayList<>();
                        Collection<String> rejectedUsers = new ArrayList<>();
                        
                        for (Vote vote: update.getVotes()) {
                            if (vote.getResult().isAccept())
                                approvedUsers.add(vote.getVoter().getName());
                            else
                                rejectedUsers.add(vote.getVoter().getName());
                        }
                        
                        updateItem.add(new Label("acceptedBy", StringUtils.join(approvedUsers.iterator(), ",")));
                        updateItem.add(new Label("rejectedBy", StringUtils.join(rejectedUsers.iterator(), ",")));
                    }
                    
                });
                
                requestItem.add(new ListView<User>("usersCanVote", new LoadableDetachableModel<List<User>>() {

                    @Override
                    public List<User> load() {
                    	List<User> usersCanVote = new ArrayList<>();
                    	MergeRequest request = requestItem.getModelObject();
                    	for (User user: Gitop.getInstance(UserManager.class).query()) {
                    		if (request.getLastCheckResult().canVote(user, request))
                    			usersCanVote.add(user);
                    	}
                    	
                    	return usersCanVote;
                    }
                    
                }) {

                    @Override
                    protected void populateItem(final ListItem<User> userItem) {
                        User user = userItem.getModelObject();
                        userItem.add(new Label("name", user.getName()));
                        
                        userItem.add(new Link<Void>("accept") {

                            @Override
                            public void onClick() {
                                Vote vote = new Vote();
                                vote.setResult(Vote.Result.ACCEPT);
                                vote.setUpdate(requestItem.getModelObject().getLatestUpdate());
                                vote.getUpdate().getVotes().add(vote);
                                vote.setVoter(userItem.getModelObject());
                                vote.getVoter().getVotes().add(vote);
                                Gitop.getInstance(VoteManager.class).save(vote);
                                
                                requestItem.getModelObject().check();
                            }
                            
                        });
                        userItem.add(new Link<Void>("reject") {

                            @Override
                            public void onClick() {
                                Vote vote = new Vote();
                                vote.setResult(Vote.Result.REJECT);
                                vote.setUpdate(requestItem.getModelObject().getLatestUpdate());
                                vote.getUpdate().getVotes().add(vote);
                                vote.setVoter(userItem.getModelObject());
                                vote.getVoter().getVotes().add(vote);
                                Gitop.getInstance(VoteManager.class).save(vote);

                                requestItem.getModelObject().check();
                            }
                            
                        });
                    }
                    
                });
                
                Set<String> invitedUserNames = new HashSet<>();
                for (VoteInvitation invitation: request.getVoteInvitations())
                	invitedUserNames.add(invitation.getVoter().getName());
                requestItem.add(new Label("invitedUsers", StringUtils.join(invitedUserNames, ",")));
            }
		    
		});
	}
	
	private Project getProject() {
	    return Gitop.getInstance(ProjectManager.class).load(1L);
	}

    @Override
    protected String getPageTitle() {
        return "test";
    }

}
