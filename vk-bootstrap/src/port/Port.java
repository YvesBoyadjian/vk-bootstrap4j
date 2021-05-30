package port;

import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.*;

public class Port {

    public static final IntBuffer datai(List<Integer> listOfInt) {

        int nb = listOfInt.size();
        IntBuffer ret = IntBuffer.allocate(nb);
        for( Integer i : listOfInt) {
            ret.put(i);
        }
        ret.flip();
        return ret;
    };

    public static final FloatBuffer dataf(List<Float> listOfFloat) {

        int nb = listOfFloat.size();
        FloatBuffer ret = memAllocFloat(nb);
        for( Float f : listOfFloat) {
            ret.put(f);
        }
        ret.flip();
        return ret;
    };

    public static final PointerBuffer datastr(List<String> listOfString) {

        int nb = listOfString.size();
        PointerBuffer pb = memAllocPointer(nb);
        for( int i=0; i<nb; i++) {
            ByteBuffer bb = memUTF8(listOfString.get(i));
            pb.put(bb);
        }
        pb.flip();

        return pb;
    }
}
