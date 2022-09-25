package io.onedev.server.terminal;

public interface ShellSession {

	void sendInput(String input);
	
	void resize(int rows, int cols);
	
	void exit();

}
