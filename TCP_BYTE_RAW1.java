import java.io.*;
import java.util.*;
import java.net.Socket;

public class TCP_BYTE_RAW1 {
    public static void main(String[] args) {
        
        String server = "36.50.135.242";
        int port = 2206;

        String code = "B23DCCN466";
        String qCode = "iA7vDjCo";

        try (Socket socket = new Socket(server, port)) {
            socket.setSoTimeout(5000);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            out.write((code + ";" + qCode).getBytes());
            out.flush();

            byte[] buffer = new byte[1024];
            int n = in.read(buffer);
            if(n <= 0) return;
            
            String response = new String(buffer, 0, n);
            
            String[] token = response.split(",");
            int[] num = new int[token.length];

            for(int i = 0; i < token.length; i++) {
                num[i] = Integer.parseInt(token[i].trim());
            }
            Arrays.sort(num);

            int min = Integer.MAX_VALUE;
            int x = num[0], y = num[1];

            for(int i = 0; i < num.length - 1; i++) {
                int res = num[i + 1] - num[i];
                if (res <= min) {
                    min = res;
                    x = num[i];
                    y = num[i + 1];
                }
            }

            String result = min + "," + x + "," + y;
            out.write(result.getBytes());
            out.flush();

            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}