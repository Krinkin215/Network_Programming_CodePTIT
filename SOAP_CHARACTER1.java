import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SOAP_CHARACTER1 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "7aQoxRRB";
        String serverIp = "36.50.135.242";
        String endpoint = "http://" + serverIp + ":2221/CharacterService";
        String ns = "http://soap.server.network.judge.dblab.ptit/";

        try {
            String soapRequest = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:requestString>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                "      </ser:requestString>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String xmlResponse = sendSoapRequest(endpoint, soapRequest);
            String inputStr = parseReturn(xmlResponse);

            String reversedStr = new StringBuilder(inputStr).reverse().toString();

            String soapSubmit = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:submitString>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                "         <data>" + reversedStr + "</data>" +
                "      </ser:submitString>" +
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

    private static String parseReturn(String xml) {
        int start = xml.indexOf("<return>");
        if (start == -1) return "";
        int end = xml.indexOf("</return>", start);
        if (end == -1) return "";
        return xml.substring(start + 8, end).trim();
    }
}