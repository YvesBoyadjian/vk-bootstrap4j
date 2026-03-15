/**
 * 
 */
package vkbootstrap;

import java.util.List;

import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;

/**
 * 
 */
public class VkBootstrapFeatureChain {

	public static void compare_VkPhysicalDeviceFeatures(List<String> error_list,
			VkPhysicalDeviceFeatures supported, VkPhysicalDeviceFeatures requested) {
		// TODO Auto-generated method stub
		
	}

	public static void compare_feature_struct(/*VkStructureType*/int sType, final List<String> error_list, Struct<?> supported, Struct<?> requested) {
		// TODO
	}

	public static void merge_feature_struct(int sType, List<Struct<?>> structures, int starting_location,
			Struct<?> structure) {
		// TODO Auto-generated method stub
		
	}
}
