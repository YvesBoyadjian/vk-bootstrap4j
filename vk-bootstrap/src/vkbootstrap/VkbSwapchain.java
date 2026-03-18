package vkbootstrap;

import org.lwjgl.vulkan.*;
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
    public /*VkColorSpaceKHR*/int color_space = KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR; // The color space actually used when creating the swapchain.
    public /*VkImageUsageFlags*/int image_usage_flags = 0;
    public final VkExtent2D extent = VkExtent2D.create();
    // The value of minImageCount actually used when creating the swapchain; note that the presentation engine is always free to create more images than that.
    public int requested_min_image_count = 0;
    public /*VkPresentModeKHR*/int present_mode = KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR; // The present mode actually used when creating the swapchain.
    public int instance_version = Version.VKB_VK_API_VERSION_1_0;
    VkAllocationCallbacks allocation_callbacks = null;//VK_NULL_HANDLE;
    
    class IT {
    		public final VkbVulkanFunctions.PFN_vkGetSwapchainImagesKHR[] fp_vkGetSwapchainImagesKHR = new VkbVulkanFunctions.PFN_vkGetSwapchainImagesKHR[1];
    		public final VkbVulkanFunctions.PFN_vkCreateImageView[] fp_vkCreateImageView = new VkbVulkanFunctions.PFN_vkCreateImageView[1];
    		public final VkbVulkanFunctions.PFN_vkDestroyImageView[] fp_vkDestroyImageView = new VkbVulkanFunctions.PFN_vkDestroyImageView[1];
    		public final VkbVulkanFunctions.PFN_vkDestroySwapchainKHR[] fp_vkDestroySwapchainKHR = new VkbVulkanFunctions.PFN_vkDestroySwapchainKHR[1];
    		
			public void copyFrom(IT other) {
				fp_vkGetSwapchainImagesKHR[0] = other.fp_vkGetSwapchainImagesKHR[0];
				fp_vkCreateImageView[0] = other.fp_vkCreateImageView[0];
				fp_vkDestroyImageView[0] = other.fp_vkDestroyImageView[0];
				fp_vkDestroySwapchainKHR[0] = other.fp_vkDestroySwapchainKHR[0];
			}
    }
    
    public final IT internal_table = new IT();

    /*1741*/
    public Result<List</*VkImage*/Long>> get_images() {
        final List</*VkImage*/Long> swapchain_images = new ArrayList<>();

        var swapchain_images_ret = VkBootstrap.get_vector/*VkImage>*/(
        swapchain_images, vulkan_functions().fp_vkGetSwapchainImagesKHR, device, swapchain[0]);
        if (swapchain_images_ret != VK_SUCCESS) {
            return new Result(new Error( new error_code(VkbSwapchainError.failed_get_swapchain_images.ordinal()), swapchain_images_ret ));
        }
        return new Result(swapchain_images);
    }

    /*1751*/ public Result<List</*VkImageView*/Long>> get_image_views() {

        var swapchain_images_ret = get_images();
        if (swapchain_images_ret.not()) return new Result<>(swapchain_images_ret.error());
        var swapchain_images = swapchain_images_ret.value();

        final List</*VkImageView*/Long> views = new ArrayList<>(/*swapchain_images.size()*/);

        for (int i = 0; i < swapchain_images.size(); i++) {
            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.create();
            createInfo.sType( VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            createInfo.image( swapchain_images.get(i));
            createInfo.viewType( VK_IMAGE_VIEW_TYPE_2D);
            createInfo.format( image_format);
            createInfo.components().r( VK_COMPONENT_SWIZZLE_IDENTITY);
            createInfo.components().g( VK_COMPONENT_SWIZZLE_IDENTITY);
            createInfo.components().b( VK_COMPONENT_SWIZZLE_IDENTITY);
            createInfo.components().a( VK_COMPONENT_SWIZZLE_IDENTITY);
            createInfo.subresourceRange().aspectMask( VK_IMAGE_ASPECT_COLOR_BIT);
            createInfo.subresourceRange().baseMipLevel( 0);
            createInfo.subresourceRange().levelCount( 1);
            createInfo.subresourceRange().baseArrayLayer( 0);
            createInfo.subresourceRange().layerCount( 1);

            final long[] p_view = new long[1];

            /*VkResult*/int res = vulkan_functions().fp_vkCreateImageView.invoke(
                    device, createInfo, allocation_callbacks, p_view);
            if (res != VK_SUCCESS)
                return new Result(new Error( new error_code(VkbSwapchainError.failed_create_swapchain_image_views.ordinal()), res ));
            views.add(p_view[0]);
        }
        return new Result<>(views);
    }
    /*1782*/ public void destroy_image_views(final List</*VkImageView*/Long> image_views) {
        for (var image_view : image_views) {
            vulkan_functions().fp_vkDestroyImageView.invoke(device, image_view, allocation_callbacks);
        }
    }

    /**
     * Copy operator
     * @param value
     */
	public void copyFrom(VkbSwapchain other) {
		device = other.device;
		swapchain[0] = other.swapchain[0];
		image_count = other.image_count;
		image_format = other.image_format;
		color_space = other.color_space;
		image_usage_flags = other.image_usage_flags;
		extent.width(other.extent.width());
		extent.height(other.extent.height());
		requested_min_image_count = other.requested_min_image_count;
		present_mode = other.present_mode;
		instance_version = other.instance_version;
		allocation_callbacks = other.allocation_callbacks;
		internal_table.copyFrom(other.internal_table);
	}
}
