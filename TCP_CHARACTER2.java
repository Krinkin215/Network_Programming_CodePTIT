import java.io.*;
import java.net.Socket;
import java.util.*;


public class TCP_CHARACTER2 {
    
    public static void main(String[] args) {
        String server = "36.50.135.242";
        int port = 2208;

        String code = "B23DCCN466";
        String qCode = "SG2LI5W9";

        try (
            Socket socket = new Socket(server, port);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())
            );
        ) {

            socket.setSoTimeout(5000);

            writer.write(code + ";" + qCode);
            writer.newLine();
            writer.flush();

            String response = reader.readLine();
            if(response == null) return;

            Map<Character, Integer> map = new LinkedHashMap<>();

            for(int i = 0; i < response.length(); i++) {
                char c = response.charAt(i);
                if(Character.isLetterOrDigit(c)) {
                    map.put(c, map.getOrDefault(c, 0) + 1);
                }
            }

            StringBuilder result = new StringBuilder();
            for(Map.Entry<Character, Integer> entry : map.entrySet()) {
                if(entry.getValue() > 1) {
                    result.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
                }
            }

            writer.write(result.toString());
            writer.newLine();
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
