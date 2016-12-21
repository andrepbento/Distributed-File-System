
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class HeartbeatThreadReceive extends Thread {
    private DatagramSocket socket;
    private DatagramPacket packet;
    
    // VER O QUE FAZER COM ISTO POSTERIORMENTE
    //protected List<ServerInfo> serverHistory;
    
    protected List<ClientInfo> activeClients;
    
    public HeartbeatThreadReceive() throws SocketException{
        socket = new DatagramSocket(Constants.HB_LISTENING_PORT);
        packet = null;
    }
    
    private void setServerLoggedOn(ServerInfo si) {
        for(ServerInfo s : DirectoryService.serversList)
            if(s.equals(si))
                s.setLogged(true);
    }
    
    @Override
    public void run() {
        if(socket == null)
                return;
        
        new CheckIfServerIsOn().start();
        new CheckIfClientIsOn().start();
        
        while(true){
            try {
                packet = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);
                socket.receive(packet);
                
                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
                ObjectInputStream oin = new ObjectInputStream(bais);
                MSG receivedMSG = (MSG)oin.readObject();
                
                if(receivedMSG.getCMDarg(0).equals(Constants.HEARTBEAT_SERVER)) {
                    System.out.println(Constants.HEARTBEAT_SERVER+" received!");
                    setServerLoggedOn(new ServerInfo(packet.getAddress(), packet.getPort()));
                } else if(receivedMSG.getCMDarg(0).equals(Constants.HEARTBEAT_CLIENT)) {
                    System.out.println(Constants.HEARTBEAT_CLIENT+" received!");
                    System.out.println("IMPLEMENTAR setClientLoggedOn(/*...*/)");
                    //setClientLoggedOn(new ClientInfo(/*...*/));
                } else
                    System.out.println("Hearth-Beat received NOT RECOGNIZED!");
                
                // VERIFICAR SE EXISTE NA LISTA O IP DE ONDE FOI RECEBIDO O HB POSTERIORMENTE (HISTÓRICO)
                
            } catch (ClassNotFoundException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    class CheckIfServerIsOn extends Thread {
        @Override
        public void run() {
            try {
                while(true) {
                    try {
                        Thread.sleep(Constants.TIME + 1000);

                        Iterator<ServerInfo> it = DirectoryService.serversList.iterator();
                        while(it.hasNext()) {
                            ServerInfo si = it.next();
                            if(!si.isLogged()) {
                                System.out.println("Server "+si.getName()+" removed!");
                                it.remove();
                            } else
                                si.setLogged(false);
                        }

                        // TENTAR VERIFICAR SE CONSEGUIMOS REESTABELECER LIGAÇÃO

                        System.out.print("\nConnected servers:\n");
                        int i = 1;
                        for(ServerInfo s : DirectoryService.serversList){
                            System.out.println(i+" - "+s.getName());
                            i++;
                        }
                        System.out.println();
                    } catch(ConcurrentModificationException e) {
                        e.printStackTrace();
                    }
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class CheckIfClientIsOn extends Thread {
        @Override
        public void run() {
            try {
                while(true) {
                    Thread.sleep(Constants.TIME + 1000);
                /*
                for(ClientInfo c : activeClients)
                    if(!c.isLogged())
                        activeClients.remove(c);
                */
                    //else
                        //c.setLog(false);
                
                // TENTAR VERIFICAR SE CONSEGUIMOS REESTABELECER LIGAÇÃO
                
                /*
                System.out.print("Clientes ligados:  ");
                for(ClientInfo c : activeClients)
                        System.out.print(c.getUsername()+ "   ");
                System.out.println("");
                */
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

