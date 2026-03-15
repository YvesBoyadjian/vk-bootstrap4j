package port;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.VkBaseOutStructure;
import org.lwjgl.vulkan.VkPhysicalDevice16BitStorageFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;

import sun.misc.Unsafe;
import vulkanguide.Vertex;

import java.nio.*;
import java.util.List;
import java.util.Vector;

import static org.lwjgl.system.MemoryUtil.*;

public class Port {

    public static final int UINT32_MAX = (int)0xffffffffl;

    public static final long UINT64_MAX = 0xffffffffffffffffl;

    public static final sun.misc.Unsafe UNSAFE = getUnsafeInstance();

    public static final IntBuffer datai(List<Integer> listOfInt) {

        int nb = listOfInt.size();
        IntBuffer ret = BufferUtils.createIntBuffer(nb);
        for( Integer i : listOfInt) {
            ret.put(i);
        }
        ret.flip();
        return ret;
    };

    public static final IntBuffer toIntBuffer(int[] arrayOfInt) {

        int nb = arrayOfInt.length;
        IntBuffer ret = BufferUtils.createIntBuffer(nb);
        for( Integer i : arrayOfInt) {
            ret.put(i);
        }
        ret.flip();
        return ret;
    };

    public static final LongBuffer toLongBuffer(long[] arrayOfInt) {

        int nb = arrayOfInt.length;
        LongBuffer ret = BufferUtils.createLongBuffer(nb);
        for( Long i : arrayOfInt) {
            ret.put(i);
        }
        ret.flip();
        return ret;
    };

    public static final FloatBuffer dataf(List<Float> listOfFloat) {

        int nb = listOfFloat.size();
        FloatBuffer ret = BufferUtils.createFloatBuffer(nb);
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

    public static final Buffer data(List<Vertex> vectorList) {

        int nb = vectorList.size();
        FloatBuffer ret = BufferUtils.createFloatBuffer(nb*Vertex.sizeof()/Float.BYTES);
        for( var vertex : vectorList) {
            ret.put(vertex.position.x());
            ret.put(vertex.position.y());
            ret.put(vertex.position.z());
            ret.put(vertex.normal.x());
            ret.put(vertex.normal.y());
            ret.put(vertex.normal.z());
            ret.put(vertex.color.x());
            ret.put(vertex.color.y());
            ret.put(vertex.color.z());
            ret.put(vertex.uv.x());
            ret.put(vertex.uv.y());
        }

        ret.flip();

        return ret;
    }

    private static sun.misc.Unsafe getUnsafeInstance() {
        java.lang.reflect.Field[] fields = sun.misc.Unsafe.class.getDeclaredFields();

        /*
        Different runtimes use different names for the Unsafe singleton,
        so we cannot use .getDeclaredField and we scan instead. For example:

        Oracle: theUnsafe
        PERC : m_unsafe_instance
        Android: THE_ONE
        */
        for (java.lang.reflect.Field field : fields) {
            if (!field.getType().equals(sun.misc.Unsafe.class)) {
                continue;
            }

            int modifiers = field.getModifiers();
            if (!(java.lang.reflect.Modifier.isStatic(modifiers) && java.lang.reflect.Modifier.isFinal(modifiers))) {
                continue;
            }

            try {
                field.setAccessible(true);
                return (sun.misc.Unsafe)field.get(null);
            } catch (Exception ignored) {
            }
            break;
        }

        throw new UnsupportedOperationException("LWJGL requires sun.misc.Unsafe to be available.");
    }

	public static void memcpy(List<Byte> _Dst, int starting_location, Struct<?> _Src, int _Size) {
		long address = _Src.address();
		Unsafe unsafe = Unsafe.getUnsafe();
		
		for (int i=0; i < _Size; i++) {
			_Dst.set(i + starting_location, (byte)unsafe.getByte(address + i));
		}
		
	}

	public static String toString(ByteBuffer buffer) {
		int size = buffer.limit();
		StringBuilder builder = new StringBuilder();
		for ( int i=0; i < size; i++) {
			byte b = buffer.get();
			if (b == 0) {
				break;
			}
			builder.append((char)b);
		}
		return builder.toString();
	}

	public static String to_string(int code) {
		return Integer.toString(code);
	}

	public static boolean binary_search(List<String> collection, String value) {
		return collection.contains(value);
	}

	public static long toLong(List<Byte> bytes) {
		int sizeBytes = bytes.size();
		Unsafe unsafe = Unsafe.getUnsafe();
		long address = unsafe.allocateMemory(sizeBytes);
		for (int i=0; i< sizeBytes; i++) {
			unsafe.putByte(address, bytes.get(i));
		}
		return address;
	}

	public static int toInt(List<Byte> buffer, int starting_location) {
		byte[] array = new byte[buffer.size()];
		for (int i=0; i < buffer.size(); i++) {
			array[i] = buffer.get(i);
		}
		int intValue = ByteBuffer.allocate(buffer.size()).put(array).getInt(starting_location);
		return intValue;
	}

	public static int sType(Struct<?> struct) {
		if (struct instanceof VkPhysicalDeviceVulkan11Features) {
			return ((VkPhysicalDeviceVulkan11Features)struct).sType();
		}
		if (struct instanceof VkPhysicalDeviceVulkan12Features) {
			return ((VkPhysicalDeviceVulkan12Features)struct).sType();
		}
		if (struct instanceof VkPhysicalDeviceFeatures2) {
			return ((VkPhysicalDeviceFeatures2)struct).sType();
		}
		throw new RuntimeException();
	}

	public static void memcpy(VkBaseOutStructure structure, Struct<?> struct, int sizeof) {
        int typeValue =  Port.sType(struct);
        structure.sType(typeValue);  
	}

	public static void memcpy(VkBaseOutStructure structure, VkBaseOutStructure struct, int sizeof) {		
		structure.set(struct);
	}

	public static void memcpy(Struct<?> struct, VkBaseOutStructure structure, int sizeof) {
		if (struct instanceof VkPhysicalDeviceVulkan11Features) {
			VkPhysicalDeviceVulkan11Features s1 = (VkPhysicalDeviceVulkan11Features)struct;
			s1.sType(structure.sType());
			s1.pNext(structure.pNext() == null ? 0 : structure.pNext().address());
			return;
		}		
		if (struct instanceof VkPhysicalDeviceVulkan12Features) {
			VkPhysicalDeviceVulkan12Features s1 = (VkPhysicalDeviceVulkan12Features)struct;
			s1.sType(structure.sType());
			s1.pNext(structure.pNext() == null ? 0 : structure.pNext().address());
			return;
		}
		if (struct instanceof VkPhysicalDeviceFeatures2) {
			VkPhysicalDeviceFeatures2 s1 = (VkPhysicalDeviceFeatures2)struct;
			s1.sType(structure.sType());
			s1.pNext(structure.pNext() == null ? 0 : structure.pNext().address());
			return;
		}
		throw new RuntimeException();
	}

	public static Struct<?> copyStruct(Struct<?> struct) {
		if (struct instanceof VkPhysicalDeviceVulkan11Features) {
			VkPhysicalDeviceVulkan11Features s1 = (VkPhysicalDeviceVulkan11Features)struct;
			VkPhysicalDeviceVulkan11Features retVal =  VkPhysicalDeviceVulkan11Features.create();
			retVal.set(s1);
			return retVal;
		}
		if (struct instanceof VkPhysicalDeviceVulkan12Features) {
			VkPhysicalDeviceVulkan12Features s1 = (VkPhysicalDeviceVulkan12Features)struct;
			VkPhysicalDeviceVulkan12Features retVal =  VkPhysicalDeviceVulkan12Features.create();
			retVal.set(s1);
			return retVal;
		}
		throw new RuntimeException();
	}
}
