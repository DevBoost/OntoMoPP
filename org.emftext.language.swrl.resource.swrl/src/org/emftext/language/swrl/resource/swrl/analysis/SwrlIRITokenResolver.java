/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.swrl.resource.swrl.analysis;

public class SwrlIRITokenResolver implements org.emftext.language.swrl.resource.swrl.ISwrlTokenResolver {
	
	private org.emftext.language.swrl.resource.swrl.analysis.SwrlDefaultTokenResolver defaultTokenResolver = new org.emftext.language.swrl.resource.swrl.analysis.SwrlDefaultTokenResolver();
	
	public String deResolve(Object value, org.eclipse.emf.ecore.EStructuralFeature feature, org.eclipse.emf.ecore.EObject container) {
		String result = defaultTokenResolver.deResolve(value, feature, container);
		return result;
	}
	
	public void resolve(String lexem, org.eclipse.emf.ecore.EStructuralFeature feature, org.emftext.language.swrl.resource.swrl.ISwrlTokenResolveResult result) {
		defaultTokenResolver.resolve(lexem, feature, result);
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		defaultTokenResolver.setOptions(options);
	}
	
}
