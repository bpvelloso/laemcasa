/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa.rede;

import bpv.laemcasa.rede.mensagens.MovimentoMsg;
import bpv.laemcasa.rede.mensagens.PlacarMsg;
import bpv.laemcasa.rede.mensagens.TiroMsg;

/**
 *
 * @author velloso
 */
public interface AplicacaoRemota {
    public void receberUpdateRemoto(MovimentoMsg m);

    public void receberUpdateTiros(TiroMsg tiro);

    public void receberUpdatePlacar(PlacarMsg placarMsg);
}
