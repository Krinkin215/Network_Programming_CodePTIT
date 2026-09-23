import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WS_DATA1 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "EGuQPhn3"; 
        String serverIp = "36.50.135.242";
        String endpoint = "http://" + serverIp + ":2221/DataService";

        String ns = "http://soap.server.network.judge.dblab.ptit/";

        try {
            // 1. Gửi request getData với đúng tên tham số studentCode và qCode
            String soapGetData = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:getData>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                "      </ser:getData>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String xmlResponse = sendSoapRequest(endpoint, soapGetData);
            System.out.println("Response XML: " + xmlResponse);

            List<Integer> list = parseNumbers(xmlResponse);
            System.out.println("Received list: " + list);

            // 2. Tìm số lớn nhất có thể ghép được
            String result = findLargestNumber(list);
            System.out.println("Result: " + result);

            // 3. Gửi submitDataString với tham số studentCode, qCode, data
            String soapSubmit = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:submitDataString>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                "         <data>" + result + "</data>" +
                "      </ser:submitDataString>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String submitResponse = sendSoapRequest(endpoint, soapSubmit);
            System.out.println("Submit response: " + submitResponse);

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

    private static List<Integer> parseNumbers(String xml) {
        List<Integer> list = new ArrayList<>();
        int index = 0;
        // Bắt mọi giá trị nằm trong thẻ <return>...</return>
        while ((index = xml.indexOf("<return>", index)) != -1) {
            int end = xml.indexOf("</return>", index);
            if (end == -1) break;
            String val = xml.substring(index + 8, end).trim();
            list.add(Integer.parseInt(val));
            index = end + 9;
        }
        return list;
    }

    private static String findLargestNumber(List<Integer> list) {
        if (list == null || list.isEmpty()) return "";

        List<String> strList = new ArrayList<>();
        for (Integer num : list) {
            strList.add(String.valueOf(num));
        }

        // So sánh theo quy tắc ghép chuỗi: b + a so với a + b
        Collections.sort(strList, (a, b) -> (b + a).compareTo(a + b));

        if (strList.get(0).equals("0")) return "0";

        StringBuilder sb = new StringBuilder();
        for (String s : strList) {
            sb.append(s);
        }
        return sb.toString();
    }
}