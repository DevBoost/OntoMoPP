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
import org.emftext.language.owlcl.Type;
import org.emftext.runtime.owltext.OWLTextEObjectPrinter;
import org.emftext.runtime.owltext.transformation.OWLTransformationConstants;


public class OwlclBuilder implements
		org.emftext.language.owlcl.resource.owlcl.IOwlclBuilder {

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

			cleanMetaclasses(spec.getConstrainedMetamodel());
			
			EList<Constraint> constraints = spec.getConstraints();
			for (Constraint c : constraints) {
				EClass constrainedMetaclass = c.getConstrainedMetaclass();
				EAnnotation anno = EcoreFactory.eINSTANCE.createEAnnotation();
				anno.setSource(OWLTransformationConstants.OWL_CONSTRAINT);
				String description = OWLTextEObjectPrinter
						.getOWLRepresentation(c.getConstraintDescription());
				anno.getDetails().put(c.getErrorMsg(), description);
				constrainedMetaclass.getEAnnotations().add(anno);
			}
			
			EList<Type> types = spec.getTypes();
			for (Type type : types) {
				EAnnotation anno = EcoreFactory.eINSTANCE.createEAnnotation();
				anno.setSource(OWLTransformationConstants.OWL_DEFINITION);
				String description = OWLTextEObjectPrinter
						.getOWLRepresentation(type.getTypeDescription());
				anno.getDetails().put(type.getName(), description);
				EList<EAnnotation> eAnnotations = type.getEAnnotations();
				for (EAnnotation eAnnotation : eAnnotations) {
					anno.getDetails().addAll(eAnnotation.getDetails());
				}
				EList<EClass> refinedTypes = type.getESuperTypes();
				for (EClass refinedType : refinedTypes) {
					refinedType.getEAnnotations().add(anno);
				}
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
		EList<EPackage> eSubpackages = constrainedMetamodel.getESubpackages();
		for (EPackage subpackages : eSubpackages) {
			cleanMetaclasses(subpackages);
		}
		EList<EClassifier> classifiers = constrainedMetamodel.getEClassifiers();
		for (EClassifier eClassifier : classifiers) {

			List<EAnnotation> toRemove = new ArrayList<EAnnotation>();
			EList<EAnnotation> eAnnotations = eClassifier.getEAnnotations();
			for (EAnnotation eAnnotation : eAnnotations) {
				if (eAnnotation.getSource().equals(
						OWLTransformationConstants.OWL_DEFINITION)) {
					toRemove.add(eAnnotation);
				}
				if (eAnnotation.getSource().equals(
						OWLTransformationConstants.OWL_CONSTRAINT)) {
					toRemove.add(eAnnotation);
				}
			}
			eClassifier.getEAnnotations().removeAll(toRemove);
		}
	}

}
