import RMI.DataService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class RMI_DATA2 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "sd81gZkV"; 
        String serverIp = "36.50.135.242";
        int serverPort = 1099;
        String serviceName = "RMIDataService";

        try {
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            DataService service = (DataService) registry.lookup(serviceName);

            // Nhận dữ liệu dưới dạng chuỗi
            Object rawData = service.requestData(studentCode, qCode);
            String csvData = (String) rawData;

            // Phân tích chuỗi CSV
            List<Double> list = new ArrayList<>();
            String[] parts = csvData.split(",");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    list.add(Double.parseDouble(part.trim()));
                }
            }

            int n = list.size();
            if (n > 0) {
                // 1. Tính giá trị trung bình (average)
                double sum = 0;
                for (double v : list) {
                    sum += v;
                }
                double avg = sum / n;

                // 2. Tính độ lệch chuẩn tổng thể (population standard deviation)
                double sumSq = 0;
                for (double v : list) {
                    sumSq += Math.pow(v - avg, 2);
                }
                double stddev = Math.sqrt(sumSq / n);

                // 3. Tính phân vị 95 (p95)
                Collections.sort(list);
                int p95Index = (int) Math.ceil(n * 0.95) - 1;
                double p95 = list.get(p95Index);

                // Định dạng kết quả (làm tròn 2 chữ số thập phân)
                String result = String.format(Locale.US, "average=%.2f;stddev=%.2f;p95=%.2f", avg, stddev, p95);

                // Gửi kết quả lên server
                service.submitData(studentCode, qCode, result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}