package com.pmease.commons.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.DirectoryScanner;

import com.pmease.commons.bootstrap.BootstrapUtils;

public class FileUtils extends org.apache.commons.io.FileUtils {
	
	/**
	 * Load properties from specified file. Surrounding white spaces of property values will 
	 * be trimmed off. And a property is considered not defined if the value is trimmed to 
	 * empty.
	 * @param file 
	 * 			file to load properties from
	 * @return
	 */
	public static Properties loadProperties(File file) {
		Properties props = new Properties();
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			props.load(is);
			
			for (Iterator<Entry<Object, Object>> it = props.entrySet().iterator(); it.hasNext();) {
				Entry<Object, Object> entry = it.next();
				String value = (String) entry.getValue();
				value = value.trim();
				if (value.length() == 0)
					it.remove();
				else
					entry.setValue(value);
			}
			return props;
		} catch (Exception e) {
			throw BootstrapUtils.unchecked(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	/**
	 * Load properties from specified path inside specified directory or jar file. 
	 * @param file
	 * 			A jar file or a directory to load property file from.
	 * @param path 
	 * 			relative path of the property file inside the jar or to the directory.
	 * @return
	 * 			Content of the property file. Null if not found.
	 */
	public static Properties loadProperties(File file, String path) {
		if (file.isFile() && file.getName().endsWith(".jar")) {
			ZipFile zip = null;
			try {
				zip = new ZipFile(file);
				ZipEntry entry = zip.getEntry(path);
				if (entry != null) {
					InputStream is = null;
					try {
						is = zip.getInputStream(entry);
						Properties props = new Properties();
						props.load(is);
						return props;
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						if (is != null) {
							is.close();
						}
					}
				} else {
					return null;
				}
			} catch (Exception e) {
				throw BootstrapUtils.unchecked(e);
			} finally {
				if (zip != null) {
					try {
						zip.close();
					} catch (IOException e) {
					}
				}
			}
		} else if (file.isDirectory() && new File(file, path).exists()) {
			Properties props = new Properties();
			InputStream is = null;
			try {
				is = new FileInputStream(new File(file, path));
				props.load(is);
				return props;
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		} else {
			return null;
		}
	}

    /**
     * Get default file encoding of underlying OS
     * @return
     */
    public static String getDefaultEncoding() {
    	return new InputStreamReader(new ByteArrayInputStream(new byte[0])).getEncoding();
    }
    
    /**
     * List all files matching specified patterns under specified base directory.
     * 
     * @param baseDir 
     * 			Base directory to scan files in
     * @param pathPattern 
     * 			Pattern of file path to be used for search
     * @return
     * 			Collection of files matching specified path pattern. Directories will not be included even 
     * 			if its path matches the pattern
     */
    public static Collection<File> listFiles(File baseDir, String pathPattern) {
    	Collection<File> files = new ArrayList<File>();
    	
    	DirectoryScanner scanner = new DirectoryScanner();
    	scanner.setBasedir(baseDir);
    	scanner.setIncludes(new String[]{pathPattern});
    	scanner.scan();
    	
    	for (String path: scanner.getIncludedFiles()) 
    		files.add(new File(baseDir, path));
    	return files;
    }

    public static File createTempDir(String prefix) {
        File temp;

        try {
			temp = File.createTempFile(prefix, "");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

        if (!temp.delete())
            throw new RuntimeException("Could not delete temp file: " + temp.getAbsolutePath());

        if (!temp.mkdir())
            throw new RuntimeException("Could not create temp directory: " + temp.getAbsolutePath());

        return temp;    
    }
    
    public static File createTempDir() {
    	return createTempDir("temp");
    }
    
    public static void deleteDir(File dir) {
    	try {
			deleteDirectory(dir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
	public static void deleteFile(File file) {
		/*
		 * Do not test existence of file here as broken symbol link will report file as non-existent  
		if (!file.exists())
			return;
		*/

		int maxTries = 10;
    	int numTries = 1;

    	while (true) {
    		if (file.delete())
    			break;
    		
    		if (file.exists()) {
            	if (numTries == maxTries) {
            		throw new GeneralException("Failed to delete file '" + 
            				file.getAbsolutePath() + "'.");
            	} else {
                    System.gc();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e2) {
                    }
                    numTries++;
            	}
    		} else {
    			break;
    		}
        }
	}    
    
	public static void writeFile(File file, String content, String encoding) {
		try {
			org.apache.commons.io.FileUtils.writeStringToFile(file, content, encoding);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
    
	public static void writeFile(File file, String content) {
		try {
			org.apache.commons.io.FileUtils.writeStringToFile(file, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void touchFile(File file) {
		try {
			org.apache.commons.io.FileUtils.touch(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void createDir(File dir) {
		if (!dir.exists()) {
			if (!dir.mkdirs())
				throw new GeneralException("Unable to create directory: " + dir.getAbsolutePath());
		}
	}
	
	public static void cleanDir(File dir) {
		if (dir.exists()) {
			try {
				org.apache.commons.io.FileUtils.cleanDirectory(dir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			createDir(dir);
		}
	}
}
