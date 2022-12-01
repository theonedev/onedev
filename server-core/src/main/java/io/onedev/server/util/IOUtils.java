package io.onedev.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.onedev.commons.bootstrap.Bootstrap;

public class IOUtils extends org.apache.commons.io.IOUtils {

	public static void copyRange(InputStream in, OutputStream out, LongRange range) throws IOException {
		int totalSkipped = 0;
		while (totalSkipped < range.getStart())	 {
			long skipped = in.skip(range.getStart()-totalSkipped);
			if (skipped == 0)
				break;
			totalSkipped += skipped;
		}
		
		if (totalSkipped < range.getStart()) 
			throw new IOException("Skipped only " + totalSkipped + " bytes out of " + range.getStart() + " required.");

		long bytesToCopy = range.getEnd() - range.getStart() + 1;

		byte buffer[] = new byte[Bootstrap.BUFFER_SIZE];
		while (bytesToCopy > 0) {
			int bytesRead = in.read(buffer);
			if (bytesRead <= 0) {
				break;
			} else if (bytesRead <= bytesToCopy) {
				out.write(buffer, 0, bytesRead);
				bytesToCopy -= bytesRead;
			} else {
				out.write(buffer, 0, (int) bytesToCopy);
				bytesToCopy = 0;
			}
		}
	}

}
