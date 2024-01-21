package io.onedev.server.util;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class StringTransformerTest {

    @Test
    public void transform() {
		var pattern = Pattern.compile("\\sin\\s(.*):line\\s(\\d+)(\\s|$)", Pattern.MULTILINE);
		var text = "----- Inner Stack Trace -----\n" +
				"   at Bit.Infrastructure.Dapper.Repositories.BaseRepository..ctor(String connectionString, String readOnlyConnectionString) in /Users/robin/Projects/onedev/server/server-product/target/sandbox/temp/server/onedev-build12565695818101171593/workspace/src/Infrastructure.Dapper/Repositories/BaseRepository.cs:line 16\n" +
				"   at Bit.Infrastructure.Dapper.Repositories.Repository`2..ctor(String connectionString, String readOnlyConnectionString, String schema, String table) in /Users/robin/Projects/onedev/server/server-product/target/sandbox/temp/server/onedev-build12565695818101171593/workspace/src/Infrastructure.Dapper/Repositories/Repository.cs:line 15\n" +
				"   at Bit.Infrastructure.Dapper.Vault.Repositories.FolderRepository..ctor(String connectionString, String readOnlyConnectionString) in /Users/robin/Projects/onedev/server/server-product/target/sandbox/temp/server/onedev-build12565695818101171593/workspace/src/Infrastructure.Dapper/Vault/Repositories/FolderRepository.cs:line 18\n" +
				"   at Bit.Infrastructure.Dapper.Vault.Repositories.FolderRepository..ctor(GlobalSettings globalSettings) in /Users/robin/Projects/onedev/server/server-product/target/sandbox/temp/server/onedev-build12565695818101171593/workspace/src/Infrastructure.Dapper/Vault/Repositories/FolderRepository.cs:line 14";	
		
		var expected = "/Users/robin/Projects/onedev/server/server-product/target/sandbox/temp/server/onedev-build12565695818101171593/workspace/src/Infrastructure.Dapper/Repositories/BaseRepository.cs:16/Users/robin/Projects/onedev/server/server-product/target/sandbox/temp/server/onedev-build12565695818101171593/workspace/src/Infrastructure.Dapper/Repositories/Repository.cs:15/Users/robin/Projects/onedev/server/server-product/target/sandbox/temp/server/onedev-build12565695818101171593/workspace/src/Infrastructure.Dapper/Vault/Repositories/FolderRepository.cs:18/Users/robin/Projects/onedev/server/server-product/target/sandbox/temp/server/onedev-build12565695818101171593/workspace/src/Infrastructure.Dapper/Vault/Repositories/FolderRepository.cs:14";
		assertEquals(expected, new StringTransformer(pattern) {

			@Override
			protected String transformUnmatched(String string) {
				return "";
			}

			@Override
			protected String transformMatched(Matcher matcher) {
				var file = matcher.group(1);
				var line = matcher.group(2);
				return file + ":" + line;
			}
		}.transform(text));
    }
	
}