package vulkanguide;

import org.joml.Matrix4f;

public class GPUCameraData {
    public final Matrix4f view = new Matrix4f();
    public final Matrix4f proj = new Matrix4f();
    public final Matrix4f viewproj = new Matrix4f();

    public static int sizeof() {
        return 3 * 4 * 4 * Float.BYTES;
    }
}
