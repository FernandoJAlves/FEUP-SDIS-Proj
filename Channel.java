import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Channel implements Runnable {

    private InetAddress address;
    private int port;

    public Channel(String address, int port) {
        try {
            this.address = InetAddress.getByName(address);
            this.port = port;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] msg) {
        try {
            DatagramSocket serviceSocket = new DatagramSocket(this.port, this.address);
            DatagramPacket packet = new DatagramPacket(msg, msg.length);
            serviceSocket.send(packet);
            serviceSocket.close();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        MulticastSocket multicastSocket;
        try {
            multicastSocket = new MulticastSocket();
            multicastSocket.setReuseAddress(true);
            multicastSocket.setLoopbackMode(false);
            multicastSocket.setTimeToLive(1);
            // multicastSocket.joinGroup(address);

            while (true) {
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                // receive request
                multicastSocket.receive(packet);
                System.out.println("Received packet!");

                // send answer
                // serviceSocket.send();
                // System.out.println("Packet sent!");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}