package vkbootstrap.example;

import tests.VulkanLibrary;
import vkbootstrap.VkbDevice;
import vkbootstrap.VkbDispatchTable;
import vkbootstrap.VkbInstance;
import vkbootstrap.VkbInstanceDispatchTable;
import vkbootstrap.VkbSwapchain;

public class Init {
    public /*GLFWwindow*/long window;
    public final VulkanLibrary vk_lib = new VulkanLibrary();
    public final VkbInstance instance = new VkbInstance();
    public final VkbInstanceDispatchTable inst_disp = new VkbInstanceDispatchTable();
    public /*VkSurfaceKHR*/long surface;
    public final VkbDevice device = new VkbDevice();
    	public final VkbDispatchTable disp = new VkbDispatchTable();
    public final VkbSwapchain swapchain = new VkbSwapchain();

    public VulkanLibrary arrow_operator() {
        return vk_lib;
    }
}
