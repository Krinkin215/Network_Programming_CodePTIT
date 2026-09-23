import java.io.*;
import java.net.Socket;


public class TCP_BYTE_RAW2 {
    
    public static void main(String[] args) {
        String server = "36.50.135.242";
        int port = 2206;

        String code = "B23DCCN466";
        String qCode = "EpO6dZ3I";

        try (Socket socket = new Socket(server, port)) {
            socket.setSoTimeout(5000);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            out.write((code + ";" + qCode).getBytes());
            out.flush();

            byte[] buffer = new byte[1024];
            int n = in.read(buffer);
            if(n <= 0) return;

            String response = new String(buffer, 0, n).trim();

            String[] token = response.split(",");
            int[] num = new int[token.length];
            for(int i = 0; i < token.length; i++) {
                num[i] = Integer.parseInt(token[i].trim());
            }

            int max = Integer.MIN_VALUE;
            int secondMax = Integer.MIN_VALUE;

            for(int res : num) {
                if(res > max) {
                    max = res;
                }
            }

            for(int res : num) {
                if(res > secondMax && res < max) {
                    secondMax = res;
                }
            }

            int idx = -1;
            for(int i = 0; i < num.length; i++) {
                if(num[i] == secondMax) {
                    idx = i;
                    break;
                }
            }

            String result = secondMax + "," + idx;
            out.write(result.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

