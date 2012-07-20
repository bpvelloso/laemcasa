/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author velloso
 */
public class Personagem extends Node{
    private String id;

    private Spatial modelo;
    protected PhysicsControl controle;
    
    public Personagem(String id) {
        super(id);
        this.id = id;
    }
    
    public Personagem() {
        this.id = "Personagem"+Math.random();
    }

    public Spatial getModelo() {
        return modelo;
    }

    public void setModelo(Spatial modelo) {
        if(this.modelo!=null){
            this.detachChild(this.modelo);
        }
        this.modelo = modelo;
        this.attachChild(this.modelo);
    }
   
    public String getId() {
        return id;
    }
    
    public void setId(String id){
        this.id=id;
    }

    public PhysicsControl getControle(){
        return controle;
    }
}
