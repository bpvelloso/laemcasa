/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa.rede;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author velloso
 */
public class ClienteLaEmCasa implements MessageListener<Client>{

    private static ClienteLaEmCasa instancia = null;
    private static Client myClient;
    private static AplicacaoRemota aplicacao;
    private LinkedList<Movimento> buffer;
    private boolean conectado;

    
    private ClienteLaEmCasa(){
        Serializer.registerClass(Movimento.class);
        this.buffer = new LinkedList<Movimento>(Collections.synchronizedList(new LinkedList<Movimento>()));
    }
    
    
    public static ClienteLaEmCasa getInstancia(){
        if(instancia==null){
            instancia=new ClienteLaEmCasa();
        }
        return instancia;
    }
    
    public boolean conecta(String host, int porta) throws LaEmCasaException{
        try {
            myClient = Network.connectToServer(host, porta);
            myClient.start();
            myClient.addMessageListener(this, Movimento.class);
            this.conectado = true;
            
            new Thread(){
                public void run(){
                    while(conectado){
                        consomeBuffer();
                    }
                }
            }.start();
            
            return true;
        } catch (Exception ex) {
            Logger.getLogger(ClienteLaEmCasa.class.getName()).log(Level.SEVERE, "Erro Conectando servidor remoto: {0}", ex);
            throw new LaEmCasaException(ex);
        }

    }
    
    
    public void envia(Movimento m) {
        //myClient.send(m);
        this.buffer.add(m);
    }

    protected void consomeBuffer(){
        if(this.buffer.size()>0){
            Movimento m;
            synchronized(buffer){
                m = buffer.remove();
            }
            myClient.send(m);
        }
    }
    
    public void messageReceived(Client source, Message m) {
        if(m instanceof Movimento){
            if(aplicacao!=null)
                aplicacao.receberUpdateRemoto((Movimento)m);
        }
    }
    
    public void setAplicacao(AplicacaoRemota a) {
        aplicacao = a;
    }

    public void terminar() {
        if((myClient!=null)&&(myClient.isConnected()))
            myClient.close();
            conectado=false;
    }
    
    
    public boolean isConectado() {
        return conectado;
    }

    public void setConectado(boolean conectado) {
        this.conectado = conectado;
    }
}
