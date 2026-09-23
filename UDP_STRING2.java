import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP_STRING2 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2208;
        String studentCode = "B23DCCN466";
        String qCode = "b1KfjXpm";

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            InetAddress serverAddress = InetAddress.getByName(serverHost);

            String requestMsg = ";" + studentCode + ";" + qCode;
            byte[] sendData = requestMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[2048];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            String receivedMsg = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();

            String[] parts = receivedMsg.split(";", 2);
            String requestId = parts[0];
            String data = parts[1];

            String normalizedData = normalizeString(data);

            String responseMsg = requestId + ";" + normalizedData;
            byte[] responseData = responseMsg.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(
                    responseData,
                    responseData.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort()
            );
            socket.send(responsePacket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String normalizeString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String[] words = input.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            sb.append(Character.toUpperCase(word.charAt(0)))
              .append(word.substring(1));

            if (i < words.length - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }
}