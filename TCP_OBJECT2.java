import TCP.Customer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TCP_OBJECT2 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242"; 
        int serverPort = 2209;
        String studentCode = "B23DCCN466";    
        String qCode = "sEl2FG4Q";            

        try (Socket socket = new Socket(serverHost, serverPort)) {
            socket.setSoTimeout(5000);

            // Khởi tạo Stream (luôn tạo ObjectOutputStream và flush trước)
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // 1. Gửi chuỗi studentCode;qCode
            String request = studentCode + ";" + qCode;
            oos.writeObject(request);
            oos.flush();

            // 2. Nhận đối tượng Customer từ server
            Customer customer = (Customer) ois.readObject();

            // 3. Chuẩn hóa thông tin
            String rawName = customer.getName();
            customer.setName(formatName(rawName));
            customer.setUserName(generateUserName(rawName));
            customer.setDayOfBirth(formatDob(customer.getDayOfBirth()));

            // Gửi lại đối tượng đã sửa lên server
            oos.writeObject(customer);
            oos.flush();

            // 4. Đóng kết nối
            oos.close();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // a. Chuyển đổi tên: nguyen van hai duong -> DUONG, Nguyen Van Hai
    private static String formatName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) return rawName;

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

    // b. Chuyển đổi ngày sinh: mm-dd-yyyy -> dd/mm/yyyy
    private static String formatDob(String dob) {
        if (dob == null || dob.trim().isEmpty()) return dob;

        String[] parts = dob.trim().split("-");
        if (parts.length == 3) {
            String mm = parts[0];
            String dd = parts[1];
            String yyyy = parts[2];
            return dd + "/" + mm + "/" + yyyy;
        }
        return dob;
    }

    // c. Sinh userName: nguyen van hai duong -> nvhduong
    private static String generateUserName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) return "";

        String[] parts = rawName.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            sb.append(Character.toLowerCase(parts[i].charAt(0)));
        }
        sb.append(parts[parts.length - 1].toLowerCase());

        return sb.toString();
    }
}