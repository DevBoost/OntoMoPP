/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.owltext.language.office.resource.office.analysis;

public class OfficeTITLETokenResolver implements org.owltext.language.office.resource.office.IOfficeTokenResolver {
	
	private org.owltext.language.office.resource.office.analysis.OfficeDefaultTokenResolver defaultTokenResolver = new org.owltext.language.office.resource.office.analysis.OfficeDefaultTokenResolver(true);
	
	public String deResolve(Object value, org.eclipse.emf.ecore.EStructuralFeature feature, org.eclipse.emf.ecore.EObject container) {
		// By default token de-resolving is delegated to the DefaultTokenResolver.
		String result = defaultTokenResolver.deResolve(value, feature, container, null, null, null);
		return result;
	}
	
	public void resolve(String lexem, org.eclipse.emf.ecore.EStructuralFeature feature, org.owltext.language.office.resource.office.IOfficeTokenResolveResult result) {
		// By default token resolving is delegated to the DefaultTokenResolver.
		defaultTokenResolver.resolve(lexem, feature, result, null, null, null);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		defaultTokenResolver.setOptions(options);
	}
	
}