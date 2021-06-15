package vulkanguide;

import org.joml.Matrix4f;

public class GPUObjectData {
    public final Matrix4f modelMatrix = new Matrix4f();

    public static int sizeof() {
        return 4 * 4 * Float.BYTES;
    }
}
