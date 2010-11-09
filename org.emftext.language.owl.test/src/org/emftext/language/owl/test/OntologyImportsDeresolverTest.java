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
		ResourceSet rs = new ResourceSetImpl();
		
		
		OwlFactory factory = OwlFactory.eINSTANCE;
		Ontology importedOntology = factory.createOntology();
		importedOntology.setUri("http://test.uri/imported");
		OntologyDocument importedDocument = factory.createOntologyDocument();
		importedDocument.setOntology(importedOntology);
		
		URI uri = URI.createURI("importedOntology.owl");
		Resource importedResource = rs.createResource(uri);
		assertNotNull("Resource could not be created", importedResource);
		importedResource.getContents().add(importedDocument);
		OutputStream os = new ByteArrayOutputStream();
		try {
			importedResource.save(os, null);
			String printedOwl = os.toString();
			System.out.println(printedOwl);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		OntologyDocument importingDocument = factory.createOntologyDocument();
		Ontology ontology = factory.createOntology();
		ontology.setUri("http://test.uri/");
		importingDocument.setOntology(ontology);
		ontology.getImports().add(importedOntology);
		
		uri = URI.createURI("importingOntology.owl");
		Resource importingResource = rs.createResource(uri);
		assertNotNull("Resource could not be created", importingResource);
		importingResource.getContents().add(importingDocument);
		os = new ByteArrayOutputStream();
		try {
			importingResource.save(os, null);
			String printedOwl = os.toString();
			System.out.println(printedOwl);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testNonSavedOntologyImportsDeresolve(){
		ResourceSet rs = new ResourceSetImpl();
		
		
		OwlFactory factory = OwlFactory.eINSTANCE;
		Ontology importedOntology = factory.createOntology();
		importedOntology.setUri("http://test.uri/imported");
		OntologyDocument importedDocument = factory.createOntologyDocument();
		importedDocument.setOntology(importedOntology);
		
		
		OntologyDocument importingDocument = factory.createOntologyDocument();
		Ontology ontology = factory.createOntology();
		ontology.setUri("http://test.uri/");
		importingDocument.setOntology(ontology);
		ontology.getImports().add(importedOntology);
		
		URI uri = URI.createURI("importingOntology.owl");
		Resource importingResource = rs.createResource(uri);
		assertNotNull("Resource could not be created", importingResource);
		importingResource.getContents().add(importingDocument);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			importingResource.save(os, null);
			String printedOwl = os.toString();
			System.out.println(printedOwl);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
}
