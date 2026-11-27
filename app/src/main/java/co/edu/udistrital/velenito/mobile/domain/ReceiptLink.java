package co.edu.udistrital.velenito.mobile.domain;

import java.net.URI;

/** Resolves only receipt links, never executes or navigates the QR contents. */
public final class ReceiptLink {
    public static final String UNAVAILABLE = "Comprobante no disponible.";
    public final String token;
    public final URI origin;

    private ReceiptLink(String token, URI origin) { this.token = token; this.origin = origin; }

    public static URI server(String value) {
        try {
            URI uri = new URI(value.trim());
            if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                    || uri.getHost() == null || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || !(uri.getPath().isEmpty() || uri.getPath().equals("/"))
                    || uri.getPort() < -1 || uri.getPort() == 0 || uri.getPort() > 65535) throw invalid();
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), null, null, null);
        } catch (Exception ex) { throw invalid(); }
    }

    public static ReceiptLink parse(String value, String serverOverride) {
        try {
            if (value == null || value.length() > 4096) throw invalid();
            String input = value.trim();
            URI source = null;
            String token;
            if (input.matches("[a-f0-9]{64}")) { token = input; }
            else {
                URI uri = new URI(input);
                if ("velenito".equals(uri.getScheme()) && "comprobante".equals(uri.getHost())
                        && uri.getRawQuery() == null && uri.getRawFragment() == null) {
                    token = uri.getRawPath().substring(1);
                } else {
                    source = server(new URI(uri.getScheme(), uri.getRawAuthority(), null, null, null).toString());
                    if (uri.getRawQuery() != null) throw invalid();
                    if (uri.getRawFragment() != null && (uri.getPath().isEmpty() || uri.getPath().equals("/"))) {
                        if (!uri.getRawFragment().startsWith("comprobante/")) throw invalid();
                        token = uri.getRawFragment().substring("comprobante/".length());
                    } else if (uri.getRawFragment() == null && uri.getRawPath().startsWith("/api/invoices/receipt/")) {
                        token = uri.getRawPath().substring("/api/invoices/receipt/".length());
                    } else { throw invalid(); }
                }
            }
            if (!token.matches("[a-f0-9]{64}")) throw invalid();
            URI origin = serverOverride == null || serverOverride.trim().isEmpty() ? source : server(serverOverride);
            if (origin == null) throw invalid();
            return new ReceiptLink(token, origin);
        } catch (Exception ex) { throw invalid(); }
    }

    public URI endpoint() { return origin.resolve("/api/invoices/receipt/" + token); }
    private static IllegalArgumentException invalid() { return new IllegalArgumentException(UNAVAILABLE); }
}
