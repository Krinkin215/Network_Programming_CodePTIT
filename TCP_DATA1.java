import java.io.*;
import java.net.Socket;

public class TCP_DATA1 {
    public static void main(String[] args) {
        String server = "36.50.135.242";
        int port = 2207;

        String code = "B23DCCN466";
        String qCode = "4dTdVPbz";

        try (Socket socket = new Socket(server, port);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        ) {

            socket.setSoTimeout(5000);

            String request = code + ";" + qCode;
            dos.writeUTF(request);
            dos.flush();

            int a = dis.readInt();
            int b = dis.readInt();

            System.out.println("a = " + a);
            System.out.println("b = " + b);

            int sum = a + b;
            int product = a * b;

            dos.writeInt(sum);
            dos.flush();

            dos.writeInt(product);
            dos.flush();

            System.out.println("Tong = " + sum);
            System.out.println("Tich = " + product);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}