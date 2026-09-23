import java.net.*;
import java.util.*;

public class UDP_DATA1 {
    public static void main(String[] args) {
        String server = "36.50.135.242";
        int port = 2207;

        String code = "B23DCCN466";
        String qCode = "pUDin9R8";

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            InetAddress serverAddress = InetAddress.getByName(server);

            // a. Gửi thông điệp: ";studentCode;qCode"
            String requestMsg = ";" + code + ";" + qCode;
            byte[] sendData = requestMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                sendData, sendData.length, serverAddress, port
            );
            socket.send(sendPacket);

            // b. Nhận thông điệp từ server
            byte[] receiveData = new byte[2048];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            // Chuyển mảng byte nhận được thành String theo đúng số byte thực tế đọc được
            String response = new String(
                receivePacket.getData(), 0, receivePacket.getLength()
            ).trim();

            // Phân tách requestId, n, và danh sách số: "requestId;n;A1,A2,...Am"
            String[] parts = response.split(";");
            String requestId = parts[0];
            int n = Integer.parseInt(parts[1].trim());

            // Lưu các số đã có vào Set
            Set<Integer> presentNumbers = new HashSet<>();
            if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                String[] nums = parts[2].trim().split(",");
                for (String s : nums) {
                    presentNumbers.add(Integer.parseInt(s.trim()));
                }
            }

            // c. Tìm các số còn thiếu từ 1 đến n
            StringBuilder missingList = new StringBuilder();
            for (int i = 1; i <= n; i++) {
                if (!presentNumbers.contains(i)) {
                    if (missingList.length() > 0) {
                        missingList.append(",");
                    }
                    missingList.append(i);
                }
            }

            // Chuỗi kết quả: "requestId;B1,B2,...,Bm"
            String resultMsg = requestId + ";" + missingList.toString();
            byte[] resultBytes = resultMsg.getBytes();

            // Gửi lại đúng địa chỉ và cổng của gói tin vừa nhận được
            DatagramPacket sendResultPacket = new DatagramPacket(
                resultBytes, resultBytes.length, 
                receivePacket.getAddress(), receivePacket.getPort()
            );
            socket.send(sendResultPacket);

            // d. Đóng socket tự động khi kết thúc try-with-resources
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
