package io.onedev.server.terminal;

public interface Shell {

	void writeToStdin(String data);
	
	void resize(int rows, int cols);
	
	void terminate();

}
