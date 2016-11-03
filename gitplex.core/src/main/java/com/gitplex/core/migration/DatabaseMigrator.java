package com.gitplex.core.migration;

import java.io.File;
import java.util.Stack;

import javax.inject.Singleton;

import org.dom4j.Element;

import com.gitplex.commons.hibernate.migration.Migrator;
import com.gitplex.commons.hibernate.migration.VersionedDocument;

@Singleton
@SuppressWarnings("unused")
public class DatabaseMigrator implements Migrator {
	
	private void migrate1(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml")) {
				VersionedDocument dom = VersionedDocument.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element branchRefElement = element.element("branchRef");
					if (branchRefElement != null)
						branchRefElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}	
	}

	private void migrate2(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Depots.xml")) {
				VersionedDocument dom = VersionedDocument.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element gateKeeperElement = element.element("gateKeeper");
					gateKeeperElement.detach();
					element.addElement("gateKeepers");
				}
				dom.writeToFile(file, false);
			}
		}	
	}

	private void migrate3(File dataDir, Stack<Integer> versions) {
	}
	
}
