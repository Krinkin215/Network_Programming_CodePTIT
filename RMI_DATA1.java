import RMI.DataService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class RMI_DATA1 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "ZkNdcytM";
        String serverIp = "36.50.135.242";
        int serverPort = 1099;
        String serviceName = "RMIDataService";

        try {
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            DataService service = (DataService) registry.lookup(serviceName);

            Object rawData = service.requestData(studentCode, qCode);
            List<Integer> list = new ArrayList<>();

            if (rawData instanceof List) {
                list.addAll((List<Integer>) rawData);
            } else if (rawData instanceof int[]) {
                for (int x : (int[]) rawData) {
                    list.add(x);
                }
            } else if (rawData instanceof Integer[]) {
                for (Integer x : (Integer[]) rawData) {
                    list.add(x);
                }
            } else if (rawData instanceof String) {
                String str = ((String) rawData).replaceAll("[\\[\\] ]", "");
                for (String s : str.split(",")) {
                    if (!s.isEmpty()) {
                        list.add(Integer.parseInt(s));
                    }
                }
            }

            List<Integer> result = new ArrayList<>();
            int n = list.size();

            if (n == 1) {
                result.add(1);
            } else if (n > 1) {
                for (int i = 0; i < n; i++) {
                    if (i == 0) {
                        if (list.get(i) > list.get(i + 1)) {
                            result.add(i + 1);
                        }
                    } else if (i == n - 1) {
                        if (list.get(i) > list.get(i - 1)) {
                            result.add(i + 1);
                        }
                    } else {
                        if (list.get(i) > list.get(i - 1) && list.get(i) > list.get(i + 1)) {
                            result.add(i + 1);
                        }
                    }
                }
            }

            service.submitData(studentCode, qCode, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}