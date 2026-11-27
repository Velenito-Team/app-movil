package co.edu.udistrital.velenito.mobile.data;

import co.edu.udistrital.velenito.mobile.domain.Receipt;
import co.edu.udistrital.velenito.mobile.domain.ReceiptLink;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

/** GET only; no authentication, cookies, logs, persistence, or redirects. */
public final class ReceiptClient {
    public Receipt load(ReceiptLink link) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) link.endpoint().toURL().openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cache-Control", "no-store");
            if (connection.getResponseCode() != 200) throw new IOException(ReceiptLink.UNAVAILABLE);
            String type = connection.getContentType();
            if (type == null || !type.toLowerCase(java.util.Locale.ROOT).startsWith("application/json"))
                throw new IOException(ReceiptLink.UNAVAILABLE);
            try (InputStream stream = connection.getInputStream(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192]; int read;
                while ((read = stream.read(buffer)) != -1) {
                    if (bytes.size() + read > 1048576) throw new IOException(ReceiptLink.UNAVAILABLE);
                    bytes.write(buffer, 0, read);
                }
                return ReceiptJson.parse(bytes.toString(StandardCharsets.UTF_8.name()));
            }
        } catch (Exception ex) { throw new IOException(ReceiptLink.UNAVAILABLE); }
        finally { connection.disconnect(); }
    }
}
