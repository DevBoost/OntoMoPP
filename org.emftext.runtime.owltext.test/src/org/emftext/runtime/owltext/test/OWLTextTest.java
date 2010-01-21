package org.emftext.runtime.owltext.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;
import org.emftext.language.owl.resource.owl.mopp.OwlResourceFactory;
import org.emftext.runtime.owltext.OWLTextEObjectImpl;
import org.emftext.runtime.owltext.OWLTransformationHelper;
import org.junit.Test;
import org.owltext.feature.Feature;
import org.owltext.feature.resource.fea.mopp.FeaResource;
import org.owltext.feature.resource.fea.mopp.FeaResourceFactory;

public class OWLTextTest {

	static{
	FeaResourceFactory feaResourceFactory = new FeaResourceFactory();
	OwlResourceFactory owlResourceFactory = new OwlResourceFactory();
	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("fea",
			feaResourceFactory);	
	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("owl",
			owlResourceFactory);	
	}
	
	
	// TODO implement test for eSet, EAttributes, EReferences, different lower and upper bounds 
	// TODO implement test for eGet, EAttributes, EReferences, different lower and upper bounds
	// TODO implement test for eUnset, EAttributes, EReferences, different lower and upper bounds
	// TODO implement test for eIsSet, EAttributes, EReferences, different lower and upper bounds
	// TODO implement test for add
	// TODO implement test for addAll
	// TODO implement test for remove
	// TODO implement test for removeAll
	// TODO implement test for clear()
	
	
	
	
	
	
	
	
	
	
	@Test
	public void testSample() throws Throwable {
		String inFileName = "sample.fea";	
		String expectedOutFileName = "sample.owl";
		assertCorrespondance(inFileName, expectedOutFileName);
	}
	
	public void assertCorrespondance(String inFileName, String expectedOutFileName) throws Throwable {
		File inFile = new File("./testInput/" + inFileName);
		File outFile = new File("./testInput/" + inFileName + ".out.owl");
		if (outFile.exists()) outFile.delete();
		File expectedOutFile = new File("./testInput/" + expectedOutFileName);
		
		
		
		org.eclipse.emf.ecore.resource.ResourceSet rs = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();
		rs.getURIConverter().getURIMap().put(
				URI.createURI("platform:/resource/org.emftext.runtime.owltext.test/metamodel/feature.owl"),
				URI.createURI("./metamodel/feature.owl"));
		OwlResource metamodel = (OwlResource) rs.getResource(URI.createURI("platform:/resource/org.emftext.runtime.owltext.test/metamodel/feature.owl"), true);
		assertTrue("Resource map adaptation does not work", metamodel.getContents().size() == 1);
	
		FeaResource resource = (FeaResource) rs.getResource(URI.createFileURI(inFile.getAbsolutePath()), true);
		assertNotNull("In resource was not loaded.", resource);
		assertTrue("In resource is empty.",resource.getContents().size() == 1);
		EObject root = resource.getContents().get(0);
		assertTrue("In root is not instanceOf OWLTextEObjectImpl", root instanceof OWLTextEObjectImpl);
		((Feature) root).setName("secondSetName");
		// write to temp file
		//ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		OWLTextEObjectImpl rootOWLTextObjectImpl = (OWLTextEObjectImpl) root;
		String owlRepresentation = OWLTransformationHelper.getOWLRepresentation(rootOWLTextObjectImpl);
		BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
		out.write(owlRepresentation);
		out.close();
		//ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		
		OwlResource outResource = (OwlResource) rs.getResource(URI.createFileURI(outFile.getAbsolutePath()), true);
		assertNotNull("Out resource was not loaded.", outResource);
		assertTrue("Out resource is empty.",outResource.getContents().size() == 1);
		EObject ontRoot2 = outResource.getContents().get(0);
		assertTrue("Out root is not instanceOf OntologyDocument", ontRoot2 instanceof OntologyDocument);
		outResource.save(new HashMap());
		
		OwlResource expectedResource = (OwlResource) rs.getResource(URI.createFileURI(expectedOutFile.getAbsolutePath()), true);
		assertNotNull("Expected out resource was not loaded.", expectedResource);
		assertTrue("Expected out resource is empty.",expectedResource.getContents().size() == 1);
		EObject ontRoot = expectedResource.getContents().get(0);
		assertTrue("Expected out root is not instanceOf OntologyDocument", ontRoot instanceof OntologyDocument);
		
		boolean equals = EcoreUtil.equals(ontRoot, ontRoot2);
		
		// TODO should use EMFCompare for comparison, to be independent of tree structure.
		assertTrue("Expected ontology output does not equal produced output.", equals);
	}
}
