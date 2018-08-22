package ru.laz.camera;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class TestGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img;
    Vector3 cameraPos;
    Vector3 cameraTarget;
    Vector3 cameraDir;
    Vector3 camUp;
    Vector3 cameraRight;

    OrthographicCamera cam;

    Matrix4 viewMatrix;




    @Override
    public void create () {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        Gdx.input.setInputProcessor(new GestureDetector(20, 0.4f, 0.7f, 0.15f, new SceneGestureListener()));
        //Matrix4 myMatrix = genMatrix();
        Gdx.app.log("CAMERA PROJ\n", batch.getProjectionMatrix().toString());
        //Gdx.app.log("CAMERA MY\n", myMatrix.toString());
        Gdx.app.log("PR MATRIX \n", batch.getProjectionMatrix().toString());
        Gdx.app.log("TR MATRIX \n", batch.getTransformMatrix().toString());



        cameraPos = new Vector3(0.0f,0.0f,0.5f);//позция камеры
        cameraTarget = new Vector3(0,0,0);//куда направлена


       cameraDir = cameraPos.cpy().sub(cameraTarget).nor();/*Вычитание вектора положения камеры из точки начала координат даст нам вектор направления камеры. Мы знаем, что камера смотрит вдоль отрицательного направления оси-Z, а нам нужен вектор направленный вдоль положительной оси-Z самой камеры. Если при вычитании мы изменим очередность аргументов, то получим вектор, указывающий в положительном направлении оси-Z камеры*/


        Vector3 tmpUp = new Vector3(0,1,0);//процесс Грама-Шмидта

        cameraRight = tmpUp.crs(cameraDir).nor();


        camUp = cameraDir.cpy().crs(cameraRight);

        Gdx.app.log("cam dir ", cameraDir.toString());
        Gdx.app.log("cam right ", cameraRight.toString());
        Gdx.app.log("cam up ", camUp.toString());

        //Matrix4 viewMatrix = genMatrix(cameraPos, cameraDir, camUp, cameraRight, Gdx.app.getGraphics().getWidth(), Gdx.app.getGraphics().getHeight());
        viewMatrix = genMatrix(cameraPos, cameraDir, camUp, cameraRight, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Gdx.app.log("cam ORIG MATR \n", batch.getProjectionMatrix().toString());
        Gdx.app.log("cam VIEW MATRIX \n", viewMatrix.toString());
        Gdx.app.log("cam XY", Gdx.graphics.getWidth() + " x " + Gdx.graphics.getHeight());
    }


    private Matrix4 genMatrix(Vector3 camPos, Vector3 dirAxis, Vector3 upAxis, Vector3 rightAxis, float viewWidth, float viewHeight) {


        //"LookAt" matrix, construct by formula (view matrix in Orthographic camera in libgdx)
        Matrix4 camAxes = new Matrix4();
        camAxes.idt();
        camAxes.val[Matrix4.M00] = rightAxis.x;
        camAxes.val[Matrix4.M01] = rightAxis.y;
        camAxes.val[Matrix4.M02] = rightAxis.z;

        camAxes.val[Matrix4.M10] = upAxis.x;
        camAxes.val[Matrix4.M11] = upAxis.y;
        camAxes.val[Matrix4.M12] = upAxis.z;

        camAxes.val[Matrix4.M20] = dirAxis.x;
        camAxes.val[Matrix4.M21] = dirAxis.y;
        camAxes.val[Matrix4.M22] = dirAxis.z;


        //like "glOrtho" in OPENGL (projection matrix in Orthographic camera in libgdx)
        Matrix4 camPosition = new Matrix4();
        camPosition.idt();
        camPosition.val[Matrix4.M00] = 2.0f/viewWidth;//scale view y. axis in opengl from -1.0 to 1.0
        camPosition.val[Matrix4.M11] = 2.0f/viewHeight;//scale view x
        camPosition.val[Matrix4.M03] = -camPos.x-1;//move lo left side
        camPosition.val[Matrix4.M13] = -camPos.y-1;//move lo bottom
        camPosition.val[Matrix4.M23] = -camPos.z;


        Matrix4 cameraMatrix = camAxes.mul(camPosition);


        Matrix4 invMatr = new Matrix4(cameraMatrix).inv();


        Gdx.app.log("cam cameraMatrix \n", cameraMatrix.toString());
        Gdx.app.log("cam invCameraMatrix \n", invMatr.toString());
        return cameraMatrix;
    }



    public Vector3 convertCoord(Vector2 input, boolean toWorld) {
        Vector3 vec3 = new Vector3(input.x,input.y, 0.0f);
        if (toWorld) {
            vec3.mul(viewMatrix);
        } else {
            Matrix4 invMatr = new Matrix4(viewMatrix).inv();
            vec3.mul(invMatr);
        }
        return vec3;
    }


    @Override
    public void render () {
        Gdx.gl.glClearColor(0, 0.7f, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(viewMatrix);
        batch.begin();
        batch.draw(img, 0, 0);
        batch.end();
    }
    @Override
    public void dispose () {
        batch.dispose();
        img.dispose();
    }

    class SceneGestureListener implements GestureDetector.GestureListener {

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            Vector2 touch = new Vector2(x,y);
            Vector3 vec3 = new Vector3(x,y,0);
            //vec3.mul(batch.getProjectionMatrix());

            vec3 = convertCoord(new Vector2(x,y), true);
            Gdx.app.log("cam TAP", (int)x + ":" + (int)y + " ui: " + vec3.x + ":" + vec3.y + "; screen "+Gdx.app.getGraphics().getWidth()+"x"+Gdx.app.getGraphics().getHeight());

            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            return false;
        }

        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            return false;
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            return false;
        }

        @Override
        public void pinchStop() {

        }
    }
}
