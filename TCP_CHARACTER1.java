import java.io.*;
import java.net.Socket;

public class TCP_CHARACTER1 {
    public static void main(String[] args) {
        String server = "36.50.135.242";
        int port = 2208;

        String code = "B23DCCN466";
        String qCode = "qVvj3zPp";

        try (
            Socket socket = new Socket(server, port);

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())
            )
        ) {
            socket.setSoTimeout(5000);

            String request = code + ";" + qCode;
            System.out.println("Sending request: " + request);

            writer.write(request);
            writer.newLine();
            writer.flush();

            String domain = reader.readLine();

            System.out.println("Received domain: " + domain);

            String[] list = domain.split(",");

            StringBuilder result = new StringBuilder();

            for(String res : list) {
                
                res = res.trim();
                if(res.endsWith(".edu")) {
                    if(result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(res);
                }
            }

            System.out.println(result);

            writer.write(result.toString());
            writer.newLine();
            writer.flush();

            System.out.println("Sent and Done");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }
}
