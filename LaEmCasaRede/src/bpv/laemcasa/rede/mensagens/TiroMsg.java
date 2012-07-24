/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa.rede.mensagens;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;

/**
 *
 * @author velloso
 */
@Serializable
public class TiroMsg extends AbstractMessage {

    private Vector3f posicaoHit;
    private String origemId;
    private String alvoId;

    public String getAlvoId() {
        return alvoId;
    }

    public void setAlvoId(String alvoId) {
        this.alvoId = alvoId;
    }

    public String getOrigemId() {
        return origemId;
    }

    public void setOrigemId(String origemId) {
        this.origemId = origemId;
    }

    public Vector3f getPosicaoHit() {
        return posicaoHit;
    }

    public void setPosicaoHit(Vector3f posicaoHit) {
        this.posicaoHit = posicaoHit;
    }
    
    @Override
    public String toString(){
        String tiro = "Tiro: "+origemId+" >>> "+alvoId+" em: "+posicaoHit;
        return tiro;
    }
}
