import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SOAP_OBJECT2 {

    static class Customer {
        String customerId;
        int purchaseCount;
        double totalSpent;

        public Customer(String customerId, int purchaseCount, double totalSpent) {
            this.customerId = customerId;
            this.purchaseCount = purchaseCount;
            this.totalSpent = totalSpent;
        }
    }

    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "RJDxBQfV";
        String serverIp = "36.50.135.242";
        String endpoint = "http://" + serverIp + ":2221/ObjectService";
        String ns = "http://soap.server.network.judge.dblab.ptit/";

        try {
            // 1. Tạo request lấy danh sách Customer
            String soapRequest = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:requestListCustomer>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                "      </ser:requestListCustomer>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String xmlResponse = sendSoapRequest(endpoint, soapRequest);
            List<Customer> list = parseCustomers(xmlResponse);

            // 2. Lọc và sắp xếp
            List<Customer> filteredList = new ArrayList<>();
            for (Customer c : list) {
                if (c.purchaseCount >= 6 && c.totalSpent >= 4000) {
                    filteredList.add(c);
                }
            }

            Collections.sort(filteredList, new Comparator<Customer>() {
                @Override
                public int compare(Customer c1, Customer c2) {
                    int cmp = Double.compare(c2.totalSpent, c1.totalSpent); // Giảm dần theo totalSpent
                    if (cmp == 0) {
                        return c1.customerId.compareTo(c2.customerId);      // Tăng dần theo customerId
                    }
                    return cmp;
                }
            });

            // 3. Đóng gói kết quả gửi lên server
            StringBuilder dataTags = new StringBuilder();
            for (Customer c : filteredList) {
                dataTags.append("<data>")
                        .append("<customerId>").append(c.customerId).append("</customerId>")
                        .append("<purchaseCount>").append(c.purchaseCount).append("</purchaseCount>")
                        .append("<totalSpent>").append(c.totalSpent).append("</totalSpent>")
                        .append("</data>");
            }

            String soapSubmit = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:submitListCustomer>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                dataTags.toString() +
                "      </ser:submitListCustomer>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String submitRes = sendSoapRequest(endpoint, soapSubmit);

            // Xử lý fallback nếu server nhận tham số mảng dưới thẻ <arg2> thay vì <data>
            if (submitRes.contains("Fault") || submitRes.contains("error")) {
                soapSubmit = 
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                    "   <soapenv:Header/>" +
                    "   <soapenv:Body>" +
                    "      <ser:submitListCustomer>" +
                    "         <arg0>" + studentCode + "</arg0>" +
                    "         <arg1>" + qCode + "</arg1>" +
                    dataTags.toString().replace("<data>", "<arg2>").replace("</data>", "</arg2>") +
                    "      </ser:submitListCustomer>" +
                    "   </soapenv:Body>" +
                    "</soapenv:Envelope>";
                sendSoapRequest(endpoint, soapSubmit);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String sendSoapRequest(String endpoint, String xmlPayload) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        conn.setRequestProperty("SOAPAction", "\"\"");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(xmlPayload.getBytes("UTF-8"));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300) 
                         ? conn.getInputStream() 
                         : conn.getErrorStream();

        StringBuilder response = new StringBuilder();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
        }

        if (responseCode >= 400) {
            throw new RuntimeException("HTTP Error " + responseCode + ": " + response.toString());
        }

        return response.toString();
    }

    private static List<Customer> parseCustomers(String xml) {
        List<Customer> list = new ArrayList<>();
        int index = 0;

        while ((index = xml.indexOf("<return>", index)) != -1) {
            int end = xml.indexOf("</return>", index);
            if (end == -1) break;

            String block = xml.substring(index + 8, end);
            String customerId = getTagValue(block, "customerId");
            
            String countStr = getTagValue(block, "purchaseCount");
            int purchaseCount = countStr.isEmpty() ? 0 : Integer.parseInt(countStr);
            
            String spentStr = getTagValue(block, "totalSpent");
            double totalSpent = spentStr.isEmpty() ? 0.0 : Double.parseDouble(spentStr);

            list.add(new Customer(customerId, purchaseCount, totalSpent));
            index = end + 9;
        }

        return list;
    }

    private static String getTagValue(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open);
        if (start == -1) return "";
        int end = xml.indexOf(close, start);
        if (end == -1) return "";
        return xml.substring(start + open.length(), end).trim();
    }
}