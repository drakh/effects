public class Matrix3D {
  float[][] mx; /* [row][col] */
  
  Matrix3D() {
    mx = new float[4][];
    for (int i = 0; i < 4; i++)
      mx[i] = new float[4];

    reset();
  }

  void reset() {
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        mx[i][j] = (i == j) ? 1.0f : 0.0f;
  }

  public String toString() {
    String row[] = new String[4];
    
    for (int i = 0; i < 4; i++)
      row[i] = String.format("%+12f, %+12f, %+12f, %+12f", mx[i][0], mx[i][1], mx[i][2], mx[i][3]);
    
    return String.format("[ %s\n  %s\n  %s\n  %s ]", row[0], row[1], row[2], row[3]);
  }
    
  static Matrix3D translation(float x, float y, float z) {
    Matrix3D d = new Matrix3D();
    d.mx[0][3] = x;
    d.mx[1][3] = y;
    d.mx[2][3] = z;   
    return d;
  }

  static Matrix3D scaling(float scaleX, float scaleY, float scaleZ) {
    Matrix3D d = new Matrix3D();
    d.mx[0][0] = scaleX;
    d.mx[1][1] = scaleY;
    d.mx[2][2] = scaleZ;
    return d;
  }

  static Matrix3D rotation(float angleX, float angleY, float angleZ) {
    angleX = (float)Math.toRadians(angleX);
    angleY = (float)Math.toRadians(angleY);
    angleZ = (float)Math.toRadians(angleZ);

    float sinX = (float)Math.sin(angleX);
    float cosX = (float)Math.cos(angleX);
    float sinY = (float)Math.sin(angleY);
    float cosY = (float)Math.cos(angleY);
    float sinZ = (float)Math.sin(angleZ);
    float cosZ = (float)Math.cos(angleZ);

    Matrix3D d = new Matrix3D();

    d.mx[0][0] = cosY * cosZ;
    d.mx[0][1] = cosY * sinZ;
    d.mx[0][2] = -sinY;
    d.mx[1][0] = sinX * sinY * cosZ - cosX * sinZ;
    d.mx[1][1] = sinX * sinY * sinZ + cosX * cosZ;
    d.mx[1][2] = sinX * cosY;
    d.mx[2][0] = cosX * sinY * cosZ + sinX * sinZ;
    d.mx[2][1] = cosX * sinY * sinZ - sinX * cosZ;
    d.mx[2][2] = cosX * cosY;
    
    return d;
  }
  
  static Matrix3D frustum(float left, float right,
                          float top, float bottom,
                          float near, float far)
  {
    Matrix3D d = new Matrix3D();
    
    assert near > 0 && far > 0;
  
    d.mx[0][0] = 2 * near / (right - left);
    d.mx[0][2] = (right + left) / (right - left);
    d.mx[1][1] = 2 * near / (top - bottom);
    d.mx[1][2] = (top + bottom) / (top - bottom);
    d.mx[2][2] = - (far + near) / (far - near);
    d.mx[2][3] = -2 * far * near / (far - near);
    d.mx[3][2] = -1;
    d.mx[3][3] = 0;
    
    return d;
  }
  
  static Matrix3D perspective(float fovy, float aspect, float near, float far) {
    float fH = (float)Math.tan(Math.toRadians(fovy / 2)) * near;
    float fW = fH * aspect;

    return frustum(-fW, fW, -fH, fH, near, far);
  }

  static Matrix3D cameraFromVectors(Vector3D direction, Vector3D position) {
    Vector3D d, u, r;

    // unit vector pointing to focal point
    d = Vector3D.normalize(direction, 1.0f);

    // unit vector pointed upwards
    u = Vector3D.scale(d, Vector3D.dot(position, d));
    u = Vector3D.sub(position, u);
    u.normalize(1.0f);

    // unit vector pointer to the right 
    r = Vector3D.cross(u, d);

    Matrix3D camera = new Matrix3D();
    
    camera.mx[0][0] = r.x;
    camera.mx[1][0] = r.y;
    camera.mx[2][0] = r.z;
    camera.mx[3][0] = position.x;
    camera.mx[0][1] = u.x;
    camera.mx[1][1] = u.y;
    camera.mx[2][1] = u.z;
    camera.mx[3][1] = position.y;
    camera.mx[0][2] = d.x;
    camera.mx[1][2] = d.y;
    camera.mx[2][2] = d.z;
    camera.mx[3][2] = position.z;
    
    return camera;
  }

  static Matrix3D cameraFromAngles(
      float azimuth, float elevation, Vector3D position)
  {
    Vector3D direction = new Vector3D(
        (float) (Math.sin(elevation) * Math.cos(azimuth)),
        (float) (Math.sin(elevation) * Math.sin(azimuth)),
        (float) (Math.cos(elevation)));

    return cameraFromVectors(direction, position);
  }

  /* http://3dgep.com/understanding-the-view-matrix/#Look_At_Camera */
  static Matrix3D cameraLookAt(Vector3D eye, Vector3D target, Vector3D up) {
    Vector3D zaxis = Vector3D.normalize(Vector3D.sub(eye, target), 1.0f);
    Vector3D xaxis = Vector3D.normalize(Vector3D.cross(up, zaxis), 1.0f);
    Vector3D yaxis = Vector3D.cross(zaxis, xaxis);

    Matrix3D camera = new Matrix3D();

    camera.mx[0][0] = xaxis.x;
    camera.mx[0][1] = yaxis.x;
    camera.mx[0][2] = zaxis.x;
    camera.mx[1][0] = xaxis.y;
    camera.mx[1][1] = yaxis.y;
    camera.mx[1][2] = zaxis.y;
    camera.mx[2][0] = xaxis.z;
    camera.mx[2][1] = yaxis.z;
    camera.mx[2][2] = zaxis.z;
    camera.mx[3][0] = -Vector3D.dot(xaxis, eye);
    camera.mx[3][1] = -Vector3D.dot(yaxis, eye);
    camera.mx[3][2] = -Vector3D.dot(zaxis, eye);

    return camera;
  }
  
  static Matrix3D mult(Matrix3D a, Matrix3D b) {
    Matrix3D d = new Matrix3D();
    
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        d.mx[i][j] = 0.0f;

        for (int k = 0; k < 4; k++)
          d.mx[i][j] += b.mx[i][k] * a.mx[k][j];
      }
    }
    
    return d;
  }

  void transform(Vector3D dst, Vector3D src) {
    dst.x = mx[0][0] * src.x + mx[0][1] * src.y + mx[0][2] * src.z + mx[0][3];
    dst.y = mx[1][0] * src.x + mx[1][1] * src.y + mx[1][2] * src.z + mx[1][3];
    dst.z = mx[2][0] * src.x + mx[2][1] * src.y + mx[2][2] * src.z + mx[2][3];
    dst.w = mx[3][0] * src.x + mx[3][1] * src.y + mx[3][2] * src.z + mx[3][3];
  }
};
