package io.onedev.server.infomanager;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.git.GitContribution;
import io.onedev.server.git.GitContributor;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.Day;
import io.onedev.server.util.NameAndEmail;

public interface CommitInfoManager {
	
	List<String> getFiles(Project project);
	
	int getCommitCount(Project project, User user, String path);
	
	int getCommitCount(Project project);
	
	List<NameAndEmail> getUsers(Project project);
	
	/**
	 * Given an ancestor commit, get all its descendant commits including the ancestor commit itself. 
	 * The result might be incomplete if some commits have not be cached yet
	 *  
	 * @param project
	 * 			project to get descendant commits
	 * @param ancestors
	 * 			for which commits to get descendants
	 * @return
	 * 			descendant commits
	 */
	Collection<ObjectId> getDescendants(Project project, Collection<ObjectId> ancestors);
	
	void cloneInfo(Project from, Project to);
	
	Collection<String> getHistoryPaths(Project project, String path);
	
	/**
	 * Get overall contributions
	 * 
	 * @param project
	 * 			project to get daily commits for
	 * @return
	 * 			map of day to contribution
	 */
	Map<Day, GitContribution> getOverallContributions(Project project);
	
	/**
	 * Get list of top contributors
	 * 
	 * @param project
	 * 			project to get top contributors for
	 * @param top
	 * 			number of top contributors to get
	 * @param type
	 * 			type of contribution to order by
	 * @param fromDay
	 * 			from day
	 * @param toDay
	 * 			to day
	 * @return
	 * 			list of top user contributors, reversely ordered by number of contributions 
	 */
	List<GitContributor> getTopContributors(Project project, int top, GitContribution.Type type, int fromDay, int toDay);

	/**
	 * Get source code line statistics over time
	 * 
	 * @param project
	 * 			project to get line stats for
	 * @return
	 * 			line statistics data
	 */
	Map<Day, Map<String, Integer>> getLineIncrements(Project project);

	Collection<ObjectId> getFixCommits(Project project, Long issueNumber);
	
	void sortUsersByContribution(List<User> users, Project project, Collection<String> files);
	
}