package vkbootstrap.example;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkQueue;

import java.util.ArrayList;
import java.util.List;

public class RenderData {
    public VkQueue graphics_queue;
    public VkQueue present_queue;

    public final List</*VkImage*/Integer> swapchain_images = new ArrayList<>();
    public final List</*VkImageView*/Integer> swapchain_image_views = new ArrayList<>();
    public final List</*VkFramebuffer*/Integer> framebuffers = new ArrayList<>();

    public /*VkRenderPass*/final long[] render_pass = new long[1];
    public /*VkPipelinLayout*/final long[] pipeline_layout = new long[1];
    public /*VkPipeline*/final long[] graphics_pipeline = new long[1];

    public /*VkCommandPool*/int command_pool;
    public final List<VkCommandBuffer> command_buffers = new ArrayList<>();

    public final List</*VkSemaphore*/Integer> available_semaphores = new ArrayList<>();
    public final List</*VkSemaphore*/Integer> finished_semaphore = new ArrayList<>();
    public final List</*VkFence*/Integer> in_flight_fences = new ArrayList<>();
    public final List</*VkFence*/Integer> image_in_flight = new ArrayList<>();
    public long current_frame = 0;
}
