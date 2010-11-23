package org.emftext.language.petrinets.resource.petrinets.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.petrinets.Arc;
import org.emftext.language.petrinets.Expression;
import org.emftext.language.petrinets.Function;
import org.emftext.language.petrinets.FunctionCall;
import org.emftext.language.petrinets.Parameter;
import org.emftext.language.petrinets.PetriNet;
import org.emftext.language.petrinets.PetrinetsFactory;
import org.emftext.language.petrinets.Place;
import org.emftext.language.petrinets.Variable;
import org.emftext.language.petrinets.VariableCall;
import org.emftext.language.petrinets.impl.PetrinetsFactoryImpl;

public class FunctionCache {

	private static FunctionCache theInstance;
	private Map<String, List<Function>> functionCache;
	private Map<String, List<Function>> basicFunctions;

	private FunctionCache() {
		functionCache = new HashMap<String, List<Function>>();
		basicFunctions = new HashMap<String, List<Function>>();
		initBasicFunctionCache();
	}

	private void initBasicFunctionCache() {
		ResourceSetImpl rs = new ResourceSetImpl();
		URI uri = URI
				.createPlatformPluginURI(
						"/org.emftext.language.petrinets.resource.petrinets/library/standardlib.petrinets",
						true);
		Resource resource = rs.getResource(uri, true);
		if (resource.getContents().size() == 1) {
			PetriNet p = (PetriNet) resource.getContents().get(0);
			EList<Function> functions = p.getFunctions();
			for (Function function : functions) {
				System.out.println(function.getName());
				EClassifier context = function.getContext();
				addBasicFunction(context, function);
			}
		}
	}

	private void addBasicFunction(EClassifier context, Function function) {
		List<Function> list = basicFunctions
				.get(context.getInstanceClassName());
		if (list == null) {
			list = new ArrayList<Function>();
			basicFunctions.put(context.getInstanceClassName(), list);
		}
		list.add(function);
	}

	public static FunctionCache getInstance() {
		if (theInstance == null) {
			theInstance = new FunctionCache();
		}
		return theInstance;
	}

	public void addFunctions(List<Function> functions, EClassifier type) {
		List<Function> basics = basicFunctions.get(type.getInstanceClassName());
		if (basics != null) {
			functions.addAll(basics);
		}
		List<Function> cachedFunctions = functionCache.get(type
				.getInstanceClassName());
		if (cachedFunctions == null) {
			cachedFunctions = calculateFunctions(type);
			functionCache.put(type.getInstanceClassName(), cachedFunctions);
		}
		functions.addAll(cachedFunctions);
	}

	private List<Function> calculateFunctions(EClassifier type) {
		List<Function> functions = new ArrayList<Function>();
		if (type instanceof EClass) {
			EClass cls = (EClass) type;
			EList<EStructuralFeature> eStructuralFeatures = cls
					.getEAllStructuralFeatures();
			for (EStructuralFeature eStructuralFeature : eStructuralFeatures) {
				PetrinetsFactory factory = PetrinetsFactoryImpl.eINSTANCE;
				Function getter = factory.createFunction();
				String name = eStructuralFeature.getName();
				name = name.substring(0, 1).toUpperCase() + name.substring(1);
				getter.setName("get" + name);
				getter.setType(eStructuralFeature.getEType());
				functions.add(getter);

				Function setter = factory.createFunction();
				setter.setName("set" + name);
				Parameter parameter = factory.createParameter();
				parameter.setType(eStructuralFeature.getEType());
				setter.getParameters().add(parameter);
				functions.add(setter);
			}
		}
		return functions;
	}
	
	public List<Function> getDeclaredFunctions(Expression container) {
		List<Function> functions = new ArrayList<Function>();
		EObject containingObject = container.eContainer();
		EClassifier contextType = null;
		if (containingObject instanceof Variable) {
			contextType = calculateVariableContextType((Variable) containingObject);

		} else if (containingObject instanceof Expression) {
			Expression e = (Expression) containingObject;
			contextType = getType(e);
		}
		if (contextType != null) {
			addFunctionsToList(functions, contextType);
		}
		return functions;
	}

	private void addFunctionsToList(List<Function> functions, EClassifier type) {
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

	public EClassifier getType(Expression e) {
		if (e == null)
			return null;
		EClassifier type = e.getType();
		if (type == null) {
			type = calculateType(e);
		}
		return type;
	}

	private EClassifier calculateType(Expression e) {
		if (e instanceof VariableCall) {
			VariableCall vc = (VariableCall) e;
			Variable variable = vc.getVariable();
			if (variable.eIsProxy()) {
				resolveAllRequired(variable, e);

			}
			Expression expression = variable.getInitialisation();
			while(expression != null && expression.getNextExpression() != null) {
				expression = expression.getNextExpression();
			}
			EClassifier type = getType(expression);
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

	private void resolveAllRequired(Variable variable, Expression container) {
		EcoreUtil.resolve(variable, container);
	}
}
