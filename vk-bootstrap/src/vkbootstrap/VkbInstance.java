package vkbootstrap;

import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkInstance;

import static org.lwjgl.vulkan.VK10.VK_MAKE_VERSION;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class VkbInstance {
    public final VkInstance[] instance = new VkInstance[1];//VK_NULL_HANDLE;
    public /*VkDebugUtilsMessengerEXT*/final long[] debug_messenger = new long[1];//VK_NULL_HANDLE;
    public VkAllocationCallbacks allocation_callbacks = null;//VK_NULL_HANDLE;

    VkbVulkanFunctions.PFN_vkGetInstanceProcAddr fp_vkGetInstanceProcAddr = null;
    VkbVulkanFunctions.PFN_vkGetDeviceProcAddr fp_vkGetDeviceProcAddr = null;

    // The apiVersion used to create the instance
    int instance_version = VK_MAKE_VERSION(1, 0, 0); // package access

    // The instance version queried from vkEnumerateInstanceVersion
    int api_version = VK_MAKE_VERSION(1, 0, 0);

    // A conversion function which allows this Instance to be used
    // in places where VkInstance would have been used.
    public VkInstance VkInstance() {
    		return instance[0];
    };

    // Return a loaded instance dispatch table
    public VkbInstanceDispatchTable make_table()  {
    		return new VkbInstanceDispatchTable(instance[0], fp_vkGetInstanceProcAddr);
    };

    boolean headless = false; // package access
    boolean properties2_ext_enabled = false;
    
    /**
     * Copy operator
     * @param other
     */
    public void copyFrom(VkbInstance other) {
    		instance[0] = other.instance[0];
    		debug_messenger[0] = other.debug_messenger[0];
    		allocation_callbacks = other.allocation_callbacks;
    		fp_vkGetInstanceProcAddr = other.fp_vkGetInstanceProcAddr;
    		fp_vkGetDeviceProcAddr = other.fp_vkGetDeviceProcAddr;
    		
    		instance_version = other.instance_version;
    		
    		api_version = other.api_version;
    		
    		headless = other.headless;
    		properties2_ext_enabled = other.properties2_ext_enabled;
    }
}
