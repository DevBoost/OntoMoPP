/*******************************************************************************
 * Copyright (c) 2006-2012
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/
/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.owl.resource.owl.analysis;

import org.emftext.language.owl.resource.owl.analysis.custom.CrossResourceIRIResolver;

public class AnnotationValueAnnotationPropertyReferenceResolver
		implements
		org.emftext.language.owl.resource.owl.IOwlReferenceResolver<org.emftext.language.owl.AnnotationValue, org.emftext.language.owl.AnnotationProperty> {

	private org.emftext.language.owl.resource.owl.analysis.OwlDefaultResolverDelegate<org.emftext.language.owl.AnnotationValue, org.emftext.language.owl.AnnotationProperty> delegate = new org.emftext.language.owl.resource.owl.analysis.OwlDefaultResolverDelegate<org.emftext.language.owl.AnnotationValue, org.emftext.language.owl.AnnotationProperty>();

	public void resolve(
			String identifier,
			org.emftext.language.owl.AnnotationValue container,
			org.eclipse.emf.ecore.EReference reference,
			int position,
			boolean resolveFuzzy,
			final org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult<org.emftext.language.owl.AnnotationProperty> result) {
		delegate.resolve(identifier, container, reference, position,
				resolveFuzzy, result);
		CrossResourceIRIResolver.theInstance().doResolve(identifier, container,
				resolveFuzzy, result,
				org.emftext.language.owl.AnnotationProperty.class);
	
	}

	public String deResolve(
			org.emftext.language.owl.AnnotationProperty element,
			org.emftext.language.owl.AnnotationValue container,
			org.eclipse.emf.ecore.EReference reference) {
		return CrossResourceIRIResolver.theInstance().deResolve(element,
				container, reference);
	}

	public void setOptions(java.util.Map<?, ?> options) {
		// save options in a field or leave method empty if this resolver does
		// not depend
		// on any option
	}

}
