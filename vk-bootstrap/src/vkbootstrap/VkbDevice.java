package vkbootstrap;

import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class VkbDevice {
    public final VkDevice[] device = new VkDevice[1];//VK_NULL_HANDLE;
    public VkbPhysicalDevice physical_device;
    public /*VkSurfaceKHR*/long surface = VK_NULL_HANDLE;
    public final List<VkQueueFamilyProperties> queue_families = new ArrayList<>();
    public final VkAllocationCallbacks[] allocation_callbacks = new VkAllocationCallbacks[1];//VK_NULL_HANDLE;
}
