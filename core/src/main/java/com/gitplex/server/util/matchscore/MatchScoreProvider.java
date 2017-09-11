package com.gitplex.server.util.matchscore;

public interface MatchScoreProvider<T> {
	
	double getMatchScore(T object);
	
}
