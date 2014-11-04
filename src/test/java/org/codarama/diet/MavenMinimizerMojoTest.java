package org.codarama.diet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codarama.diet.api.DefaultMinimizer;
import org.codarama.diet.api.Minimizer;
import org.codarama.diet.api.reporting.MinimizationReport;
import org.codarama.diet.model.ClassName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * <p>
 * Unit tests for the {@link MavenMinimizerMojo}
 * </p>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Minimizer.class)
public class MavenMinimizerMojoTest {

	private static final String SOURCE_DIRECTORY = "/source";
	private static final String LIBS_DIRECTORY = "/libs";
	private static final String CLASS_1 = "com.verification.runtime.Library.class";
	private static final String TARGET_DIRECTORY = "/target";

	@Before
	public void setMeUpSweety() {
		mockStatic(Minimizer.class);
	}

	@Test
	public void testExecuteOneArgument() throws MojoExecutionException, MojoFailureException, IOException {

		MavenMinimizerMojo mojo = new MavenMinimizerMojo();

		// somebody set us up the mock
		Minimizer mockMinimizer = mock(Minimizer.class);

		Whitebox.setInternalState(mojo, "sources", SOURCE_DIRECTORY);
		Whitebox.setInternalState(mojo, "libs", LIBS_DIRECTORY);
		Whitebox.setInternalState(mojo, "log", mock(Log.class));

		ArgumentCaptor<String> sources = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> libs = ArgumentCaptor.forClass(String.class);

		when(DefaultMinimizer.sources(sources.capture())).thenReturn(mockMinimizer);
		when(mockMinimizer.libs(libs.capture())).thenReturn(mockMinimizer);
		when(mockMinimizer.minimize()).thenReturn(mock(MinimizationReport.class));

		// execute for great justice
		mojo.execute();

		// all your verification is belong to us
		verifyStatic(times(1)); // static call is made only once

		assertEquals(SOURCE_DIRECTORY, sources.getValue());
		assertEquals(LIBS_DIRECTORY, libs.getValue());
	}

	@Test(expected = MojoExecutionException.class)
	public void testExecuteError() throws MojoExecutionException, MojoFailureException, IOException {

		MavenMinimizerMojo mojo = new MavenMinimizerMojo();

		// somebody set us up the mock
		Minimizer mockMinimizer = mock(Minimizer.class);

		Whitebox.setInternalState(mojo, "sources", SOURCE_DIRECTORY);
		Whitebox.setInternalState(mojo, "libs", LIBS_DIRECTORY);
		Whitebox.setInternalState(mojo, "log", mock(Log.class));

		ArgumentCaptor<String> sources = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> libs = ArgumentCaptor.forClass(String.class);

		when(DefaultMinimizer.sources(sources.capture())).thenReturn(mockMinimizer);
		when(mockMinimizer.libs(libs.capture())).thenReturn(mockMinimizer);
		when(mockMinimizer.minimize()).thenThrow(new IOException());

		// execute for great justice
		mojo.execute();

		// all your verification is belong to us
		verifyStatic(times(1)); // static call is made only once

		assertEquals(SOURCE_DIRECTORY, sources.getValue());
		assertEquals(LIBS_DIRECTORY, libs.getValue());
	}

	@Test
	public void testExecuteAllArguments() throws MojoExecutionException, MojoFailureException, IOException {

		MavenMinimizerMojo mojo = new MavenMinimizerMojo();

		// somebody set us up the mock
		Minimizer mockMinimizer = mock(Minimizer.class);

		Whitebox.setInternalState(mojo, "sources", SOURCE_DIRECTORY);
		Whitebox.setInternalState(mojo, "libs", LIBS_DIRECTORY);
		Whitebox.setInternalState(mojo, "forceInclude", new String[] { CLASS_1 });
		Whitebox.setInternalState(mojo, "target", TARGET_DIRECTORY);
		Whitebox.setInternalState(mojo, "log", mock(Log.class));

		ArgumentCaptor<String> sources = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> libs = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> target = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<ClassName> forceInclude = ArgumentCaptor.forClass(ClassName.class);

		when(DefaultMinimizer.sources(sources.capture())).thenReturn(mockMinimizer);
		when(mockMinimizer.libs(libs.capture())).thenReturn(mockMinimizer);
		when(mockMinimizer.output(target.capture())).thenReturn(mockMinimizer);
		when(mockMinimizer.forceInclude(forceInclude.capture())).thenReturn(mockMinimizer);
		when(mockMinimizer.minimize()).thenReturn(mock(MinimizationReport.class));

		// execute for great justice
		mojo.execute();

		// all your verification is belong to us
		verifyStatic(times(1)); // static call is made only once

		assertEquals(SOURCE_DIRECTORY, sources.getValue());
		assertEquals(LIBS_DIRECTORY, libs.getValue());
		assertEquals(TARGET_DIRECTORY, target.getValue());
		assertEquals(CLASS_1, forceInclude.getValue().toString());

	}
}
