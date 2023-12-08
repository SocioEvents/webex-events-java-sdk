package com.webex.events;

import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class PomReader {

    static String packageVersion = null;

    static String sdkVersion() {
        try {
            if (packageVersion == null) {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                org.apache.maven.model.Model model;
                if (new File("pom.xml").exists()) {
                    model = reader.read(new FileReader("pom.xml"));
                }else {
                    model = reader.read(new
                            InputStreamReader(Objects.requireNonNull(PomReader.class.getResourceAsStream(
                            "/META-INF/maven/com.webex.events/webex-events/pom.xml"))));
                }
                packageVersion = model.getVersion();

            }
        } catch (IOException | XmlPullParserException e) {
            packageVersion = "";
        }
        return packageVersion;
    }
}
