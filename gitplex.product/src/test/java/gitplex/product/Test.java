package gitplex.product;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class Test {

	private InputStream download(String url) {
		try {
			HttpURLConnection urlConn = (HttpURLConnection) new URL(url).openConnection();
			urlConn.addRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
			return urlConn.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@org.junit.Test
	public void test() {
        InputStream is = null;
        OutputStream bos = null;
        try {
        	is = download("http://lark:6610/wicket/resource/org.apache.wicket.Application/a/l/o/n/g/p/a/t/h/bundle-ver-1472770062571.js");
        	
            byte[] data = new byte[2048];
            bos = new BufferedOutputStream(new FileOutputStream(new File("w:\\temp\\download.css")), 2048);
            int count;
            while ((count = is.read(data)) > -1) 
            	bos.write(data, 0, count);
        } catch (IOException e) {
        	throw new RuntimeException(e);
        } finally {
        	IOUtils.closeQuietly(is);
        	IOUtils.closeQuietly(bos);
        }
	}
	
}