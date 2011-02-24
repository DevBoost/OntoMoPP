package org.emftext.ontomopp.modelsync.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.resource.owl.mopp.OwlResourceFactory;
import org.emftext.runtime.owltext.transformation.Ecore2Owl;
import org.junit.Test;

public class OWLizerTest {

	@Test
	public void transformMetamodels() {
		ResourceSet rs = getResourceSet();
		Ecore2Owl transformer = new Ecore2Owl();
		String relativePath = "../org.emftext.language.petrinet/metamodel/petrinet.ecore";
		File metamodelFile = new File(relativePath).getAbsoluteFile();
		Resource metamodelResource = rs.getResource(URI.createFileURI(metamodelFile.getAbsolutePath()), true);
		EPackage metamodel = (EPackage) metamodelResource.getContents().get(0);
		URI targetURI = URI.createURI("http://example");
		OntologyDocument ontology = transformer.transformMetamodel(metamodel, targetURI);
		Resource resource = rs.createResource(getOutputModelURI("owlizer-test", "petrinet-metamodel", "owl"));
		resource.getContents().clear();
		resource.getContents().add(ontology);
		try {
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private URI getOutputModelURI(String testcaseName, String modelName, String extension) {
		File outputFolder = new File("output" + File.separator + testcaseName);
		outputFolder.mkdirs();
		
		URI targetURI = URI.createFileURI(outputFolder.getAbsolutePath() + File.separator + modelName + "." + extension);
		return targetURI;
	}
	
	private ResourceSet getResourceSet() {
		// this is just to register the Ecore language
		EcorePackage.eINSTANCE.getEClass();
		
		ResourceSet rs = new ResourceSetImpl();
		Map<String, Object> extensionToFactoryMap = rs.getResourceFactoryRegistry().getExtensionToFactoryMap();
		extensionToFactoryMap.put("ecore", new XMIResourceFactoryImpl());
		extensionToFactoryMap.put("owl", new OwlResourceFactory());
		return rs;
	}
}
