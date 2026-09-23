import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WS_CHARACTER2 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "Qqq8GFGV";
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
            List<String> words = parseStrings(xmlResponse);

            // Gom nhóm theo số lượng nguyên âm (TreeMap tự động sort tăng dần theo số nguyên âm)
            Map<Integer, List<String>> map = new TreeMap<>();
            for (String word : words) {
                int vowels = countVowels(word);
                if (!map.containsKey(vowels)) {
                    map.put(vowels, new ArrayList<>());
                }
                map.get(vowels).add(word);
            }

            // Danh sách kết quả phẳng gồm từng từ theo thứ tự nhóm và từ điển
            List<String> resultList = new ArrayList<>();
            for (List<String> group : map.values()) {
                Collections.sort(group);
                resultList.addAll(group);
            }

            // Mỗi từ là 1 thẻ <data>
            StringBuilder dataTags = new StringBuilder();
            for (String item : resultList) {
                dataTags.append("<data>").append(item).append("</data>");
            }

            String soapSubmit = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:submitCharacterStringArray>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                dataTags.toString() +
                "      </ser:submitCharacterStringArray>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            sendSoapRequest(endpoint, soapSubmit);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int countVowels(String s) {
        int count = 0;
        String lower = s.toLowerCase();
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') {
                count++;
            }
        }
        return count;
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