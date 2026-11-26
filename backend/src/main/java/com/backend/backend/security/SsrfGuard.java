package com.backend.backend.security;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
/** Blocks scanning targets that resolve to private, loopback, or link-local addresses. */
public final class SsrfGuard {
    private SsrfGuard() {}
    /** Throws if the URL's host resolves to an address the scanner must never touch. */
    public static void check(String url) throws SsrfBlockedException {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new SsrfBlockedException("Not a valid URL: " + url);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new SsrfBlockedException("URL has no host: " + url);
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new SsrfBlockedException("Only http and https URLs are allowed");
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(host);   // resolves DNS, so a sneaky hostname is caught too
        } catch (UnknownHostException e) {
            throw new SsrfBlockedException("Could not resolve host: " + host);
        }

        if (address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isAnyLocalAddress()
                || isCloudMetadataAddress(address)) {
            throw new SsrfBlockedException("Blocked: " + host + " resolves to a private or internal address");
        }
    }

    /** 169.254.169.254 is the cloud metadata endpoint on AWS, GCP and Azure. */
    private static boolean isCloudMetadataAddress(InetAddress address) {
        return "169.254.169.254".equals(address.getHostAddress());
    }

    public static class SsrfBlockedException extends Exception {
        public SsrfBlockedException(String message) {
            super(message);
        }
    }
}