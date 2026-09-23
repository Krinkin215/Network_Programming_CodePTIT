import TCP.Laptop;
import java.io.*;
import java.net.Socket;


public class TCP_OBJECT1 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2209;
        String studentCode = "B23DCCN466";
        String qCode = "ATY8kjFc";

        try (Socket socket = new Socket(serverHost, serverPort)) {
            socket.setSoTimeout(5000);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            String request = studentCode + ";" + qCode;
            oos.writeObject(request);
            oos.flush();

            Laptop laptop = (Laptop) ois.readObject();

            laptop.setName(fixName(laptop.getName()));
            laptop.setQuantity(reverseQuantity(laptop.getQuantity()));

            oos.writeObject(laptop);
            oos.flush();

            oos.close();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String fixName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }

        String[] words = name.trim().split("\\s+");
        if (words.length > 1) {
            String temp = words[0];
            words[0] = words[words.length - 1];
            words[words.length - 1] = temp;
            return String.join(" ", words);
        }
        return name;
    }

    private static int reverseQuantity(int quantity) {
        String reversedStr = new StringBuilder(String.valueOf(quantity)).reverse().toString();
        return Integer.parseInt(reversedStr);
    }
}