package org.codarama.diet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.maven.ProjectDependenciesResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.InputLocation;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codarama.diet.api.IndexedMinimizer;
import org.codarama.diet.api.ListenerRegistrar;
import org.codarama.diet.api.Minimizer;
import org.codarama.diet.api.reporting.MinimizationReport;
import org.codarama.diet.api.reporting.MinimizationStatistics;
import org.codarama.diet.api.reporting.listener.EventListener;
import org.codarama.diet.event.model.ComponentEvent;
import org.codarama.diet.model.ClassName;

/**
 * <p>
 * This implementation of the {@link AbstractMojo} can be used to minimize the dependencies of a Maven project.
 * </p>
 */
@Mojo(name = "putondiet", defaultPhase = LifecyclePhase.PACKAGE)
public class MavenMinimizerMojo extends AbstractMojo {

    private static final String LOG_PATTERN = "%d [%p|%c|%C{1}] %m%n";
    private static final String CODARAMA_ROOT_PACKAGE = "org.codarama";

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

    @Component
    private MavenSession session;

    @Component
    private ProjectDependenciesResolver projectDependenciesResolver;

	@Component
	private MavenProjectHelper projectHelper;

    /**
     * <p>
     * Calls the Facade library in order to minimize the project's dependencies
     * </p>
     * <p>
     * The method would first attempt to configure the Diet's {@link Minimizer}
     * </p>
     * 
     * @see AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        initizlizeLogger();

        try {
            getLog().info("Minimizing dependencies");

            // start by building up the minimizer using the path to the source files
            Minimizer minimizer = IndexedMinimizer.sources(sources);

            // ... then attempt to build a path to the dependencies
            minimizer = buildUpDependencies(minimizer);

            // ... then set up the target directory
            minimizer.output(target);

            attachProgressListeners();

            // ... then attempt to output the minimized dependency JAR file
            final MinimizationReport report = minimizer.minimize();

            // ... finally inject the newly minimized dependency in the package
			final File minimizedJar = new File(report.getJar().getName());
			projectHelper.attachArtifact(project, "jar", "slimjar", minimizedJar);

            logStatistics(report.getStatistics());

            getLog().info("The minimized jar is at: " + report.getJar().getName());
        } catch (IOException e) {
            getLog().error("Minimize not successful!", e);
            throw new MojoExecutionException(MavenMinimizerMojo.class, "Unable to minimize dependencies",
                    "An IO error spooked me out : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            getLog().error("Minimize not successful!", e);
            throw new MojoExecutionException("Unable to minimize dependencies, configuration spooked me out : "
                    + e.getMessage(), e);

        }
    }

    private void attachProgressListeners() {
        ListenerRegistrar.register(new EventListener<ComponentEvent>() {
            public void on(ComponentEvent event) {
                getLog().debug(event.toString());
            }
        });
    }

    private void initizlizeLogger() {
        // We want to log codarama messages with priority
        ConsoleAppender codaramaAppender = new ConsoleAppender(new PatternLayout(LOG_PATTERN));
        if (getLog().isDebugEnabled()) {
            // consider log level - if the Maven was started with the debug flag, we should also respect it and enable
            // debug messages in our code
            codaramaAppender.setThreshold(Level.DEBUG);
        } else {
            // in any other case we assume INFO level should be enough
            codaramaAppender.setThreshold(Level.INFO);
        }
        codaramaAppender.activateOptions();
        Logger.getLogger(CODARAMA_ROOT_PACKAGE).addAppender(codaramaAppender);

        // root appender would output anything we use in diet-engine, we want to limit it to errors only
        // because of chatty frameworks such as Spring, change it and all hell breaks loose, guaranteed
        ConsoleAppender rootAppender = new ConsoleAppender(new PatternLayout(LOG_PATTERN));
        rootAppender.setThreshold(Level.ERROR);
        rootAppender.activateOptions();
        Logger.getRootLogger().addAppender(rootAppender);
    }

    private void logStatistics(MinimizationStatistics statistics) {
        // display a cherry, cherries are part of any healthy diet
        getLog().info("__.--~~.,-.__");
        getLog().info("`~-._.-(`-.__`-.");
        getLog().info("        \\    `~~`");
        getLog().info("   .--./ \\");
        getLog().info("  /#   \\  \\.--.");
        getLog().info("  \\    /  /#   \\");
        getLog().info("   '--'   \\    /");
        getLog().info("           '--'");
        // then the report
        getLog().info("=========================");
        getLog().info("   Minimization Report");
        getLog().info("=========================");
        getLog().info("Total execution time : " + statistics.getFormattedExecutionTime());
        getLog().info("Total source files : " + statistics.getSourceFilesCount());
        getLog().info("Total dependencies before minimization : " + statistics.getTotalDependenciesCount());
        getLog().info("Total dependencies after minimization : " + statistics.getMinimizedDependenciesCount());
        double percentage = statistics.getMinimizedDependenciesCount() * 100 / statistics.getTotalDependenciesCount();
        getLog().info("Minimized dependencies as part of the total depednencies : " + percentage + "%");
    }

    // [tmateev] keeping this in for now, although it is unused, should use it when enabling it Maven 2.x support
    private Minimizer buildUpDependenciesLegacy(Minimizer minimizer) throws IOException {

        // set up list of classes to force include (e.g. runtime dependencies)
        if (forceInclude != null) {
            List<ClassName> classNames = new ArrayList<ClassName>();
            for (String classToForceInclude : forceInclude) {
                classNames.add(new ClassName(classToForceInclude));
            }

            minimizer.forceInclude(classNames.toArray(new ClassName[classNames.size()]));
        }

        // set up Maven project dependencies
        // Set<Artifact> artifacts = project.getDependencyArtifacts(); // FIXME transitive dependencies NOT included
        DependencyManagement dependancyManagenment = project.getDependencyManagement();

        Set<File> artifactLocations = new HashSet<File>();
        for (Dependency dependency : dependancyManagenment.getDependencies()) {
            // we want to exclude test or provided artifacts - the first because
            // they will never be required in the final assembly of the
            // minimized application, the second because they are already
            // provided and we might introduce conflicts
            if (Artifact.SCOPE_TEST.equals(dependency.getScope())
                    || Artifact.SCOPE_PROVIDED.equals(dependency.getScope())) {
                getLog().debug("Excluding test or provided artifact : " + dependency.getArtifactId());
            } else {
                InputLocation location = dependancyManagenment.getLocation(dependency);
                artifactLocations.add(new File(location.getSource().getLocation()));
            }
        }

        // if inspecting the Maven project resulted in an empty list then we
        // fallback to the Maven project local repository (could be very costly
        // as this directory typically contains lots of files)
        if (artifactLocations.isEmpty()) {
            getLog().warn("Could not get list of dependencies from Maven, reverting to Maven local repository");
            return minimizer.libs(pathToLocalRepo);
        }

        return minimizer.libs(artifactLocations);
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

        final Set<File> artifactLocations = askMavenForDependencies();

        // if inspecting the Maven project resulted in an empty list then we
        // fallback to the Maven project local repository (could be very costly
        // as this directory typically contains lots of files)
        if (artifactLocations.isEmpty()) {
            getLog().warn("Could not get list of dependencies from Maven, reverting to Maven local repository");
            return minimizer.libs(pathToLocalRepo);
        }

        return minimizer.libs(artifactLocations);
    }

    // Unfortunately this code only works with Maven 3.x, meaning that any users of our plugin that have older Maven
    // version would have a hard time. There seems to be quite a huge gap between the way Maven 2.x and MAven 3.x
    // handles dependency resolution.

    // see http://blog.sonatype.com/2011/01/how-to-use-aether-in-maven-plugins/#.VV5jarylilM
    // see http://labs.bsb.com/2012/10/using-aether-to-resolve-dependencies-in-a-maven-plugins/
    private Set<File> askMavenForDependencies() {
        ArrayList<String> scopes = new ArrayList<String>();
        scopes.add(Artifact.SCOPE_COMPILE_PLUS_RUNTIME);
        Collection<Artifact> artifacts = new ArrayList<Artifact>();
        try {
            // FIXME [tmateev] try to decrypt the magic behind the project dependencies resolver that
            // deprecates this one and use the one that is recommended
            artifacts = projectDependenciesResolver.resolve(project, scopes, session);
        } catch (ArtifactNotFoundException e) {
            getLog().error("Minimize not successful!", e);
        } catch (ArtifactResolutionException e) {
            getLog().error("Minimize not successful!", e);
        }

        Set<File> artifactLocations = new HashSet<File>();
        for (Artifact dependency : artifacts) {
            artifactLocations.add(dependency.getFile());
        }
        return artifactLocations;
    }
}
