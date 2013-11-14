package jme3test.helloworld;
 
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.math.ColorRGBA;
 
/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
public class HelloJME3 extends SimpleApplication {
    // start the game
    public static void main(String[] args){
        HelloJME3 app = new HelloJME3();
        app.start(); 
    }
    //initialize box
    public void simpleInitApp() {
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);  // create cube geometry from the shape
        //1) CREATE the material
        //2) set color of material
        //3) SET the material
        //4) set the starting location of Object (cube)
        Material mat = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");  
        mat.setColor("Color", ColorRGBA.Red);   
        geom.setMaterial(mat);              
        rootNode.attachChild(geom);
    }
}