/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.petrinets.resource.petrinets.mopp;

import org.emftext.runtime.owltext.OWLTextValidationMarker;

public class PetrinetsBuilder implements
		org.emftext.language.petrinets.resource.petrinets.IPetrinetsBuilder {

	public boolean isBuildingNeeded(org.eclipse.emf.common.util.URI uri) {
		return true;
	}

	public org.eclipse.core.runtime.IStatus build(
			org.emftext.language.petrinets.resource.petrinets.mopp.PetrinetsResource resource,
			org.eclipse.core.runtime.IProgressMonitor monitor) {
		if (resource.getErrors().isEmpty()) {
			OWLTextValidationMarker ovm = new OWLTextValidationMarker();
			ovm.annotateValidationResults(resource);
			if (resource.getErrors().isEmpty()) {
				PetriNetsCodeGenerator pcg = new PetriNetsCodeGenerator();
				pcg.generateJavaCode(resource);
			}
		}
		return org.eclipse.core.runtime.Status.OK_STATUS;
	}

}
