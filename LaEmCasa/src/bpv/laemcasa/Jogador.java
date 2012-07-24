/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bpv.laemcasa;

import bpv.laemcasa.rede.ClienteLaEmCasa;
import bpv.laemcasa.rede.mensagens.TiroMsg;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
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
    private static final Integer ENERGIA_INICIAL=3;
    //private CharacterControl controle;
    private boolean left = false, right = false, up = false, down = false, frente = false;
    private Vector3f walkDirection = new Vector3f();
    private float velocidade=10;
    private Geometry mark;
    private AssetManager assetManager;
    private final Node rootNode;
    private Camera camera;
    private Integer energia = Jogador.ENERGIA_INICIAL;


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
        
        BoxCollisionShape collisionShape = new BoxCollisionShape(new Vector3f(22.0f, 17.0f, 12.0f));
        controle = new CharacterControl(collisionShape, 1000);
        this.getControle().setJumpSpeed(5);
        this.getControle().setFallSpeed(250);
        this.getControle().setGravity(9.81f);
        this.getControle().setSpatial(this.getModelo());
        this.getControle().setPhysicsLocation(new Vector3f(0, 100, 0));
        
        this.getModelo().addControl(this.getControle());
        
        this.initMark();    
    }

    
    @Override
    public CharacterControl getControle() {
        return (CharacterControl)controle;
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
        this.getControle().jump();
    }

    

    public void atualizaPosicao(Camera cam) {
        Vector3f camDir = cam.getDirection().clone().multLocal(this.velocidade);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left)  { walkDirection.addLocal(camLeft); }
        if (right) { walkDirection.addLocal(camLeft.negate()); }
        if (up)    { walkDirection.addLocal(camDir); }
        if (down)  { walkDirection.addLocal(camDir.negate()); }
        this.getControle().setWalkDirection(walkDirection);
        cam.setLocation(this.getControle().getPhysicsLocation().clone());
        this.getModelo().setLocalRotation(cam.getRotation().clone());
        this.getModelo().setLocalTranslation(this.getControle().getPhysicsLocation().clone());
    }

    private void atira() {
       
        CollisionResults results = new CollisionResults();      
        Ray ray = new Ray(this.getControle().getPhysicsLocation(), this.getCamera().getDirection());
       
        rootNode.collideWith(ray, results);
       
        if (results.size() > 0) {
 
          CollisionResult closest = results.getClosestCollision();
          Spatial no = closest.getGeometry();
          
          boolean acertou=false;
          while(!no.equals(rootNode)){
              if(no instanceof Inimigo){
                  System.out.println("TIROOOOOOOOOOOOOOOOOO!!!!!!!!!! "+no);
                  enviaTiroRemoto(((Inimigo)no).getId(), closest.getContactPoint());
                  acertou=true;
                  break;
              }else{
                  no = no.getParent();
              }
          }
          if(!acertou){
              enviaTiroRemoto("cenario", closest.getContactPoint());
          }
          
          mark.setLocalTranslation(closest.getContactPoint());
          rootNode.attachChild(mark);
          
        } else {
          rootNode.detachChild(mark);
        }
    }
    
    private void initMark() {
        
        Sphere sphere = new Sphere(30, 30, 5.2f);
        mark = new Geometry("tiro", sphere);
        
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

    private void enviaTiroRemoto(String alvo, Vector3f pos) {
        TiroMsg t = new TiroMsg();
        
        t.setOrigemId(this.getId());
        t.setAlvoId(alvo);
        t.setPosicaoHit(pos);
        
        ClienteLaEmCasa.getInstancia().enviaTiro(t);
    }

    void levarDano() {
        this.energia-=1;
        if(energia==0){
            energia=Jogador.ENERGIA_INICIAL;
            getControle().setPhysicsLocation(new Vector3f(0, 100, 0));
        }
    }
}
