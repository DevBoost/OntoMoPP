package org.emftext.runtime.owltext.transformation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.Description;
import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;
import org.emftext.language.owl.resource.owl.mopp.OwlResourceFactory;

public class OWLParsingHelper {
	static {
		 Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap(
	     ).put("owl", new OwlResourceFactory());
	  
	}

	public Description parseSubClassOf(String superClassDescription,
			Resource resource) {
		try {
			String toParse = "Ontology: <http://dummy/ontology.owl>\n"
					+ "Class: DummyElement \n" + "\tSubClassOf: ";
			toParse += "(" + superClassDescription + ")";
			InputStream inputStream = new ByteArrayInputStream(
					toParse.getBytes());

			ResourceSet resourceSet = resource.getResourceSet();
			if (resourceSet == null)
				resourceSet = new ResourceSetImpl();
			URI tempUri = URI.createFileURI("_" + inputStream.hashCode())
					.appendFileExtension("owl");
			OwlResource res = (OwlResource) resourceSet.createResource(tempUri);
			res.load(inputStream, Collections.EMPTY_MAP);

			EList<EObject> contents = res.getContents();
			if (contents.size() == 1
					&& contents.get(0) instanceof OntologyDocument) {
				OntologyDocument od = (OntologyDocument) contents.get(0);
				if (od.getOntology().getFrames().size() == 1
						&& od.getOntology().getFrames().get(0) instanceof Class) {
					Class dummyClass = (Class) od.getOntology().getFrames()
							.get(0);
					if (dummyClass.getSuperClassesDescriptions().size() == 1
							&& dummyClass.getSuperClassesDescriptions().get(0) instanceof Description) {

						return (Description) dummyClass
								.getSuperClassesDescriptions().get(0);
					}

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
