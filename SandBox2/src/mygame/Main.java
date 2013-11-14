package mygame;

import com.jme3.math.ColorRGBA;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication implements AnalogListener, ActionListener {

    private BulletAppState bulletAppState;
    //private BetterCharacterControl physicsCharacter;
    private Node characterNode;
    boolean rotate = false;
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private Vector3f viewDirection = new Vector3f(0, 0, 1);
    boolean leftStrafe = false, rightStrafe = false, forward = false, backward = false,
            leftRotate = false, rightRotate = false;
    private Vector3f normalGravity = new Vector3f(0, -9.81f, 0);
    private Geometry planet;
    private Geometry teaGeom;
    private Node teaNode;
    CameraNode camNode;
    Vector3f direction = new Vector3f();
    Spatial golem;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
    Node shootables;
    Geometry mark;

    @Override
    public void simpleInitApp() {
        golem = assetManager.loadModel("Models/Simpleboy.obj");
        golem.scale(0.5f);
        golem.setLocalTranslation(0.0f, -4.0f, 0.0f);
        
        Material golemMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        golemMat.setTexture("ColorMap", assetManager.loadTexture("Textures/WallTexture2.JPG"));
        //golemMat.setColor("Color", ColorRGBA.Green);
        golem.setMaterial(golemMat);
        
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0.0f, 1.5f, 1.0f));
        sun.setColor(ColorRGBA.White);
        golem.addLight(sun);
        
        shootables = new Node("Shootables");
        initMark();

        Box box = new Box(new Vector3f(0, 0, 0), 1, 1, 1);
        teaGeom = new Geometry("cameraFocal", box);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        teaGeom.setQueueBucket(Bucket.Transparent);
        mat1.setColor("Color",new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f));
        teaGeom.setMaterial(mat1);
       

        //create a node to attach the geometry and the camera node
        teaNode = new Node("teaNode");
        teaNode.attachChild(teaGeom);
        rootNode.attachChild(teaNode);
        // Set forward camera node that follows the character, only used when
        // view is "locked"
        //creating the camera Node
        camNode = new CameraNode("CamNode", cam);
        //Setting the direction to Spatial to camera, this means the camera will copy the movements of the Node
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        //attaching the camNode to the teaNode
        teaNode.attachChild(camNode);
        //setting the local translation of the cam node to move it away from the teanNode a bit
        camNode.setLocalTranslation(new Vector3f(0, 20, 0));
        //setting the camNode to look at the teaNode
        camNode.lookAt(teaNode.getLocalTranslation(), Vector3f.UNIT_Y);

        //disable the default 1st-person flyCam (don't forget this!!)
        flyCam.setEnabled(false);

        registerInput();
    }

    @Override
    public void simpleUpdate(float tpf) {

        Vector3f origin = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
        Vector3f direction = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.3f);
        direction.subtractLocal(origin).normalizeLocal();

        Ray ray = new Ray(origin, direction);
        CollisionResults results = new CollisionResults();
        shootables.collideWith(ray, results);
        System.out.println("----- Collisions? " + results.size() + "-----");
        for (int i = 0; i < results.size(); i++) {
            // For each hit, we know distance, impact point, name of geometry.
            float dist = results.getCollision(i).getDistance();
            //Vector3f pt = results.getCollision(i).getWorldContactPoint();
            String hit = results.getCollision(i).getGeometry().getName();
            System.out.println("* Collision #" + i);
            System.out.println("  You shot " + hit + " at " + /*pt +*/ ", " + dist + " wu away.");
        }
        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            mark.setLocalTranslation(closest.getContactPoint());

            Quaternion q = new Quaternion();
            q.lookAt(closest.getContactNormal(), Vector3f.UNIT_Y);
            mark.setLocalRotation(q);

            rootNode.attachChild(mark);
        } else {
            rootNode.detachChild(mark);
        }
    }

    public void registerInput() {
        inputManager.addMapping("moveForward", new KeyTrigger(keyInput.KEY_W));
        inputManager.addMapping("moveBackward", new KeyTrigger(keyInput.KEY_S));
        inputManager.addMapping("moveRight", new KeyTrigger(keyInput.KEY_D));
        inputManager.addMapping("moveLeft", new KeyTrigger(keyInput.KEY_A));
        inputManager.addMapping("rotateRight", new KeyTrigger(keyInput.KEY_Q));
        inputManager.addMapping("rotateLeft", new KeyTrigger(keyInput.KEY_Z));
        //inputManager.addMapping("toggleRotate", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        //inputManager.addMapping("rotateRight", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        //inputManager.addMapping("rotateLeft", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addListener(this, "moveForward", "moveBackward", "moveRight", "moveLeft");
        inputManager.addListener(this, "rotateRight", "rotateLeft", "toggleRotate");
    }

    public void onAnalog(String name, float value, float tpf) {
        //computing the normalized direction of the cam to move the teaNode
        direction.set(cam.getDirection()).normalizeLocal();
        if (name.equals("moveForward")) {
            direction.crossLocal(Vector3f.UNIT_X).multLocal(5 * tpf);
            teaNode.move(direction);
        }
        if (name.equals("moveBackward")) {
            direction.crossLocal(Vector3f.UNIT_X).multLocal(-5 * tpf);
            teaNode.move(direction);
        }
        if (name.equals("moveRight")) {
            direction.crossLocal(Vector3f.UNIT_Z).multLocal(5 * tpf);
            teaNode.move(direction);
        }
        if (name.equals("moveLeft")) {
            direction.crossLocal(Vector3f.UNIT_Z).multLocal(-5 * tpf);
            teaNode.move(direction);
        }
        if (name.equals("rotateRight")) {
            direction.multLocal(5 * tpf);
            teaNode.move(direction);
        }
        if (name.equals("rotateLeft")) {
            direction.multLocal(-5 * tpf);
            teaNode.move(direction);
        }

        //initMark();       // a red sphere to mark the hit

        /**
         * create four colored boxes and a floor to shoot at:
         */
       
        rootNode.attachChild(shootables);
        shootables.attachChild(makeCube("a Dragon", -2f, 0f, 1f));
        shootables.attachChild(makeCube("a tin can", 1f, -2f, 0f));
        shootables.attachChild(makeCube("the Sheriff", 0f, 1f, -2f));
        shootables.attachChild(makeCube("the Deputy", 1f, 0f, -4f));
        shootables.attachChild(makeFloor());
        shootables.attachChild(golem);
    }

    /**
     * A cube object for target practice
     */
    protected Geometry makeCube(String name, float x, float y, float z) {
        Box box = new Box(new Vector3f(x, y, z), 1, 1, 1);
        Geometry cube = new Geometry(name, box);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.randomColor());
        cube.setMaterial(mat1);
        return cube;
    }

    /**
     * A floor to show that the "shot" can go through several objects.
     */
    protected Geometry makeFloor() {
        Box box = new Box(new Vector3f(0, -4, -5), 15, .2f, 15);
        Geometry floor = new Geometry("the Floor", box);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Gray);
        floor.setMaterial(mat1);
        return floor;
    }

    /**
     * A red ball that marks the last spot that was "hit" by the "shot".
     */
    
     protected void initMark() {
     Arrow arrow = new Arrow(Vector3f.UNIT_Z.mult(2f));
     arrow.setLineWidth(3);

     //Sphere sphere = new Sphere(30, 30, 0.2f);
     mark = new Geometry("BOOM!", arrow);
     //mark = new Geometry("BOOM!", sphere);
     Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
     mark_mat.setColor("Color", ColorRGBA.Red);
     mark.setMaterial(mark_mat);
     }
     
    public void onAction(String name, boolean keyPressed, float tpf) {
        //toggling rotation on or off
        if (name.equals("toggleRotate") && keyPressed) {
            rotate = true;
            inputManager.setCursorVisible(false);
        }
        if (name.equals("toggleRotate") && !keyPressed) {
            rotate = false;
            inputManager.setCursorVisible(true);
        }
    }

    
/*     protected Spatial makeCharacter() {
     // load a character from jme3test-test-data
     Spatial golem = assetManager.loadModel("Models/Simpleboy.obj");
     golem.scale(0.5f);
     golem.setLocalTranslation(-1.0f, -1.5f, -0.6f);

     // We must add a light to make the model visible
     DirectionalLight sun = new DirectionalLight();
     sun.setDirection(new Vector3f(-1.0f, 10.5f, 5.0f));
     sun.setColor(ColorRGBA.White);
     golem.addLight(sun);
     return golem;
     }
  */  
}
