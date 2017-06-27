package org.codarama.diet.packaging;

import com.google.common.collect.Sets;
import org.apache.maven.artifact.Artifact;
import org.zeroturnaround.zip.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * An {@link ArtifactPackager} for .war artifacts.
 */
public class WarArtifactPackager extends AbstractArtifactPackager implements ArtifactPackager {

    public File packageArtifact(Artifact currentArtifact, JarFile minimizedJar) throws IOException {

        final Set<ZipEntrySource> resultEntriesSet = Sets.newHashSet();
        ZipUtil.iterate(currentArtifact.getFile(), new ZipEntryCallback() {

            public void process(InputStream in, ZipEntry warEntry) throws IOException {

                final String warEntryName = warEntry.getName();
                final boolean entryIsNotInLibDirs =
                        !warEntry.isDirectory() && (warEntryName.startsWith("lib") || warEntryName.startsWith("WEB-INF/lib"));

                if (entryIsNotInLibDirs) {
                    resultEntriesSet.add(new ByteSource(warEntryName, readBytes(in)));
                }
            }
        });
        resultEntriesSet.add(new FileSource("lib/minimized.jar", new File(minimizedJar.getName())));

        final ZipFile warFile = new ZipFile(currentArtifact.getFile());
        final File result = new File(appendUnderscoreMinimizedToArtifactName(warFile.getName()));

        final ZipEntrySource[] newEntries = resultEntriesSet.toArray(new ZipEntrySource[resultEntriesSet.size()]);
        ZipUtil.addEntries(
                currentArtifact.getFile(),
                newEntries,
                result
        );

        return result;
    }
}
