package io.onedev.server.terminal;

public interface Shell {

	void sendInput(String input);
	
	void resize(int rows, int cols);
	
	void exit();

}
