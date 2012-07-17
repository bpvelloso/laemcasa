/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa.rede;

import com.jme3.app.SimpleApplication;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.system.JmeContext;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author velloso
 */

public class ServidorLaEmCasa extends SimpleApplication implements MessageListener<HostedConnection>{
    public final static int PORTA_PADRAO=5555;

    private Server myServer;
    private static int portaAtual=0;
    
    public static void main(String[] args) {
        try{
            portaAtual = Integer.parseInt(args[0]);
        }catch(Exception e){
            portaAtual = ServidorLaEmCasa.PORTA_PADRAO;
        }
        ServidorLaEmCasa app = new ServidorLaEmCasa();
        app.start(JmeContext.Type.Headless); // headless type for servers!
    }

    @Override
    public void simpleInitApp() {

        Serializer.registerClass(Movimento.class);

        try {
            myServer = Network.createServer(portaAtual);
            myServer.start();
            myServer.addMessageListener(this, Movimento.class);
            Logger.getLogger(ServidorLaEmCasa.class.getName()).log(Level.INFO,"Servidor Iniciado: {0}",myServer);
        } catch (IOException ex) {
            Logger.getLogger(ServidorLaEmCasa.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void messageReceived(HostedConnection source, Message m) {
        if (m instanceof Movimento) {
            Logger.getLogger(ServidorLaEmCasa.class.getName()).log(Level.INFO, "Recebido Movimento: {0}", ((Movimento)m).toString());
            
            myServer.broadcast(m);
        }
    }
  
    @Override
    public void destroy() {
      myServer.close();
      super.destroy();
  }

}