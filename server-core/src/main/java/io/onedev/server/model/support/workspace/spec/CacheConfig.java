package io.onedev.server.model.support.workspace.spec;

import static io.onedev.k8shelper.UploadStrategy.UPLOAD_IF_NOT_EXACT_MATCH;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.tuple.Pair;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.k8shelper.CacheConfigFacade;
import io.onedev.k8shelper.UploadStrategy;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
public class CacheConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String key;
	
	private String checksumFiles;
	
	private List<CacheEntry> entries = new ArrayList<>();
	
	private UploadStrategy uploadStrategy = UPLOAD_IF_NOT_EXACT_MATCH;
	
	private String uploadProjectPath;
	
	private String uploadAccessToken;

	@Editable(order=100, name="Cache Key", description = """
			This key will be used to identify the cache in project hierarchy (search from current 
			project to root project in order), together with checksum (see below). An exact match 
			means that both key and checksum match, and a partial match means that only key 
			matches""")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Editable(order=175, name="Checksum Files", description = """
			Optionally specify files to compute checksum from. This is useful when your project 
			has lock files (package-lock.json, pom.xml, etc.) that represent cache state. When 
			checksum changes, cache can still be loaded as a partial match, but 
			will be re-uploaded with the new checksum if upload strategy is set to <i>Upload If Not Exact Match</i>. 
			Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. 
			Multiple files should be separated by space, and single file containing space should be quoted. 
			Non-absolute file is relative to <a href='https://docs.onedev.io/concepts#job-workdir' target='_blank'>job working directory</a>.<br> 
			<b>NOTE: </b> An empty checksum is assumed if this property is empty.""")
	@Interpolative(variableSuggester="suggestVariables")
	public String getChecksumFiles() {
		return checksumFiles;
	}

	public void setChecksumFiles(String checksumFiles) {
		this.checksumFiles = checksumFiles;
	}

	@Editable(order=300, name="Cache Entries", description = "Specify cache entries")
	@Valid
	@Size(min=1, max=100)
	public List<CacheEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<CacheEntry> entries) {
		this.entries = entries;
	}

	public List<String> getPaths() {
		return getEntries().stream().map(CacheEntry::getPath).collect(Collectors.toList());
	}

	public void setPaths(List<String> paths) {
		entries = paths.stream().map(it -> {
			var entry = new CacheEntry();
			entry.setPath(it);
			return entry;
		}).collect(Collectors.toList());
	}
	
	@Editable(order=400, description = """
			Specify cache upload strategy after build successful. <i>Upload If Not Exact Match</i> 
			means to upload when no cache found with matching key and checksum , and 
			<i>Upload If Changed</i> means to upload if some files in cache path are changed""")
	@NotNull
	public UploadStrategy getUploadStrategy() {
		return uploadStrategy;
	}

	public void setUploadStrategy(UploadStrategy uploadStrategy) {
		this.uploadStrategy = uploadStrategy;
	}

	@Editable(order=450, name="Upload to Project", placeholder = "Current project", description = """
			In case cache needs to be uploaded, this property specifies target project for the upload. 
			Leave empty for current project""")
	@ProjectChoice
	public String getUploadProjectPath() {
		return uploadProjectPath;
	}

	public void setUploadProjectPath(String uploadProjectPath) {
		this.uploadProjectPath = uploadProjectPath;
	}

	@Editable(order=500, description = """
			Specify access token with upload cache permission for above project. 
			Note that this property is not required if upload cache to current project""")
	public String getUploadAccessToken() {
		return uploadAccessToken;
	}

	public void setUploadAccessToken(String uploadAccessToken) {
		this.uploadAccessToken = uploadAccessToken;
	}
		
	public CacheConfigFacade getFacade() {
		Pair<Set<String>, Set<String>> parsedChecksumFiles = null;
		if (checksumFiles != null) {
			var patterns = PatternSet.parse(checksumFiles);
			parsedChecksumFiles = Pair.of(patterns.getIncludes(), patterns.getExcludes());
		}
		return new CacheConfigFacade(key, parsedChecksumFiles,
				getEntries().stream().map(CacheEntry::getFacade).collect(Collectors.toList()), uploadStrategy,
				uploadProjectPath, uploadAccessToken);
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}
		
}
