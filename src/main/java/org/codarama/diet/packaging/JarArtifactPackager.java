package org.codarama.diet.packaging;

import com.google.common.collect.Sets;
import org.apache.maven.artifact.Artifact;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JarArtifactPackager extends AbstractArtifactPackager implements ArtifactPackager {

    public File packageArtifact(final Artifact currentArtifact, JarFile minimizedJar) throws IOException {
        final File currentArtifactFile = currentArtifact.getFile();

        final Set<String> currentArtifactEntryNames = getCurrentArtifactNames(currentArtifactFile);
        emptyCurrentArtifact(currentArtifactFile, currentArtifactEntryNames);

        final File minimizedJarFile = new File(minimizedJar.getName());
        final ZipEntrySource[] minimizedArtifactEntries =
                getMinimizedEntries(minimizedJarFile, currentArtifactFile.getName());

        final File result = new File(appendUnderscoreMinimizedToArtifactName(currentArtifactFile.getPath()));
        ZipUtil.addEntries(currentArtifactFile, minimizedArtifactEntries, result);

        return result;
    }

    private ZipEntrySource[] getMinimizedEntries(File minimizedJarFile, String currentArtifactFileName) {
        final Set<ZipEntrySource> resultEntriesSet = Sets.newHashSet();
        ZipUtil.iterate(minimizedJarFile, new ZipEntryCallback() {

            public void process(InputStream in, ZipEntry minimizedEntry) throws IOException {
                final String minimizedEntryName = minimizedEntry.getName();
                resultEntriesSet.add(new ByteSource(minimizedEntryName, readBytes(in)));
            }
        });
        return resultEntriesSet.toArray(new ZipEntrySource[resultEntriesSet.size()]);
    }

    private Set<String> getCurrentArtifactNames(File currentArtifactFile) {
        final Set<String> result = Sets.newHashSet();
        ZipUtil.iterate(currentArtifactFile, new ZipEntryCallback() {

            public void process(InputStream in, ZipEntry currentEntry) throws IOException {
                result.add(currentEntry.getName());
            }
        });

        return result;
    }

    private void emptyCurrentArtifact(File currentArtifactFile, Set<String> currentArtifactEntryNames) throws IOException {
        ZipUtil.removeEntries(currentArtifactFile, toArray(currentArtifactEntryNames));
    }

    private String[] toArray(Set<String> set) {
        return set.toArray(new String[set.size()]);
    }
}
