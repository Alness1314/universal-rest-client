package com.alness.universalrestclient.internal;

import com.alness.universalrestclient.api.HttpHeaders;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Redacts common credentials before values reach exceptions or logs. */
public final class SensitiveDataSanitizer {
    public static final String REDACTED = "[REDACTED]";
    private static final Set<String> SENSITIVE_HEADERS = new HashSet<String>(Arrays.asList(
            "authorization", "proxy-authorization", "cookie", "set-cookie",
            "x-api-key", "api-key"));
    private static final Set<String> SENSITIVE_QUERY = new HashSet<String>(Arrays.asList(
            "token", "access_token", "refresh_token", "api_key", "apikey",
            "password", "secret", "signature"));

    private SensitiveDataSanitizer() {
    }

    public static HttpHeaders headers(HttpHeaders source) {
        HttpHeaders.Builder result = HttpHeaders.builder();
        for (Map.Entry<String, List<String>> entry : source.asMap().entrySet()) {
            boolean sensitive = SENSITIVE_HEADERS.contains(entry.getKey().toLowerCase(Locale.ROOT));
            for (String value : entry.getValue()) {
                result.add(entry.getKey(), sensitive ? REDACTED : value);
            }
        }
        return result.build();
    }

    public static URI uri(URI source) {
        if (source == null) {
            return null;
        }
        try {
            return new URI(source.getScheme(), null, source.getHost(), source.getPort(),
                    source.getRawPath(), sanitizeQuery(source.getRawQuery()), null);
        } catch (URISyntaxException ignored) {
            return URI.create(source.getScheme() + "://" + source.getHost());
        }
    }

    private static String sanitizeQuery(String query) {
        if (query == null || query.isEmpty()) {
            return query;
        }
        StringBuilder result = new StringBuilder();
        String[] parameters = query.split("&");
        for (String parameter : parameters) {
            if (result.length() > 0) {
                result.append('&');
            }
            int separator = parameter.indexOf('=');
            String name = separator < 0 ? parameter : parameter.substring(0, separator);
            result.append(name);
            if (separator >= 0) {
                result.append('=');
                result.append(SENSITIVE_QUERY.contains(name.toLowerCase(Locale.ROOT))
                        ? "%5BREDACTED%5D" : parameter.substring(separator + 1));
            }
        }
        return result.toString();
    }
}
