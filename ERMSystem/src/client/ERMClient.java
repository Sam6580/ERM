package client;

import model.Reflection;
import java.io.*;
import java.net.*;
import java.util.List;

public class ERMClient {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public ERMClient() {
        try {
            socket = new Socket("localhost", 5000);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("✅ Connected to ERM Server!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendReflection(Reflection reflection) {
        try {
            oos.writeObject(reflection);
            return (String) ois.readObject();
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Reflection> fetchReflections() {
        try {
            oos.writeObject("FETCH");
            return (List<Reflection>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
