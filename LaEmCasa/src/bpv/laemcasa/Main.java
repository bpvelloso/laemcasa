package bpv.laemcasa;

import bpv.laemcasa.rede.AplicacaoRemota;
import bpv.laemcasa.rede.mensagens.MovimentoMsg;
import bpv.laemcasa.rede.ClienteLaEmCasa;
import bpv.laemcasa.rede.LaEmCasaException;
import bpv.laemcasa.rede.ServidorLaEmCasa;
import bpv.laemcasa.rede.mensagens.PlacarMsg;
import bpv.laemcasa.rede.mensagens.TiroMsg;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication implements ActionListener, AplicacaoRemota, ScreenController{
    private static String host=null;
    private static Integer porta=null;
    private static Main app;

    public static void main(String[] args) {
       
        app = new Main();
        
        AppSettings cfg = new AppSettings(true);
        cfg.setVSync(false);   // prevents page tearing
        cfg.setResolution(1024, 768);   
        cfg.setFullscreen(false); 
        cfg.setSamples(2);    // anti-aliasing
        cfg.setTitle("La em Casa"); // branding: window name
        cfg.setSettingsDialogImage("Interface/TinyHouse.png"); 
        //app.setShowSettings(false); // or don't display splashscreen
        app.setSettings(cfg);
        
        if(args.length>=2){
            Logger.getLogger(Main.class.getName()).log(Level.INFO,"Parametros de Conexao - {0}",args[0]+":"+args[1]);
            host = args[0];
            try{
                porta = Integer.parseInt(args[1]);
            }catch(Exception e){
                porta = null;
            }
            
        }
        
        app.start();
        
    }

    
    private Spatial modeloCasa;
    private RigidBodyControl controleCenario;
    private static BulletAppState bulletAppState;
    private HashMap<String,Inimigo> inimigos = new HashMap<String, Inimigo>();
    private List<MovimentoMsg> listaMovimentos;
    private List<TiroMsg> listaTiros;
    private Nifty nifty;
    private boolean conectado;
    private Jogador heroi;
    private int impacto=0;
    
    @Override
    public void simpleInitApp() {
      
        listaMovimentos = Collections.synchronizedList(new ArrayList<MovimentoMsg>());
        listaTiros = Collections.synchronizedList(new ArrayList<TiroMsg>());
        
        if((host!=null)&&(porta!=null)){
            this.conecta(host, porta);
        }else{
            this.mostrarMenuConexao();
        }
        
        this.addLuzes();
        
        this.initLaEmCasa();
        
        this.initHeroi();
        
        this.setUpComandosInterface();
        
        //this.initCrossHairs();
    }

    private void conecta(String host, int porta) {
        Label resultado = (Label) nifty.getCurrentScreen().findNiftyControl("resultado", Label.class);
        resultado.setText("Conectando...");
        try{
           ClienteLaEmCasa.getInstancia().conecta(host, porta);
           conectado=true;
           resultado.setText("Conexão ok!");
           nifty.gotoScreen("hud");
           ClienteLaEmCasa.getInstancia().setAplicacao(this);
           flyCam.setDragToRotate(false);
        }catch(LaEmCasaException ex){
            String erro="Erro conectando ao servidor ";
            erro+=ex.getLocalizedMessage().substring(25);
            resultado.setText(erro);
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void destroy() {
        ClienteLaEmCasa.getInstancia().terminar();
        super.destroy();
    }
    
    
    
    private void addLuzes() {
        cam.setFrustumFar(100000f);
        /** A white, spot light source. */ 
        PointLight lamp = new PointLight();
        lamp.setPosition(new Vector3f(0f,150f,0f));
        lamp.setColor(ColorRGBA.White);
        lamp.setRadius(200000);
        rootNode.addLight(lamp); 
        /** A white, directional light source */ 
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun); 
    }
     
    private void initLaEmCasa() {
        modeloCasa = assetManager.loadModel("Scenes/Casa/casa.j3o");
        modeloCasa.setLocalScale(new Vector3f(20f, 20f, 20f));
        
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape((Node) modeloCasa);
        controleCenario = new RigidBodyControl(sceneShape, 0);
        controleCenario.setGravity(Vector3f.ZERO);
        modeloCasa.addControl(controleCenario);
        rootNode.attachChild(modeloCasa);  
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().add(controleCenario);
        
        rootNode.attachChild(modeloCasa);
               
    }

    
    
   
    public static final Quaternion ROLL090  = new Quaternion().fromAngleAxis(FastMath.PI/2,   new Vector3f(0,0,1));
    public static final Quaternion YAW090   = new Quaternion().fromAngleAxis(FastMath.PI/2,   new Vector3f(0,1,0));
    public static final Quaternion PITCH090 = new Quaternion().fromAngleAxis(FastMath.PI/2,   new Vector3f(1,0,0));
    
    private void initHeroi() { 
        heroi = new Jogador("Jogador"+Math.random(),assetManager.loadModel("Models/Nave/vadertie.j3o"), rootNode, assetManager, cam);
        bulletAppState.getPhysicsSpace().add(heroi.getControle());   
        heroi.setUpKeys(inputManager);
        heroi.setAssetManager(assetManager);
    }
    
  
    private void setUpComandosInterface() {
        inputManager.addMapping("hud", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addListener(this, "hud");
        
        inputManager.addMapping("hudAtira", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "hudAtira");
    }
    
    public void onAction(String binding, boolean isPressed, float tpf) {
        if (binding.equals("hud")) {
            if(isPressed){
                if(nifty.getCurrentScreen().getScreenId().equals("hud")){
                    nifty.gotoScreen("vazio");
                }else{
                    nifty.gotoScreen("hud");
                }
            }
        }
        if (binding.equals("hudAtira")) {
            if(nifty.getCurrentScreen().getScreenId().equals("hud")){
                if(isPressed){
                    nifty.getCurrentScreen().findElementByName("visaoNormal").setVisible(false);
                    nifty.getCurrentScreen().findElementByName("visaoAtirando").setVisible(true);   
                }else{
                    nifty.getCurrentScreen().findElementByName("visaoNormal").setVisible(true);
                    nifty.getCurrentScreen().findElementByName("visaoAtirando").setVisible(false);
                }
            }
        }
    }
  
  @Override
  public void simpleUpdate(float tpf) {
    heroi.atualizaPosicao(cam);
        
    if(conectado){
        enviarUpdateRemoto(heroi.getControle().getPhysicsLocation(), cam.getRotation());
    }
    
    if(this.impacto>0){
        Element painelAlvejado = nifty.getCurrentScreen().findElementByName("alvejado");
        painelAlvejado.setVisible(false);
    }else{
        this.impacto-=1;
    }
    
    synchronized(listaMovimentos) {
        for(MovimentoMsg m: listaMovimentos){
            if(!m.getInimigoId().equals(heroi.getId())){
                onRemoteUpdate(m);
            }
        }
        listaMovimentos.clear();
    }
    
    synchronized(listaTiros) {
        for(TiroMsg t: listaTiros){
            if(t.getAlvoId().equals(heroi.getId())){
                onHited(t);
            }else{
                plotaTiro(t);
            }
        }
        listaTiros.clear();
    }

    
    
  }

  private MovimentoMsg movimentoAtual = new MovimentoMsg();
  public void enviarUpdateRemoto(Vector3f pos, Quaternion or){
      
      movimentoAtual.setInimigoId(heroi.getId());
      movimentoAtual.setOrientacao(or);
      movimentoAtual.setPosicao(pos);
      
      //Enviar movimento
      ClienteLaEmCasa.getInstancia().envia(movimentoAtual);
  }
  
    public void receberUpdateRemoto(MovimentoMsg movimento){
       listaMovimentos.add(movimento);
    } 

    public void receberUpdateTiros(TiroMsg tiro) {
        listaTiros.add(tiro);
    }
   
    public void receberUpdatePlacar(PlacarMsg placarMsg) {
        this.atualizaPlacar(placarMsg);
    }
   
    public void onRemoteUpdate(MovimentoMsg movimento){
        Inimigo inimigo = inimigos.get(movimento.getInimigoId());
        
        if(inimigo==null){
            inimigo = new Inimigo(movimento.getInimigoId());
            inimigo.setModelo(assetManager.loadModel("Models/Nave/vadertie.j3o"));
            inimigo.getModelo().setLocalScale(20f);
            inimigos.put(movimento.getInimigoId(), inimigo);
            rootNode.attachChild(inimigo);
        }
        
        inimigo.getModelo().setLocalRotation(movimento.getOrientacao());
        inimigo.getModelo().setLocalTranslation(movimento.getPosicao());
        
    }

    private void mostrarMenuConexao() {
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
            assetManager, inputManager, audioRenderer, guiViewPort);
        /** Create a new NiftyGUI object */
        nifty = niftyDisplay.getNifty();
        /** Read your XML and initialize your custom ScreenController */
        nifty.fromXml("Interface/screen.xml", "start", this);
        // nifty.fromXml("Interface/helloworld.xml", "start", new MySettingsScreen(data));
        // attach the Nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);
        
        flyCam.setDragToRotate(true);
        inputManager.setCursorVisible(true);
    }

    public void bind(Nifty nifty, Screen screen) {
        
    }

    public void onStartScreen() {
        
    }

    public void onEndScreen() {
        
    }
    
    public void conectarPorDadosMenu(){

        Label resultado = (Label) nifty.getCurrentScreen().findNiftyControl("resultado", Label.class);
        resultado.setText("Conectando...");
        
        TextField hostTextField = (TextField) nifty.getCurrentScreen().findNiftyControl("host", TextField.class);
        host = hostTextField.getText();
        
        TextField playerIdTextField = (TextField) nifty.getCurrentScreen().findNiftyControl("playerId", TextField.class);
        String playerId = playerIdTextField.getText();
        
        TextField portaTextField = (TextField) nifty.getCurrentScreen().findNiftyControl("porta", TextField.class);
        try{
            porta = Integer.parseInt(portaTextField.getText());
        }catch(Exception e){
            Logger.getLogger(Main.class.getName()).log(Level.WARNING,"Erro convertento porta de conexão, usando padrão {0}", ServidorLaEmCasa.PORTA_PADRAO);
            porta = ServidorLaEmCasa.PORTA_PADRAO;
        }        
        Logger.getLogger(Main.class.getName()).log(Level.INFO,"Parametros do usuário para Conexão: {0}", host+":"+porta);
        this.conecta(host, porta);
        if(!playerId.equals("")){
            heroi.setId(playerId);
        }
    }
    
    public void quitGame() {
        this.stop(); 
    }
    
   
    protected void initCrossHairs() {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+");        // fake crosshairs :)
        ch.setLocalTranslation( // center
          settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
          settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
   }

    public static BulletAppState getBulletAppState() {
        return bulletAppState;
    }

    private void onHited(TiroMsg t) {
        Logger.getLogger(Main.class.getName()).log(Level.INFO,"TIRO RECEBIDO!!!! Inimigo: {0}", t.getOrigemId());
        Element painelAlvejado = nifty.getCurrentScreen().findElementByName("alvejado");
        painelAlvejado.setVisible(true);
        this.impacto=5;
        heroi.levarDano();
    }

    private void plotaTiro(TiroMsg t) {
        Logger.getLogger(Main.class.getName()).log(Level.INFO,"Tiro recebido do servido: {0}", t);
    }

    private void atualizaPlacar(PlacarMsg placarMsg) {
        Logger.getLogger(Main.class.getName()).log(Level.INFO,"Atualizando Placar: {0}", placarMsg);
        Element placarText = nifty.getCurrentScreen().findElementByName("placar");
        placarText.getRenderer(TextRenderer.class).setText(placarMsg.toString());
        
    }

}
        