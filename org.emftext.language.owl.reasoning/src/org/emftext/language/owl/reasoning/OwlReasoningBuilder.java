package org.emftext.language.owl.reasoning;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.emftext.language.owl.Class;
import org.emftext.language.owl.resource.owl.IOwlBuilder;
import org.emftext.language.owl.resource.owl.IOwlProblem;
import org.emftext.language.owl.resource.owl.OwlEProblemType;
import org.emftext.language.owl.resource.owl.mopp.OwlBuilderAdapter;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;
import org.emftext.language.owl.resource.owl.ui.OwlMarkerHelper;
import org.emftext.language.owl.resource.owl.util.OwlStreamUtil;
import org.semanticweb.owl.model.OWLClass;

public class OwlReasoningBuilder extends IncrementalProjectBuilder implements IOwlBuilder {

	private EMFTextOWLReasoner reasoner;

	public OwlReasoningBuilder() {
		super();
		this.reasoner = new PelletReasoner();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		OwlBuilderAdapter adapter = new OwlBuilderAdapter();
		return adapter.build(kind, args, monitor, this, getProject());
	}

	public IStatus build(OwlResource resource, IProgressMonitor monitor) {
		IFile file = WorkspaceSynchronizer.getFile(resource);
		try {			
			InputStream stream = file.getContents();
			String content = OwlStreamUtil.getContent(stream);
			Set<OWLClass> inconsistentClasses;
			//System.out.println(content);
			try {
				
				inconsistentClasses = reasoner.getInconsistentClasses(content);
			} catch (final ReasoningException e) {
				resource.addProblem(new IOwlProblem() {

					public OwlEProblemType getType() {
						return OwlEProblemType.ERROR;
					}

					public String getMessage() {
						return e.getMessage();
					}
				}, resource.getContents().get(0));
				OwlMarkerHelper.mark(resource);
				return Status.OK_STATUS;
			}
			Set<String> invalidIris = new HashSet<String>();
			for (OWLClass class1 : inconsistentClasses) {
				invalidIris.add(class1.getURI().getFragment());
			}
			TreeIterator<EObject> allContents = resource.getAllContents();
			while (allContents.hasNext()) {
				EObject next = allContents.next();
				if (next instanceof Class) {
					final Class c = ((Class) next);
					if (invalidIris.contains(c.getIri())) {
						resource.addProblem(new IOwlProblem() {

							public OwlEProblemType getType() {
								return OwlEProblemType.ERROR;
							}

							public String getMessage() {
								return "The class '" + c.getIri()
										+ "' is inconsistent.";
							}
						}, next);
					}
				}
			}
			OwlMarkerHelper.mark(resource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

	public boolean isBuildingNeeded(URI uri) {
		return true;
	}

}
