package org.codarama.diet.packaging;

import org.codarama.diet.util.Tokenizer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractArtifactPackager implements ArtifactPackager {

    protected String appendUnderscoreMinimizedToArtifactName(String name) {
        return name.substring(0, name.lastIndexOf(".")) + "_minimized." + Tokenizer.delimiter(".").tokenize(name).lastToken();
    }

    protected byte[] readBytes(InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        int readLen;
        byte[] buf = new byte[16384]; // 4k

        while ((readLen = in.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, readLen);
        }
        out.flush();

        try {
            return out.toByteArray();
        } finally {
            out.close();
        }
    }
}
