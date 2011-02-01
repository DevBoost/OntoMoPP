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
import org.emftext.language.petrinets.BooleanLiteral;
import org.emftext.language.petrinets.Component;
import org.emftext.language.petrinets.ConstructorCall;
import org.emftext.language.petrinets.ConsumingArc;
import org.emftext.language.petrinets.DoubleLiteral;
import org.emftext.language.petrinets.Expression;
import org.emftext.language.petrinets.FloatLiteral;
import org.emftext.language.petrinets.FunctionCall;
import org.emftext.language.petrinets.InitialisedVariable;
import org.emftext.language.petrinets.IntegerLiteral;
import org.emftext.language.petrinets.LongLiteral;
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
				stringBuffer.appendLine("transition_" + trimQuotes(t.getName())
						+ "_doFire();");
				stringBuffer.appendLine("petrinetWasUpdated = true;");
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

		stringBuffer.appendLine("private void transition_"
				+ trimQuotes(t.getName()) + "_doFire() {");
		stringBuffer.indent();
		for (ConsumingArc arc : incomingArcs) {
			Place in = arc.getIn();

			stringBuffer.appendLine(printType(in) + " "
					+ arc.getVariable().getName() + " = " + "_place_"
					+ trimQuotes(in.getName()) + ".remove(0);");
		}

		EList<Statement> statements = t.getStatements();
		for (Statement statement : statements) {
			generateCode(statement);
		}

		for (ProducingArc arc : t.getOutgoing()) {
			Place out = arc.getOut();
			stringBuffer.append("_place_" + trimQuotes(out.getName()) + ".add("
					+ arc.getVariable().getVariable().getName() + ");");
		}

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
				while (initialisation.getNextExpression() != null) {
					initialisation = initialisation.getNextExpression();
				}
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
		if (o instanceof InitialisedVariable)
			generateCode((InitialisedVariable) o);
		else if (o instanceof FunctionCall)
			generateCode((FunctionCall) o);
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

		if (vc.getNextExpression() != null)
			generateCode(vc.getNextExpression());
	}

	private void generateCode(ConstructorCall cc) {
		String contextVar = generateContextVariableName();
		stringBuffer.appendLine(printType(cc) + " " + contextVar + " = ");
		stringBuffer.append(printCreation(cc.getType(), cc));
		stringBuffer.newline();
		this.contextVariableName = contextVar;

		if (cc.getNextExpression() != null)
			generateCode(cc.getNextExpression());
	}

	private void generateCode(FunctionCall fc) {
		stringBuffer.appendLine("// ." + fc.getFunction().getName() + "()");
		String prevContextVarName = this.contextVariableName;
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
		if ((fc.getFunction().getParameters().size() + 1) == fc.getParameters()
				.size()) {
			stringBuffer.appendLine(fc.getFunction().getName() + "(");
		}

		if (fc.getPreviousExpression() != null) {
			stringBuffer.append(prevContextVarName);
		}
		// api function
		if ((fc.getFunction().getParameters().size()) == fc.getParameters()
				.size()) {
			stringBuffer.append("." + fc.getFunction().getName() + "(");
		} else {
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
		if (fc.getNextExpression() != null)
			generateCode(fc.getNextExpression());
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
}
