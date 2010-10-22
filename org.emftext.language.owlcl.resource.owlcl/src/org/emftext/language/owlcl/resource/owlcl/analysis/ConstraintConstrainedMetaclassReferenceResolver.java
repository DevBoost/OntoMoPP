/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.owlcl.resource.owlcl.analysis;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.emftext.language.owlcl.OWLCLSpec;

public class ConstraintConstrainedMetaclassReferenceResolver
		implements
		org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolver<org.emftext.language.owlcl.Constraint, org.eclipse.emf.ecore.EClass> {

	private org.emftext.language.owlcl.resource.owlcl.analysis.OwlclDefaultResolverDelegate<org.emftext.language.owlcl.Constraint, org.eclipse.emf.ecore.EClass> delegate = new org.emftext.language.owlcl.resource.owlcl.analysis.OwlclDefaultResolverDelegate<org.emftext.language.owlcl.Constraint, org.eclipse.emf.ecore.EClass>();

	public void resolve(
			String identifier,
			org.emftext.language.owlcl.Constraint container,
			org.eclipse.emf.ecore.EReference reference,
			int position,
			boolean resolveFuzzy,
			final org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolveResult<org.eclipse.emf.ecore.EClass> result) {
		OWLCLSpec spec = (OWLCLSpec) container.eContainer();
		EPackage constrainedMetamodel = spec.getConstrainedMetamodel();
		EList<EClassifier> eClassifiers = constrainedMetamodel
				.getEClassifiers();
		for (EClassifier eClassifier : eClassifiers) {
			if (eClassifier instanceof EClass) {
				EClass cls = (EClass) eClassifier;
				if (resolveFuzzy) {
					if (cls.getName().startsWith(identifier)) {
						result.addMapping(eClassifier.getName(), cls);
					}
				} else {
					if (cls.getName().equals(identifier)) {
						result.addMapping(identifier, cls);
						return;
					}
				}
			}

		}
	}

	public String deResolve(org.eclipse.emf.ecore.EClass element,
			org.emftext.language.owlcl.Constraint container,
			org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}

	public void setOptions(java.util.Map<?, ?> options) {
		// save options in a field or leave method empty if this resolver does
		// not depend
		// on any option
	}

}
