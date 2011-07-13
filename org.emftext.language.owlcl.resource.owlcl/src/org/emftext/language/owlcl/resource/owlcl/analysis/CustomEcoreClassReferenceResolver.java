/*******************************************************************************
 * Copyright (c) 2006-2011
 * Software Technology Group, Dresden University of Technology
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany
 *      - initial API and implementation
 ******************************************************************************/
package org.emftext.language.owlcl.resource.owlcl.analysis;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.emftext.language.owlcl.OWLCLSpec;
import org.emftext.language.owlcl.Type;
import org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolveResult;

public class CustomEcoreClassReferenceResolver {

	public void resolve(
			String identifier,
			OWLCLSpec spec,
			org.eclipse.emf.ecore.EReference reference,
			int position,
			boolean resolveFuzzy,
			final org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolveResult<org.eclipse.emf.ecore.EClass> result) {

		EList<Type> types = spec.getTypes();
		findInClasses(identifier, resolveFuzzy, result, types);
		
		EPackage constrainedMetamodel = spec.getConstrainedMetamodel();
		EList<EClassifier> eClassifiers = constrainedMetamodel
				.getEClassifiers();
		findInClasses(identifier, resolveFuzzy, result, eClassifiers);
		findInSubpackages(identifier, resolveFuzzy, result, constrainedMetamodel);
	}

	private void findInSubpackages(String identifier, boolean resolveFuzzy,
			IOwlclReferenceResolveResult<EClass> result,
			EPackage superpackage) {
		if (! resolveFuzzy && result.getMappings() != null && result.getMappings().size() > 0) return;
		EList<EPackage> eSubpackages = superpackage.getESubpackages();
		for (EPackage ePackage : eSubpackages) {
			findInClasses(identifier, resolveFuzzy, result, ePackage.getEClassifiers());
		}
	}

	private void findInClasses(
			String identifier,
			boolean resolveFuzzy,
			final org.emftext.language.owlcl.resource.owlcl.IOwlclReferenceResolveResult<org.eclipse.emf.ecore.EClass> result,
			EList<? extends EClassifier> eClassifiers) {
		if (! resolveFuzzy && result.getMappings() != null && result.getMappings().size() > 0) return;
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
	
}
