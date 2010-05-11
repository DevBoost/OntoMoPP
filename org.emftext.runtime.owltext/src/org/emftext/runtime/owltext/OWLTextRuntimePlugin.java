package org.emftext.runtime.owltext;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class OWLTextRuntimePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.emftext.runtime.owltext";

	// The shared instance
	private static OWLTextRuntimePlugin plugin;

	/**
	 * The constructor
	 */
	public OWLTextRuntimePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static OWLTextRuntimePlugin getDefault() {
		return plugin;
	}

}
