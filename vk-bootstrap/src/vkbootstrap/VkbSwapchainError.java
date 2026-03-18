package vkbootstrap;

public enum VkbSwapchainError {
    surface_handle_not_provided,
    failed_query_surface_support_details,
    failed_create_swapchain,
    failed_get_swapchain_images,
    failed_create_swapchain_image_views,
    required_min_image_count_too_low,
    required_usage_not_supported
}
