package org.emftext.language.owl.test.resolving;

import static org.junit.Assert.assertTrue;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emftext.language.owl.OntologyDocument;

public class ModelStorageUtil {

	public static OntologyDocument loadModelFromFileName(String string) {
		ResourceSet rs = new ResourceSetImpl();
		URI uri = URI.createURI("./src/" + string);
		Resource importedResource = rs.getResource(uri, true);
		assertTrue(importedResource.getContents().size() == 1);
		EObject root = importedResource.getContents().get(0);
		assertTrue(root instanceof OntologyDocument);
		return (OntologyDocument) root;

	}
}
