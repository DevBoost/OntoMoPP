/*******************************************************************************
 * Copyright (c) 2006-2010 
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
package org.emftext.language.owl.resource.owl.analysis;

import java.util.Map;

import org.emftext.language.owl.resource.owl.IOwlReferenceResolveResult;
import org.emftext.language.owl.resource.owl.IOwlReferenceResolver;
import org.emftext.language.owl.resource.owl.analysis.custom.CrossResourceIRIResolver;

public class ClassAtomicClazzReferenceResolver
		implements
		IOwlReferenceResolver<org.emftext.language.owl.ClassAtomic, org.emftext.language.owl.Class> {

	private OwlDefaultResolverDelegate<org.emftext.language.owl.ClassAtomic, org.emftext.language.owl.Class> delegate = new OwlDefaultResolverDelegate<org.emftext.language.owl.ClassAtomic, org.emftext.language.owl.Class>();

	public java.lang.String deResolve(org.emftext.language.owl.Class element,
			org.emftext.language.owl.ClassAtomic container,
			org.eclipse.emf.ecore.EReference reference) {
		return CrossResourceIRIResolver.theInstance().deResolve(element,
				container, reference);
	}

	public void resolve(java.lang.String identifier,
			org.emftext.language.owl.ClassAtomic container,
			org.eclipse.emf.ecore.EReference reference, int position,
			boolean resolveFuzzy,
			IOwlReferenceResolveResult<org.emftext.language.owl.Class> result) {
		CrossResourceIRIResolver.theInstance().doResolve(identifier, container,
				resolveFuzzy, result, org.emftext.language.owl.Class.class);
		delegate.resolve(identifier, container, reference, position,
				resolveFuzzy, result);
	}

	public void setOptions(Map<?, ?> options) {
	}
}
