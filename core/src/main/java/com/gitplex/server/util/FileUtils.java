package com.gitplex.server.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.bootstrap.Bootstrap;
import com.gitplex.launcher.bootstrap.BootstrapUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class FileUtils extends org.apache.commons.io.FileUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
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
	@Nullable
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

	public static boolean isWritable(File dir) {
		boolean dirExists = dir.exists();
		File tempFile = null;
		try {
			FileUtils.createDir(dir);
			tempFile = File.createTempFile("test", "test", dir);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (tempFile != null)
				tempFile.delete();
			if (!dirExists)
				deleteDir(dir);
		}
	}
	
	public static boolean isOutsideOfInstallDir(File dir) {
		File normalizedDir;
		try {
			normalizedDir = dir.getCanonicalFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return !normalizedDir.toPath().startsWith(Bootstrap.installDir.toPath());
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

    /**
     * Delete directory if specified directory exists, or do nothing if the 
     * directory does not exist. 
     * 
     * @param dir
     * 			directory to be deleted. Does not have to be exist
     */
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
	
	/**
	 * Create the directory if it does not exist, or do nothing if it 
	 * already exists. 
	 * 
	 * @param dir
	 * 			directory to be created. Does not have to be non-existent
	 */
	public static void createDir(File dir) {
		BootstrapUtils.createDir(dir);
	}
	
	/**
	 * Delete all files/sub directories under specified directory. If the directory 
	 * does not exist, create the directory. 
	 * 
	 * @param dir
	 *			directory to be cleaned up 
	 */
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
	
	@SuppressWarnings("unchecked")
	public static <T> T readFile(File file, Callable<T> callable) {
		T result = null;
		String lockName;
		try {
			lockName = file.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Lock lock = LockUtils.getReadWriteLock(lockName).readLock();
		try {
			lock.lockInterruptibly();
			if (file.exists()) {
				byte[] bytes = FileUtils.readFileToByteArray(file);
				result = (T) SerializationUtils.deserialize(bytes);
			}
		} catch (Exception e) {
			logger.error("Error reading callable result from file '" + file.getAbsolutePath() 
					+ "', fall back to execute callable.", e);
		} finally {
			lock.unlock();
		}
		
		if (result == null) {
			FileUtils.createDir(file.getParentFile());
			
			lock = LockUtils.getReadWriteLock(lockName).writeLock();
			try {
				lock.lockInterruptibly();
				Preconditions.checkNotNull(result = callable.call());
				
				FileUtils.writeByteArrayToFile(file, SerializationUtils.serialize((Serializable) result));
			} catch (Exception e) {
				Throwables.propagate(e);
			} finally {
				lock.unlock();
			}
		}
		
		return result;
	}

}
