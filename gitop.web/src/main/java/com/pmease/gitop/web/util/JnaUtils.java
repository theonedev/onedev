package com.pmease.gitop.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Library;
import com.sun.jna.Native;

/*
 * Copyright 2013 gitblit.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Collection of static methods to access native OS library functionality.
 *
 * @author Florian Zschocke
 */
public class JnaUtils {
	public static final int S_ISUID =  0004000; // set user id on execution
	public static final int S_ISGID =  0002000; // set group id on execution
	public static final int S_ISVTX =  0001000; // sticky bit, save swapped text even after use

	public static final int S_IRWXU =  0000700; // RWX mask for owner
	public static final int S_IRUSR =  0000400; // read permission for owner
	public static final int S_IWUSR =  0000200; // write permission for owner
	public static final int S_IXUSR =  0000100; // execute/search permission for owner
	public static final int S_IRWXG =  0000070; // RWX mask for group
	public static final int S_IRGRP =  0000040; // read permission for group
	public static final int S_IWGRP =  0000020; // write permission for group
	public static final int S_IXGRP =  0000010; // execute/search permission for group
	public static final int S_IRWXO =  0000007; // RWX mask for other
	public static final int S_IROTH =  0000004; // read permission for other
	public static final int S_IWOTH =  0000002; // write permission for other
	public static final int S_IXOTH =  0000001; // execute/search permission for other

	public static final int S_IFMT =   0170000; // type of file mask
	public static final int S_IFIFO =  0010000; // named pipe (fifo)
	public static final int S_IFCHR =  0020000; // character special device
	public static final int S_IFDIR =  0040000; // directory
	public static final int S_IFBLK =  0060000; // block special device
	public static final int S_IFREG =  0100000; // regular file
	public static final int S_IFLNK =  0120000; // symbolic link
	public static final int S_IFSOCK = 0140000; // socket


	private static final Logger LOGGER = LoggerFactory.getLogger(JnaUtils.class);

	private static UnixCLibrary unixlibc = null;


	/**
	 * Utility method to check if the JVM is running on a Windows OS.
	 *
	 * @return true, if the system property 'os.name' starts with 'Windows'.
	 */
	public static boolean isWindows()
	{
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}


	private interface UnixCLibrary extends Library {
		public int chmod(String path, int mode);
		public int getgid();
		public int getegid();
	}


	public static int getgid()
	{
		if (isWindows()) {
			throw new UnsupportedOperationException("The method JnaUtils.getgid is not supported under Windows.");
		}

		return getUnixCLibrary().getgid();
	}


	public static int getegid()
	{
		if (isWindows()) {
			throw new UnsupportedOperationException("The method JnaUtils.getegid is not supported under Windows.");
		}

		return getUnixCLibrary().getegid();
	}


	/**
	 * Set the permission bits of a file.
	 *
	 * The permission bits are set to the provided mode. This method is only
	 * implemented for OSes of the Unix family and makes use of the 'chmod'
	 * function of the native C library. See 'man 2 chmod' for more information.
	 *
	 * @param path
	 * 			File/directory to set the permission bits for.
	 * @param mode
	 * 			A mode created from or'd permission bit masks S_I*
	 * @return	Upon successful completion, a value of 0 returned. Otherwise, a value of -1 is returned.
	 */
	public static int setFilemode(File file, int mode)
	{
		return setFilemode(file.getAbsolutePath(), mode);
	}

	/**
	 * Set the permission bits of a file.
	 *
	 * The permission bits are set to the provided mode. This method is only
	 * implemented for OSes of the Unix family and makes use of the 'chmod'
	 * function of the native C library. See 'man 2 chmod' for more information.
	 *
	 * @param path
	 * 			Path to a file/directory to set the permission bits for.
	 * @param mode
	 * 			A mode created from or'd permission bit masks S_I*
	 * @return	Upon successful completion, a value of 0 returned. Otherwise, a value of -1 is returned.
	 */
	public static int setFilemode(String path, int mode)
	{
		if (isWindows()) {
			throw new UnsupportedOperationException("The method JnaUtils.getFilemode is not supported under Windows.");
		}

		return getUnixCLibrary().chmod(path, mode);
	}



	/**
	 * Get the file mode bits of a file.
	 *
	 * This method is only implemented for OSes of the Unix family. It returns the file mode
	 * information as available in the st_mode member of the resulting struct stat when calling
	 * 'lstat' on a file.
	 *
	 * @param path
	 * 			File/directory to get the file mode from.
	 * @return	Upon successful completion, the file mode bits are returned. Otherwise, a value of -1 is returned.
	 */
	public static int getFilemode(File path)
	{
		return getFilemode(path.getAbsolutePath());
	}

	/**
	 * Get the file mode bits of a file.
	 *
	 * This method is only implemented for OSes of the Unix family. It returns the file mode
	 * information as available in the st_mode member of the resulting struct stat when calling
	 * 'lstat' on a file.
	 *
	 * @param path
	 * 			Path to a file/directory to get the file mode from.
	 * @return	Upon successful completion, the file mode bits are returned. Otherwise, a value of -1 is returned.
	 */
	public static int getFilemode(String path)
	{
		if (isWindows()) {
			throw new UnsupportedOperationException("The method JnaUtils.getFilemode is not supported under Windows.");
		}

		Filestat stat = getFilestat(path);
		if ( stat == null ) return -1;
		return stat.mode;
	}


	/**
	 * Status information of a file.
	 */
	public static class Filestat
	{
		public int mode;  // file mode, permissions, type
		public int uid;   // user Id of owner
		public int gid;   // group Id of owner

		Filestat(int mode, int uid, int gid) {
			this.mode = mode; this.uid = uid; this.gid = gid;
		}
	}


	/**
	 * Get Unix file status information for a file.
	 *
	 * This method is only implemented for OSes of the Unix family. It returns file status
	 * information for a file. Currently this is the file mode, the user id and group id of the owner.
	 *
	 * @param path
	 * 			File/directory to get the file status from.
	 * @return	Upon successful completion, a Filestat object containing the file information is returned.
	 * 			Otherwise, null is returned.
	 */
	public static Filestat getFilestat(File path)
	{
		return getFilestat(path.getAbsolutePath());
	}


	/**
	 * Get Unix file status information for a file.
	 *
	 * This method is only implemented for OSes of the Unix family. It returns file status
	 * information for a file. Currently this is the file mode, the user id and group id of the owner.
	 *
	 * @param path
	 * 			Path to a file/directory to get the file status from.
	 * @return	Upon successful completion, a Filestat object containing the file information is returned.
	 * 			Otherwise, null is returned.
	 */
	public static Filestat getFilestat(String path)
	{
		if (isWindows()) {
			throw new UnsupportedOperationException("The method JnaUtils.getFilestat is not supported under Windows.");
		}


		int mode = 0;

		// Use a Runtime, because implementing stat() via JNA is just too much trouble.
		// This could be done with the 'stat' command, too. But that may have a shell specific implementation, so we use 'ls' instead.
		String lsLine = runProcessLs(path);
		if (lsLine == null) {
			LOGGER.debug("Could not get file information for path " + path);
			return null;
		}

		Pattern p = Pattern.compile("^(([-bcdlspCDMnP?])([-r][-w][-xSs])([-r][-w][-xSs])([-r][-w][-xTt]))[@+.]? +[0-9]+ +([0-9]+) +([0-9]+) ");
		Matcher m = p.matcher(lsLine);
		if ( !m.lookingAt() ) {
			LOGGER.debug("Could not parse valid file mode information for path " + path);
			return null;
		}

		// Parse mode string to mode bits
		String group = m.group(2);
		switch (group.charAt(0)) {
		case 'p' :
			mode |= 0010000; break;
		case 'c':
			mode |= 0020000; break;
		case 'd':
			mode |= 0040000; break;
		case 'b':
			mode |= 0060000; break;
		case '-':
			mode |= 0100000; break;
		case 'l':
			mode |= 0120000; break;
		case 's':
			mode |= 0140000; break;
		}

		for ( int i = 0; i < 3; i++) {
			group = m.group(3 + i);
			switch (group.charAt(0)) {
			case 'r':
				mode |= (0400 >> i*3); break;
			case '-':
				break;
			}

			switch (group.charAt(1)) {
			case 'w':
				mode |= (0200 >> i*3); break;
			case '-':
				break;
			}

			switch (group.charAt(2)) {
			case 'x':
				mode |= (0100 >> i*3); break;
			case 'S':
				mode |= (04000 >> i); break;
			case 's':
				mode |= (0100 >> i*3);
				mode |= (04000 >> i); break;
			case 'T':
				mode |= 01000; break;
			case 't':
				mode |= (0100 >> i*3);
				mode |= 01000; break;
			case '-':
				break;
			}
		}

		return new Filestat(mode, Integer.parseInt(m.group(6)), Integer.parseInt(m.group(7)));
	}


	/**
	 * Run the unix command 'ls -ldn' on a single file and return the resulting output line.
	 *
	 * @param path
	 * 			Path to a single file or directory.
	 * @return The first line of output from the 'ls' command. Null, if an error occurred and no line could be read.
	 */
	private static String runProcessLs(String path)
	{
		String cmd = "ls -ldn " + path;
		Runtime rt = Runtime.getRuntime();
		Process pr = null;
		InputStreamReader ir = null;
		BufferedReader br = null;
		String output = null;

		try {
			pr = rt.exec(cmd);
			ir = new InputStreamReader(pr.getInputStream());
			br = new BufferedReader(ir);

			output = br.readLine();

			while (br.readLine() != null) ; // Swallow remaining output
		}
		catch (IOException e) {
			LOGGER.debug("Exception while running unix command '" + cmd + "': " + e);
		}
		finally {
			if (pr != null) try { pr.waitFor();	} catch (Exception ignored) {}

			if (br != null) try { br.close(); } catch (Exception ignored) {}
			if (ir != null) try { ir.close(); } catch (Exception ignored) {}

			if (pr != null) try { pr.getOutputStream().close();	} catch (Exception ignored) {}
			if (pr != null) try { pr.getInputStream().close();	} catch (Exception ignored) {}
			if (pr != null) try { pr.getErrorStream().close();	} catch (Exception ignored) {}
		}

		return output;
	}


	private static UnixCLibrary getUnixCLibrary()
	{
		if (unixlibc == null) {
			unixlibc = (UnixCLibrary) Native.loadLibrary("c", UnixCLibrary.class);
			if (unixlibc == null) throw new RuntimeException("Could not initialize native C library.");
		}
		return unixlibc;
	}

}
