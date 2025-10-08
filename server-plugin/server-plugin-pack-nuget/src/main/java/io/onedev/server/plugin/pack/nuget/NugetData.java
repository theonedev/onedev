package io.onedev.server.plugin.pack.nuget;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;

public class NugetData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final String nupkgBlobSha256Hash;
	
	private final String snupkgBlobSha256Hash;
	
	private final byte[] metadata;
	
	public NugetData(String nupkgBlobSha256Hash, @Nullable String snupkgBlobSha256Hash, 
					 byte[] metadata) {
		this.nupkgBlobSha256Hash = nupkgBlobSha256Hash;
		this.snupkgBlobSha256Hash = snupkgBlobSha256Hash;
		this.metadata = metadata;
	}

	public String getNupkgBlobSha256Hash() {
		return nupkgBlobSha256Hash;
	}

	@Nullable
	public String getSnupkgBlobSha256Hash() {
		return snupkgBlobSha256Hash;
	}

	public byte[] getMetadata() {
		return metadata;
	}
}
