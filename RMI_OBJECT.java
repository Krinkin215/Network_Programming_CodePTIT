import RMI.ObjectService;
import RMI.TicketSla;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMI_OBJECT {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "MT05Wmhq";
        String serverIp = "36.50.135.242";
        int serverPort = 1099;
        String serviceName = "RMIObjectService";

        try {
            // Kết nối tới RMI Registry
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            ObjectService service = (ObjectService) registry.lookup(serviceName);

            // b. Nhận đối tượng từ server
            TicketSla ticket = (TicketSla) service.requestObject(studentCode, qCode);

            if (ticket != null) {
                String priority = ticket.getPriority();
                int hours = ticket.getOpenedHoursAgo();
                boolean isBreached = false;

                // c. Kiểm tra điều kiện breached
                if ("CRITICAL".equalsIgnoreCase(priority) && hours > 2) {
                    isBreached = true;
                } else if ("HIGH".equalsIgnoreCase(priority) && hours > 8) {
                    isBreached = true;
                } else if ("MEDIUM".equalsIgnoreCase(priority) && hours > 24) {
                    isBreached = true;
                } else if ("LOW".equalsIgnoreCase(priority) && hours > 72) {
                    isBreached = true;
                }

                ticket.setBreached(isBreached);

                // c. Thiết lập action
                if (!isBreached) {
                    ticket.setAction("MONITOR");
                } else {
                    if ("CRITICAL".equalsIgnoreCase(priority) || hours > 96) {
                        ticket.setAction("ESCALATE_L2");
                    } else {
                        ticket.setAction("ESCALATE_L1");
                    }
                }

                // d. Gửi lại đối tượng lên server
                service.submitObject(studentCode, qCode, ticket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}