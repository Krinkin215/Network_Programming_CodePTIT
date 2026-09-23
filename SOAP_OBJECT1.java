import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SOAP_OBJECT1 {

    static class ProductY {
        String name = "";
        double price;
        double taxRate;
        double discount;
        double finalPrice;
    }

    public static void main(String[] args) {
        String studentCode = "B23DCCN466";
        String qCode = "uwaOJ3XQ";
        String serverIp = "36.50.135.242";
        String endpoint = "http://" + serverIp + ":2221/ObjectService";
        String ns = "http://soap.server.network.judge.dblab.ptit/";

        try {
            String soapRequest = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:requestProductY>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                "      </ser:requestProductY>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String xmlResponse = sendSoapRequest(endpoint, soapRequest);
            ProductY product = parseProductY(xmlResponse);

            double rawFinal = product.price * (1.0 + product.taxRate / 100.0) * (1.0 - product.discount / 100.0);
            product.finalPrice = Math.round(rawFinal * 100.0) / 100.0;

            String fields = 
                "<discount>" + product.discount + "</discount>" +
                "<finalPrice>" + product.finalPrice + "</finalPrice>" +
                "<name>" + product.name + "</name>" +
                "<price>" + product.price + "</price>" +
                "<taxRate>" + product.taxRate + "</taxRate>";

            String soapSubmit = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                "   <soapenv:Header/>" +
                "   <soapenv:Body>" +
                "      <ser:submitProductY>" +
                "         <studentCode>" + studentCode + "</studentCode>" +
                "         <qCode>" + qCode + "</qCode>" +
                "         <productY>" + fields + "</productY>" +
                "      </ser:submitProductY>" +
                "   </soapenv:Body>" +
                "</soapenv:Envelope>";

            String submitRes = sendSoapRequest(endpoint, soapSubmit);

            if (submitRes.contains("Fault") || submitRes.contains("null") || submitRes.contains("WA")) {
                String soapSubmitArg = 
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"" + ns + "\">" +
                    "   <soapenv:Header/>" +
                    "   <soapenv:Body>" +
                    "      <ser:submitProductY>" +
                    "         <arg0>" + studentCode + "</arg0>" +
                    "         <arg1>" + qCode + "</arg1>" +
                    "         <arg2>" + fields + "</arg2>" +
                    "      </ser:submitProductY>" +
                    "   </soapenv:Body>" +
                    "</soapenv:Envelope>";
                sendSoapRequest(endpoint, soapSubmitArg);
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

    private static ProductY parseProductY(String xml) {
        ProductY p = new ProductY();
        int start = xml.indexOf("<return>");
        int end = xml.indexOf("</return>");
        String block = (start != -1 && end != -1) ? xml.substring(start + 8, end) : xml;

        p.name = getTagValue(block, "name");
        
        String priceStr = getTagValue(block, "price");
        p.price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);

        String taxStr = getTagValue(block, "taxRate");
        p.taxRate = taxStr.isEmpty() ? 0.0 : Double.parseDouble(taxStr);

        String discountStr = getTagValue(block, "discount");
        p.discount = discountStr.isEmpty() ? 0.0 : Double.parseDouble(discountStr);

        return p;
    }

    private static String getTagValue(String xml, String tag) {
        Pattern p = Pattern.compile("<(?:[a-zA-Z0-9]+:)?" + tag + ">(.*?)</(?:[a-zA-Z0-9]+:)?" + tag + ">");
        Matcher m = p.matcher(xml);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }
}