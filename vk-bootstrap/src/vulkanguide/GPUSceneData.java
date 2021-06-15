package vulkanguide;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class GPUSceneData {
    public final Vector4f fogColor = new Vector4f(); // w is for exponent
    public final Vector4f fogDistances = new Vector4f(); //x for min, y for max, zw unused.
    public final Vector4f ambientColor = new Vector4f();
    public final Vector4f sunlightDirection = new Vector4f(); //w for sun power
    public final Vector4f sunlightColor = new Vector4f();

    public static int sizeof() {
        return 5 * 4 * Float.BYTES;
    }
}
