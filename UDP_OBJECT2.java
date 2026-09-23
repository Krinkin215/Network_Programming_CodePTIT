import UDP.Customer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP_OBJECT2 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2209;
        String studentCode = "B23DCCN466";
        String qCode = "h1hVyHAr";

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            InetAddress serverAddress = InetAddress.getByName(serverHost);

            String requestMsg = ";" + studentCode + ";" + qCode;
            byte[] sendData = requestMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket);

            byte[] buffer = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket);

            byte[] requestIdBytes = new byte[8];
            System.arraycopy(receivePacket.getData(), 0, requestIdBytes, 0, 8);

            int objectDataLength = receivePacket.getLength() - 8;
            ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData(), 8, objectDataLength);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Customer customer = (Customer) ois.readObject();

            String rawName = customer.getName();
            customer.setName(formatName(rawName));
            customer.setUserName(generateUserName(rawName));
            customer.setDayOfBirth(formatDob(customer.getDayOfBirth()));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(customer);
            oos.flush();
            byte[] customerBytes = baos.toByteArray();

            byte[] responseData = new byte[8 + customerBytes.length];
            System.arraycopy(requestIdBytes, 0, responseData, 0, 8);
            System.arraycopy(customerBytes, 0, responseData, 8, customerBytes.length);

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

    private static String formatName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return rawName;
        }

        String[] parts = rawName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].toUpperCase();
        }

        String lastName = parts[parts.length - 1].toUpperCase();

        StringBuilder middleAndFirst = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            String word = parts[i].toLowerCase();
            String capitalized = Character.toUpperCase(word.charAt(0)) + word.substring(1);
            middleAndFirst.append(capitalized);
            if (i < parts.length - 2) {
                middleAndFirst.append(" ");
            }
        }

        return lastName + ", " + middleAndFirst.toString();
    }

    private static String formatDob(String dob) {
        if (dob == null || dob.trim().isEmpty()) {
            return dob;
        }

        String[] parts = dob.trim().split("-");
        if (parts.length == 3) {
            String mm = parts[0];
            String dd = parts[1];
            String yyyy = parts[2];
            return dd + "/" + mm + "/" + yyyy;
        }
        return dob;
    }

    private static String generateUserName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return "";
        }

        String[] parts = rawName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            sb.append(Character.toLowerCase(parts[i].charAt(0)));
        }
        sb.append(parts[parts.length - 1].toLowerCase());

        return sb.toString();
    }
}