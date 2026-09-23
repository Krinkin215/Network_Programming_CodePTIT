import RMI.CharacterService;

import java.net.URI;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMI_CHARACTER1 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242"; 
        int serverPort = 1099;
        String serviceName = "RMICharacterService";
        String studentCode = "B23DCCN466";    
        String qCode = "f2DGIs82";             

        try {
            Registry registry = LocateRegistry.getRegistry(serverHost, serverPort);
            CharacterService characterService = (CharacterService) registry.lookup(serviceName);

            // a. Nhận chuỗi từ server
            String rawStr = characterService.requestCharacter(studentCode, qCode);

            // b. Mã hóa URL theo quy tắc phần đường dẫn của URI
            String encodedStr = encodeUrlPath(rawStr);

            // c. Gửi chuỗi đã mã hóa lên server
            characterService.submitCharacter(studentCode, qCode, encodedStr);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encodeUrlPath(String s) {
        if (s == null) {
            return "";
        }
        try {
            // Sử dụng constructor URI(scheme, userInfo, path, fragment)
            // Phương thức toASCIIString() sẽ tự động mã hóa phần path đúng chuẩn RFC 2396
            return new URI(null, null, s, null).toASCIIString();
        } catch (Exception e) {
            e.printStackTrace();
            return s;
        }
    }
}