/**
 * 
 */
package vkbootstrap;

import org.lwjgl.vulkan.VkDevice;

/**
 * 
 */
public class VkbDispatchTable {
	
    public VkDevice device = /*VK_NULL_HANDLE*/null;
    private boolean populated = false;

    public VkbDispatchTable(VkDevice device, VkbVulkanFunctions.PFN_vkGetDeviceProcAddr procAddr) {
    this.device = device;
    this.populated = true;
    
    //TODO
    }

    public VkbDispatchTable() {
	}

	public boolean is_populated() { return populated; }

	public void copyFrom(VkbDispatchTable other) {
		device = other.device;
		populated = other.populated;
	}
}
