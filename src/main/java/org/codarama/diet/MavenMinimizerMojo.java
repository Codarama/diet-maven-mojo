package org.codarama.diet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codarama.diet.api.DefaultMinimizer;
import org.codarama.diet.api.Minimizer;
import org.codarama.diet.api.reporting.MinimizationReport;
import org.codarama.diet.api.reporting.MinimizationStatistics;
import org.codarama.diet.model.ClassName;

/**
 * <p>
 * This implementation of the {@link AbstractMojo} can be used to minimize the dependencies of a Maven project.
 * </p>
 * 
 * @see https://diet.codarama.org
 */
@Mojo(name = "minimizer", defaultPhase = LifecyclePhase.PACKAGE)
public class MavenMinimizerMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.build.sourceDirectory}", property = "sources", required = true)
	private String sources;

	@Parameter(defaultValue = "${settings.localRepository}", property = "libs", required = true)
	private String pathToLocalRepo;

	@Parameter(property = "forceInclude", required = false)
	private String[] forceInclude;

	@Parameter(defaultValue = "${project.build.directory}", property = "target", required = false)
	private String target;

	@Component
	private MavenProject project;

	/**
	 * <p>
	 * Calls the Facade library in order to minimize the project's dependencies
	 * </p>
	 * <p>
	 * The method would first attempt to configure the Facade's {@link Minimizer} using the parameters that Maven has
	 * passed
	 * </p>
	 * 
	 * @see {@link AbstractMojo#execute()}
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			getLog().info("Minimizing dependencies");

			// start by building up the minimizer using the path to the source files
			Minimizer minimizer = DefaultMinimizer.sources(sources);

			// ... then attempt to build a path to the dependencies
			minimizer = buildUpDependencies(minimizer);

			// ... then set up the target directory
			minimizer.output(target);

			// ... finally attempt to output the minimized dependency JAR file
			final MinimizationReport report = minimizer.minimize();
			// TODO inject newly minimized dependency here
			logStatistics(report.getStatistics());
		} catch (IOException e) {
			getLog().error("Minimize not successful!", e);
			throw new MojoExecutionException(MavenMinimizerMojo.class, "Unable to minimize dependencies",
					"An IO error spooked me out : " + e.getMessage());
		} catch (IllegalArgumentException e) {
			getLog().error("Minimize not successful!", e);
			throw new MojoExecutionException(MavenMinimizerMojo.class, "Unable to minimize dependencies",
					"Configuration spooked me out : " + e.getMessage());

		}
	}

	private void logStatistics(MinimizationStatistics statistics) {
		getLog().info("=========================");
		getLog().info("   Minimization Report");
		getLog().info("=========================");
		getLog().info("Total execution time : " + statistics.getTotalExecutionTime() + "ms");
		getLog().info("Total source files : " + statistics.getSourceFilesCount());
		getLog().info("Total dependencies before minimization : " + statistics.getTotalDependenciesCount());
		getLog().info("Total dependencies after minimization : " + statistics.getMinimizedDependenciesCount());
		double percentage = statistics.getMinimizedDependenciesCount() * 100 / statistics.getTotalDependenciesCount();
		getLog().info("Minimized dependencies as part of the total depednencies : " + percentage + "%");
	}

	private Minimizer buildUpDependencies(Minimizer minimizer) throws IOException {

		// set up list of classes to force include (e.g. runtime dependencies)
		if (forceInclude != null) {
			List<ClassName> classNames = new ArrayList<ClassName>();
			for (String classToForceInclude : forceInclude) {
				classNames.add(new ClassName(classToForceInclude));
			}

			minimizer.forceInclude(classNames.toArray(new ClassName[classNames.size()]));
		}

		// set up Maven project dependencies
		Set<Artifact> artifacts = project.getDependencyArtifacts(); // FIXME are transitive dependencies included?
		Set<File> artifactLocations = new HashSet<File>();
		for (Artifact artifact : artifacts) {
			// we want to exclude test or provided artifacts - the first because
			// they will never be required in the final assembly of the
			// minimized application, the second because they are already
			// provided and we might introduce conflicts
			if (Artifact.SCOPE_TEST.equals(artifact.getScope()) || Artifact.SCOPE_PROVIDED.equals(artifact.getScope())) {
				getLog().debug("Excluding test or provided artifact : " + artifact.getArtifactId());
			} else {
				artifactLocations.add(artifact.getFile());
			}
		}

		// if inspecting the Maven project resulted in an empty list then we
		// fallback to the Maven project local repository (could be very costly
		// as this directory typically contains lots of files)
		if (artifactLocations.isEmpty()) {
			getLog().warn("Could not get list of dependencies from Maven, reverting to Maven local repository");
			return minimizer.libs(pathToLocalRepo);
		} else {
			return minimizer.libs(artifactLocations);
		}
	}
}
