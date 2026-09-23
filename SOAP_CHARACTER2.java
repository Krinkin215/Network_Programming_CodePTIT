import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SOAP_CHARACTER2 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "y235j4iN";
        String serverIp = "36.50.135.242";
        String endpoint = "http://" + serverIp + ":2221/CharacterService";
        String ns = "http://soap.server.network.judge.dblab.ptit/";

        try {
            String soapRequest = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:requestStringArray>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                "      </ser:requestStringArray>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String xmlResponse = sendSoapRequest(endpoint, soapRequest);
            List<String> list = parseStrings(xmlResponse);

            List<String> errors = new ArrayList<>();
            List<String> warns = new ArrayList<>();
            List<String> infos = new ArrayList<>();
            List<String> others = new ArrayList<>();

            for (String s : list) {
                // Che email, số điện thoại và token
                s = s.replaceAll("email=[^\\s]+", "email=[EMAIL]");
                s = s.replaceAll("phone=[^\\s]+", "phone=[PHONE]");
                s = s.replaceAll("token=[^\\s]+", "token=[TOKEN]");

                // Phân loại theo mức độ log
                if (s.contains("ERROR")) {
                    errors.add(s);
                } else if (s.contains("WARN")) {
                    warns.add(s);
                } else if (s.contains("INFO")) {
                    infos.add(s);
                } else {
                    others.add(s);
                }
            }

            // Gộp lại theo thứ tự ưu tiên
            List<String> resultList = new ArrayList<>();
            resultList.addAll(errors);
            resultList.addAll(warns);
            resultList.addAll(infos);
            resultList.addAll(others);

            StringBuilder dataTags = new StringBuilder();
            for (String item : resultList) {
                // Escape các ký tự đặc biệt trong XML nếu có
                item = item.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                dataTags.append("<data>").append(item).append("</data>");
            }

            String soapSubmit = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:submitStringArray>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                dataTags.toString() +
                "      </ser:submitStringArray>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String submitRes = sendSoapRequest(endpoint, soapSubmit);

            // Thử nghiệm dự phòng nếu server yêu cầu thẻ tham số mảng khác tên <data>
            if (submitRes.contains("Fault") || submitRes.contains("error")) {
                soapSubmit = 
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                    "   <soapenv:Header/>" +
                    "   <soapenv:Body>" +
                    "      <ser:submitStringArray>" +
                    "         <arg0>" + studentCode + "</arg0>" +
                    "         <arg1>" + qCode + "</arg1>" +
                    dataTags.toString().replace("<data>", "<arg2>").replace("</data>", "</arg2>") +
                    "      </ser:submitStringArray>" +
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

    private static List<String> parseStrings(String xml) {
        List<String> list = new ArrayList<>();
        int index = 0;
        while ((index = xml.indexOf("<return>", index)) != -1) {
            int end = xml.indexOf("</return>", index);
            if (end == -1) break;
            String val = xml.substring(index + 8, end).trim();
            list.add(val);
            index = end + 9;
        }
        return list;
    }
}