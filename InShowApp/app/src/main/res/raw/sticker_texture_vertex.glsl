uniform mat4 matrix;
attribute vec4 position;
attribute vec2 inputTextureCoordinate;
varying vec2 textureCoordinate;
void main(){
    textureCoordinate = inputTextureCoordinate;
    gl_Position = matrix*position;
}
