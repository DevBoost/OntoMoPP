/**
 * <copyright>
 * </copyright>
 *
 * 
 */
package org.emftext.language.petrinets.resource.petrinets.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.emftext.language.petrinets.Arc;
import org.emftext.language.petrinets.Expression;
import org.emftext.language.petrinets.Function;
import org.emftext.language.petrinets.FunctionCall;
import org.emftext.language.petrinets.Parameter;
import org.emftext.language.petrinets.Place;
import org.emftext.language.petrinets.Variable;
import org.emftext.language.petrinets.VariableCall;
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
		List<Function> candidates = getDeclaredFunctions(container);
		filterFunctions(candidates, container, identifier, resolveFuzzy, result);
	}

	private void filterFunctions(List<Function> candidates,
			FunctionCall container, String identifier, boolean resolveFuzzy,
			IPetrinetsReferenceResolveResult<Function> result) {
		for (Function function : candidates) {
			if (resolveFuzzy) {
				if (function.getName().startsWith(identifier)) {
					result.addMapping(function.getName(), function);
				}
			} else {
				if (function.getName().equals(identifier)) {
					if (parametersMatch(function.getParameters(),
							container.getParameters())) {
						result.addMapping(identifier, function);
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
		for (int i = 0; i < expected.size(); i++) {
			EClassifier parameterType = expected.get(i).getType();
			EClassifier type = getType(found.get(i));
			if (type.equals(parameterType)) {
				continue;
			}
			if (parameterType instanceof EClass && type instanceof EClass) {
				EClass parameterClass = (EClass) parameterType;
				if (parameterClass.isSuperTypeOf((EClass) type)) {
					continue;
				}
			}
			return false;
		}
		return false;
	}

	public String deResolve(org.emftext.language.petrinets.Function element,
			org.emftext.language.petrinets.FunctionCall container,
			org.eclipse.emf.ecore.EReference reference) {
		return element.getName();
	}

	private List<Function> getDeclaredFunctions(FunctionCall container) {
		List<Function> functions = new ArrayList<Function>();
		EObject containingObject = container.eContainer();
		EClassifier contextType = null;
		if (containingObject instanceof Variable) {
			contextType = calculateVariableContextType((Variable) containingObject);

		} else if (containingObject instanceof Expression) {
			Expression e = (Expression) containingObject;
			contextType = getType(e);
		}
		addFunctions(functions, contextType);

		return functions;
	}

	private void addFunctions(List<Function> functions, EClassifier type) {
		FunctionCache.getInstance().addFunctions(functions, type);
	}

	private EClassifier calculateVariableContextType(Variable containingObject) {
		Arc arc = (Arc) containingObject.eContainer();
		if (arc.getIn() instanceof Place) {
			Place p = (Place) arc.getIn();
			return p.getType();
		}
		return null;
	}

	private EClassifier getType(Expression e) {
		EClassifier type = e.getType();
		if (type == null) {
			type = calculateType(e);
		}
		return type;
	}

	private EClassifier calculateType(Expression e) {
		if (e instanceof VariableCall) {
			VariableCall vc = (VariableCall) e;
			EClassifier type = getType(vc.getVariable().getInitialisation());
			vc.setType(type);
			return type;
		}
		if (e instanceof FunctionCall) {
			FunctionCall fc = (FunctionCall) e;
			EClassifier type = fc.getFunction().getType();
			fc.setType(type);
			return type;
		}
		return null;
	}

	public void setOptions(java.util.Map<?, ?> options) {
		// save options in a field or leave method empty if this resolver does
		// not depend
		// on any option
	}

}
