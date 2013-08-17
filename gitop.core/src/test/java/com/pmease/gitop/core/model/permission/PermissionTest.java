package com.pmease.gitop.core.model.permission;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.pmease.gitop.core.model.Repository;
import com.pmease.gitop.core.model.RepositoryAuthorization;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.permission.operation.Administration;
import com.pmease.gitop.core.model.permission.operation.NoAccess;
import com.pmease.gitop.core.model.permission.operation.Read;
import com.pmease.gitop.core.model.permission.operation.Write;
import com.pmease.gitop.core.model.permission.operation.WriteToBranch;

public class PermissionTest {

	@Test
	public void shouldHandleUserAdminPermissionAppropriately() {
		User user = new User();
		user.setId(100L);
		user.setName("robin");
		
		Repository gitop = new Repository();
		gitop.setId(200L);
		gitop.setOwner(user);
		gitop.setName("gitop");
		
		Repository quickbuild = new Repository();
		quickbuild.setId(250L);
		quickbuild.setOwner(user);
		quickbuild.setName("quickbuild");

		Team team = new Team();
		team.setOwner(user);
		
		team.setOperation(new Administration());
		
		RepositoryAuthorization authorization = new RepositoryAuthorization();
		authorization.setId(300L);
		authorization.setTeam(team);
		authorization.setRepository(gitop);
		authorization.setOperation(new NoAccess());
		
		team.getRepositoryAuthorizations().add(authorization);
		
		assertTrue(team.implies(ObjectPermission.ofUserAdmin(user)));
		assertTrue(team.implies(ObjectPermission.ofUserWrite(user)));
		assertTrue(team.implies(ObjectPermission.ofUserRead(user)));
		assertTrue(team.implies(ObjectPermission.ofRepositoryAdmin(quickbuild)));
		assertTrue(team.implies(ObjectPermission.ofRepositoryWrite(quickbuild)));
		assertTrue(team.implies(ObjectPermission.ofRepositoryRead(quickbuild)));
		assertTrue(team.implies(ObjectPermission.ofBranchAdmin(quickbuild, "branch1")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(quickbuild, "branch1")));
		assertTrue(team.implies(ObjectPermission.ofBranchRead(quickbuild, "branch1")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(quickbuild, "branch1", "src/file")));

		assertFalse(team.implies(ObjectPermission.ofRepositoryRead(gitop)));
		assertFalse(team.implies(ObjectPermission.ofBranchRead(gitop, "branch1")));
		assertFalse(team.implies(ObjectPermission.ofSystem(new Administration())));
		assertFalse(team.implies(ObjectPermission.ofSystem(new Write())));
	}

	@Test
	public void shouldHandleUserWriterPermissionAppropriately() {
		User user = new User();
		user.setId(100L);
		user.setName("robin");
		
		Repository repository = new Repository();
		repository.setId(200L);
		repository.setOwner(user);
		repository.setName("gitop");
		
		Team team = new Team();
		team.setOwner(user);
		
		team.setOperation(new Write());
		
		RepositoryAuthorization authorization = new RepositoryAuthorization();
		authorization.setId(300L);
		authorization.setTeam(team);
		authorization.setRepository(repository);
		authorization.setOperation(new Write());
		authorization.getBranchPermissions().add(new BranchPermission("branch2", new Read()));
		
		team.getRepositoryAuthorizations().add(authorization);

		assertTrue(team.implies(ObjectPermission.ofUserWrite(user)));
		assertTrue(team.implies(ObjectPermission.ofUserRead(user)));
		assertTrue(team.implies(ObjectPermission.ofRepositoryWrite(repository)));
		assertTrue(team.implies(ObjectPermission.ofRepositoryRead(repository)));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(repository, "branch1")));
		assertTrue(team.implies(ObjectPermission.ofBranchRead(repository, "branch1")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(repository, "branch1", "src/file")));
		assertTrue(team.implies(ObjectPermission.ofBranchRead(repository, "branch2")));

		assertFalse(team.implies(ObjectPermission.ofBranchWrite(repository, "branch2")));
		assertFalse(team.implies(ObjectPermission.ofUserAdmin(user)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryAdmin(repository)));
		assertFalse(team.implies(ObjectPermission.ofBranchAdmin(repository, "branch1")));
	}

	@Test
	public void shouldHandleUserReaderPermissionAppropriately() {
		User user = new User();
		user.setId(100L);
		user.setName("robin");
		
		Repository repository = new Repository();
		repository.setId(200L);
		repository.setOwner(user);
		repository.setName("gitop");
		
		Team team = new Team();
		team.setOwner(user);
		
		team.setOperation(new Read());
		
		assertTrue(team.implies(ObjectPermission.ofUserRead(user)));
		assertTrue(team.implies(ObjectPermission.ofRepositoryRead(repository)));
		assertTrue(team.implies(ObjectPermission.ofBranchRead(repository, "branch1")));

		assertFalse(team.implies(ObjectPermission.ofBranchWrite(repository, "branch1", "src/file")));
		assertFalse(team.implies(ObjectPermission.ofBranchWrite(repository, "branch1")));
		assertFalse(team.implies(ObjectPermission.ofRepositoryWrite(repository)));
		assertFalse(team.implies(ObjectPermission.ofUserWrite(user)));
		assertFalse(team.implies(ObjectPermission.ofUserAdmin(user)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryAdmin(repository)));
		assertFalse(team.implies(ObjectPermission.ofBranchAdmin(repository, "branch1")));
	}

	
	@Test
	public void shouldHandleRepositoryAdminPermissionAppropriately() {
		User user = new User();
		user.setId(100L);
		
		Repository gitop = new Repository();
		gitop.setId(200L);
		gitop.setName("gitop");
		gitop.setOwner(user);
		
		Repository quickbuild = new Repository();
		quickbuild.setId(250L);
		quickbuild.setName("quickbuild");
		quickbuild.setOwner(user);

		Team team = new Team();
		team.setId(300L);
		team.setOwner(user);
		team.setOperation(new NoAccess());
		
		RepositoryAuthorization authorization = new RepositoryAuthorization();
		authorization.setId(400L);
		authorization.setTeam(team);
		authorization.setRepository(gitop);
		authorization.setOperation(new Administration());
		authorization.getBranchPermissions().add(new BranchPermission("branch2", new NoAccess()));
		
		team.getRepositoryAuthorizations().add(authorization);
		
		assertTrue(team.implies(ObjectPermission.ofRepositoryAdmin(gitop)));
		assertTrue(team.implies(ObjectPermission.ofBranchAdmin(gitop, "branch1")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(gitop, "branch1", "src/file")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(gitop, "branch1")));
		assertTrue(team.implies(ObjectPermission.ofRepositoryWrite(gitop)));
		assertTrue(team.implies(ObjectPermission.ofRepositoryRead(gitop)));
		assertTrue(team.implies(ObjectPermission.ofBranchRead(gitop, "branch1")));

		assertFalse(team.implies(ObjectPermission.ofBranchRead(gitop, "branch2")));
		assertFalse(team.implies(ObjectPermission.ofUserRead(user)));
		assertFalse(team.implies(ObjectPermission.ofUserWrite(user)));
		assertFalse(team.implies(ObjectPermission.ofUserAdmin(user)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryRead(quickbuild)));
	}
	
	@Test
	public void shouldHandleRepositoryWritePermissionAppropriately() {
		User user = new User();
		user.setId(100L);
		
		Repository gitop = new Repository();
		gitop.setId(200L);
		gitop.setName("gitop");
		gitop.setOwner(user);
		
		Repository quickbuild = new Repository();
		quickbuild.setId(250L);
		quickbuild.setName("quickbuild");
		quickbuild.setOwner(user);

		Team team = new Team();
		team.setId(300L);
		team.setOwner(user);
		team.setOperation(new NoAccess());
		
		RepositoryAuthorization authorization = new RepositoryAuthorization();
		authorization.setId(400L);
		authorization.setTeam(team);
		authorization.setRepository(gitop);
		authorization.setOperation(new Write());
		
		team.getRepositoryAuthorizations().add(authorization);
		
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(gitop, "branch1", "src/file")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(gitop, "branch1")));
		assertTrue(team.implies(ObjectPermission.ofRepositoryWrite(gitop)));
		assertTrue(team.implies(ObjectPermission.ofRepositoryRead(gitop)));
		assertTrue(team.implies(ObjectPermission.ofBranchRead(gitop, "branch1")));

		assertFalse(team.implies(ObjectPermission.ofBranchAdmin(gitop, "branch1")));
		assertFalse(team.implies(ObjectPermission.ofRepositoryAdmin(gitop)));
		assertFalse(team.implies(ObjectPermission.ofUserRead(user)));
		assertFalse(team.implies(ObjectPermission.ofUserWrite(user)));
		assertFalse(team.implies(ObjectPermission.ofUserAdmin(user)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryRead(quickbuild)));
	}
	
	@Test
	public void shouldHandleRepositoryReadPermissionAppropriately() {
		User user = new User();
		user.setId(100L);
		
		Repository gitop = new Repository();
		gitop.setId(200L);
		gitop.setName("gitop");
		gitop.setOwner(user);
		
		Repository quickbuild = new Repository();
		quickbuild.setId(250L);
		quickbuild.setName("quickbuild");
		quickbuild.setOwner(user);

		Team team = new Team();
		team.setId(300L);
		team.setOwner(user);
		team.setOperation(new NoAccess());
		
		RepositoryAuthorization authorization = new RepositoryAuthorization();
		authorization.setId(400L);
		authorization.setTeam(team);
		authorization.setRepository(gitop);
		authorization.setOperation(new Read());
		
		team.getRepositoryAuthorizations().add(authorization);
		
		assertTrue(team.implies(ObjectPermission.ofRepositoryRead(gitop)));
		assertTrue(team.implies(ObjectPermission.ofBranchRead(gitop, "branch1")));

		assertFalse(team.implies(ObjectPermission.ofBranchWrite(gitop, "branch1", "src/file")));
		assertFalse(team.implies(ObjectPermission.ofBranchWrite(gitop, "branch1")));
		assertFalse(team.implies(ObjectPermission.ofRepositoryWrite(gitop)));
		assertFalse(team.implies(ObjectPermission.ofBranchAdmin(gitop, "branch1")));
		assertFalse(team.implies(ObjectPermission.ofRepositoryAdmin(gitop)));
		assertFalse(team.implies(ObjectPermission.ofUserRead(user)));
		assertFalse(team.implies(ObjectPermission.ofUserWrite(user)));
		assertFalse(team.implies(ObjectPermission.ofUserAdmin(user)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryRead(quickbuild)));
	}

	@Test
	public void shouldHandleBranchAdminPermissionAppropriately() {
		User user = new User();
		user.setId(100L);
		
		Repository gitop = new Repository();
		gitop.setId(200L);
		gitop.setName("gitop");
		gitop.setOwner(user);
		
		Repository quickbuild = new Repository();
		quickbuild.setId(250L);
		quickbuild.setName("quickbuild");
		quickbuild.setOwner(user);

		Team team = new Team();
		team.setId(300L);
		team.setOwner(user);
		team.setOperation(new NoAccess());
		
		RepositoryAuthorization authorization = new RepositoryAuthorization();
		authorization.setId(400L);
		authorization.setTeam(team);
		authorization.setRepository(gitop);
		authorization.setOperation(new NoAccess());
		authorization.getBranchPermissions().add(new BranchPermission("**/release", new Administration()));
		
		team.getRepositoryAuthorizations().add(authorization);
		
		assertTrue(team.implies(ObjectPermission.ofBranchAdmin(gitop, "5.0/release")));
		assertTrue(team.implies(ObjectPermission.ofBranchRead(gitop, "test/release")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(gitop, "1.0/release", "src/file")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(gitop, "2.0/release")));
		
		assertFalse(team.implies(ObjectPermission.ofBranchAdmin(quickbuild, "release")));
		assertFalse(team.implies(ObjectPermission.ofRepositoryWrite(gitop)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryRead(gitop)));
		assertFalse(team.implies(ObjectPermission.ofBranchAdmin(gitop, "branch1")));
		assertFalse(team.implies(ObjectPermission.ofUserRead(user)));
		assertFalse(team.implies(ObjectPermission.ofUserWrite(user)));
		assertFalse(team.implies(ObjectPermission.ofUserAdmin(user)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryAdmin(gitop)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryRead(quickbuild)));
	}

	@Test
	public void shouldHandleBranchWritePermissionAppropriately() {
		User user = new User();
		user.setId(100L);
		
		Repository gitop = new Repository();
		gitop.setId(200L);
		gitop.setName("gitop");
		gitop.setOwner(user);
		
		Repository quickbuild = new Repository();
		quickbuild.setId(250L);
		quickbuild.setName("quickbuild");
		quickbuild.setOwner(user);

		Team team = new Team();
		team.setId(300L);
		team.setOwner(user);
		team.setOperation(new NoAccess());
		
		RepositoryAuthorization authorization = new RepositoryAuthorization();
		authorization.setId(400L);
		authorization.setTeam(team);
		authorization.setRepository(gitop);
		authorization.setOperation(new NoAccess());
		authorization.getBranchPermissions().add(new BranchPermission("**/release", new WriteToBranch("-**/*.java, **")));
		
		team.getRepositoryAuthorizations().add(authorization);
		
		assertTrue(team.implies(ObjectPermission.ofBranchRead(gitop, "test/release")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(gitop, "1.0/release", "src/file")));
		
		assertFalse(team.implies(ObjectPermission.ofBranchWrite(gitop, "2.0/release", "test.java")));
		assertFalse(team.implies(ObjectPermission.ofBranchAdmin(gitop, "1.0/release")));
		assertFalse(team.implies(ObjectPermission.ofRepositoryWrite(gitop)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryRead(gitop)));
		assertFalse(team.implies(ObjectPermission.ofBranchAdmin(gitop, "branch1")));
		assertFalse(team.implies(ObjectPermission.ofUserRead(user)));
		assertFalse(team.implies(ObjectPermission.ofUserWrite(user)));
		assertFalse(team.implies(ObjectPermission.ofUserAdmin(user)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryAdmin(gitop)));
		assertFalse(team.implies(ObjectPermission.ofRepositoryRead(quickbuild)));
	}

	@Test
	public void shouldHandleMultipleBranchPermissionsAppropriately() {
		User user = new User();
		user.setId(100L);
		
		Repository gitop = new Repository();
		gitop.setId(200L);
		gitop.setName("gitop");
		gitop.setOwner(user);
		
		Repository quickbuild = new Repository();
		quickbuild.setId(250L);
		quickbuild.setName("quickbuild");
		quickbuild.setOwner(user);

		Team team = new Team();
		team.setId(300L);
		team.setOwner(user);
		team.setOperation(new NoAccess());
		
		RepositoryAuthorization authorization = new RepositoryAuthorization();
		authorization.setId(400L);
		authorization.setTeam(team);
		authorization.setRepository(gitop);
		authorization.setOperation(new NoAccess());
		authorization.getBranchPermissions().add(new BranchPermission("branch1", new WriteToBranch("**")));
		authorization.getBranchPermissions().add(new BranchPermission("branch2", new Read()));
		
		team.getRepositoryAuthorizations().add(authorization);
		
		assertTrue(team.implies(ObjectPermission.ofBranchRead(gitop, "branch1")));
		assertTrue(team.implies(ObjectPermission.ofBranchRead(gitop, "branch2")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(gitop, "branch1")));
		
		assertFalse(team.implies(ObjectPermission.ofBranchWrite(gitop, "branch2")));
		
		team.setOperation(new Read());
		
		assertTrue(team.implies(ObjectPermission.ofBranchRead(gitop, "branch1")));
		assertTrue(team.implies(ObjectPermission.ofBranchRead(quickbuild, "branch2")));
		assertTrue(team.implies(ObjectPermission.ofBranchWrite(gitop, "branch1")));
		
		assertFalse(team.implies(ObjectPermission.ofBranchWrite(gitop, "branch2")));
	}
	
}
