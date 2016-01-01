package gitplex.product;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		List<Path> paths = new ArrayList<>();
		paths.add(Paths.get("a/b"));
		paths.add(Paths.get("a/b/c"));
		paths.add(Paths.get("a/c"));
		paths.add(Paths.get("a/c/d"));
		Collections.sort(paths, new Comparator<Path>() {

			@Override
			public int compare(Path path1, Path path2) {
				int count1 = path1.getNameCount();
				int count2 = path2.getNameCount();
				if (count1 != count2)
					return count1 - count2;
				else
					return path1.compareTo(path2);
			}
			
		});
		for (Path path: paths) 
			System.out.println(path);
	}
	
}