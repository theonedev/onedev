package com.pmease.commons.bootstrap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BootstrapUtils {
	
	private static final int BUFFER_SIZE = 64*1024;
	
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
	
	public static void createDir(File dir) {
		if (dir.exists()) {
            if (dir.isFile()) {
                throw new RuntimeException("Unable to create directory since the path " +
                		"is already used by a file: " + dir.getAbsolutePath());
            } 
		} else if (!dir.mkdirs()) {
            if (!dir.exists())
                throw new RuntimeException("Unable to create directory: " + dir.getAbsolutePath());
		}
	}

    /**
     * Zip specified directory recursively as specified file.
     * @param dir
     * @param file
     */
    public static void zip(File dir, File file) {
    	try (OutputStream os = new FileOutputStream(file)) {
    		zip(dir, os);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
		};
    }
    
    /**
     * Zip specified directory recursively as specified file.
     * @param dir
     * @param file
     */
    public static void zip(File dir, OutputStream os) {
    	try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os, BUFFER_SIZE))) {
			zos.setLevel(Deflater.DEFAULT_COMPRESSION);
			zip(zos, dir, "");
    	} catch (IOException e) {
    		throw new RuntimeException(e);
		}
    }
    
    private static void zip(ZipOutputStream zos, File dir, String basePath) {
		byte buffer[] = new byte[BUFFER_SIZE];
		
		try {
			if (basePath.length() != 0)
				zos.putNextEntry(new ZipEntry(basePath + "/"));

			for (File file: dir.listFiles()) {
				if (file.isDirectory()) {
					if (basePath.length() != 0)
						zip(zos, file, basePath + "/" + file.getName());
					else
						zip(zos, file, file.getName());
				} else {
					try (FileInputStream is = new FileInputStream(file)) {
						if (basePath.length() != 0)
							zos.putNextEntry(new ZipEntry(basePath + "/" + file.getName()));
						else
							zos.putNextEntry(new ZipEntry(file.getName()));
						int len;
					    while ((len = is.read(buffer)) > 0)
					    	zos.write(buffer, 0, len);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
    /**
	 * Unzip files matching specified matcher from specified file to specified directory.
	 * 
	 * @param srcFile 
	 * 		zip file to extract from
	 * @param 
	 * 		destDir destination directory to extract to
	 * @param matcher 
	 * 		if not null, only entries with name matching this param in the zip will be extracted.
	 * 		Use null if you want to extract all entries from the zip file.  
	 */
	public static void unzip(File srcFile, File destDir, StringMatcher matcher) {
	    try (InputStream is = new FileInputStream(srcFile);) {
	    	unzip(is, destDir, matcher);
	    } catch (Exception e) {
	    	throw unchecked(e);
	    }
	} 	
	
	/**
	 * Unzip files matching specified matcher from input stream to specified directory.
	 * 
	 * @param is
	 * 			input stream to unzip files from. This method will not close the stream 
	 * 			after using it. Caller should be responsible for closing
	 * @param destDir
	 * 			destination directory to extract files to
	 * @param matcher
	 * 			matcher to select matched files for extracting
	 */
	public static void unzip(InputStream is, File destDir, StringMatcher matcher) {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is, BUFFER_SIZE));		
	    try {
		    ZipEntry entry;
		    while((entry = zis.getNextEntry()) != null) {
		    	if (matcher == null || matcher.matches(entry.getName())) {
					if (entry.getName().endsWith("/")) {
						createDir(new File(destDir, entry.getName()));
					} else {		    		
					    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(destDir, entry.getName())), BUFFER_SIZE);) {
					        int count;
					        byte data[] = new byte[BUFFER_SIZE];
					        while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) 
					        	bos.write(data, 0, count);
					        bos.flush();
					    }
					}
		    	}
		    }
	    } catch (Exception e) {
	    	throw unchecked(e);
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
