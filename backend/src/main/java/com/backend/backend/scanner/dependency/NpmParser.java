package com.backend.backend.scanner.dependency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads "dependencies" and "devDependencies" from package.json, normalizing version ranges. */
public final class NpmParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern VERSION = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?");

    private NpmParser() {}

    public static List<Dependency> parse(Path packageJson, String relativePath) throws Exception {
        JsonNode root = MAPPER.readTree(packageJson.toFile());
        List<Dependency> result = new ArrayList<>();

        for (String section : List.of("dependencies", "devDependencies")) {
            JsonNode node = root.path(section);
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String version = normalize(entry.getValue().asText());
                if (version != null) {
                    result.add(new Dependency("npm", entry.getKey(), version, relativePath));
                }
            }
        }
        return result;
    }

    /** "^4.17.1" or "~4.17.1" becomes "4.17.1". Ranges with no real version ("latest", a git URL) return null. */
    static String normalize(String range) {
        Matcher m = VERSION.matcher(range);
        if (!m.find()) return null;
        String major = m.group(1);
        String minor = m.group(2) == null ? "0" : m.group(2);
        String patch = m.group(3) == null ? "0" : m.group(3);
        return major + "." + minor + "." + patch;
    }
}