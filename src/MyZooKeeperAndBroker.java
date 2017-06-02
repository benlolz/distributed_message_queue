import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Kawayipk on 6/1/17.
 */
public class MyZooKeeperAndBroker {
    public static void main(String[] args) {
        Server server = new Server();
        new Thread(server).start();
        System.out.println("MyZooKeeperAndBroker exit");

    }

    private static class Server implements Runnable {

        private static ServerSocket _serverSocket;

        private Server() {
            try {
                _serverSocket = new ServerSocket(0);
                System.out.println(InetAddress.getLocalHost().getHostAddress() + " " + _serverSocket.getLocalPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    new Thread(new ServerWorker(_serverSocket.accept())).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static class ServerWorker implements Runnable {
            private Socket _socket;
            private Queue<Package> _queue;

            private ServerWorker(Socket socket) {
                _socket = socket;
                _queue = new LinkedList<>();
            }

            @Override
            public void run() {
                Package p;
                try(ObjectInputStream in = new ObjectInputStream(_socket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(_socket.getOutputStream())) {

                    if((p = (Package)in.readObject())._type == TYPE.P2BUP) {
                        P2BUp p2bup = (P2BUp)in.readObject();

                        System.out.println("in run() P2BUp");
                        System.out.println(InetAddress.getLocalHost().getHostAddress() + " " + _serverSocket.getLocalPort());

                        p2bup._partitionList = new ArrayList<>();
                        p2bup._partitionList.add(new String[]{InetAddress.getLocalHost().getHostAddress(), _serverSocket.getLocalPort()+"", 1+""});
                    } else {
                        System.out.println("in run() P2BDdata");
                        while((p = (Package)in.readObject())._type == TYPE.P2BDATA) {

                            _queue.offer((Package)in.readObject());
                        }

                    }

                    System.out.println("in run() exit");
                    p._ack = true;
                    out.writeObject(p);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                System.out.println(_queue);

            }
        }
    }
}
