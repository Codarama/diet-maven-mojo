package net.ayld.facade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import net.ayld.facade.api.Minimizer;
import net.ayld.facade.model.ClassName;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * <p>
 * This implementation of the {@link AbstractMojo} can be used to minimize the
 * dependencies of a Maven project.
 * </p>
 * 
 * @see https://github.com/ayld/Facade
 * @see https://github.com/amaranthius/facade-maven
 */
@Mojo(name = "minimizer", defaultPhase = LifecyclePhase.PACKAGE)
public class MavenMinimizerMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.sourceDirectory}", property = "sources", required = true)
	private String sources;

	@Parameter(defaultValue = "${settings.localRepository}", property = "libs", required = true)
	private String libs;

	@Parameter(property = "forceInclude", required = false)
	private String[] forceInclude;

	@Parameter(defaultValue = "${project.build.directory}", property = "target", required = false)
	private String target;

	/**
	 * <p>
	 * Calls the Facade library in order to minimize the project's dependencies
	 * </p>
	 * <p>
	 * The method would first attempt to configure the Facade's
	 * {@link Minimizer} using the parameters that Maven has passed
	 * </p>
	 * 
	 * @see {@link AbstractMojo#execute()}
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Minimizing dependencies");

		// set up required arguments here
		Minimizer minimizer = Minimizer.sources(sources).libs(libs);

		// set up list of classes to force include (e.g. runtime dependencies)
		if (forceInclude != null) {
			List<ClassName> classNames = new ArrayList<ClassName>();
			for (String classToForceInclude : forceInclude) {
				classNames.add(new ClassName(classToForceInclude));
			}

			minimizer.forceInclude(classNames.toArray(new ClassName[classNames.size()]));
		}

		// set up custom target directory
		if (target != null) {
			minimizer.output(target);
		}

		// attempt to output the minimized dependency JAR file
		try {
			final JarFile jar = minimizer.getJar();
			getLog().info("Minimized dependencies are located in " + jar.getName());
		} catch (IOException e) {
			getLog().error("Minimize not successful!", e);
			throw new MojoExecutionException(MavenMinimizerMojo.class, "Unable to minimize dependencies",
					"There was na IO error while outputing the minimized dependency jar : " + e.getMessage());
		}
	}

}
