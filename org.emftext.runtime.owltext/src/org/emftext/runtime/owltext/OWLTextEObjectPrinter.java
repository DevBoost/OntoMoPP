package org.emftext.runtime.owltext;

import java.io.ByteArrayOutputStream;

import org.emftext.language.owl.OntologyDocument;
import org.emftext.language.owl.resource.owl.mopp.OwlPrinter;

/**
 * A simple helper class to derive a string representation of the ontology
 * derived for a given OntologyDocument
 * 
 * @author cwende
 * 
 */
public class OWLTextEObjectPrinter {

	/**
	 * Derives a string holding the ontology representation of the given EObject
	 * and its children
	 * 
	 * @param rootOWLTextObjectImpl
	 * @return
	 */
	public static String getOWLRepresentation(OntologyDocument ontologyDocument) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		OwlPrinter printer = new OwlPrinter(outStream, null);
		printer.print(ontologyDocument);
		String string = outStream.toString();
		return string;
	}
}
