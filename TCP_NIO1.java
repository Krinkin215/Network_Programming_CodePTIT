import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class TCP_NIO1 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "wqhjKYA2";
        String serverIp = "36.50.135.242";
        int serverPort = 2211;

        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(serverIp, serverPort));
            channel.configureBlocking(true);

            String requestMsg = studentCode + ";" + qCode;
            sendFrame(channel, requestMsg);

            StringBuilder httpReqBuilder = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                String payload = receiveFrame(channel);
                httpReqBuilder.append(payload);
            }
            
            String httpRequest = httpReqBuilder.toString();

            String method = "";
            String path = "";
            String host = "";

            String[] lines = httpRequest.split("\r\n");
            if (lines.length > 0) {
                String firstLine = lines[0];
                String[] parts = firstLine.split(" ");
                if (parts.length >= 2) {
                    method = parts[0];
                    path = parts[1];
                }

                for (String line : lines) {
                    if (line.toLowerCase().startsWith("host:")) {
                        host = line.substring(5).trim();
                        break;
                    }
                }
            }

            String submitMsg = method + ";" + path + ";" + host;
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
}