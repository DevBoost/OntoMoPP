/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.owltext.language.office.resource.office.mopp;

import org.emftext.runtime.owltext.OWLTextValidationMarker;

public class OfficeBuilder implements org.emftext.language.office.resource.office.IOfficeBuilder {
	
	public boolean isBuildingNeeded(org.eclipse.emf.common.util.URI uri) {
		return true;
	}
	
	public org.eclipse.core.runtime.IStatus build(org.emftext.language.office.resource.office.mopp.OfficeResource resource, org.eclipse.core.runtime.IProgressMonitor monitor) {
		if (resource.getErrors().size() == 0) {
			OWLTextValidationMarker ovm = new OWLTextValidationMarker();
			ovm.annotateValidationResults(resource);
		}
		return org.eclipse.core.runtime.Status.OK_STATUS;	}
	
}
