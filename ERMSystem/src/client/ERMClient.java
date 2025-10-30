package client;

import model.Reflection;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ERMClient implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;

    public ERMClient() throws IOException {
        this.socket = new Socket("localhost", 5000);
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
    }

    public String sendReflection(Reflection reflection) throws IOException, ClassNotFoundException {
        oos.writeObject(reflection);
        return (String) ois.readObject();
    }

    @SuppressWarnings("unchecked")
    public List<Reflection> fetchReflections() throws IOException, ClassNotFoundException {
        oos.writeObject("FETCH");
        return (List<Reflection>) ois.readObject();
    }

    @Override
    public void close() throws IOException {
        // The try-with-resources statement handles the closing of the underlying streams.
        socket.close();
    }
}
