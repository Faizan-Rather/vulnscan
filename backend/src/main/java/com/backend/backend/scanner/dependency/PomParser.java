package com.backend.backend.scanner.dependency;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reads <dependency> entries from a pom.xml, resolving ${property} versions where possible. */
public final class PomParser {

    private PomParser() {}

    public static List<Dependency> parse(Path pom, String relativePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);  // blocks XXE
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        Document doc = factory.newDocumentBuilder().parse(pom.toFile());

        Map<String, String> properties = new HashMap<>();
        NodeList propsNodes = doc.getElementsByTagName("properties");
        if (propsNodes.getLength() > 0) {
            for (Node n = propsNodes.item(0).getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n instanceof Element e) properties.put(e.getTagName(), e.getTextContent().trim());
            }
        }

        List<Dependency> result = new ArrayList<>();
        NodeList deps = doc.getElementsByTagName("dependency");
        for (int i = 0; i < deps.getLength(); i++) {
            Element d = (Element) deps.item(i);
            String groupId = childText(d, "groupId");
            String artifactId = childText(d, "artifactId");
            String version = childText(d, "version");
            if (groupId == null || artifactId == null || version == null) continue;

            if (version.startsWith("${")) {
                version = properties.get(version.substring(2, version.length() - 1));
            }
            if (version == null || version.contains("${")) continue;   // unresolved, skip

            result.add(new Dependency("Maven", groupId + ":" + artifactId, version, relativePath));
        }
        return result;
    }

    private static String childText(Element parent, String tag) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && e.getTagName().equals(tag)) return e.getTextContent().trim();
        }
        return null;
    }
}