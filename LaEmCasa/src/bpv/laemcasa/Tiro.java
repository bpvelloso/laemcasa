/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;




/**
 *
 * @author velloso
 */
public class Tiro extends Personagem {
    private final Node rootNode;
    private final AssetManager assetManager;
    private final Jogador origem;
    private RigidBodyControl controle;
    
    public static final float VELOCIDADE=1;


    
    public Tiro(Node rootNode, AssetManager assetManager, Jogador origem) {
        this.origem = origem;
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.setId("Tiro"+Math.random());
        
        this.init();
    }

    private void init() {
        /** Load a model. Uses model and texture from jme3-test-data library! */ 
        Geometry m = new Geometry("cannon ball", new Sphere(20, 20, 2));
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        m.setMaterial(mat);
        this.setModelo(m);
        
        rootNode.attachChild(getModelo());
        /** Position the cannon ball  */
        this.getModelo().setLocalTranslation(origem.getControle().getPhysicsLocation().clone());
        /** Make the ball physcial with a mass > 0.0f */
        this.controle = new RigidBodyControl(0.10f);
        
        this.getModelo().addControl(controle);
        
        Main.getBulletAppState().getPhysicsSpace().add(controle);
        /** Accelerate the physcial ball to shoot it. */
        //controle.setLinearVelocity( origem.getCamera().getDirection().clone().multLocal(Tiro.VELOCIDADE) );
        this.getModelo().setLocalRotation(origem.getCamera().getRotation().clone().inverse());
        Vector3f impulso = origem.getCamera().getDirection().clone().multLocal(Tiro.VELOCIDADE);
        controle.applyImpulse( impulso, origem.getControle().getPhysicsLocation().clone() );
        System.out.println(">>>>>>>Impulso:"+impulso);
    }
    
}
	  
    