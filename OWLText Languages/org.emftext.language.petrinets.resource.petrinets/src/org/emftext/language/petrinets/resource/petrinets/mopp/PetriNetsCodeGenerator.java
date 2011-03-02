package org.emftext.language.petrinets.resource.petrinets.mopp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.emftext.language.petrinets.BooleanExpression;
import org.emftext.language.petrinets.BooleanLiteral;
import org.emftext.language.petrinets.Call;
import org.emftext.language.petrinets.Cast;
import org.emftext.language.petrinets.Component;
import org.emftext.language.petrinets.ConstructorCall;
import org.emftext.language.petrinets.ConsumingArc;
import org.emftext.language.petrinets.DoubleLiteral;
import org.emftext.language.petrinets.EClassLiteral;
import org.emftext.language.petrinets.Expression;
import org.emftext.language.petrinets.FloatLiteral;
import org.emftext.language.petrinets.Function;
import org.emftext.language.petrinets.FunctionCall;
import org.emftext.language.petrinets.InitialisedVariable;
import org.emftext.language.petrinets.IntegerLiteral;
import org.emftext.language.petrinets.LongLiteral;
import org.emftext.language.petrinets.MemberCallExpression;
import org.emftext.language.petrinets.NestedExpression;
import org.emftext.language.petrinets.PList;
import org.emftext.language.petrinets.PetriNet;
import org.emftext.language.petrinets.Place;
import org.emftext.language.petrinets.ProducingArc;
import org.emftext.language.petrinets.Statement;
import org.emftext.language.petrinets.StringLiteral;
import org.emftext.language.petrinets.Transition;
import org.emftext.language.petrinets.TypedElement;
import org.emftext.language.petrinets.VariableCall;
import org.emftext.language.petrinets.resource.petrinets.PetrinetsEProblemType;
import org.emftext.language.petrinets.resource.petrinets.analysis.FunctionCache;

public class PetriNetsCodeGenerator {

	private static final String OUT_TOKEN_NAME = "outToken";

	private static final String IN_TOKEN_NAME = "inToken";

	private JavaStringBuffer stringBuffer;

	private String contextVariableName;

	private int counter = 0;

	private Map<String, GenClass> genClassMap = new HashMap<String, GenClass>();

	private PetrinetsResource resource;

	private String generateContextVariableName() {
		counter++;
		return "__temp_" + counter;
	}

	public void generateJavaCode(PetrinetsResource resource) {
		this.resource = resource;
		IFile file = WorkspaceSynchronizer.getFile(resource);

		stringBuffer = new JavaStringBuffer();
		if (resource.getContents().isEmpty())
			return;
		if (resource.getContents().get(0) instanceof PetriNet) {
			PetriNet pn = (PetriNet) resource.getContents().get(0);
			EcoreUtil.resolveAll(pn.eResource().getResourceSet());

			EcoreUtil.resolveAll(pn.eResource().getResourceSet());
			EcoreUtil.resolveAll(pn.eResource().getResourceSet());
			EcoreUtil.resolveAll(pn.eResource().getResourceSet());
			initialiseGenClassMap(pn);
			generateCode(pn);
			try {

				String name = toFirstUpper(trimQuotes(pn.getName()));
				name = name + "SemanticsEvaluation.java";

				IPath currentDir = file.getLocation().removeLastSegments(1)
						.append("" + IPath.SEPARATOR);

				EList<String> pkg = pn.getPkg();
				for (String p : pkg) {
					currentDir = currentDir.append(p + IPath.SEPARATOR);
					File folder = currentDir.toFile();
					if (!folder.exists()) {
						folder.mkdir();
					}
				}
				File f = currentDir.append(name).toFile();

				if (f.exists()) {
					f.delete();
				}
				f.createNewFile();
				FileOutputStream fout = new FileOutputStream(f);
				Writer out = new OutputStreamWriter(fout);
				try {
					out.write(stringBuffer.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

		}

	}

	private void initialiseGenClassMap(PetriNet pn) {
		EList<GenModel> genModels = pn.getGenModels();
		for (GenModel genModel : genModels) {
			TreeIterator<EObject> eAllContents = genModel.eAllContents();
			while (eAllContents.hasNext()) {
				EObject eObject = (EObject) eAllContents.next();
				if (eObject instanceof GenClass) {
					GenClass gc = (GenClass) eObject;
					EClass ecoreClass = gc.getEcoreClass();
					this.genClassMap.put(ecoreClass.getName(), gc);
				}

			}
		}

	}

	private String trimQuotes(String name) {

		return name;
	}

	private void generateCode(PetriNet pn) {
		String name = toFirstUpper(trimQuotes(pn.getName()));
		EList<Component> components = pn.getComponents();
		if (!pn.getPkg().isEmpty()) {
			stringBuffer.appendLine("package " + pn.getPkg().get(0));
			for (String p : pn.getPkg().subList(1, pn.getPkg().size())) {
				stringBuffer.append("." + p);
			}
			stringBuffer.append(";");
		}
		stringBuffer.appendLine("import org.eclipse.emf.ecore.*;");
		stringBuffer.appendLine("import java.util.*;");
		EList<GenModel> imports = pn.getGenModels();
		for (GenModel i : imports) {
			EList<GenPackage> genPackages = i.getGenModel().getGenPackages();
			for (GenPackage genPackage : genPackages) {
				stringBuffer.appendLine("import "
						+ genPackage.getInterfacePackageName() + ".*;");
			}
		}
		stringBuffer.appendLine("public class " + name
				+ "SemanticsEvaluation {");
		stringBuffer.indent();
		stringBuffer.newline();
		for (Component component : components) {
			if (component instanceof Place)
				generateCode((Place) component);
		}
		stringBuffer.newline();
		stringBuffer.appendLine("public void intialisePlaces(EObject model) {");
		stringBuffer.indent();
		stringBuffer
				.appendLine("// TODO implement place initialisation for places w/o incomming arcs.");
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");

		stringBuffer.newline();

		stringBuffer.appendLine("public void evaluatePetriNet() {");
		stringBuffer.indent();
		stringBuffer.appendLine("boolean petrinetWasUpdated = true;");
		stringBuffer.appendLine("while(petrinetWasUpdated) {;");
		stringBuffer.indent();
		stringBuffer.appendLine("petrinetWasUpdated = false;");
		for (Component component : components) {
			if (component instanceof Transition) {
				Transition t = (Transition) component;
				stringBuffer.appendLine("if(transition_"
						+ trimQuotes(t.getName()) + "_canFire()) {");
				stringBuffer.indent();
				stringBuffer.appendLine("petrinetWasUpdated = transition_"
						+ trimQuotes(t.getName()) + "_doFire();");
				stringBuffer.unIndent();
				stringBuffer.appendLine("}");
			}
		}
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");

		for (Component component : components) {
			if (component instanceof Transition) {
				Transition t = (Transition) component;
				generateCode(t);
			}
		}

		stringBuffer.newline();
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");
	}

	private String toFirstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	private void generateCode(Place p) {
		stringBuffer.appendLine("private List<" + printType(p) + "> "
				+ "_place_" + trimQuotes(p.getName()) + " = new ArrayList<"
				+ printType(p) + ">();");
	}

	private void generateCode(Transition t) {
		stringBuffer.appendLine("private boolean transition_"
				+ trimQuotes(t.getName()) + "_canFire() {");
		stringBuffer.indent();
		EList<ConsumingArc> incomingArcs = t.getIncoming();
		for (ConsumingArc arc : incomingArcs) {
			Place in = arc.getIn();
			stringBuffer.appendLine("if (" + "_place_"
					+ trimQuotes(in.getName()) + ".isEmpty() ) return false;");
		}

		stringBuffer.appendLine("return true;");

		stringBuffer.unIndent();
		stringBuffer.appendLine("}");

		stringBuffer.newline();

		stringBuffer.appendLine("private boolean transition_"
				+ trimQuotes(t.getName()) + "_doFire() {");
		stringBuffer.indent();
		for (ConsumingArc arc : incomingArcs) {
			Place in = arc.getIn();

			stringBuffer.appendLine(printType(in) + " "
					+ arc.getFreeVariable().getName() + " = " + "_place_"
					+ trimQuotes(in.getName()) + ".get(0);");
		}
		stringBuffer.appendLine("// evaluate guard expression");
		stringBuffer.appendLine("boolean guardSatisfied = false;");
		String guardEvaluationResult = null;
		if (t.getGuard() != null) {
			generateCode(t.getGuard());
			guardEvaluationResult = this.contextVariableName;
		} else {
			guardEvaluationResult = "true";
		}
		stringBuffer
				.appendLine("guardSatisfied = " + guardEvaluationResult + ";");
		stringBuffer.appendLine("if (guardSatisfied) {");
		stringBuffer.indent();
		for (ConsumingArc arc : incomingArcs) {
			Place in = arc.getIn();

			stringBuffer.appendLine(arc.getFreeVariable().getName() + " = "
					+ "_place_" + trimQuotes(in.getName()) + ".remove(0);");
		}
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");
		stringBuffer.appendLine("else return false;");

		EList<Statement> statements = t.getStatements();
		for (Statement statement : statements) {
			generateCode(statement);
		}

		stringBuffer.appendLine("// init out places");
		for (ProducingArc arc : t.getOutgoing()) {
			Place out = arc.getOut();
			stringBuffer.appendLine("_place_" + trimQuotes(out.getName())
					+ ".add(" + arc.getOutput().getName() + ");");
		}

		stringBuffer.appendLine("return true;");
		stringBuffer.unIndent();
		stringBuffer.appendLine("}");

	}

	private String printCreation(EClassifier type, ConstructorCall cc) {
		if (type.getDefaultValue() != null) {
			return type.getDefaultValue().toString();
		} else {
			GenClass genClass = findGenClass(type);
			if (genClass.isAbstract()) {
				resource.addError("'" + genClass.getName()
						+ "' is abstract and can not be instantiated",
						PetrinetsEProblemType.ANALYSIS_PROBLEM, cc);

			}
			GenPackage genPackage = genClass.getGenPackage();
			return genPackage.getQualifiedFactoryInterfaceName()
					+ ".eINSTANCE.create" + genClass.getName() + "();";

		}

	}

	private GenClass findGenClass(EClassifier type) {
		return genClassMap.get(type.getName());
	}

	private String printType(TypedElement element) {
		if (element.getType() == null) {
			EClassifier type = null;
			if (element instanceof Expression) {
				type = FunctionCache.getInstance()
						.getType((Expression) element);
			}
			if (element instanceof InitialisedVariable) {
				Expression initialisation = ((InitialisedVariable) element)
						.getInitialisation();
				type = FunctionCache.getInstance().getType(initialisation);
				((InitialisedVariable) element).setType(type);
			}
			if (type == null) {
				resource.addError("Type was not resolved",
						PetrinetsEProblemType.BUILDER_ERROR, element);
				return "UnresolvedType";
			}

		}
		EClassifier type = element.getType();
		if (type instanceof PList) {
			PList list = (PList) type;
			return "List<" + printType(list) + ">";
		}
		if (type.getInstanceClassName() != null) {
			return type.getInstanceClassName();
		}
		return type.getName();
	}

	private void generateCode(EObject o) {
		if (o == null)
			return;
		else if (o instanceof InitialisedVariable)
			generateCode((InitialisedVariable) o);
		else if (o instanceof MemberCallExpression)
			generateCode((MemberCallExpression) o);
		else if (o instanceof VariableCall)
			generateCode((VariableCall) o);
		else if (o instanceof ConstructorCall)
			generateCode((ConstructorCall) o);
		else if (o instanceof StringLiteral)
			generateCode((StringLiteral) o);
		else if (o instanceof IntegerLiteral)
			generateCode((IntegerLiteral) o);
		else if (o instanceof DoubleLiteral)
			generateCode((DoubleLiteral) o);
		else if (o instanceof LongLiteral)
			generateCode((LongLiteral) o);
		else if (o instanceof FloatLiteral)
			generateCode((FloatLiteral) o);
		else if (o instanceof BooleanLiteral)
			generateCode((BooleanLiteral) o);
		else if (o instanceof LongLiteral)
			generateCode((LongLiteral) o);
		else if (o instanceof EClassLiteral)
			generateCode((EClassLiteral) o);
		else if (o instanceof Cast)
			generateCode((Cast) o);
		else if (o instanceof BooleanExpression)
			generateCode((BooleanExpression) o);
		else if (o instanceof NestedExpression)
			generateCode((NestedExpression) o);
		else
			throw new RuntimeException("No codegeneration routine found for "
					+ o);
	}

	private void generateCode(VariableCall vc) {
		String contextVar = generateContextVariableName();
		stringBuffer.appendLine(printType(vc) + " " + contextVar + " = ");
		stringBuffer.append(vc.getVariable().getName() + ";");
		stringBuffer.newline();
		this.contextVariableName = contextVar;

		// if (vc.getNextExpression() != null)
		// generateCode(vc.getNextExpression());
	}

	private void generateCode(ConstructorCall cc) {
		String contextVar = generateContextVariableName();
		stringBuffer.appendLine(printType(cc) + " " + contextVar + " = ");
		stringBuffer.append(printCreation(cc.getType(), cc));
		stringBuffer.newline();
		this.contextVariableName = contextVar;

		// if (cc.getNextExpression() != null)
		// generateCode(cc.getNextExpression());
	}

	private void generateCode(MemberCallExpression mce) {
		Expression target = mce.getTarget();
		generateCode(target);
		String prevContextVarName = this.contextVariableName;
		for (Call c : mce.getCalls()) {
			generateCode((FunctionCall) c, prevContextVarName);
			prevContextVarName = this.contextVariableName;
		}
	}

	private void generateCode(FunctionCall fc, String prevContextVarName) {
		Function function = fc.getFunction();
		stringBuffer.appendLine("// ." + function.getName() + "()");
		EList<Expression> parameters = fc.getParameters();
		Map<Expression, String> argumentVars = new HashMap<Expression, String>();
		for (Expression expression : parameters) {
			generateCode(expression);
			argumentVars.put(expression, this.contextVariableName);
		}
		String contextVar = null;
		if (fc.getType() != null && !"PVoid".equals(fc.getType().getName())) {
			contextVar = generateContextVariableName();
			stringBuffer.appendLine(printType(fc) + " " + contextVar + " = ");
		} else {
			stringBuffer.appendLine("");
		}
		// library function
		if (function.isLibrary()) {
			PetriNet net = (PetriNet) function.eResource().getContents().get(0);
			EList<String> pkgs = net.getPkg();
			String libraryName = "";
			for (String pkg : pkgs) {
				libraryName = libraryName + pkg + ".";
			}
			libraryName = libraryName + toFirstUpper(trimQuotes(net.getName()));
			libraryName = libraryName + "SemanticsLibrary";

			stringBuffer.appendLine(libraryName + "." + function.getName()
					+ "(");
		}
		if (prevContextVarName != null) {
			stringBuffer.append(prevContextVarName);
		}
		// api function
		if (!function.isLibrary()) {
			stringBuffer.append("." + function.getName() + "(");
		} else {
			if (parameters.size() > 0)
				stringBuffer.append(", ");
		}
		int i = 0;
		for (Expression expression : parameters) {
			i++;
			if (i == parameters.size()) {
				stringBuffer.append(argumentVars.get(expression));
			} else {
				stringBuffer.append(argumentVars.get(expression) + ", ");
			}
		}
		stringBuffer.append(");");
		this.contextVariableName = contextVar;
		// if (fc.getNextExpression() != null)
		// generateCode(fc.getNextExpression());
	}

	private void generateCode(InitialisedVariable v) {
		stringBuffer.newline();
		stringBuffer.appendLine("// " + v.getName() + " initialization ");
		Expression expression = v.getInitialisation();
		generateCode(expression);
		String contextVariableName = this.contextVariableName;
		stringBuffer.appendLine(printType(v) + " " + v.getName() + " = "
				+ contextVariableName + ";");
	}

	private void generateCode(StringLiteral sl) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("String " + this.contextVariableName + " = "
				+ "\"" + sl.getValue() + "\";");
	}

	private void generateCode(IntegerLiteral il) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("int " + this.contextVariableName + " = "
				+ il.getValue() + ";");

	}

	private void generateCode(LongLiteral ll) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("long " + this.contextVariableName + " = "
				+ ll.getValue() + ";");

	}

	private void generateCode(EClassLiteral ecl) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("Class " + this.contextVariableName + " = "
				+ ecl.getClazz().getName() + ".class;");

	}

	private void generateCode(DoubleLiteral dl) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("double " + this.contextVariableName + " = "
				+ dl.getValue() + ";");

	}

	private void generateCode(FloatLiteral fl) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("float " + this.contextVariableName + " = "
				+ fl.getValue() + "f;");

	}

	private void generateCode(BooleanLiteral bl) {
		this.contextVariableName = generateContextVariableName();
		stringBuffer.appendLine("boolean " + this.contextVariableName + " = "
				+ bl.isValue() + ";");

	}

	private void generateCode(Cast cast) {
		String castVariable = generateContextVariableName();
		generateCode(cast.getExpression());
		String expressionVariable = this.contextVariableName;
		stringBuffer.appendLine(printType(cast) + " " + castVariable + " = "
				+ "(" + printType(cast) + ")" + expressionVariable + ";");
		this.contextVariableName = castVariable;
	}

	private void generateCode(NestedExpression ne) {
		String nv = generateContextVariableName();
		generateCode(ne.getExpression());
		String expressionVariable = this.contextVariableName;
		stringBuffer.appendLine(printType(ne) + " " + nv + " = "
				+ expressionVariable + ";");
		this.contextVariableName = nv;
	}

	private void generateCode(BooleanExpression be) {
		String booleanExpressionResult = generateContextVariableName();
		generateCode(be.getLeft());
		String leftResult = this.contextVariableName;
		if (be.getOperator().equals("&&")) {
			stringBuffer.appendLine("boolean " + booleanExpressionResult
					+ " = " + leftResult + ";");
			stringBuffer.appendLine("if (" + booleanExpressionResult + ") {");
			stringBuffer.indent();
			generateCode(be.getRight());
			String rightResult = this.contextVariableName;
			stringBuffer.appendLine(booleanExpressionResult + " = "
					+ rightResult + ";");
			stringBuffer.unIndent();
			stringBuffer.appendLine("}");
		} else // || operator
		{
			stringBuffer.appendLine("boolean " + booleanExpressionResult
					+ " = " + leftResult + ";");
			stringBuffer.appendLine("if (!" + booleanExpressionResult + ") {");
			stringBuffer.indent();
			generateCode(be.getRight());
			String rightResult = this.contextVariableName;
			stringBuffer.appendLine(booleanExpressionResult + " = "
					+ rightResult + ";");
			stringBuffer.unIndent();
			stringBuffer.append("}");
		}

		this.contextVariableName = booleanExpressionResult;
	}
}
