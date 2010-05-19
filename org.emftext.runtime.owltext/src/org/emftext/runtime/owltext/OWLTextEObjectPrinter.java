package org.emftext.runtime.owltext;

/**
 * A simple helper class to derive a string representation of 
 * the ontology derived for a given EObject and its children
 * 
 * @author cwende
 *
 */
public class OWLTextEObjectPrinter {

	/**
	 * Derives a string holding the ontology representation of the
	 * given EObject and its children
	 * 
	 * @param rootOWLTextObjectImpl
	 * @return
	 */
	public static String getOWLRepresentation(
			OWLTextEObjectImpl rootOWLTextObjectImpl) {
		return rootOWLTextObjectImpl
				.getOWLRepresentation(rootOWLTextObjectImpl);
	}
}
