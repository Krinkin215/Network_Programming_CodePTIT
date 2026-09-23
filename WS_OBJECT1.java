import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class WS_OBJECT1 {

    static class EmployeeY {
        String name;
        String startDateStr;
        Date startDate;

        public EmployeeY(String name, String startDateStr, Date startDate) {
            this.name = name;
            this.startDateStr = startDateStr;
            this.startDate = startDate;
        }
    }

    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "oVQIyy7g";
        String serverIp = "36.50.135.242";
        String endpoint = "http://" + serverIp + ":2221/ObjectService";
        String ns = "http://soap.server.network.judge.dblab.ptit/";

        try {
            String soapRequest = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:requestListEmployeeY>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                "      </ser:requestListEmployeeY>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String xmlResponse = sendSoapRequest(endpoint, soapRequest);
            List<EmployeeY> list = parseEmployees(xmlResponse);

            Collections.sort(list, new Comparator<EmployeeY>() {
                @Override
                public int compare(EmployeeY o1, EmployeeY o2) {
                    if (o1.startDate == null && o2.startDate == null) return 0;
                    if (o1.startDate == null) return 1;
                    if (o2.startDate == null) return -1;
                    return o1.startDate.compareTo(o2.startDate);
                }
            });

            StringBuilder dataTags = new StringBuilder();
            for (EmployeeY emp : list) {
                dataTags.append("<data>")
                        .append("<name>").append(emp.name).append("</name>")
                        .append("<startDate>").append(emp.startDateStr).append("</startDate>")
                        .append("</data>");
            }

            String soapSubmit = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:submitListEmployeeY>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                dataTags.toString() +
                "      </ser:submitListEmployeeY>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            sendSoapRequest(endpoint, soapSubmit);

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

    private static List<EmployeeY> parseEmployees(String xml) {
        List<EmployeeY> list = new ArrayList<>();
        int index = 0;

        while ((index = xml.indexOf("<return>", index)) != -1) {
            int end = xml.indexOf("</return>", index);
            if (end == -1) break;

            String block = xml.substring(index + 8, end);
            String name = getTagValue(block, "name");
            String startDateStr = getTagValue(block, "startDate");
            Date date = parseDate(startDateStr);

            list.add(new EmployeeY(name, startDateStr, date));
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

    private static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;

        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        };

        for (String format : formats) {
            try {
                return new SimpleDateFormat(format).parse(dateStr);
            } catch (Exception ignored) {}
        }

        try {
            return javax.xml.datatype.DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(dateStr)
                    .toGregorianCalendar()
                    .getTime();
        } catch (Exception ignored) {}

        return null;
    }
}