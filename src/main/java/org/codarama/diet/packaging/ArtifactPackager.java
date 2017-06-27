package org.codarama.diet.packaging;

import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * Should be used to modify artifact built by maven, removing unused classes.
 *
 * Created by Ayld on 11/5/16.
 */
public interface ArtifactPackager {

    /**
     * Modifies a given Maven artifact leaving only dependent classes contained in given minimized jar.
     *
     * @param currentArtifact the artifact to packageArtifact
     * @param minimizedJar a jar containing all classes current project depends on
     *
     * @return the new artifact
     * */
    File packageArtifact(Artifact currentArtifact, JarFile minimizedJar) throws IOException;
}
