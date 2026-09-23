import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WS_CHARACTER1 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "rRbxWc8c";
        String serverIp = "36.50.135.242";
        String endpoint = "http://" + serverIp + ":2221/CharacterService";
        String ns = "http://soap.server.network.judge.dblab.ptit/";

        try {
            String soapRequest = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:requestCharacter>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                "      </ser:requestCharacter>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String xmlResponse = sendSoapRequest(endpoint, soapRequest);
            List<Integer> list = parseNumbers(xmlResponse);

            if (!list.isEmpty()) {
                int shift = list.get(0);
                int k = shift % list.size();
                Collections.rotate(list, k);
            }

            StringBuilder dataTags = new StringBuilder();
            for (Integer val : list) {
                dataTags.append("<data>").append(val).append("</data>");
            }

            String soapSubmit = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:submitCharacterCharArray>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                dataTags.toString() +
                "      </ser:submitCharacterCharArray>" +
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

    private static List<Integer> parseNumbers(String xml) {
        List<Integer> list = new ArrayList<>();
        int index = 0;
        while ((index = xml.indexOf("<return>", index)) != -1) {
            int end = xml.indexOf("</return>", index);
            if (end == -1) break;
            String val = xml.substring(index + 8, end).trim();
            list.add(Integer.parseInt(val));
            index = end + 9;
        }
        return list;
    }
}