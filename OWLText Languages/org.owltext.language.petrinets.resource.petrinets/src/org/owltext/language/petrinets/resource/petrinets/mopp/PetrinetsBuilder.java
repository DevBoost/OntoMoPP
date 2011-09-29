/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.owltext.language.petrinets.resource.petrinets.mopp;

import org.emftext.runtime.owltext.OWLTextValidationMarker;

public class PetrinetsBuilder implements org.owltext.language.petrinets.resource.petrinets.IPetrinetsBuilder {
	
	public boolean isBuildingNeeded(org.eclipse.emf.common.util.URI uri) {
		return true;
	}
	
	public org.eclipse.core.runtime.IStatus build(org.owltext.language.petrinets.resource.petrinets.mopp.PetrinetsResource resource, org.eclipse.core.runtime.IProgressMonitor monitor) {
		OWLTextValidationMarker ovm = new OWLTextValidationMarker();
		ovm.annotateValidationResults(resource);
		return org.eclipse.core.runtime.Status.OK_STATUS;
	}
	
}
