import RMI.ByteService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedHashMap;
import java.util.Map;

public class RMI_BYTE2 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242"; // Thay bằng IP server tương ứng
        int serverPort = 1099;
        String serviceName = "RMIByteService";
        String studentCode = "B23DCCN466";    // Thay bằng mã sinh viên
        String qCode = "MTim8tXv";             // Thay bằng mã câu hỏi

        try {
            Registry registry = LocateRegistry.getRegistry(serverHost, serverPort);
            ByteService byteService = (ByteService) registry.lookup(serviceName);

            // a. Nhận dữ liệu mảng byte từ server
            byte[] rawData = byteService.requestData(studentCode, qCode);

            // b. Tìm phần tử xuất hiện nhiều nhất (ưu tiên phần tử đầu tiên nếu trùng tần suất)
            byte[] result = findMostFrequent(rawData);

            // c. Gửi mảng kết quả lên server
            byteService.submitData(studentCode, qCode, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] findMostFrequent(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        Map<Byte, Integer> freqMap = new LinkedHashMap<>();
        for (byte b : data) {
            freqMap.put(b, freqMap.getOrDefault(b, 0) + 1);
        }

        byte maxElem = data[0];
        int maxCount = 0;

        for (Map.Entry<Byte, Integer> entry : freqMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxElem = entry.getKey();
            }
        }

        return new byte[]{maxElem, (byte) maxCount};
    }
}