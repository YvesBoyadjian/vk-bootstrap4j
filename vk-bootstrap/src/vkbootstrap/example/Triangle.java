package vkbootstrap.example;

import org.lwjgl.vulkan.*;
import tests.Common;
import vkbootstrap.*;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class Triangle {
    public static void main(String[] args) {
        final Init init = new Init();
        final RenderData render_data = new RenderData();

        if (0 != device_initialization (init)) return;
        if (0 != create_swapchain (init)) return;
        if (0 != get_queues (init, render_data)) return;
        if (0 != create_render_pass (init, render_data)) return;
    }

    static int device_initialization (Init init) {
        init.window = Common.create_window_glfw ("Vulkan Triangle", true);

        final VkbInstanceBuilder instance_builder = new VkbInstanceBuilder();
        var instance_ret = instance_builder.use_default_debug_messenger ().request_validation_layers ().build ();
        if (instance_ret.not()) {
            System.out.println( instance_ret.error ().message () );
            return -1;
        }
        init.instance = instance_ret.value ();

        init.vk_lib.init(init.instance.instance[0]);

        init.surface = Common.create_surface_glfw (init.instance.instance[0], init.window);

        final VkbPhysicalDeviceSelector phys_device_selector = new VkbPhysicalDeviceSelector(init.instance);
        var phys_device_ret = phys_device_selector.set_surface (init.surface).select ();
        if (phys_device_ret.not()) {
            System.out.println( phys_device_ret.error ().message () );
            return -1;
        }
        final VkbPhysicalDevice physical_device = phys_device_ret.value ();

        final VkbDeviceBuilder device_builder = new VkbDeviceBuilder( physical_device );
        var device_ret = device_builder.build ();
        if (device_ret.not()) {
            System.out.println(device_ret.error ().message ());
            return -1;
        }
        init.device = device_ret.value ();
        init.vk_lib.init(init.device.device[0]);

        return 0;
    }

    static int create_swapchain (final Init init) {

        final VkbSwapchainBuilder swapchain_builder = new VkbSwapchainBuilder( init.device );
        var swap_ret = swapchain_builder.set_old_swapchain (init.swapchain).build ();
        if (swap_ret.not()) {
            System.out.println( swap_ret.error().message () + " " + swap_ret.vk_result() );
            return -1;
        }
        VkBootstrap.destroy_swapchain(init.swapchain);
        init.swapchain = swap_ret.value ();
        return 0;
    }

    static int get_queues (final Init init, final RenderData data) {
        var gq = init.device.get_queue (VkbQueueType.graphics);
        if (!gq.has_value ()) {
            System.out.println( "failed to get graphics queue: " + gq.error ().message () );
            return -1;
        }
        data.graphics_queue = gq.value ();

        var pq = init.device.get_queue (VkbQueueType.present);
        if (!pq.has_value ()) {
            System.out.println( "failed to get present queue: " + pq.error ().message () );
            return -1;
        }
        data.present_queue = pq.value ();
        return 0;
    }

    static int create_render_pass (final Init init, final RenderData data) {
        final VkAttachmentDescription.Buffer color_attachment_desc_buf = VkAttachmentDescription.create(1);
        final VkAttachmentDescription color_attachment = color_attachment_desc_buf.get(0);
        color_attachment.format( init.swapchain.image_format);
        color_attachment.samples( VK_SAMPLE_COUNT_1_BIT);
        color_attachment.loadOp( VK_ATTACHMENT_LOAD_OP_CLEAR);
        color_attachment.storeOp( VK_ATTACHMENT_STORE_OP_STORE);
        color_attachment.stencilLoadOp( VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        color_attachment.stencilStoreOp( VK_ATTACHMENT_STORE_OP_DONT_CARE);
        color_attachment.initialLayout( VK_IMAGE_LAYOUT_UNDEFINED);
        color_attachment.finalLayout( VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        final VkAttachmentReference.Buffer color_attachment_buf = VkAttachmentReference.create(1);
        final VkAttachmentReference color_attachment_ref = color_attachment_buf.get(0);
        color_attachment_ref.attachment( 0);
        color_attachment_ref.layout( VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        final VkSubpassDescription.Buffer subpass_buf = VkSubpassDescription.create(1);
        final VkSubpassDescription subpass = subpass_buf.get(0);
        subpass.pipelineBindPoint( VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.colorAttachmentCount( 1);
        subpass.pColorAttachments( color_attachment_buf);

        final VkSubpassDependency.Buffer dependency_buf = VkSubpassDependency.create(1);
        final VkSubpassDependency dependency = dependency_buf.get(0);
        dependency.srcSubpass( VK_SUBPASS_EXTERNAL);
        dependency.dstSubpass( 0);
        dependency.srcStageMask( VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.srcAccessMask( 0);
        dependency.dstStageMask( VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.dstAccessMask( VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        final VkRenderPassCreateInfo render_pass_info = VkRenderPassCreateInfo.create();
        render_pass_info.sType( VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
        //render_pass_info.attachmentCount( 1); java port
        render_pass_info.pAttachments( color_attachment_desc_buf);
        //render_pass_info.subpassCount( 1); java port
        render_pass_info.pSubpasses( subpass_buf);
        //render_pass_info.dependencyCount( 1); java port
        render_pass_info.pDependencies( dependency_buf);

        if (init.arrow_operator().vkCreateRenderPass.invoke (init.device.device[0], render_pass_info, null, data.render_pass) != VK_SUCCESS) {
            System.out.println( "failed to create render pass");
            return -1; // failed to create render pass!
        }
        return 0;
    }
}
