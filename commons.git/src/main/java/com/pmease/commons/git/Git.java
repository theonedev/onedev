package com.pmease.commons.git;

import java.io.File;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class Git {
	
	private static final String GIT_EXE = "git";
	
	private static final String MIN_VERSION = "1.0.0";
	
	private final File repoDir;
	
	private Map<String, String> environments;
	
	public Git(final File repoDir, @Nullable final Map<String, String> environments) {
		this.repoDir = repoDir;
		if (!repoDir.exists())
		    FileUtils.createDir(repoDir);
		
		this.environments = environments;
	}
	
	public Git(final File repoDir) {
		this(repoDir, null);
	}
	
	public File repoDir() {
	    return repoDir;
	}
	
	public UploadCommand upload() {
		return new UploadCommand(this);
	}
	
	public ReceiveCommand receive() {
		return new ReceiveCommand(this);
	}

	public AdvertiseUploadRefsCommand advertiseUploadRefs() {
		return new AdvertiseUploadRefsCommand(this);
	}
	
	public AdvertiseReceiveRefsCommand advertiseReceiveRefs() {
		return new AdvertiseReceiveRefsCommand(this);
	}
	
	public InitCommand init() {
		return new InitCommand(this);
	}
	
	public ListBranchesCommand listBranches() {
	    return new ListBranchesCommand(this);
	}
	
	public ListTagsCommand listTags() {
	    return new ListTagsCommand(this);
	}

	public GetCommitCommand getCommit() {
	    return new GetCommitCommand(this);
	}
	
	public MergeCommand merge() {
	    return new MergeCommand(this);
	}
	
	public AddCommand add() {
		return new AddCommand(this);
	}
	
	public CommitCommand commit() {
		return new CommitCommand(this);
	}
	
	public ListChangedFilesCommand listChangedFiles() {
		return new ListChangedFilesCommand(this);
	}
	
	public ListFilesCommand listFiles() {
		return new ListFilesCommand(this);
	}
	
	public CheckAncestorCommand checkAncestor() {
		return new CheckAncestorCommand(this);
	}
	
	public CalcMergeBaseCommand calcMergeBase() {
		return new CalcMergeBaseCommand(this);
	}
	
	public UpdateRefCommand updateRef() {
	    return new UpdateRefCommand(this);
	}
	
    public DeleteRefCommand deleteRef() {
        return new DeleteRefCommand(this);
    }
    
    public CheckoutCommand checkout() {
        return new CheckoutCommand(this);
    }
    
    public BranchCommand branch() {
        return new BranchCommand(this);
    }

    /**
	 * Check if there are any errors with git command line. 
	 *
	 * @return
	 * 			error message if failed to check git command line, 
	 * 			or <tt>null</tt> otherwise
	 * 			
	 */
	public static String checkError() {
		try {
			final String[] version = new String[]{null};
			
			new Commandline(GIT_EXE).addArgs("--version").execute(new LineConsumer() {
	
				@Override
				public void consume(String line) {
					if (line.trim().length() != 0)
						version[0] = line.trim();
				}
				
			}, new LineConsumer.ErrorLogger()).checkReturnCode();
	
			if (version[0] == null)
				throw new RuntimeException("Unable to determine git version.");
			
			GitVersion gitVersion = new GitVersion(version[0]);
			
			if (gitVersion.isOlderThan(new GitVersion(MIN_VERSION)))
				throw new RuntimeException("Git version should be at least " + MIN_VERSION);
			
			return null;
			
		} catch (Exception e) {
			return ExceptionUtils.getMessage(e);
		}
	}
	
	public Commandline cmd() {
		Commandline cmd = new Commandline(GIT_EXE).workingDir(repoDir);
		if (environments != null)
			cmd.environment(environments);
		return cmd;
	}

}
