package co.edu.udistrital.velenito.mobile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/** One-request loopback HTTP fixture; never connects to the real store database. */
final class TestServer implements AutoCloseable {
    private final ServerSocket socket;
    private final Thread thread;
    final ConcurrentHashMap<String, String> headers = new ConcurrentHashMap<>();
    volatile String request;
    TestServer(int status, String body) throws Exception {
        socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        thread = new Thread(() -> {
            try (var client = socket.accept()) {
                client.setSoTimeout(5000);
                var reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                request = reader.readLine();
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    int colon = line.indexOf(':');
                    if (colon > 0) headers.put(line.substring(0, colon).toLowerCase(Locale.ROOT), line.substring(colon + 1).trim());
                }
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                String head = "HTTP/1.1 " + status + " Test\r\nContent-Type: application/json\r\n"
                        + "Location: http://127.0.0.1:1/private\r\nContent-Length: " + bytes.length + "\r\nConnection: close\r\n\r\n";
                client.getOutputStream().write(head.getBytes(StandardCharsets.UTF_8));
                client.getOutputStream().write(bytes); client.getOutputStream().flush();
            } catch (Exception ignored) { /* shutdown closes an unconsumed fixture */ }
        });
        thread.setDaemon(true); thread.start();
    }
    String origin() { return "http://127.0.0.1:" + socket.getLocalPort(); }
    @Override public void close() throws Exception { socket.close(); thread.join(1000); }
}
