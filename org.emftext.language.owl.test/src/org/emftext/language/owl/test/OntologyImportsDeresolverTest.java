package org.emftext.language.owl.test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.OwlFactory;
import org.emftext.language.owl.OwlPackage;
import org.junit.Test;

public class OntologyImportsDeresolverTest {

	public OntologyImportsDeresolverTest(){
		registerFactories();
	}

	private void registerFactories() {
		EPackage.Registry.INSTANCE.put(OwlPackage.eNS_URI, OwlPackage.eINSTANCE);
		Map<String, Object> extensionToFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		extensionToFactoryMap.put("owl",new org.emftext.language.owl.resource.owl.mopp.OwlResourceFactory());
	}

	@Test
	public void testOntologyImportsDeresolve(){
		OwlFactory factory = OwlFactory.eINSTANCE;
		OntologyDocument doc = factory.createOntologyDocument();
		Ontology ontology = factory.createOntology();
		ontology.setUri("http://test.uri/");
		doc.setOntology(ontology);
		Ontology importedOntology = factory.createOntology();
		importedOntology.setUri("http://test.uri/imported");
		ontology.getImports().add(importedOntology);
		ResourceSet rs = new ResourceSetImpl();
		URI uri = URI.createURI("importedOntology.owl");
		Resource resource = rs.createResource(uri);
		assertNotNull("Resource could not be created", resource);
		resource.getContents().add(doc);
		OutputStream os = new ByteArrayOutputStream();
		try {
			resource.save(os, null);
			String printedOwl = os.toString();
			System.out.println(printedOwl);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
}
