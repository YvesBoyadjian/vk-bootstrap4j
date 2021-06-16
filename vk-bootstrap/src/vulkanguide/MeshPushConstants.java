package vulkanguide;

import org.joml.Vector4f;

public class MeshPushConstants {
    public final Vector4f data = new Vector4f();
    public final Vector4f render_matrix = new Vector4f();

    public static int sizeof() {
        return 2 * 4 * Float.BYTES;
    }
}
