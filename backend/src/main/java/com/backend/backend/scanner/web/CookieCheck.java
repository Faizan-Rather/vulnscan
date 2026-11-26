package com.backend.backend.scanner.web;

import com.backend.backend.finding.Finding;
import com.backend.backend.finding.Severity;
import com.backend.backend.scanner.ScanContext;
import com.backend.backend.scanner.ScanType;
import com.backend.backend.scanner.SecurityCheck;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Flags cookies set without the Secure or HttpOnly flags. */
@Component
public class CookieCheck implements SecurityCheck {

    private static final String OWASP_CATEGORY = "A05:2021 Security Misconfiguration";

    @Override
    public String id() {
        return "WEB-COOKIES-001";
    }

    @Override
    public ScanType type() {
        return ScanType.WEB;
    }

    @Override
    public List<Finding> run(ScanContext ctx) throws Exception {
        List<CrawledPage> pages = new Crawler().crawl(ctx.targetUrl());
        List<Finding> findings = new ArrayList<>();

        for (CrawledPage page : pages) {
            for (String setCookie : page.headers().allValues("set-cookie")) {
                String cookieName = setCookie.split("=", 2)[0].trim();
                String lower = setCookie.toLowerCase();

                if (!lower.contains("secure")) {
                    findings.add(Finding.builder(id(), Severity.MEDIUM,
                                    "Cookie \"" + cookieName + "\" missing Secure flag")
                            .description("Without the Secure flag, this cookie can be sent over an unencrypted HTTP connection, exposing it to interception.")
                            .file(page.url(), null)
                            .evidence(setCookie)
                            .remediation("Add the Secure flag so the cookie is only sent over HTTPS.")
                            .owasp(OWASP_CATEGORY)
                            .build());
                }

                if (!lower.contains("httponly")) {
                    findings.add(Finding.builder(id(), Severity.MEDIUM,
                                    "Cookie \"" + cookieName + "\" missing HttpOnly flag")
                            .description("Without the HttpOnly flag, this cookie can be read by JavaScript, making it a target for theft via cross-site scripting.")
                            .file(page.url(), null)
                            .evidence(setCookie)
                            .remediation("Add the HttpOnly flag so the cookie cannot be accessed from JavaScript.")
                            .owasp(OWASP_CATEGORY)
                            .build());
                }
            }
        }
        return findings;
    }
}