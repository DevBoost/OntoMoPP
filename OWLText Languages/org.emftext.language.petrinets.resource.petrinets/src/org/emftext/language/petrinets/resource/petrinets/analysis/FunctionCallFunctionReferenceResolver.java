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
		String message = "The function '" + identifier
				+ "' is not defined for '" + contextType.getName() + "'";
		EList<Expression> parameters = container.getParameters();
		if (!parameters.isEmpty()) {
			message += " with argument(s) ";
			for (Expression expression : parameters) {
				EClassifier type = FunctionCache.getInstance().getType(
						expression);
				message += type.getName() + ", ";

			}
			message = message.substring(0, message.length()-2);
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
				if (function.getName().equals(identifier)) {
					if (parametersMatch(function.getParameters(),
							container.getParameters())) {
						result.addMapping(identifier, function);
						container.setType(FunctionCache.getInstance().getFunctionReturnType(container, function));
						return;
					}
				}
			}
		}

	}

	private boolean parametersMatch(EList<Parameter> expected,
			EList<Expression> found) {
		if (expected.size() != found.size()) {
			return false;
		}
		parameterloop: for (int i = 0; i < expected.size(); i++) {
			EClassifier parameterType = expected.get(i).getType();
			Expression parameterExpression = found.get(i);
			while (parameterExpression.getNextExpression() != null) {
				parameterExpression = parameterExpression.getNextExpression();
			}
			EClassifier type = FunctionCache.getInstance()
					.getType(parameterExpression);
			if (type.getInstanceClassName().equals(
					parameterType.getInstanceClassName())) {
				continue;
			}
			if (parameterType instanceof EClass && type instanceof EClass) {
				EClass parameterClass = (EClass) parameterType;
				EClass typeClass = (EClass) type;
				EList<EClass> eAllSuperTypes = typeClass.getEAllSuperTypes();
				for (EClass supertype : eAllSuperTypes) {
					if (supertype.getInstanceClassName().equals(
							parameterClass.getInstanceClassName())) {
						break parameterloop;
					}
				}
			}
			return false;
		}
		return true;
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
