package vkbootstrap;

import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_MAKE_VERSION;

public class VkbPhysicalDevice {
	
	public enum Suitable {
		yes, partial, no
	}
	
    public String name;
	
    public VkPhysicalDevice physical_device = null;//VK_NULL_HANDLE;
    public /*VkSurfaceKHR*/long surface = 0;//VK_NULL_HANDLE;

    // Note that this reflects selected features carried over from required features, not all features the physical device supports.
    public final VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.create();
    public VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.create(); // Immutable
    public VkPhysicalDeviceMemoryProperties memory_properties = VkPhysicalDeviceMemoryProperties.create(); // Immutable

    public int instance_version = VK_MAKE_VERSION(1, 0, 0);
    public final List<String> extensions_to_enable = new ArrayList<>();
    public final List<String> available_extensions = new ArrayList<>();
    /*372*/ final List<VkQueueFamilyProperties> queue_families = new ArrayList<>();
    public final /*List<VkbGenericFeaturesPNextNode>*/VkbFeaturesChain extended_features_chain = new VkbFeaturesChain();
    
    public boolean defer_surface_initialization = false;
    public boolean properties2_ext_enabled = false;
    public Suitable suitable = Suitable.yes;
    
    /**
     * Copy operator
     * @param other
     */
	public void copyFrom(VkbPhysicalDevice other) {
		name = other.name;
		physical_device = other.physical_device;
		surface = other.surface;
		
		features.set(other.features);
		properties = other.properties;
		memory_properties = other.memory_properties;
		
		instance_version = other.instance_version;
		extensions_to_enable.clear(); extensions_to_enable.addAll(other.extensions_to_enable);
		available_extensions.clear(); available_extensions.addAll(other.available_extensions);
		queue_families.clear(); queue_families.addAll(other.queue_families);
		extended_features_chain.copyFrom(other.extended_features_chain);
		
		defer_surface_initialization = other.defer_surface_initialization;
		properties2_ext_enabled = other.properties2_ext_enabled;
		suitable = other.suitable;
	}
}
