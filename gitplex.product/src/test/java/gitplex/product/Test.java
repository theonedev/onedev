package gitplex.product;

import java.lang.reflect.Field;

import com.pmease.commons.hibernate.migration.VersionTable;

public class Test {

	@org.junit.Test
	public void test() {
		for (Field field: VersionTable.class.getFields()) {
			System.out.println(field.getName());
		}
	}

}