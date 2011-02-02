package org.emftext.language.owl.test.printing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.print.attribute.standard.MediaSize.Other;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.ClassAtomic;
import org.emftext.language.owl.Namespace;
import org.emftext.language.owl.ObjectProperty;
import org.emftext.language.owl.Ontology;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.OwlFactory;
import org.emftext.language.owl.OwlPackage;
import org.emftext.language.owl.impl.OwlFactoryImpl;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;
import org.junit.Test;

public class TestOwlPrinting {
	

	public TestOwlPrinting(){
		registerFactories();
	}

	private void registerFactories() {
		EPackage.Registry.INSTANCE.put(OwlPackage.eNS_URI, OwlPackage.eINSTANCE);
		Map<String, Object> extensionToFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		extensionToFactoryMap.put("owl",new org.emftext.language.owl.resource.owl.mopp.OwlResourceFactory());
	}
	
	@Test
	public void testPrintingOfIriReferencesWithEmptyPrefix() throws IOException {
		OwlFactory owlFactory = OwlFactoryImpl.eINSTANCE;
		OntologyDocument od = owlFactory.createOntologyDocument();
		Ontology o = owlFactory.createOntology();
		od.setOntology(o);
		o.setUri("http://owl/printing.test");
		
		Namespace local = owlFactory.createNamespace();
		local.setImportedOntology(o);
		local.setPrefix(":");
		od.getNamespace().add(local);
		
		Class employee = owlFactory.createClass();
		employee.setIri("Employee");
		o.getFrames().add(employee);
			
		Class employeeIdDomain = owlFactory.createClass();
		employeeIdDomain.setIri("EmployeeIdDomain");
		o.getFrames().add(employeeIdDomain);
		
		ObjectProperty id = owlFactory.createObjectProperty();
		id.setIri("id");
		o.getFrames().add(id);
		
		ClassAtomic eca = owlFactory.createClassAtomic();
		eca.setClazz(employee);
		id.getPropertyRange().add(eca);
		
		ClassAtomic eidd = owlFactory.createClassAtomic();
		eidd.setClazz(employeeIdDomain);
		id.getDomain().add(eidd);
		
		ResourceSet rs = new ResourceSetImpl();
		URI uri = URI.createURI("./src/printingTest.owl");
		Resource newResource = rs.createResource(uri);
		newResource.getContents().add(od);
		
		newResource.save(Collections.EMPTY_MAP);
		newResource.unload();
		
		// reparse test, would fail when generated resource is invalid
		Resource reloadedResource = rs.getResource(uri, true);
		assertTrue(reloadedResource.getContents().size() == 1);
		assertTrue(reloadedResource instanceof OwlResource);
		assertTrue(((OwlResource) reloadedResource).getErrors().size() == 0);
		
	}
}
