import RMI.ByteService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedHashMap;
import java.util.Map;

public class RMI_BYTE1 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242"; // Thay bằng IP server tương ứng
        int serverPort = 1099;               // Cổng mặc định của RMI Registry là 1099
        String serviceName = "RMIByteService";
        String studentCode = "B23DCCN466";    // Thay bằng mã SV của bạn
        String qCode = "bNew3Cwn";             // Thay bằng mã câu hỏi

        try {
            // 1. Kết nối tới RMI Registry và lookup service
            Registry registry = LocateRegistry.getRegistry(serverHost, serverPort);
            ByteService byteService = (ByteService) registry.lookup(serviceName);

            // a. Nhận dữ liệu mảng byte từ server
            byte[] rawData = byteService.requestData(studentCode, qCode);

            // b. Tìm phần tử có số lần xuất hiện ít nhất (duy trì thứ tự xuất hiện đầu tiên)
            byte[] result = findLeastFrequent(rawData);

            // c. Gửi mảng kết quả lên server
            byteService.submitData(studentCode, qCode, result);

            // d. Kết thúc chương trình
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] findLeastFrequent(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        // Sử dụng LinkedHashMap để giữ thứ tự xuất hiện ban đầu của các phần tử
        Map<Byte, Integer> freqMap = new LinkedHashMap<>();
        for (byte b : data) {
            freqMap.put(b, freqMap.getOrDefault(b, 0) + 1);
        }

        byte minElem = data[0];
        int minCount = Integer.MAX_VALUE;

        for (Map.Entry<Byte, Integer> entry : freqMap.entrySet()) {
            if (entry.getValue() < minCount) {
                minCount = entry.getValue();
                minElem = entry.getKey();
            }
        }

        // Mảng kết quả gồm: [phần tử, số lần xuất hiện]
        return new byte[]{minElem, (byte) minCount};
    }
}