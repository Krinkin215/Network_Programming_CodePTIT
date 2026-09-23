import java.net.*;

public class UDP_STRING1 {
    
    public static void main(String[] args) {
        String server = "36.50.135.242";
        int port = 2208;

        String code = "B23DCCN466";
        String qCode = "3FcYvKtn";

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(5000);

            InetAddress address = InetAddress.getByName(server);

            String request = ";" + code + ";" + qCode;
            System.out.println("Sending request: " + request);

            byte[] sendData = request.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(
                sendData,
                sendData.length,
                address,
                port
            );

            socket.send(sendPacket);

            byte[] buffer = new byte[65535];

            DatagramPacket receivePacket = new DatagramPacket(
                buffer,
                buffer.length
            );

            socket.receive(receivePacket);

            String response = new String(
                receivePacket.getData(),
                0,
                receivePacket.getLength()
            );

            System.out.println("Received: " + response);

            String[] parts = response.split(";", 2);

            String requestId = parts[0];
            String data = parts[1];

            char maxChar = data.charAt(0);
            int maxCount = 0;

            for (int i = 0; i < data.length(); i++) {
                char c = data.charAt(i);
                int count = 0;

                for (int j = 0; j < data.length(); j++) {
                    if (data.charAt(j) == c) {
                        count++;
                    }
                }

                if (count > maxCount) {
                    maxCount = count;
                    maxChar = c;
                }
            }

            StringBuilder positions = new StringBuilder();

            for (int i = 0; i < data.length(); i++) {
                if (data.charAt(i) == maxChar) {
                    positions.append(i + 1).append(",");
                }
            }

            String result = requestId + ";" + maxChar + ":" + positions;

            System.out.println("Sending result: " + result);

            sendData = result.getBytes();

            sendPacket = new DatagramPacket(
                sendData,
                sendData.length,
                receivePacket.getAddress(),
                receivePacket.getPort()
            );

            socket.send(sendPacket);

            System.out.println("Sent and Done");

            socket.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
