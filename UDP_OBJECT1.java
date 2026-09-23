import UDP.Student;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP_OBJECT1 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242"; 
        int serverPort = 2209;
        String studentCode = "B23DCCN466";    
        String qCode = "lSMxlWZC";         

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            InetAddress serverAddress = InetAddress.getByName(serverHost);

            // a. Gửi ;studentCode;qCode
            String requestMsg = ";" + studentCode + ";" + qCode;
            byte[] sendData = requestMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket);

            // b. Nhận thông điệp
            byte[] buffer = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket);

            // 08 byte đầu là requestId
            byte[] requestIdBytes = new byte[8];
            System.arraycopy(receivePacket.getData(), 0, requestIdBytes, 0, 8);

            // Các byte còn lại là đối tượng Student
            int objectDataLength = receivePacket.getLength() - 8;
            ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData(), 8, objectDataLength);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Student student = (Student) ois.readObject();

            // c. Xử lý chuẩn hóa
            String rawName = student.getName();
            student.setName(formatName(rawName));
            student.setEmail(generateEmail(rawName));

            // Serialize đối tượng Student đã xử lý
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(student);
            oos.flush();
            byte[] studentBytes = baos.toByteArray();

            // Ghép 08 byte requestId + các byte đối tượng Student
            byte[] responseData = new byte[8 + studentBytes.length];
            System.arraycopy(requestIdBytes, 0, responseData, 0, 8);
            System.arraycopy(studentBytes, 0, responseData, 8, studentBytes.length);

            // Gửi dữ liệu đã cập nhật lên server
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
            return "";
        }

        String[] words = rawName.trim().split("\\s+");
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

    private static String generateEmail(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return "";
        }

        String[] words = rawName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();

        // Tên (từ cuối cùng)
        sb.append(words[words.length - 1].toLowerCase());

        // Các chữ cái đầu của họ và tên đệm
        for (int i = 0; i < words.length - 1; i++) {
            sb.append(Character.toLowerCase(words[i].charAt(0)));
        }

        sb.append("@ptit.edu.vn");
        return sb.toString();
    }
}