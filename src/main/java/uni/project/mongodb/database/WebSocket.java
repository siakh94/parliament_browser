package uni.project.mongodb.database;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.websocket.Encoder;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Messaging service handler class.
 */
@ServerEndpoint(value = "/chat/{username}")
public class WebSocket {
    List<Thread> waiters;
    Map<String, Socket> users;
    MongoDatabase db;
    ServerSocket server;

    public WebSocket(MongoDatabase db) throws IOException {
        this.db = db;
        users = new HashMap<>();
        server = new ServerSocket(8080);
        waiters = new ArrayList<>();
    }

    /**
     * Message handler.
     * @param sck Socket instance this event came from.
     * @param message Received message.
     * @throws IOException
     */
    public void onMessage(Socket sck, JSONObject message) throws IOException {
        if (message.has("iam")) {
            users.put(message.getString("iam"), sck);
        } else {
            String recipient = message.getString("recipient");
            if (!users.containsKey(recipient)) return;
            Socket recSck = users.get(recipient);
            Document outDoc = new Document();
            outDoc.put("sender", message.getString("sender"));
            outDoc.put("message", message.getString("message"));
            OutputStream os = recSck.getOutputStream();
            byte[] ob = outDoc.toJson().getBytes("UTF-8");
            if (ob.length > 125) return;
            byte[] outarr = new byte[ob.length + 2];
            System.arraycopy(new byte[]{ (byte) 129, (byte) ob.length }, 0, outarr, 0, 2);
            System.arraycopy(ob, 0, outarr, 2, ob.length);
            os.write(outarr);
        }
    }

    /**
     * Asynchronous waiter for WebSocket connections.
     * @return Execution thread.
     */
    private Thread waiter() {
        Thread w = new Thread(() -> {
            try {
                Socket sck = server.accept();
                waiters.add(waiter());
                InputStream in = sck.getInputStream();
                OutputStream out = sck.getOutputStream();
                Scanner s = new Scanner(in, "UTF-8");
                String data = s.useDelimiter("\\r\\n\\r\\n").next();
                Matcher get = Pattern.compile("^GET").matcher(data);
                if (get.find()) {
                    Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                    match.find();
                    byte[] res = ("HTTP/1.1 101 Switching Protocols\r\n"
                            + "Connection: Upgrade\r\n"
                            + "Upgrade: websocket\r\n"
                            + "Sec-WebSocket-Accept: "
                            + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                            + "\r\n\r\n").getBytes("UTF-8");
                    out.write(res);
                }
                while (true) {
                    int op = in.read();
                    int length = in.read() - 128;
                    if (length > 125) {
                        int[] b;
                        if (length == 126) {
                            b = new int[]{in.read(), in.read()};
                        } else {
                            b = new int[]{in.read(), in.read(), in.read(), in.read(), in.read(), in.read(), in.read(), in.read()};
                        }
                        length = Arrays.stream(b).reduce((a, c) -> a * 0xFF + c).getAsInt();
                    }
                    byte[] key = {(byte) in.read(), (byte) in.read(), (byte) in.read(), (byte) in.read()};
                    byte[] outArr = new byte[length];
                    for (int i = 0; i < length; i++) {
                        outArr[i] = (byte) (in.read() ^ key[i & 0x3]);
                    }
                    onMessage(sck, new JSONObject(new JSONTokener(new String(outArr, "UTF-8"))));
                }
            } catch (Exception ignored) {  }
        });
        w.start();
        return w;
    }

    /**
     * Initialize first connection waiter.
     */
    public void init() {
        waiters.add(waiter());
    }
}
