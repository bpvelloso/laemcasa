/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa.rede;

import bpv.laemcasa.rede.mensagens.MovimentoMsg;
import bpv.laemcasa.rede.mensagens.PlacarMsg;
import bpv.laemcasa.rede.mensagens.TiroMsg;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import com.jme3.scene.Geometry;
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
    private LinkedList<MovimentoMsg> buffer;
    private LinkedList<TiroMsg> bufferTiros;
    private boolean conectado;

    
    private ClienteLaEmCasa(){
        Serializer.registerClass(MovimentoMsg.class);
        Serializer.registerClass(TiroMsg.class);
        Serializer.registerClass(PlacarMsg.class);
        
        this.buffer = new LinkedList<MovimentoMsg>(Collections.synchronizedList(new LinkedList<MovimentoMsg>()));
        this.bufferTiros = new LinkedList<TiroMsg>(Collections.synchronizedList(new LinkedList<TiroMsg>()));
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
            myClient.addMessageListener(this, MovimentoMsg.class);
            myClient.addMessageListener(this, TiroMsg.class);
            myClient.addMessageListener(this, PlacarMsg.class);
            this.conectado = true;
            
            new Thread(){
                public void run(){
                    while(conectado){
                        consomeBuffer();
                    }
                }
            }.start();
            
            new Thread(){
                public void run(){
                    while(conectado){
                        consomeBufferTiros();
                    }
                }
            }.start();
            
            return true;
        } catch (Exception ex) {
            Logger.getLogger(ClienteLaEmCasa.class.getName()).log(Level.SEVERE, "Erro Conectando servidor remoto: {0}", ex);
            throw new LaEmCasaException(ex);
        }

    }
    
    
    public void envia(MovimentoMsg m) {
        //myClient.send(m);
        this.buffer.add(m);
    }
    
    public void enviaTiro(TiroMsg tiro) {
        this.bufferTiros.add(tiro);
    }

    protected void consomeBuffer(){
        if(this.buffer.size()>0){
            MovimentoMsg m;
            synchronized(buffer){
                m = buffer.remove();
            }
            myClient.send(m);
        }
    }
    
    protected void consomeBufferTiros(){
        if(this.bufferTiros.size()>0){
            TiroMsg t;
            synchronized(bufferTiros){
                t = bufferTiros.remove();
            }
            myClient.send(t);
        }
    }
    
    public void messageReceived(Client source, Message m) {
        if(m instanceof MovimentoMsg){
            if(aplicacao!=null)
                aplicacao.receberUpdateRemoto((MovimentoMsg)m);
        }
        if(m instanceof TiroMsg){
            if(aplicacao!=null)
                aplicacao.receberUpdateTiros((TiroMsg)m);
        }
        if(m instanceof PlacarMsg){
            if(aplicacao!=null)
                aplicacao.receberUpdatePlacar((PlacarMsg)m);
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
