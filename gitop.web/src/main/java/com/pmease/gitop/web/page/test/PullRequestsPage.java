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
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.manager.VoteManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.VoteInvitation;
import com.pmease.gitop.web.page.AbstractLayoutPage;

@SuppressWarnings("serial")
public class PullRequestsPage extends AbstractLayoutPage {

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new ListView<PullRequest>("pullRequests", new LoadableDetachableModel<List<PullRequest>>() {

            @Override
            protected List<PullRequest> load() {
                List<PullRequest> pullRequests = new ArrayList<PullRequest>();
                for (Branch branch: Gitop.getInstance(BranchManager.class).query()) {
                	for (PullRequest request: branch.getIngoingRequests()) {
                		if (request.getStatus() != PullRequest.Status.DECLINED && request.getCheckResult() != null)
                			pullRequests.add(request);
                	}
                }
                
                return pullRequests;
            }
		    
		}) {

            @Override
            protected void populateItem(final ListItem<PullRequest> requestItem) {
                PullRequest request = requestItem.getModelObject();
                requestItem.add(new Label("title", request.findTitle()));

                requestItem.add(new Label("status", request.getStatus().toString()));
                
                requestItem.add(new ListView<String>("reasons", request.getCheckResult().getReasons()) {

                    @Override
                    protected void populateItem(ListItem<String> item) {
                        item.add(new Label("reason", item.getModelObject()));
                    }
                    
                });
                
                Link<Void> mergeLink = new Link<Void>("merge") {

					@Override
					public void onClick() {
						PullRequest request = requestItem.getModelObject();
						Gitop.getInstance(PullRequestManager.class).merge(request);
					}

                };
                
                if (request.getStatus() != PullRequest.Status.PENDING_MERGE)
                	mergeLink.add(new AttributeAppender("class", " disabled"));
                
                requestItem.add(mergeLink);
                
                requestItem.add(new Link<Void>("close") {

					@Override
					public void onClick() {
						PullRequest request = requestItem.getModelObject();
						Gitop.getInstance(PullRequestManager.class).decline(request);
					}
                	
                });
                
                requestItem.add(new ListView<PullRequestUpdate>("updates", new AbstractReadOnlyModel<List<PullRequestUpdate>>() {

                    @Override
                    public List<PullRequestUpdate> getObject() {
                        return (List<PullRequestUpdate>) requestItem.getModelObject().getUpdates();
                    }
                    
                }) {

                    @Override
                    protected void populateItem(final ListItem<PullRequestUpdate> updateItem) {
                        PullRequestUpdate update = updateItem.getModelObject();
                        updateItem.add(new Label("title", update.getSubject()));
                        
                        updateItem.add(new Label("commitHash", update.getHeadCommit()));

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
                    	PullRequest request = requestItem.getModelObject();
                    	for (User user: Gitop.getInstance(UserManager.class).query()) {
                    		if (request.getCheckResult().canVote(user, request))
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
                                
                                Gitop.getInstance(PullRequestManager.class).refresh(requestItem.getModelObject());
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
                                Gitop.getInstance(PullRequestManager.class).refresh(requestItem.getModelObject());
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
	
    @Override
    protected String getPageTitle() {
        return "test";
    }

}
