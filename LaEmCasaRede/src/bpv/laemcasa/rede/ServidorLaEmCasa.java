/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa.rede;

import bpv.laemcasa.rede.mensagens.MovimentoMsg;
import bpv.laemcasa.rede.mensagens.TiroMsg;
import bpv.laemcasa.rede.mensagens.PlacarMsg;
import com.jme3.app.SimpleApplication;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.system.JmeContext;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.util.HashMap;
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
    private HashMap<String, Integer>placar;
    
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

        Serializer.registerClass(MovimentoMsg.class);
        Serializer.registerClass(TiroMsg.class);
        Serializer.registerClass(PlacarMsg.class);

        placar = new HashMap<String, Integer>();
        
        try {
            myServer = Network.createServer(portaAtual);
            myServer.start();
            myServer.addMessageListener(this, MovimentoMsg.class);
            myServer.addMessageListener(this, TiroMsg.class);
            myServer.addMessageListener(this, PlacarMsg.class);
            
            Logger.getLogger(ServidorLaEmCasa.class.getName()).log(Level.INFO,"Servidor Iniciado: {0}",myServer);
        } catch (IOException ex) {
            Logger.getLogger(ServidorLaEmCasa.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    
    public void messageReceived(HostedConnection source, Message m) {
        if (m instanceof MovimentoMsg) {
            //Logger.getLogger(ServidorLaEmCasa.class.getName()).log(Level.INFO, "Recebido Movimento: {0}", ((Movimento)m).toString());
            atualizaStatus((MovimentoMsg)m);
            myServer.broadcast(m);
        }else
        if (m instanceof TiroMsg) {
            Logger.getLogger(ServidorLaEmCasa.class.getName()).log(Level.INFO, "Recebido Tiro: {0}", ((TiroMsg)m).toString());
            myServer.broadcast(m);
            this.atualizaPlacar((TiroMsg)m);
        }
    }
  
    @Override
    public void destroy() {
      myServer.close();
      super.destroy();
  }

    private void atualizaPlacar(TiroMsg tiro) {
        if(placar.containsKey(tiro.getAlvoId())){
            if(placar.containsKey(tiro.getOrigemId())){
                placar.put(tiro.getOrigemId(), ((Integer)placar.get(tiro.getOrigemId()))+1 );
                enviaPlacar();
            }
        }
    }

    private void atualizaStatus(MovimentoMsg m) {
        if(!placar.containsKey(m.getInimigoId())){
            placar.put(m.getInimigoId(), new Integer(0));
            enviaPlacar();
        }
    }

    private void enviaPlacar() {
        PlacarMsg p = new PlacarMsg();
        p.setPlacar(placar);
        myServer.broadcast(p);
    }

}