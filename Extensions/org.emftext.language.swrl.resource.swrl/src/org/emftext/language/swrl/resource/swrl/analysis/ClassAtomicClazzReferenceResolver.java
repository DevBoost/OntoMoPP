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
package org.emftext.language.swrl.resource.swrl.analysis;

public class ClassAtomicClazzReferenceResolver implements org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolver<org.emftext.language.owl.ClassAtomic, org.emftext.language.owl.Class> {
	
	private org.emftext.language.swrl.resource.swrl.analysis.SwrlDefaultResolverDelegate<org.emftext.language.owl.ClassAtomic, org.emftext.language.owl.Class> delegate = new org.emftext.language.swrl.resource.swrl.analysis.SwrlDefaultResolverDelegate<org.emftext.language.owl.ClassAtomic, org.emftext.language.owl.Class>();
	
	public void resolve(String identifier, org.emftext.language.owl.ClassAtomic container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.emftext.language.swrl.resource.swrl.ISwrlReferenceResolveResult<org.emftext.language.owl.Class> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, result);
	}
	
	public String deResolve(org.emftext.language.owl.Class element, org.emftext.language.owl.ClassAtomic container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
