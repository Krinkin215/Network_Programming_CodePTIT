import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TCP_GZIP2 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "n9qffD8Y";
        String serverIp = "36.50.135.242";
        int serverPort = 2210;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverIp, serverPort), 5000);
            socket.setSoTimeout(5000);

            OutputStream out = socket.getOutputStream();

            ByteArrayOutputStream reqBaos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(reqBaos)) {
                String requestMsg = studentCode + ";" + qCode + "\n";
                gzipOut.write(requestMsg.getBytes(StandardCharsets.UTF_8));
                gzipOut.finish();
            }
            out.write(reqBaos.toByteArray());
            out.flush();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                GZIPInputStream gzipIn = new GZIPInputStream(socket.getInputStream());
                int b;
                while ((b = gzipIn.read()) != -1) {
                    if (b == '\n') break;
                    buffer.write(b);
                }
            } catch (Exception e) {
            }

            String line = new String(buffer.toByteArray(), StandardCharsets.UTF_8).replace("\r", "").trim();

            if (!line.isEmpty()) {
                char[] chars = line.toCharArray();
                Arrays.sort(chars);
                String sortedStr = new String(chars);
                String submitMsg = sortedStr + "\n";

                ByteArrayOutputStream submitBaos = new ByteArrayOutputStream();
                try (GZIPOutputStream submitGzip = new GZIPOutputStream(submitBaos)) {
                    submitGzip.write(submitMsg.getBytes(StandardCharsets.UTF_8));
                    submitGzip.finish();
                }
                out.write(submitBaos.toByteArray());
                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}