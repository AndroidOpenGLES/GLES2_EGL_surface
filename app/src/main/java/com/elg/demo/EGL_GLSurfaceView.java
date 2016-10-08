package com.elg.demo;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

/**
 * EGL
 */
public class EGL_GLSurfaceView extends SurfaceView
        implements SurfaceHolder.Callback {


    private GL10 mGL10;

    // show color
    private float red = 0.9f, green = 0.0f, blue = 0.0f;


    // 渲染线程
    private EGL_GLThread mEGL_GLThread;


    public EGL_GLSurfaceView(Context context) {
        super(context);
        //注册回调接口
        getHolder().addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //开启线程
        startGLThreads();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 关闭
        stopGLThreads();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }


    /**
     * 渲染
     *
     * @param gl
     */
    private void onDrawFrame(GL10 gl) {
        gl.glClearColor(red, green, blue, 1.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
    }


    //开启线程
    public void startGLThreads() {
        //初始化刷帧线程
        mEGL_GLThread = new EGL_GLThread();
        mEGL_GLThread.setKeyFlag(true);        //线程标志位为true
        mEGL_GLThread.start();


    }

    //停止线程
    public void stopGLThreads() {
        //退出界面时，关闭刷帧线程
        mEGL_GLThread.setKeyFlag(false);
        mEGL_GLThread = null;
    }


    //刷帧线程
    private class EGL_GLThread extends Thread {
        private boolean keyFlag = false;

        @Override
        public void run() {

            EGL10 egl10 = (EGL10) EGLContext.getEGL();
            EGLDisplay eglDisplay = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            egl10.eglInitialize(eglDisplay, new int[2]);
            EGLConfig[] configs = new EGLConfig[1];
            egl10.eglChooseConfig(eglDisplay, new int[]{EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_NONE}, configs, 1, new int[1]);
            EGLConfig eglEGLConfig = configs[0];
            EGLSurface eglSurface = egl10.eglCreateWindowSurface(eglDisplay, eglEGLConfig,
                    EGL_GLSurfaceView.this.getHolder(), null);
            EGLContext eglContext = egl10.eglCreateContext(eglDisplay, eglEGLConfig,
                    EGL10.EGL_NO_CONTEXT, null);
            egl10.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
            mGL10 = (GL10) eglContext.getGL();

            // 刷帧
            while (isKeyFlag()) {
                //
                onDrawFrame(mGL10);
                egl10.eglSwapBuffers(eglDisplay, eglSurface);
            }
            Log.d("OpenGlDemo >>>", "Stop isRendering");

            egl10.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT);
            egl10.eglDestroyContext(eglDisplay, eglContext);
            egl10.eglDestroySurface(eglDisplay, eglSurface);
            mGL10 = null;
        }

        public void setKeyFlag(boolean keyFlag) {
            this.keyFlag = keyFlag;
        }

        public boolean isKeyFlag() {
            return keyFlag;
        }
    }


}

