/**
 * 
 */
package vkbootstrap;

import org.lwjgl.vulkan.VK10;

/**
 * 
 */
public class Version {

	public static final int VKB_VK_API_VERSION_1_0 = VKB_MAKE_VK_VERSION(0, 1, 0, 0);
	public static final int VKB_VK_API_VERSION_1_1 = VKB_MAKE_VK_VERSION(0, 1, 1, 0);

	public static final int VKB_MAKE_VK_VERSION(int variant, int major, int minor, int patch) {
		return VK10.VK_MAKE_API_VERSION(variant, major, minor, patch);
	}
}
