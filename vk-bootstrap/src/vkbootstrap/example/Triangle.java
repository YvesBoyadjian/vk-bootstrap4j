package vkbootstrap.example;

import org.lwjgl.BufferUtils;
import org.lwjgl.vulkan.*;
import port.Port;
import tests.Common;
import vkbootstrap.*;

import javax.management.RuntimeErrorException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class Triangle {

    static final String EXAMPLE_BUILD_DIRECTORY = "vk-bootstrap/src/vkbootstrap/example/shaders";

    public static void main(String[] args) {
        final Init init = new Init();
        final RenderData render_data = new RenderData();

        if (0 != device_initialization (init)) return;
        if (0 != create_swapchain (init)) return;
        if (0 != get_queues (init, render_data)) return;
        if (0 != create_render_pass (init, render_data)) return;
        if (0 != create_graphics_pipeline (init, render_data)) return;
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

    static ByteBuffer readFile (final String filename) {
        //std::ifstream file (filename, std::ios::ate | std::ios::binary);

        ByteBuffer buffer;
        try {
            FileInputStream fis = new FileInputStream(filename);
            FileChannel fc = fis.getChannel();
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            fc.close();
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException("failed to open file!");
        }
        return buffer;
    }

    static /*VkShaderModule*/long createShaderModule (final Init init, final ByteBuffer code) {
        final VkShaderModuleCreateInfo create_info = VkShaderModuleCreateInfo.create();
        create_info.sType( VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
        //create_info.codeSize( code.size ()); java port
        create_info.pCode( code );

        final /*VkShaderModule*/long[] shaderModule = new long[1];
        if (init.arrow_operator().vkCreateShaderModule.invoke(init.device.device[0], create_info, null, shaderModule) != VK_SUCCESS) {
            return VK_NULL_HANDLE; // failed to create shader module
        }

        return shaderModule[0];
    }

    static int create_graphics_pipeline (final Init init, final RenderData data) {
        var vert_code = readFile(EXAMPLE_BUILD_DIRECTORY + "/vert.spv");
        var frag_code = readFile(EXAMPLE_BUILD_DIRECTORY + "/frag.spv");

        /*VkShaderModule*/long vert_module = createShaderModule (init, vert_code);
        /*VkShaderModule*/long frag_module = createShaderModule (init, frag_code);
        if (vert_module == VK_NULL_HANDLE || frag_module == VK_NULL_HANDLE) {
            System.out.println( "failed to create shader module");
            return -1; // failed to create shader modules
        }

        final VkPipelineShaderStageCreateInfo.Buffer shader_stages = VkPipelineShaderStageCreateInfo.create(2); // java port

        final VkPipelineShaderStageCreateInfo vert_stage_info = shader_stages.get(0); // java port
        vert_stage_info.sType( VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        vert_stage_info.stage( VK_SHADER_STAGE_VERTEX_BIT);
        vert_stage_info.module( vert_module);
        vert_stage_info.pName( memUTF8("main"));

        final VkPipelineShaderStageCreateInfo frag_stage_info = shader_stages.get(1); // java port
        frag_stage_info.sType( VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        frag_stage_info.stage( VK_SHADER_STAGE_FRAGMENT_BIT);
        frag_stage_info.module( frag_module);
        frag_stage_info.pName( memUTF8("main"));

        //VkPipelineShaderStageCreateInfo shader_stages[] = { vert_stage_info, frag_stage_info }; java port

        final VkPipelineVertexInputStateCreateInfo vertex_input_info = VkPipelineVertexInputStateCreateInfo.create();
        vertex_input_info.sType( VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        //vertex_input_info.vertexBindingDescriptionCount( 0); java port
        VkVertexInputBindingDescription.Buffer dummy1 = VkVertexInputBindingDescription.create(0); // java port
        vertex_input_info.pVertexBindingDescriptions( dummy1); // java port
        //vertex_input_info.vertexAttributeDescriptionCount( 0); java port
        VkVertexInputAttributeDescription.Buffer dummy2 = VkVertexInputAttributeDescription.create(0); // java port
        vertex_input_info.pVertexAttributeDescriptions( dummy2); // java port

        final VkPipelineInputAssemblyStateCreateInfo input_assembly = VkPipelineInputAssemblyStateCreateInfo.create();
        input_assembly.sType( VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        input_assembly.topology( VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
        input_assembly.primitiveRestartEnable( VK_FALSE != 0);

        final VkViewport.Buffer viewport_buf = VkViewport.create(1);
        final VkViewport viewport = viewport_buf.get(0);
        viewport.x( 0.0f);
        viewport.y( 0.0f);
        viewport.width( (float)init.swapchain.extent.width());
        viewport.height( (float)init.swapchain.extent.height());
        viewport.minDepth( 0.0f);
        viewport.maxDepth( 1.0f);

        final VkRect2D.Buffer scissor_buf = VkRect2D.create(1);
        final VkRect2D scissor = scissor_buf.get(0);
        final VkOffset2D dummy = VkOffset2D.create(); dummy.x(0); dummy.y(0);
        scissor.offset( dummy);
        scissor.extent( init.swapchain.extent);

        final VkPipelineViewportStateCreateInfo viewport_state = VkPipelineViewportStateCreateInfo.create();
        viewport_state.sType( VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
        viewport_state.viewportCount( 1);
        viewport_state.pViewports( viewport_buf);
        viewport_state.scissorCount( 1);
        viewport_state.pScissors( scissor_buf);

        final VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.create();
        rasterizer.sType( VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
        rasterizer.depthClampEnable( VK_FALSE != 0);
        rasterizer.rasterizerDiscardEnable( VK_FALSE != 0);
        rasterizer.polygonMode( VK_POLYGON_MODE_FILL);
        rasterizer.lineWidth( 1.0f);
        rasterizer.cullMode( VK_CULL_MODE_BACK_BIT);
        rasterizer.frontFace( VK_FRONT_FACE_CLOCKWISE);
        rasterizer.depthBiasEnable( VK_FALSE != 0);

        final VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.create();
        multisampling.sType( VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
        multisampling.sampleShadingEnable( VK_FALSE != 0);
        multisampling.rasterizationSamples( VK_SAMPLE_COUNT_1_BIT);

        final VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment_buf = VkPipelineColorBlendAttachmentState.create(1);
        final VkPipelineColorBlendAttachmentState colorBlendAttachment = colorBlendAttachment_buf.get(0);
        colorBlendAttachment.colorWriteMask( VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT |
                VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
        colorBlendAttachment.blendEnable( VK_FALSE != 0);

        final VkPipelineColorBlendStateCreateInfo color_blending = VkPipelineColorBlendStateCreateInfo.create();
        color_blending.sType( VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
        color_blending.logicOpEnable( VK_FALSE != 0);
        color_blending.logicOp( VK_LOGIC_OP_COPY);
        //color_blending.attachmentCount( 1); java port
        color_blending.pAttachments( colorBlendAttachment_buf);
        color_blending.blendConstants(0, 0.0f);
        color_blending.blendConstants(1, 0.0f);
        color_blending.blendConstants(2, 0.0f);
        color_blending.blendConstants(3, 0.0f);

        final VkPipelineLayoutCreateInfo pipeline_layout_info = VkPipelineLayoutCreateInfo.create();
        pipeline_layout_info.sType( VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
        //pipeline_layout_info.setLayoutCount( 0); java port
        pipeline_layout_info.pSetLayouts(BufferUtils.createLongBuffer(0)); // java port
        //pipeline_layout_info.pushConstantRangeCount( 0); java port
        VkPushConstantRange.Buffer dummy3 = VkPushConstantRange.create(0); // java port
        pipeline_layout_info.pPushConstantRanges( dummy3); // java port

        if (init.arrow_operator().vkCreatePipelineLayout.invoke (
                init.device.device[0], pipeline_layout_info, null, data.pipeline_layout) != VK_SUCCESS) {
            System.out.println( "failed to create pipeline layout");
            return -1; // failed to create pipeline layout
        }

        final int[] dynamic_states = { VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR };

        final VkPipelineDynamicStateCreateInfo dynamic_info = VkPipelineDynamicStateCreateInfo.create();
        dynamic_info.sType( VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO);
        //dynamic_info.dynamicStateCount( dynamic_states.length); java port
        dynamic_info.pDynamicStates( Port.toIntBuffer(dynamic_states));

        final VkGraphicsPipelineCreateInfo.Buffer pipeline_info_buf = VkGraphicsPipelineCreateInfo.create(1);
        final VkGraphicsPipelineCreateInfo pipeline_info = pipeline_info_buf.get(0);
        pipeline_info.sType( VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
        //pipeline_info.stageCount( 2); java port
        pipeline_info.pStages( shader_stages);
        pipeline_info.pVertexInputState( vertex_input_info);
        pipeline_info.pInputAssemblyState( input_assembly);
        pipeline_info.pViewportState( viewport_state);
        pipeline_info.pRasterizationState( rasterizer);
        pipeline_info.pMultisampleState( multisampling);
        pipeline_info.pColorBlendState( color_blending);
        pipeline_info.pDynamicState( dynamic_info);
        pipeline_info.layout( data.pipeline_layout[0]);
        pipeline_info.renderPass( data.render_pass[0]);
        pipeline_info.subpass( 0);
        pipeline_info.basePipelineHandle( VK_NULL_HANDLE);

        if (init.arrow_operator().vkCreateGraphicsPipelines.invoke (
                init.device.device[0], VK_NULL_HANDLE,/* 1,*/ pipeline_info_buf, null, data.graphics_pipeline) != VK_SUCCESS) {
            System.out.println( "failed to create pipline");
            return -1; // failed to create graphics pipeline
        }

        init.arrow_operator().vkDestroyShaderModule.invoke (init.device.device[0], frag_module, null);
        init.arrow_operator().vkDestroyShaderModule.invoke (init.device.device[0], vert_module, null);
        return 0;
    }
}
