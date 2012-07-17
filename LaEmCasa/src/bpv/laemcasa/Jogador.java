/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author velloso
 */
public class Jogador extends Personagem implements ActionListener{
    private CharacterControl controle;
    private boolean left = false, right = false, up = false, down = false, frente = false;
    private Vector3f walkDirection = new Vector3f();
    private float velocidade=10;
    private Geometry mark;
    private AssetManager assetManager;
    private final Node rootNode;
    private Camera camera;


    public Jogador(String id, Spatial modelo, Node rootNode,AssetManager assetManager, Camera camera) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.camera = camera;
        this.setId(id);
        this.init(modelo);
    }

    private void init(Spatial modelo) {
        //assetManager.loadModel("Models/Nave/vadertie.j3o")
        this.setModelo(modelo);
        
        CapsuleCollisionShape boxShape = new CapsuleCollisionShape(7f, 7f);
        controle = new CharacterControl(boxShape, 1000);
        controle.setJumpSpeed(5);
        controle.setFallSpeed(250);
        controle.setGravity(9);
        //naveSpacial.addControl(player);
        controle.setSpatial(this.getModelo());
        controle.setPhysicsLocation(new Vector3f(0, 100, 0));
        
         this.initMark();    
    }

    public CharacterControl getControle() {
        return controle;
    }
    
    
    public void setUpKeys(InputManager inputManager) {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("HUD", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addMapping("Atira",new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "HUD");
        inputManager.addListener(this, "Atira");
    }
  
  public void onAction(String binding, boolean value, float tpf) {
    if (binding.equals("Left")) {
      if (value) { left = true; } else { left = false; }
    } else if (binding.equals("Right")) {
      if (value) { right = true; } else { right = false; }
    } else if (binding.equals("Up")) {
      if (value) { up = true; } else { up = false; }
    } else if (binding.equals("Down")) {
      if (value) { down = true; } else { down = false; }
    } else if (binding.equals("Jump")) {
      if(value)
            this.voa();
    } else if (binding.equals("Atira")) {
        if(value)
            this.atira();
    }
  }

    private void voa() {
        controle.jump();
    }

    

    public void atualizaPosicao(Camera cam) {
        Vector3f camDir = cam.getDirection().clone().multLocal(this.velocidade);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left)  { walkDirection.addLocal(camLeft); }
        if (right) { walkDirection.addLocal(camLeft.negate()); }
        if (up)    { walkDirection.addLocal(camDir); }
        if (down)  { walkDirection.addLocal(camDir.negate()); }
        controle.setWalkDirection(walkDirection);
        cam.setLocation(controle.getPhysicsLocation().clone());
        this.getModelo().setLocalRotation(cam.getRotation().clone());
        this.getModelo().setLocalTranslation(controle.getPhysicsLocation().clone());
    }

    private void atira() {
        
        
        Tiro t = new Tiro(rootNode, assetManager, this);
        
//        // 1. Reset results list.
//        CollisionResults results = new CollisionResults();
//        // 2. Aim the ray from cam loc to cam direction.
//        Ray ray = new Ray(this.getControle().getPhysicsLocation(), this.getControle().getWalkDirection());
//        // 3. Collect intersections between Ray and alvos in results list.
//        rootNode.collideWith(ray, results);
//        // 4. Print the results
//        System.out.println("----- Collisions? " + results.size() + "-----");
//        for (int i = 0; i < results.size(); i++) {
//          // For each hit, we know distance, impact point, name of geometry.
//          float dist = results.getCollision(i).getDistance();
//          Vector3f pt = results.getCollision(i).getContactPoint();
//          String hit = results.getCollision(i).getGeometry().getName();
//          System.out.println("* Collision #" + i);
//          System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
//        }
//        // 5. Use the results (we mark the hit object)
//        if (results.size() > 0) {
//          // The closest collision point is what was truly hit:
//          CollisionResult closest = results.getClosestCollision();
//          // Let's interact - we mark the hit with a red dot.
//          mark.setLocalTranslation(closest.getContactPoint());
//          rootNode.attachChild(mark);
//          this.raio(controle.getPhysicsLocation(), closest.getContactPoint());
//          
//          
//        } else {
//          // No hits? Then remove the red mark.
//          rootNode.detachChild(mark);
//        }
    }
    
    private void initMark() {
        
        Sphere sphere = new Sphere(30, 30, 5.2f);
        mark = new Geometry("BOOM!", sphere);
        
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
        
        
        
    }
    
    Geometry tiro;
    private void raio(Vector3f inicio, Vector3f fim) {
        
        if(tiro!=null) rootNode.detachChild(tiro);
        Line linhaTiro = new Line(inicio, fim);
        linhaTiro.setLineWidth(5);
        tiro = new Geometry("Tiro", linhaTiro);
        Material materialTiro = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        materialTiro.setColor("Color", ColorRGBA.Yellow);
        tiro.setMaterial(materialTiro);
        rootNode.attachChild(tiro);
//        new Thread(){
//            @Override
//            public void run(){
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                rootNode.detachChild(tiro);
//            }
//        }.start();
    }

    void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public Camera getCamera() {
        return camera;
    }
}
