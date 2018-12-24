package com.idx.inshowapp.utils;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class PointsMatrix {
	private final String vertexShaderCode =
			// This matrix member variable provides a hook to manipulate
			// the coordinates of the objects that use this vertex shader
			"uniform mat4 uMVPMatrix;" +
					"attribute vec4 vPosition;" + "void main() {" +
					// the matrix must be included as a modifier of gl_Position
					"  gl_Position = vPosition * uMVPMatrix; gl_PointSize = 8.0;" + "}";

	private final String fragmentShaderCode = "precision mediump float;" + "uniform vec4 vColor;" + "void main() {"
			+ "  gl_FragColor = vColor;" + "}";

	// private final FloatBuffer vertexBuffer;
	private final int mProgram;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;

	// Set color with red, green, blue and alpha (opacity) values
	float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

	// 画点
	public ArrayList<ArrayList> points = new ArrayList<ArrayList>();

	public PointsMatrix() {

		// prepare shaders and OpenGL program
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		mProgram = GLES20.glCreateProgram(); // create empty OpenGL Program
		Log.d(TAG, "PointsMatrix: mprogram "+mProgram);
		GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader
		// to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment
		// shader to program
		GLES20.glLinkProgram(mProgram); // create OpenGL program executables
	}

	public void draw() {
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

		// get handle to fragment shader's vColor member
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

		// get handle to shape's transformation matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
//		checkGlError("glGetUniformLocation");

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
//		checkGlError("glUniformMatrix4fv");
		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		// Set color for drawing the triangle

		GLES20.glUniform4fv(mColorHandle, 1, color, 0);

		synchronized (this) {
			//points.size()表示有多少张脸
			for (int i = 0; i < points.size(); i++) {
				ArrayList<FloatBuffer> triangleVBList = points.get(i);
				//画每张脸的关键点
				for (int j = 0; j < triangleVBList.size(); j++) {
					FloatBuffer fb = triangleVBList.get(j);
					if (fb != null) {
						GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, fb);
						// Draw the point
						GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
					}
				}
			}
		}

			// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
	}

	public int loadShader(int type, String shaderCode) {
		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjMatrix = new float[16];
	private final float[] mVMatrix = new float[16];

	public void setMatrix(){
		float ratio = 1;
		Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
		Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3,
				0f, 0f, 0f, 0f, 1f, 0f);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
	}

}
