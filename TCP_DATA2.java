import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class TCP_DATA2 {
    
    public static void main(String[] args) {
        String server = "36.50.135.242";
        int port = 2207;

        String code = "B23DCCN466";
        String qCode = "hwESF9u7";

        try {
            Socket socket = new Socket(server, port);
            socket.setSoTimeout(5000);

            DataInputStream reader = new DataInputStream(
                socket.getInputStream()
            );

            DataOutputStream writer = new DataOutputStream(
                socket.getOutputStream()
            );

            // a. Gửi mã sinh viên và mã câu hỏi
            String request = code + ";" + qCode;
            System.out.println("Sending request: " + request);

            writer.writeUTF(request);
            writer.flush();

            // b. Nhận chuỗi Caesar và giá trị dịch chuyển s
            String encrypted = reader.readUTF();
            int s = reader.readInt();

            System.out.println("Received: " + encrypted);
            System.out.println("Shift: " + s);

            // c. Giải mã Caesar
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < encrypted.length(); i++) {
                char c = encrypted.charAt(i);

                if (c >= 'A' && c <= 'Z') {
                    c = (char) ((c - 'A' - s + 26) % 26 + 'A');
                } 
                else if (c >= 'a' && c <= 'z') {
                    c = (char) ((c - 'a' - s + 26) % 26 + 'a');
                }

                result.append(c);
            }

            String decrypted = result.toString();

            System.out.println("Decrypted: " + decrypted);

            // Gửi kết quả về server
            writer.writeUTF(decrypted);
            writer.flush();

            System.out.println("Sent and Done");

            // d. Đóng kết nối
            socket.close();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
