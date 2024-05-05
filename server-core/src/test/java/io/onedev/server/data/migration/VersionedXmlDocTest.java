package io.onedev.server.data.migration;

import com.thoughtworks.xstream.XStream;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class VersionedXmlDocTest extends AppLoaderMocker {

	@Test
	public void test() {
		var xstream = new XStream();
		xstream.allowTypes(new Class<?>[]{Bean.class});
		Mockito.when(AppLoader.getInstance(XStream.class)).thenReturn(xstream);

		var bean = new Bean();
		bean.setName("\uD83E\uDD8A✅\uD83D\uDDC4️\uffffreplaceme");
		var xml = VersionedXmlDoc.fromBean(bean).toXML().replace("replaceme", "&#27;");
		Bean beanClone = (Bean) VersionedXmlDoc.fromXML(xml).toBean();
		assertEquals("\uD83E\uDD8A✅\uD83D\uDDC4️?", beanClone.getName());
	}

	@Override
	protected void setup() {
		
	}

	@Override
	protected void teardown() {

	}

	public static class Bean {
		
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}