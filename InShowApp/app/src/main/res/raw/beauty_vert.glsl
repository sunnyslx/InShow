attribute vec4 position;
attribute vec2 inputTextureCoordinate;
varying vec2 textureCoordinate;
varying vec2 blurCoord1s[14];
const highp float mWidth=800.0;
const highp float mHeight=1184.0;
void main( )
{
    gl_Position = position;
    textureCoordinate = inputTextureCoordinate;

    highp float mul_x = 2.0 / mWidth;
    highp float mul_y = 2.0 / mHeight;

    // 14个采样点
    blurCoord1s[0] = inputTextureCoordinate + vec2( 0.0 * mul_x, -10.0 * mul_y );
    blurCoord1s[1] = inputTextureCoordinate + vec2( 8.0 * mul_x, -5.0 * mul_y );
    blurCoord1s[2] = inputTextureCoordinate + vec2( 8.0 * mul_x, 5.0 * mul_y );
    blurCoord1s[3] = inputTextureCoordinate + vec2( 0.0 * mul_x, 10.0 * mul_y );
    blurCoord1s[4] = inputTextureCoordinate + vec2( -8.0 * mul_x, 5.0 * mul_y );
    blurCoord1s[5] = inputTextureCoordinate + vec2( -8.0 * mul_x, -5.0 * mul_y );
    blurCoord1s[6] = inputTextureCoordinate + vec2( 0.0 * mul_x, -6.0 * mul_y );
    blurCoord1s[7] = inputTextureCoordinate + vec2( -4.0 * mul_x, -4.0 * mul_y );
    blurCoord1s[8] = inputTextureCoordinate + vec2( -6.0 * mul_x, 0.0 * mul_y );
    blurCoord1s[9] = inputTextureCoordinate + vec2( -4.0 * mul_x, 4.0 * mul_y );
    blurCoord1s[10] = inputTextureCoordinate + vec2( 0.0 * mul_x, 6.0 * mul_y );
    blurCoord1s[11] = inputTextureCoordinate + vec2( 4.0 * mul_x, 4.0 * mul_y );
    blurCoord1s[12] = inputTextureCoordinate + vec2( 6.0 * mul_x, 0.0 * mul_y );
    blurCoord1s[13] = inputTextureCoordinate + vec2( 4.0 * mul_x, -4.0 * mul_y );
}