package vkbootstrap;

import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import port.error_code;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static vkbootstrap.VkBootstrap.vulkan_functions;

public class VkbSwapchain {
    public VkDevice device = null;//VK_NULL_HANDLE;
    public /*VkSwapchainKHR*/final long[] swapchain = new long[1];//VK_NULL_HANDLE;
    public int image_count = 0;
    public /*VkFormat*/int image_format = VK_FORMAT_UNDEFINED;
    public VkExtent2D extent = VkExtent2D.create();
    VkAllocationCallbacks allocation_callbacks = null;//VK_NULL_HANDLE;

    /*1741*/ Result<List</*VkImage*/Long>> get_images() {
        final List</*VkImage*/Long> swapchain_images = new ArrayList<>();

        var swapchain_images_ret = VkBootstrap.get_vector/*VkImage>*/(
        swapchain_images, vulkan_functions().fp_vkGetSwapchainImagesKHR, device, swapchain[0]);
        if (swapchain_images_ret != VK_SUCCESS) {
            return new Result(new Error( new error_code(VkbSwapchainError.failed_get_swapchain_images.ordinal()), swapchain_images_ret ));
        }
        return new Result(swapchain_images);
    }
}
