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
package org.emftext.language.owl.resource.owl.analysis;

import java.util.Map;

import org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult;
import org.emftext.language.owl.resource.owl.IOwlReferenceResolver;
import org.emftext.language.owl.resource.owl.analysis.custom.CrossResourceIRIResolver;

public class SameIndividualsIndividualsReferenceResolver
		implements
		IOwlReferenceResolver<org.emftext.language.owl.SameIndividuals, org.emftext.language.owl.Individual> {

	private OwlDefaultResolverDelegate<org.emftext.language.owl.SameIndividuals, org.emftext.language.owl.Individual> delegate = new OwlDefaultResolverDelegate<org.emftext.language.owl.SameIndividuals, org.emftext.language.owl.Individual>();

	public java.lang.String deResolve(
			org.emftext.language.owl.Individual element,
			org.emftext.language.owl.SameIndividuals container,
			org.eclipse.emf.ecore.EReference reference) {
		return CrossResourceIRIResolver.theInstance().deResolve(element,
				container, reference);
	}

	public void resolve(
			java.lang.String identifier,
			org.emftext.language.owl.SameIndividuals container,
			org.eclipse.emf.ecore.EReference reference,
			int position,
			boolean resolveFuzzy,
			IOwlReferenceResolveResult<org.emftext.language.owl.Individual> result) {

		delegate.resolve(identifier, container, reference, position,
				resolveFuzzy, result);
		CrossResourceIRIResolver.theInstance()
		.doResolve(identifier, container, resolveFuzzy, result,
				org.emftext.language.owl.Individual.class);
	}

	public void setOptions(Map<?, ?> options) {
	}
}
