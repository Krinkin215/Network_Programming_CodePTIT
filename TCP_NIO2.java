import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TCP_NIO2 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "6eGzQr60";
        String serverIp = "36.50.135.242";
        int serverPort = 2211;

        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(serverIp, serverPort));
            channel.configureBlocking(true);

            String requestMsg = studentCode + ";" + qCode;
            sendFrame(channel, requestMsg);

            StringBuilder jsonBuilder = new StringBuilder();
            for (int i = 0; i < 2; i++) {
                String payload = receiveFrame(channel);
                jsonBuilder.append(payload);
            }
            
            String jsonStr = jsonBuilder.toString();

            String event = extractString(jsonStr, "event");
            String user = extractString(jsonStr, "user");
            boolean ok = extractBoolean(jsonStr, "ok");

            String submitMsg = "event=" + event + ";user=" + user + ";ok=" + (ok ? "1" : "0");
            sendFrame(channel, submitMsg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendFrame(SocketChannel channel, String msg) throws IOException {
        byte[] payload = msg.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + payload.length);
        buffer.putInt(payload.length);
        buffer.put(payload);
        buffer.flip();

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private static String receiveFrame(SocketChannel channel) throws IOException {
        ByteBuffer lenBuffer = ByteBuffer.allocate(4);
        readFully(channel, lenBuffer);
        lenBuffer.flip();
        int length = lenBuffer.getInt();

        ByteBuffer payloadBuffer = ByteBuffer.allocate(length);
        readFully(channel, payloadBuffer);
        payloadBuffer.flip();
        
        byte[] payloadBytes = new byte[length];
        payloadBuffer.get(payloadBytes);

        return new String(payloadBytes, StandardCharsets.UTF_8);
    }

    private static void readFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read == -1) {
                throw new IOException("Connection closed by server before reading all expected bytes.");
            }
        }
    }

    private static String extractString(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private static boolean extractBoolean(String json, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return Boolean.parseBoolean(m.group(1));
        }
        return false;
    }
}