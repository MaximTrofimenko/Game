#version 120

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float time;
uniform float px;
uniform float py;

void main() {
  gl_FragColor = texture2D(u_texture, v_texCoords);
  float xx = px + v_texCoords.x;
  float yy = py + v_texCoords.y;
  gl_FragColor.rgb += 0.08 * sin(xx * 3 + time / 2 + yy * 2) + 0.08 * sin(-xx * 4 - time / 2 + yy * 5);
}
