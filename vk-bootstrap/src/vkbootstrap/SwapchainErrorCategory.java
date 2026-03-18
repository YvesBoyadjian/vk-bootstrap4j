/**
 * 
 */
package vkbootstrap;

import static vkbootstrap.VkBootstrap.to_string;

import port.error_category;

/**
 * 
 */
public class SwapchainErrorCategory extends error_category {

	@Override
	public String name() {
		return "vbk_swapchain";
	}

	@Override
	public String message(int err) {
        return VkBootstrap.to_string(VkbSwapchainError.values()[err]);
	}

}
