package org.emftext.language.owl.reasoning;

import java.io.IOException;
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
import org.emftext.language.owl.IRIIdentified;
import org.emftext.language.owl.resource.owl.IOwlBuilder;
import org.emftext.language.owl.resource.owl.IOwlProblem;
import org.emftext.language.owl.resource.owl.OwlEProblemType;
import org.emftext.language.owl.resource.owl.mopp.OwlBuilderAdapter;
import org.emftext.language.owl.resource.owl.mopp.OwlResource;
import org.emftext.language.owl.resource.owl.ui.OwlMarkerHelper;
import org.emftext.language.owl.resource.owl.util.OwlStreamUtil;
import org.semanticweb.owlapi.model.OWLNamedObject;

public class OwlReasoningBuilder extends IncrementalProjectBuilder implements
		IOwlBuilder {

	private EMFTextOWLReasoner reasoner;

	public OwlReasoningBuilder() {
		super();
		this.reasoner = new EMFTextPelletReasoner();
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
		InputStream stream;
		try {
			stream = file.getContents();
			String content = OwlStreamUtil.getContent(stream);
			validateOWL(content, resource);
			OwlMarkerHelper.mark(resource);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

	public void validateOWL(String content, OwlResource resource) {

		try {
			Set<OWLNamedObject> inconsistentOWLObjects;
			// System.out.println(content);
			try {
				inconsistentOWLObjects = reasoner
						.getInconsistentFrames(content);
			} catch (final ReasoningException e) {
				resource.addProblem(new IOwlProblem() {

					public OwlEProblemType getType() {
						return OwlEProblemType.ERROR;
					}

					public String getMessage() {
						return e.getMessage();
					}
				}, resource.getContents().get(0));

				return;
			}
			Set<String> invalidIris = new HashSet<String>();

			for (OWLNamedObject i : inconsistentOWLObjects) {
				invalidIris.add(i.getIRI().getFragment());
			}
			TreeIterator<EObject> allContents = resource.getAllContents();
			while (allContents.hasNext()) {
				EObject next = allContents.next();
				if (next instanceof IRIIdentified) {
					final IRIIdentified c = ((IRIIdentified) next);
					if (invalidIris.contains(c.getIri())) {
						resource.addProblem(new IOwlProblem() {

							public OwlEProblemType getType() {
								return OwlEProblemType.ERROR;
							}

							public String getMessage() {
								return "The element '" + c.getIri()
										+ "' is inconsistent.";
							}
						}, next);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isBuildingNeeded(URI uri) {
		return true;
	}

}
