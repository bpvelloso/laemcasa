/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa.rede.mensagens;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author velloso
 */
@Serializable
public class MovimentoMsg extends AbstractMessage {
    private String inimigoId;
    private Quaternion orientacao;
    private Vector3f posicao;

    public String getInimigoId() {
        return inimigoId;
    }

    public void setInimigoId(String inimigoId) {
        this.inimigoId = inimigoId;
    }

    public Quaternion getOrientacao() {
        return orientacao;
    }

    public void setOrientacao(Quaternion orientacao) {
        this.orientacao = orientacao;
    }

    public Vector3f getPosicao() {
        return posicao;
    }

    public void setPosicao(Vector3f posicao) {
        this.posicao = posicao;
    }
    
    @Override
    public String toString(){
        String m = "Movimento: "+this.getInimigoId()+"\n\tPos: "+this.getPosicao()+"\n\tOrient: "+this.getOrientacao();
        return m;
    }
}
