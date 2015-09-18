package com.pmease.gitplex.web.page.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UploadServlet extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Compute the output file name extension based on image MIME type
	    String mimeType = request.getContentType();
	    String extension = null;
		String supportedFormatTable[][] = {
			{"image/png", "png"},
			{"image/jpeg", "jpg"},
			{"image/gif", "gif"}
		};
		
		for (int i = 0; i < supportedFormatTable.length; ++i) {
			if (supportedFormatTable[i][0].equals(mimeType)) {
				extension = supportedFormatTable[i][1];
				
				break;
			}
		}
		
		if (extension == null) {
			throw new IOException("Unsupported data type: " + mimeType);
		}

	    //Read the file contents from the input stream
		File saveTo = new File("w:\\temp\\image." + extension);
		FileOutputStream os = new FileOutputStream(saveTo);
		InputStream is = request.getInputStream();
	    byte buff[] = new byte[256];
	    int len;

	    while ((len = is.read(buff)) > 0) {
	    	os.write(buff, 0, len);
	    }
	    os.close();
	    
	    //Show the file name in browser
	    PrintWriter out = response.getWriter();
	    
	    out.println("Saved image to: " + saveTo.getAbsolutePath());
	}
}
