package vkbootstrap;

import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.VkBaseOutStructure;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.*;

public class VkbGenericFeaturesPNextNode extends Struct implements NativeResource {

    /** The struct size in bytes. */
    public static final int SIZEOF;

    public static final int
            STYPE,
            PNEXT;

    static {
        Layout layout = __struct(
                __member(4),
                __member(POINTER_SIZE),
                __member(4),
                __array(4,256)
        );

        SIZEOF = layout.getSize();

        STYPE = layout.offsetof(0);
        PNEXT = layout.offsetof(1);
    }

    /**
     * Creates a {@code VkBaseOutStructure} instance at the current position of the specified {@link ByteBuffer} container. Changes to the buffer's content will be
     * visible to the struct instance and vice versa.
     *
     * <p>The created instance holds a strong reference to the container object.</p>
     */
    public VkbGenericFeaturesPNextNode(ByteBuffer container) {
        super(memAddress(container), __checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public VkbGenericFeaturesPNextNode pNext(VkbGenericFeaturesPNextNode value) { npNext(address(), value); return this; }

    public static void npNext(long struct, VkbGenericFeaturesPNextNode value) {
        memPutAddress(struct + VkbGenericFeaturesPNextNode.PNEXT, memAddressSafe(value));
    }
}
