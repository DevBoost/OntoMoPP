/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.swrl.resource.swrl.analysis;

public class OwlDECIMALTokenResolver implements org.emftext.language.swrl.resource.swrl.ISwrlTokenResolver {
	
	private org.emftext.language.owl.resource.owl.analysis.OwlDECIMALTokenResolver importedResolver = new org.emftext.language.owl.resource.owl.analysis.OwlDECIMALTokenResolver();
	
	public String deResolve(Object value, org.eclipse.emf.ecore.EStructuralFeature feature, org.eclipse.emf.ecore.EObject container) {
		String result = importedResolver.deResolve(value, feature, container);
		return result;
	}
	
	public void resolve(String lexem, org.eclipse.emf.ecore.EStructuralFeature feature, final org.emftext.language.swrl.resource.swrl.ISwrlTokenResolveResult result) {
		importedResolver.resolve(lexem, feature, new org.emftext.language.owl.resource.owl.IOwlTokenResolveResult() {
			public String getErrorMessage() {
				return result.getErrorMessage();
			}
			
			public Object getResolvedToken() {
				return result.getResolvedToken();
			}
			
			public void setErrorMessage(String message) {
				result.setErrorMessage(message);
			}
			
			public void setResolvedToken(Object resolvedToken) {
				result.setResolvedToken(resolvedToken);
			}
			
		});
	}
	
	public void setOptions(java.util.Map<?,?> options) {
		importedResolver.setOptions(options);
	}
	
}
