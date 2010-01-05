package org.emftext.runtime.owltext.test;

import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;
import org.owltext.feature.resource.fea.mopp.FeaResource;
import org.owltext.feature.resource.fea.mopp.FeaResourceFactory;

public class OWLTextTest {

	static{
	FeaResourceFactory feaResourceFactory = new FeaResourceFactory();
	
	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("fea",
			feaResourceFactory);	
	}
	
	@Test
	public void testSample() {
		File file = new File("./testInput/sample.fea");
		
		org.eclipse.emf.ecore.resource.ResourceSet rs = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();
		FeaResource resource = (FeaResource) rs.getResource(URI.createFileURI(file.getAbsolutePath()), true);
		assertNotNull("Resource was not loaded.", resource);
		assertTrue("Resorce is empty.",resource.getContents().size() == 1);
		
	}
}
