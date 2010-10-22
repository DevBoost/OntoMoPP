package org.emftext.runtime.owltext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.emf.ecore.EObject;
import org.emftext.language.owl.resource.owl.IOwlTextPrinter;
import org.emftext.language.owl.resource.owl.mopp.OwlMetaInformation;

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
	public static String getOWLRepresentation(EObject objectToPrint) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		IOwlTextPrinter printer = new OwlMetaInformation().createPrinter(
				outStream, null);
		try {
			printer.print(objectToPrint);
		} catch (IOException e) {
			// TODO handle exception
			e.printStackTrace();
		}

		String string = outStream.toString();
		return string;
	}
}
