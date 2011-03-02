/**
  * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.petrinets.resource.petrinets.analysis;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.emftext.language.petrinets.Expression;
import org.emftext.language.petrinets.Function;
import org.emftext.language.petrinets.FunctionCall;
import org.emftext.language.petrinets.PGenericType;
import org.emftext.language.petrinets.PList;
import org.emftext.language.petrinets.Parameter;
import org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolveResult;

public class FunctionCallFunctionReferenceResolver
		implements
		org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolver<org.emftext.language.petrinets.FunctionCall, org.emftext.language.petrinets.Function> {

	private org.emftext.language.petrinets.resource.petrinets.analysis.PetrinetsDefaultResolverDelegate<org.emftext.language.petrinets.FunctionCall, org.emftext.language.petrinets.Function> delegate = new org.emftext.language.petrinets.resource.petrinets.analysis.PetrinetsDefaultResolverDelegate<org.emftext.language.petrinets.FunctionCall, org.emftext.language.petrinets.Function>();

	public void resolve(
			String identifier,
			org.emftext.language.petrinets.FunctionCall container,
			org.eclipse.emf.ecore.EReference reference,
			int position,
			boolean resolveFuzzy,
			final org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolveResult<org.emftext.language.petrinets.Function> result) {

 		List<Function> candidates = FunctionCache.getInstance()
				.getDeclaredFunctions(container);
		if (!resolveFuzzy) {
			setHelpingErrorMessage(identifier, container, result);
		}
		filterFunctions(candidates, container, identifier, resolveFuzzy, result);
	}

	private void setHelpingErrorMessage(
			String identifier,
			org.emftext.language.petrinets.FunctionCall container,
			final org.emftext.language.petrinets.resource.petrinets.IPetrinetsReferenceResolveResult<org.emftext.language.petrinets.Function> result) {
		EClassifier contextType = FunctionCache.getInstance().getContextType(
				container);
		String typeNote = "";
		if (contextType != null) {
			typeNote = " for '" + contextType.getName() + "'";
		}
		if (contextType instanceof PList
				&& ((PList) contextType).getType() != null) {
			typeNote = " for 'List<"
					+ ((PList) contextType).getType().getName() + ">" + "'";
		}
		String message = "The function '" + identifier + "' is not defined"
				+ typeNote;
		EList<Expression> parameters = container.getParameters();
		if (!parameters.isEmpty()) {
			message += " with argument(s) ";
			for (Expression expression : parameters) {
				EClassifier type = FunctionCache.getInstance().getType(
						expression);
				String name = "Type";
				if (type != null) {
					name = type.getName();
				}
				type = FunctionCache.getInstance().getType(expression);
				message += name + ", ";

			}
			message = message.substring(0, message.length() - 2);
		}
		message += ".";
		result.setErrorMessage(message);
	}

	private void filterFunctions(List<Function> candidates,
			FunctionCall container, String identifier, boolean resolveFuzzy,
			IPetrinetsReferenceResolveResult<Function> result) {
		for (Function function : candidates) {
			if (resolveFuzzy) {
				result.addMapping(function.getName(), function);
			} else {
				EClassifier contextType = FunctionCache.getInstance()
						.getContextType(container);
				if (function.getName().equals(identifier)) {
					if (parametersMatch(function.getParameters(),
							container.getParameters(), contextType)) {
						result.addMapping(identifier, function);
						container.setType(FunctionCache.getInstance()
								.getFunctionReturnType(null, function));
						return;
					}
				}
			}
		}

	}

	private boolean parametersMatch(EList<Parameter> expected,
			EList<Expression> found, EClassifier contextType) {
		if (expected.size() != found.size()) {
			return false;
		}
		parameterloop: for (int i = 0; i < expected.size(); i++) {
			EClassifier parameterType = expected.get(i).getType();
			Expression parameterExpression = found.get(i);
		
 			EClassifier argumentType = FunctionCache.getInstance().getType(
					parameterExpression);
			if (argumentType == null)
				return false;
			if (parameterType instanceof PGenericType) {
				if (contextType instanceof PList) {
					if (isSubtype(argumentType, ((PList)contextType).getType()) ) {
						continue; // check next parameter
					
					}
				}
			}
			if (parameterType instanceof EClassifier
					&& argumentType instanceof EClassifier) {
				if (isSubtype(argumentType, parameterType)) {
					continue; // check next parameter
				}
			}

			return false;
		}
		return true;
	}

	private boolean isSubtype(EClassifier subtype, EClassifier supertype) {
		if (subtype instanceof PList && supertype instanceof PList) {
			return isSubtype(((PList) subtype).getType(),
					((PList) supertype).getType());
		}
		if (subtype.getInstanceClassName() != null) {
			if (subtype.getInstanceClassName().equals(
					supertype.getInstanceClassName())) {
				return true;
			}
		}
		if (supertype instanceof EClass && subtype instanceof EClass) {
			return ((EClass) supertype).isSuperTypeOf((EClass) subtype);
		}
 		if (supertype.getInstanceClass().getName().equals("java.lang.Object")) return true;
		return false;
	}

	public String deResolve(org.emftext.language.petrinets.Function element,
			org.emftext.language.petrinets.FunctionCall container,
			org.eclipse.emf.ecore.EReference reference) {
		return element.getName();
	}

	public void setOptions(java.util.Map<?, ?> options) {
		// save options in a field or leave method empty if this resolver does
		// not depend
		// on any option
	}

}
