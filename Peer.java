import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Peer implements RemoteInterface {

    String protocolVersion;
    int peerId;

    private Channel mdc, mdb, mdr;
    private static HashMap<String, String> database = new HashMap<>();

    public Peer(String[] args) {
        parseArguments(args);
    }

    public static void main(String args[]) {
        if (args.length < 1 || args.length > 5) {
            System.out.println("Usage: java Peer <protocol_version> <peer_id> <mdc_addr:mdc_port> <mdb_addr:mdb_port> <mdr_addr:mdr_port>");
            return;
        }

        try {
            Peer sv = new Peer(args);
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(sv,0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(args[0], stub);
            System.out.println("Peer ready to receive requests!");
        } catch(Exception e) {
            System.out.println("ERROR in main\n");
            e.printStackTrace();
        }
    }

    void parseArguments(String args[]) {
        this.protocolVersion = args[0];
        this.peerId = Integer.parseInt(args[1]);
        
        String[] addrPort;
        String addr;
        int port;

        addr = "localhost";
        addrPort = args[2].split(":");
        if (addrPort.length > 1) {
            addr = addrPort[0];   
        }
        port = Integer.parseInt(addrPort[1]);        
        this.mdc = new Channel(addr,port);

        addr = "localhost";
        addrPort = args[3].split(":");
        if (addrPort.length > 1) {
            addr = addrPort[0];   
        }
        port = Integer.parseInt(addrPort[1]);        
        this.mdb = new Channel(addr,port);

        addr = "localhost";
        addrPort = args[4].split(":");
        if (addrPort.length > 1) {
            addr = addrPort[0];   
        }
        port = Integer.parseInt(addrPort[1]);        
        this.mdr = new Channel(addr,port);
    }

    //@Override
    public void backup(String filepath, int replicationDeg) {

    }

    //@Override
    public void restore(String filepath) {

    }

    //@Override
    public void delete(String pathname) {

    }

    //@Override
    public void reclaim(int maxDiskSpace) {

    }

    //@Override
    public void state() {

    }
    

}