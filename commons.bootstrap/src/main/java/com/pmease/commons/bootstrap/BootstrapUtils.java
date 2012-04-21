package com.pmease.commons.bootstrap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BootstrapUtils {
	
	public static Object readObject(File file) {
    	ObjectInputStream ois = null;
    	try {
    		ois = new ObjectInputStream(new FileInputStream(file));
    		return ois.readObject();
    	} catch (Exception e) {
			throw unchecked(e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
				}
			}
    	}
	}
	
	public static void cleanDir(File dir) {
		visitDir(dir, new FileVisitor() {

			@Override
			public boolean visit(File file) {
				if (!file.delete())
					throw new RuntimeException("Unable to delete file '" + file.getAbsolutePath() + "'.");
				return true;
			}
			
		});
	}
	
	public static RuntimeException unchecked(Throwable e) {
		if (e instanceof RuntimeException)
			throw (RuntimeException)e;
		else
			throw new RuntimeException(e);
	}

	/**
	 * @param srcFile 
	 * 		zip file to extract from
	 * @param 
	 * 		destDir destination directory to extract to
	 * @param matcher 
	 * 		if not null, only entries with name matching this param in the zip will be extracted.
	 * 		Use null if you want to extract all entries from the zip file.  
	 */
	public static void unzip(File srcFile, File destDir, StringMatcher matcher) {
		int BUFFER = 8192;
		
	    ZipInputStream zis = null;
	    try {
		    zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(srcFile)));
		    ZipEntry entry;
		    while((entry = zis.getNextEntry()) != null) {
		    	if (matcher == null || matcher.matches(entry.getName())) {
				    BufferedOutputStream bos = null;
				    try {
				        bos = new BufferedOutputStream(new FileOutputStream(new File(destDir, entry.getName())));
	
				        int count;
				        byte data[] = new byte[BUFFER];
				        while ((count = zis.read(data, 0, BUFFER)) != -1) 
				        	bos.write(data, 0, count);
				        bos.flush();
				    } finally {
				    	if (bos != null)
				    		bos.close();
				    }
		    	}
		    }
	    } catch (Exception e) {
	    	throw unchecked(e);
	    } finally {
	    	if (zis != null) {
				try {
					zis.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
	    	}
	    }
	} 	
	
	public static boolean visitDir(File dir, FileVisitor visitor) {
		if (!dir.exists())
			throw new RuntimeException("Directory '" + dir.getAbsolutePath() + "' does not exist.");
		if (dir.isFile()) 
			throw new RuntimeException("Path '" + dir.getAbsolutePath() + "' points to a file.");
		
		for (File file: dir.listFiles()) {
			if (file.isDirectory() && visitDir(file, visitor) || visitor.visit(file))
				return true;
		}
		return false;
	}
	
}
