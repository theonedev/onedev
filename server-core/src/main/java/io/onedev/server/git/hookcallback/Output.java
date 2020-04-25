package io.onedev.server.git.hookcallback;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class Output {

	private ServletOutputStream stream;
	
	public Output(ServletOutputStream stream) {
		this.stream = stream;
	}
	
    public void writeLine(String line) {
    	if (line.matches("\\*+"))
    		line = line + " ";
        try {
			stream.println(line);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
    public void writeLine() {
        try {
			stream.println(" ");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    public void markError() {
        writeLine("ERROR");
    }

}
