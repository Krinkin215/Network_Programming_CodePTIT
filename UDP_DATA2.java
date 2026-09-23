import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP_DATA2 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2207;
        String studentCode = "B23DCCN466";
        String qCode = "Nohq3XTJ";

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
            String[] numbersStr = parts[1].split(",");

            int max = Integer.MIN_VALUE;
            int min = Integer.MAX_VALUE;

            for (String num : numbersStr) {
                int value = Integer.parseInt(num.trim());
                if (value > max) {
                    max = value;
                }
                if (value < min) {
                    min = value;
                }
            }

            String responseMsg = requestId + ";" + max + "," + min;
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
}