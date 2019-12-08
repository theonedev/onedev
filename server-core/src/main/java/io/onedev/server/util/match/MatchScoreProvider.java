package io.onedev.server.util.match;

public interface MatchScoreProvider<T> {
	
	double getMatchScore(T object);
	
}
