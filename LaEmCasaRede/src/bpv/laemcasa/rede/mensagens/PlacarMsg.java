/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa.rede.mensagens;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.HashMap;

/**
 *
 * @author velloso
 */
@Serializable
public class PlacarMsg extends AbstractMessage{
    private HashMap<String, Integer> placar;

    public HashMap<String, Integer> getPlacar() {
        return placar;
    }

    public void setPlacar(HashMap<String, Integer> placar) {
        this.placar = placar;
    }
    
    @Override
    public String toString(){
        String msg = "Placar:\n";
        if(this.placar!=null){
            for(String chave: this.placar.keySet()){
                msg+="\t"+chave+": "+this.placar.get(chave)+"\n";
            }
        }
        return msg;
    }
}
