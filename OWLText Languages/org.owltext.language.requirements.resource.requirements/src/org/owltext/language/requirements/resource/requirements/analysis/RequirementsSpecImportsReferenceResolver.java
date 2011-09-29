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
package org.owltext.language.requirements.resource.requirements.analysis;

public class RequirementsSpecImportsReferenceResolver implements org.owltext.language.requirements.resource.requirements.IRequirementsReferenceResolver<org.owltext.language.requirements.RequirementsSpec, org.owltext.language.requirements.RequirementsSpec> {
	
	private org.owltext.language.requirements.resource.requirements.analysis.RequirementsDefaultResolverDelegate<org.owltext.language.requirements.RequirementsSpec, org.owltext.language.requirements.RequirementsSpec> delegate = new org.owltext.language.requirements.resource.requirements.analysis.RequirementsDefaultResolverDelegate<org.owltext.language.requirements.RequirementsSpec, org.owltext.language.requirements.RequirementsSpec>();
	
	public void resolve(String identifier, org.owltext.language.requirements.RequirementsSpec container, org.eclipse.emf.ecore.EReference reference, int position, boolean resolveFuzzy, final org.owltext.language.requirements.resource.requirements.IRequirementsReferenceResolveResult<org.owltext.language.requirements.RequirementsSpec> result) {
		delegate.resolve(identifier, container, reference, position, resolveFuzzy, result);
	}
	
	public String deResolve(org.owltext.language.requirements.RequirementsSpec element, org.owltext.language.requirements.RequirementsSpec container, org.eclipse.emf.ecore.EReference reference) {
		return delegate.deResolve(element, container, reference);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		// save options in a field or leave method empty if this resolver does not depend
		// on any option
	}
	
}
