/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.owlcl.resource.owlcl.mopp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.emftext.language.owlcl.Constraint;
import org.emftext.language.owlcl.OWLCLSpec;
import org.emftext.runtime.owltext.OWLTextEObjectPrinter;

public class OwlclBuilder implements
		org.emftext.language.owlcl.resource.owlcl.IOwlclBuilder {

	private static final String OWL_CONSTRAINT = "OWL_CONSTRAINT";

	public boolean isBuildingNeeded(org.eclipse.emf.common.util.URI uri) {
		return true;
	}

	public org.eclipse.core.runtime.IStatus build(
			org.emftext.language.owlcl.resource.owlcl.mopp.OwlclResource resource,
			org.eclipse.core.runtime.IProgressMonitor monitor) {
		if (resource.getErrors().size() > 0) {
			return Status.OK_STATUS;
		}
		if (resource.getContents().size() == 1) {
			OWLCLSpec spec = (OWLCLSpec) resource.getContents().get(0);
			EList<Constraint> constraints = spec.getConstraints();
			cleanMetaclasses(spec.getConstrainedMetamodel());

			for (Constraint c : constraints) {
				EClass constrainedMetaclass = c.getConstrainedMetaclass();
				EAnnotation anno = EcoreFactory.eINSTANCE.createEAnnotation();
				anno.setSource(OWL_CONSTRAINT);
				String description = OWLTextEObjectPrinter
						.getOWLRepresentation(c.getConstraintDescription());
				anno.getDetails().put(c.getErrorMsg(), description);
				constrainedMetaclass.getEAnnotations().add(anno);
			}
			try {
				spec.getConstrainedMetamodel().eResource()
						.save(Collections.EMPTY_MAP);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return Status.OK_STATUS;
	}

	private void cleanMetaclasses(EPackage constrainedMetamodel) {
		EList<EClassifier> classifiers = constrainedMetamodel.getEClassifiers();
		for (EClassifier eClassifier : classifiers) {
			List<EAnnotation> toRemove = new ArrayList<EAnnotation>();
			EList<EAnnotation> eAnnotations = eClassifier.getEAnnotations();
			for (EAnnotation eAnnotation : eAnnotations) {
				if (eAnnotation.getSource().equals(OWL_CONSTRAINT)) {
					toRemove.add(eAnnotation);
				}
			}
			eClassifier.getEAnnotations().removeAll(toRemove);
		}
	}

}
