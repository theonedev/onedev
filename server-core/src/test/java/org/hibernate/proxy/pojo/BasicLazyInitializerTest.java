package org.hibernate.proxy.pojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.onedev.server.model.AbstractEntity;

public class BasicLazyInitializerTest {

	@Test
	public void comparesEntityIdsWithoutLoadingDetachedProxies() throws Throwable {
		// #875: membership checks over large collections of users must not load each user.
		// Exercise the interceptor directly: AbstractEntity's final equals/hashCode would
		// otherwise bypass it on a ByteBuddy proxy and fail to protect this customization.
		for (boolean overridesEquals : new boolean[] {false, true}) {
			var initializer = new Initializer(42L, overridesEquals);
			var proxy = new EntityReference(initializer);
			var same = new EntityReference(new Initializer(42L, overridesEquals));
			var other = new EntityReference(new Initializer(43L, overridesEquals));
			var loaded = new EntityReference(null);
			loaded.setId(42L);

			assertEquals(true, initializer.call("equals", proxy, proxy));
			assertEquals(true, initializer.call("equals", proxy, same));
			assertEquals(true, initializer.call("equals", proxy, loaded));
			assertTrue(loaded.equals(proxy));
			assertEquals(false, initializer.call("equals", proxy, other));
			assertEquals(false, initializer.call("equals", proxy, (Object) null));
			assertEquals(false, initializer.call("equals", proxy, "42"));
			assertEquals(loaded.hashCode(), initializer.call("hashCode", proxy));
			assertEquals(initializer.call("hashCode", proxy), same.hashCode());
			assertTrue(initializer.isUninitialized());
			assertTrue(same.initializer.isUninitialized());
			assertTrue(other.initializer.isUninitialized());
		}
	}

	@Test
	public void unsavedEntitiesKeepIdentitySemantics() throws Throwable {
		var initializer = new Initializer(null, true);
		var proxy = new EntityReference(initializer);
		var other = new EntityReference(new Initializer(null, true));
		assertEquals(true, initializer.call("equals", proxy, proxy));
		assertEquals(false, initializer.call("equals", proxy, other));
		assertFalse(other.equals(proxy));
		assertEquals(System.identityHashCode(proxy), initializer.call("hashCode", proxy));
		assertTrue(initializer.isUninitialized());
	}

	@Test
	public void ordinaryMethodsStillDelegateToTheEntity() throws Throwable {
		var initializer = new Initializer(42L, true);
		var proxy = new EntityReference(initializer);
		assertEquals(42L, initializer.invoke(AbstractEntity.class.getMethod("getId"), new Object[0], proxy));
		assertSame(BasicLazyInitializer.INVOKE_IMPLEMENTATION,
				initializer.invoke(AbstractEntity.class.getMethod("isNew"), new Object[0], proxy));
		assertTrue(initializer.isUninitialized());
	}

	private static class Initializer extends BasicLazyInitializer {
		private Initializer(Long id, boolean overridesEquals) throws NoSuchMethodException {
			super(EntityReference.class.getName(), EntityReference.class, id,
					AbstractEntity.class.getMethod("getId"), AbstractEntity.class.getMethod("setId", Long.class),
					null, null, overridesEquals);
		}

		private Object call(String method, Object proxy, Object... args) throws Throwable {
			return invoke(args.length == 0 ? Object.class.getMethod(method) : Object.class.getMethod(method, Object.class),
					args, proxy);
		}

		@Override
		protected Object serializableProxy() {
			throw new AssertionError("Unexpected serialization");
		}
	}

	private static class EntityReference extends AbstractEntity {
		private static final long serialVersionUID = 1L;

		private final Initializer initializer;

		private EntityReference(Initializer initializer) {
			this.initializer = initializer;
		}

		@Override
		public Long getId() {
			return initializer != null ? (Long) initializer.getIdentifier() : super.getId();
		}
	}
}
