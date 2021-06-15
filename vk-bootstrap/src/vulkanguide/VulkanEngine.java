package vulkanguide;

import org.lwjgl.PointerBuffer;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.*;
import vkbootstrap.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanEngine {

    public static final/*constexpr unsigned*/ int FRAME_OVERLAP = 2;

    public static final boolean bUseValidationLayers = true;

    public boolean _isInitialized = false;
    public int _frameNumber = 0;
    public int _selectedShader = 0;

    public final VkExtent2D _windowExtent = VkExtent2D.create();

    public/*struct SDL_Window**/long _window = /*nullptr*/0;

    public VkInstance _instance;
    public /*VkDebugUtilsMessengerEXT*/long _debug_messenger;
    public VkPhysicalDevice _chosenGPU;
    public VkDevice _device;

    public final VkPhysicalDeviceProperties _gpuProperties = VkPhysicalDeviceProperties.create();

    public final FrameData[] _frames = new FrameData[FRAME_OVERLAP];

    public VkQueue _graphicsQueue;
    public int _graphicsQueueFamily;

    public /*VkRenderPass*/final long[] _renderPass = new long[1];

    public /*VkSurfaceKHR*/final long[] _surface = new long[1];
    public /*VkSwapchainKHR*/long _swapchain;
    public /*VkFormat*/long _swachainImageFormat;

    public List</*VkFramebuffer*/long[]> _framebuffers = new ArrayList<>();
    public List</*VkImage*/Long> _swapchainImages = new ArrayList<>();
    public List</*VkImageView*/Long> _swapchainImageViews = new ArrayList<>();

    public final DeletionQueue _mainDeletionQueue = new DeletionQueue();

    public /*VmaAllocator*/long _allocator; //vma lib allocator

    //depth resources
    public /*VkImageView*/final long[] _depthImageView = new long[1];
    public final AllocatedImage _depthImage = new AllocatedImage();

    //the format for the depth image
    public /*VkFormat*/long _depthFormat;

    public /*VkDescriptorPool*/final long[] _descriptorPool = new long[1];

    public /*VkDescriptorSetLayout*/final long[] _globalSetLayout = new long[1];
    public /*VkDescriptorSetLayout*/final long[] _objectSetLayout = new long[1];
    public /*VkDescriptorSetLayout*/final long[] _singleTextureSetLayout = new long[1];

    public final GPUSceneData _sceneParameters = new GPUSceneData();
    public AllocatedBuffer _sceneParameterBuffer = new AllocatedBuffer();

    public final UploadContext _uploadContext = new UploadContext();

    public VulkanEngine() {
        _windowExtent.width(1700);
        _windowExtent.height(900);

        for( int i = 0; i< FRAME_OVERLAP; i++) {
            _frames[i] = new FrameData();
        }
    }

    public void VK_CHECK(int err) {
        if (err != 0)
        {
            System.out.println("Detected Vulkan error: " + err );
            System.exit(-1);
        }
    }

    /*37*/ public void init()
    {
        // We initialize SDL and create a window with it.
        //SDL_Init(SDL_INIT_VIDEO);
        glfwInit();

        //SDL_WindowFlags window_flags = (SDL_WindowFlags)(SDL_WINDOW_VULKAN);
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        _window = /*SDL_CreateWindow(
                "Vulkan Engine",
                SDL_WINDOWPOS_UNDEFINED,
                SDL_WINDOWPOS_UNDEFINED,
                _windowExtent.width,
                _windowExtent.height,
                window_flags
        );*/
                glfwCreateWindow(_windowExtent.width(), _windowExtent.height(), "Vulkan Engine", 0, 0);

        init_vulkan();

        init_swapchain();

        init_default_renderpass();

        init_framebuffers();

        init_commands();

        init_sync_structures();

        init_descriptors();
/*
        init_pipelines();

        load_images();

        load_meshes();

        init_scene();

 */
        //everything went fine
        _isInitialized = true;
    }

    /*230*/ public void init_vulkan()
    {
        final VkbInstanceBuilder builder = new VkbInstanceBuilder();

        //make the vulkan instance, with basic debug features
        var inst_ret = builder.set_app_name("Example Vulkan Application")
                .request_validation_layers(bUseValidationLayers)
                .use_default_debug_messenger()
                .require_api_version(1, 1, 0)
                .build();

        VkbInstance vkb_inst = inst_ret.value();

        //grab the instance
        _instance = vkb_inst.instance[0];
        _debug_messenger = vkb_inst.debug_messenger[0];

        //SDL_Vulkan_CreateSurface(_window, _instance, &_surface);
        glfwCreateWindowSurface(_instance, _window, /*allocator*/null, _surface);

        //use vkbootstrap to select a gpu.
        //We want a gpu that can write to the SDL surface and supports vulkan 1.2
        final VkbPhysicalDeviceSelector selector = new VkbPhysicalDeviceSelector( vkb_inst );
        final VkbPhysicalDevice physicalDevice = selector
            .set_minimum_version(1, 1)
            .set_surface(_surface[0])
            .select()
            .value();

        //create the final vulkan device

        final VkbDeviceBuilder deviceBuilder = new VkbDeviceBuilder( physicalDevice );

        final VkbDevice vkbDevice = deviceBuilder.build().value();

        // Get the VkDevice handle used in the rest of a vulkan application
        _device = vkbDevice.device[0];
        _chosenGPU = physicalDevice.physical_device;

        // use vkbootstrap to get a Graphics queue
        _graphicsQueue = vkbDevice.get_queue(VkbQueueType.graphics).value();

        _graphicsQueueFamily = vkbDevice.get_queue_index(VkbQueueType.graphics).value();

        //initialize the memory allocator
        VmaAllocatorCreateInfo allocatorInfo = VmaAllocatorCreateInfo.create();
        allocatorInfo.physicalDevice( _chosenGPU );
        allocatorInfo.device( _device );
        allocatorInfo.instance( _instance );

        VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.create();
        vmaVulkanFunctions.set( _instance, _device );
        allocatorInfo.pVulkanFunctions( vmaVulkanFunctions ); // java port

        PointerBuffer pb = memAllocPointer(1);
        vmaCreateAllocator(allocatorInfo, /*_allocator*/pb);
        _allocator = pb.get(0);
        memFree(pb);

        _mainDeletionQueue.push_function(() -> {
        vmaDestroyAllocator(_allocator);
    });

        vkGetPhysicalDeviceProperties(_chosenGPU, _gpuProperties);

        System.out.println( "The gpu has a minimum buffer alignement of " + _gpuProperties.limits().minUniformBufferOffsetAlignment() );

    }

    /*290*/ public void init_swapchain()
    {
        final VkbSwapchainBuilder swapchainBuilder = new VkbSwapchainBuilder(_chosenGPU,_device,_surface[0] );

        VkbSwapchain vkbSwapchain = swapchainBuilder
            .use_default_format_selection()
            //use vsync present mode
            .set_desired_present_mode(VK_PRESENT_MODE_FIFO_KHR)
            .set_desired_extent(_windowExtent.width(), _windowExtent.height())
            .build()
            .value();

        //store swapchain and its related images
        _swapchain = vkbSwapchain.swapchain[0];
        _swapchainImages = vkbSwapchain.get_images().value();
        _swapchainImageViews = vkbSwapchain.get_image_views().value();

        _swachainImageFormat = vkbSwapchain.image_format;

        _mainDeletionQueue.push_function(() -> {
        vkDestroySwapchainKHR(_device, _swapchain, null);
    });

        //depth image size will match the window
        final VkExtent3D depthImageExtent = VkExtent3D.create();

        depthImageExtent.set(
                _windowExtent.width(),
                _windowExtent.height(),
                1
        );

        //hardcoding the depth format to 32 bit float
        _depthFormat = VK_FORMAT_D32_SFLOAT;

        //the depth image will be a image with the format we selected and Depth Attachment usage flag
        VkImageCreateInfo dimg_info = VkInit.image_create_info(_depthFormat, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, depthImageExtent);

        //for the depth image, we want to allocate it from gpu local memory
        final VmaAllocationCreateInfo dimg_allocinfo = VmaAllocationCreateInfo.create();
        dimg_allocinfo.usage( VMA_MEMORY_USAGE_GPU_ONLY);
        dimg_allocinfo.requiredFlags( /*VkMemoryPropertyFlags*/(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));

        //allocate and create the image
        LongBuffer dummy1 = memAllocLong(1);
        PointerBuffer dummy2 = memAllocPointer(1);

        vmaCreateImage(_allocator, dimg_info, dimg_allocinfo, /*_depthImage._image*/dummy1, /*_depthImage._allocation*/dummy2, null);
        _depthImage._image = dummy1.get(0);
        _depthImage._allocation = dummy2.get(0);
        memFree(dummy1);
        memFree(dummy2);

        //build a image-view for the depth image to use for rendering
        VkImageViewCreateInfo dview_info = VkInit.imageview_create_info(_depthFormat, _depthImage._image, VK_IMAGE_ASPECT_DEPTH_BIT);;

        VK_CHECK(VK10.vkCreateImageView(_device, dview_info, null, _depthImageView));

        //add to deletion queues
        _mainDeletionQueue.push_function(() -> {
        vkDestroyImageView(_device, _depthImageView[0], null);
        vmaDestroyImage(_allocator, _depthImage._image, _depthImage._allocation);
    });
    }

    /*346*/ public void init_default_renderpass()
    {
        //we define an attachment description for our main color image
        //the attachment is loaded as "clear" when renderpass start
        //the attachment is stored when renderpass ends
        //the attachment layout starts as "undefined", and transitions to "Present" so its possible to display it
        //we dont care about stencil, and dont use multisampling

        final VkAttachmentDescription color_attachment = VkAttachmentDescription.create();
        color_attachment.format ( (int)_swachainImageFormat);
        color_attachment.samples ( VK_SAMPLE_COUNT_1_BIT);
        color_attachment.loadOp ( VK_ATTACHMENT_LOAD_OP_CLEAR);
        color_attachment.storeOp ( VK_ATTACHMENT_STORE_OP_STORE);
        color_attachment.stencilLoadOp ( VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        color_attachment.stencilStoreOp ( VK_ATTACHMENT_STORE_OP_DONT_CARE);
        color_attachment.initialLayout ( VK_IMAGE_LAYOUT_UNDEFINED);
        color_attachment.finalLayout ( VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        final VkAttachmentReference.Buffer color_attachment_ref = VkAttachmentReference.create(1);
        color_attachment_ref.attachment ( 0);
        color_attachment_ref.layout ( VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        final VkAttachmentDescription depth_attachment = VkAttachmentDescription.create();
        // Depth attachment
        depth_attachment.flags ( 0);
        depth_attachment.format ( (int)_depthFormat);
        depth_attachment.samples ( VK_SAMPLE_COUNT_1_BIT);
        depth_attachment.loadOp ( VK_ATTACHMENT_LOAD_OP_CLEAR);
        depth_attachment.storeOp ( VK_ATTACHMENT_STORE_OP_STORE);
        depth_attachment.stencilLoadOp ( VK_ATTACHMENT_LOAD_OP_CLEAR);
        depth_attachment.stencilStoreOp ( VK_ATTACHMENT_STORE_OP_DONT_CARE);
        depth_attachment.initialLayout ( VK_IMAGE_LAYOUT_UNDEFINED);
        depth_attachment.finalLayout ( VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        final VkAttachmentReference depth_attachment_ref = VkAttachmentReference.create();
        depth_attachment_ref.attachment ( 1);
        depth_attachment_ref.layout ( VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        //we are going to create 1 subpass, which is the minimum you can do
        final VkSubpassDescription.Buffer subpass = VkSubpassDescription.create(1);
        subpass.pipelineBindPoint ( VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.colorAttachmentCount ( 1);
        subpass.pColorAttachments ( color_attachment_ref);
        //hook the depth attachment into the subpass
        subpass.pDepthStencilAttachment ( depth_attachment_ref);

        //1 dependency, which is from "outside" into the subpass. And we can read or write color
        final VkSubpassDependency.Buffer dependency = VkSubpassDependency.create(1);
        dependency.srcSubpass ( VK_SUBPASS_EXTERNAL);
        dependency.dstSubpass ( 0);
        dependency.srcStageMask ( VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.srcAccessMask ( 0);
        dependency.dstStageMask ( VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.dstAccessMask ( VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);


        //array of 2 attachments, one for the color, and other for depth
        VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.create(2);
        attachments.put(0, color_attachment); attachments.put(1, depth_attachment);

        final VkRenderPassCreateInfo render_pass_info = VkRenderPassCreateInfo.create();
        render_pass_info.sType ( VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
        //2 attachments from said array
        //render_pass_info.attachmentCount ( 2); java port
        render_pass_info.pAttachments ( attachments);
        //render_pass_info.subpassCount ( 1); // java port
        render_pass_info.pSubpasses ( subpass);
        //render_pass_info.dependencyCount ( 1); java port
        render_pass_info.pDependencies ( dependency);

        VK_CHECK(VK10.vkCreateRenderPass(_device, render_pass_info, null, _renderPass));

        _mainDeletionQueue.push_function(() -> {
        vkDestroyRenderPass(_device, _renderPass[0], null);
    });
    }

    /*422*/ public void init_framebuffers()
    {
        //create the framebuffers for the swapchain images. This will connect the render-pass to the images for rendering
        VkFramebufferCreateInfo fb_info = VkInit.framebuffer_create_info(_renderPass[0], _windowExtent);

	int swapchain_imagecount = _swapchainImages.size();
        _framebuffers.clear(); // java port
	for( int i=0; i< swapchain_imagecount;i++) {
        _framebuffers.add(new long[1]); // java port
    }
        for (int i = 0; i < swapchain_imagecount; i++) {

            LongBuffer attachments = memAllocLong(2);
            attachments.put(0, _swapchainImageViews.get(i));
            attachments.put(1, _depthImageView[0]);

            fb_info.pAttachments( attachments);
            //fb_info.attachmentCount ( 2); java port
            VK_CHECK(VK10.vkCreateFramebuffer(_device, fb_info, null, _framebuffers.get(i)));

            final int ii = i;

            _mainDeletionQueue.push_function(() -> {
                vkDestroyFramebuffer(_device, _framebuffers.get(ii)[0], null);
                vkDestroyImageView(_device, _swapchainImageViews.get(ii), null);
            });
        }
    }

    /*447*/ public void init_commands()
    {
        //create a command pool for commands submitted to the graphics queue.
        //we also want the pool to allow for resetting of individual command buffers
        VkCommandPoolCreateInfo commandPoolInfo = VkInit.command_pool_create_info(_graphicsQueueFamily, VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);


        for (int i = 0; i < FRAME_OVERLAP; i++) {


            VK_CHECK(VK10.vkCreateCommandPool(_device, commandPoolInfo, null, _frames[i]._commandPool));

            //allocate the default command buffer that we will use for rendering
            VkCommandBufferAllocateInfo cmdAllocInfo = VkInit.command_buffer_allocate_info(_frames[i]._commandPool[0], 1);

            PointerBuffer dummy = memAllocPointer(1);
            VK_CHECK(vkAllocateCommandBuffers(_device, cmdAllocInfo, /*_frames[i]._mainCommandBuffer)*/dummy));
            _frames[i]._mainCommandBuffer = new VkCommandBuffer(dummy.get(0),_device);

            final int ii = i;

            _mainDeletionQueue.push_function(() -> {
                vkDestroyCommandPool(_device, _frames[ii]._commandPool[0], null);
            });
        }


        VkCommandPoolCreateInfo uploadCommandPoolInfo = VkInit.command_pool_create_info(_graphicsQueueFamily);
        //create pool for upload context
        VK_CHECK(VK10.vkCreateCommandPool(_device, uploadCommandPoolInfo, null, _uploadContext._commandPool));

        _mainDeletionQueue.push_function(() -> {
        vkDestroyCommandPool(_device, _uploadContext._commandPool[0], null);
    });
    }

    /*479*/ public void init_sync_structures()
    {
        //create syncronization structures
        //one fence to control when the gpu has finished rendering the frame,
        //and 2 semaphores to syncronize rendering with swapchain
        //we want the fence to start signalled so we can wait on it on the first frame
        VkFenceCreateInfo fenceCreateInfo = VkInit.fence_create_info(VK_FENCE_CREATE_SIGNALED_BIT);

        VkSemaphoreCreateInfo semaphoreCreateInfo = VkInit.semaphore_create_info();

        for (int i = 0; i < FRAME_OVERLAP; i++) {

            VK_CHECK(VK10.vkCreateFence(_device, fenceCreateInfo, null, _frames[i]._renderFence));

            final int ii = i;
            //enqueue the destruction of the fence
            _mainDeletionQueue.push_function(() -> {
                vkDestroyFence(_device, _frames[ii]._renderFence[0], null);
            });


            VK_CHECK(vkCreateSemaphore(_device, semaphoreCreateInfo, null, _frames[i]._presentSemaphore));
            VK_CHECK(vkCreateSemaphore(_device, semaphoreCreateInfo, null, _frames[i]._renderSemaphore));

            //enqueue the destruction of semaphores
            _mainDeletionQueue.push_function(() -> {
                vkDestroySemaphore(_device, _frames[ii]._presentSemaphore[0], null);
                vkDestroySemaphore(_device, _frames[ii]._renderSemaphore[0], null);
            });
        }


        VkFenceCreateInfo uploadFenceCreateInfo = VkInit.fence_create_info();

        VK_CHECK(vkCreateFence(_device, uploadFenceCreateInfo, null, _uploadContext._uploadFence));
        _mainDeletionQueue.push_function(() -> {
        vkDestroyFence(_device, _uploadContext._uploadFence[0], null);
    });
    }

    /*1104*/ long pad_uniform_buffer_size(long originalSize)
    {
        // Calculate required alignment based on minimum device offset alignment
        long minUboAlignment = _gpuProperties.limits().minUniformBufferOffsetAlignment();
        long alignedSize = originalSize;
        if (minUboAlignment > 0) {
            alignedSize = (alignedSize + minUboAlignment - 1) & ~(minUboAlignment - 1);
        }
        return alignedSize;
    }

    /*1078*/ AllocatedBuffer create_buffer(long allocSize, /*VkBufferUsageFlags*/int usage, /*VmaMemoryUsage*/int memoryUsage)
    {
        //allocate vertex buffer
        final VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.create();
        bufferInfo.sType ( VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
        bufferInfo.pNext ( 0);
        bufferInfo.size ( allocSize);

        bufferInfo.usage ( usage);


        //let the VMA library know that this data should be writeable by CPU, but also readable by GPU
        final VmaAllocationCreateInfo vmaallocInfo = VmaAllocationCreateInfo.create();
        vmaallocInfo.usage ( memoryUsage);

        final AllocatedBuffer newBuffer = new AllocatedBuffer();

        LongBuffer dummy1 = memAllocLong(1);
        PointerBuffer dummy2 = memAllocPointer(1);

        //allocate the buffer
        VK_CHECK(vmaCreateBuffer(_allocator, bufferInfo, vmaallocInfo,
		/*newBuffer._buffer*/dummy1,
		/*newBuffer._allocation*/dummy2,
            null));

        newBuffer._buffer = dummy1.get(0);
        newBuffer._allocation = dummy2.get(0);

        memFree(dummy1);
        memFree(dummy2);

        return newBuffer;
    }

    /*1149*/ public void init_descriptors()
    {

        //create a descriptor pool that will hold 10 uniform buffers
        VkDescriptorPoolSize.Buffer sizes = VkDescriptorPoolSize.create(4);
                {
                    sizes.get(0).set( VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 10 );
                    sizes.get(1).set( VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, 10 );
                    sizes.get(2).set( VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, 10 );
                    sizes.get(3).set( VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 10 );
                };

        final VkDescriptorPoolCreateInfo pool_info = VkDescriptorPoolCreateInfo.create();
        pool_info.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
        pool_info.flags ( 0);
        pool_info.maxSets ( 10);
        //pool_info.poolSizeCount ( (int)sizes.size()); // java port
        pool_info.pPoolSizes ( sizes);

        vkCreateDescriptorPool(_device, pool_info, null, _descriptorPool);

        VkDescriptorSetLayoutBinding cameraBind = VkInit.descriptorset_layout_binding(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,VK_SHADER_STAGE_VERTEX_BIT,0);
        VkDescriptorSetLayoutBinding sceneBind = VkInit.descriptorset_layout_binding(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 1);

        VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.create(2);
        bindings.put(0,cameraBind);
        bindings.put(1,sceneBind);

        final VkDescriptorSetLayoutCreateInfo setinfo = VkDescriptorSetLayoutCreateInfo.create();
        //setinfo.bindingCount ( 2); java port
        setinfo.flags ( 0);
        setinfo.pNext ( 0);
        setinfo.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
        setinfo.pBindings ( bindings);

        vkCreateDescriptorSetLayout(_device, setinfo, null, _globalSetLayout);

        VkDescriptorSetLayoutBinding.Buffer objectBind = VkDescriptorSetLayoutBinding.create(1);
        objectBind.put(0,VkInit.descriptorset_layout_binding(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, VK_SHADER_STAGE_VERTEX_BIT, 0));

        final VkDescriptorSetLayoutCreateInfo set2info = VkDescriptorSetLayoutCreateInfo.create();
        //set2info.bindingCount ( 1); java port
        set2info.flags ( 0);
        set2info.pNext ( 0);
        set2info.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
        set2info.pBindings ( objectBind);

        vkCreateDescriptorSetLayout(_device, set2info, null, _objectSetLayout);

        VkDescriptorSetLayoutBinding.Buffer textureBind = VkDescriptorSetLayoutBinding.create(1);
        textureBind.put(0,VkInit.descriptorset_layout_binding(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT, 0));

        VkDescriptorSetLayoutCreateInfo set3info = VkDescriptorSetLayoutCreateInfo.create();
        //set3info.bindingCount ( 1); java port
        set3info.flags ( 0);
        set3info.pNext ( 0);
        set3info.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
        set3info.pBindings ( textureBind);

        vkCreateDescriptorSetLayout(_device, set3info, null, _singleTextureSetLayout);


	    long sceneParamBufferSize = FRAME_OVERLAP * pad_uniform_buffer_size(GPUSceneData.sizeof());

        _sceneParameterBuffer = create_buffer(sceneParamBufferSize, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VMA_MEMORY_USAGE_CPU_TO_GPU);


        for (int i = 0; i < FRAME_OVERLAP; i++)
        {
            _frames[i].cameraBuffer = create_buffer(GPUCameraData.sizeof(), VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VMA_MEMORY_USAGE_CPU_TO_GPU);

		final int MAX_OBJECTS = 10000;
            _frames[i].objectBuffer = create_buffer(GPUObjectData.sizeof() * MAX_OBJECTS, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, VMA_MEMORY_USAGE_CPU_TO_GPU);

            final VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.create();
            allocInfo.pNext ( 0);
            allocInfo.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocInfo.descriptorPool ( _descriptorPool[0]);
            //allocInfo.descriptorSetCount ( 1); java port
            LongBuffer dummy = memAllocLong(1);
            dummy.put(0,_globalSetLayout[0]);

            allocInfo.pSetLayouts ( /*_globalSetLayout*/dummy);

            VK10.vkAllocateDescriptorSets(_device, allocInfo, _frames[i].globalDescriptor);

            final VkDescriptorSetAllocateInfo objectSetAlloc = VkDescriptorSetAllocateInfo.create();
            objectSetAlloc.pNext ( 0);
            objectSetAlloc.sType ( VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            objectSetAlloc.descriptorPool ( _descriptorPool[0]);
            //objectSetAlloc.descriptorSetCount ( 1); java port

            LongBuffer dummy2 = memAllocLong(1);
            dummy2.put(0,_objectSetLayout[0]);
            objectSetAlloc.pSetLayouts ( /*_objectSetLayout*/dummy2);

            vkAllocateDescriptorSets(_device, objectSetAlloc, _frames[i].objectDescriptor);

            final VkDescriptorBufferInfo.Buffer cameraInfo = VkDescriptorBufferInfo.create(1);
            cameraInfo.buffer ( _frames[i].cameraBuffer._buffer);
            cameraInfo.offset ( 0);
            cameraInfo.range ( GPUCameraData.sizeof());

            final VkDescriptorBufferInfo.Buffer sceneInfo = VkDescriptorBufferInfo.create(1);
            sceneInfo.buffer ( _sceneParameterBuffer._buffer);
            sceneInfo.offset ( 0);
            sceneInfo.range ( GPUSceneData.sizeof());

            final VkDescriptorBufferInfo.Buffer objectBufferInfo = VkDescriptorBufferInfo.create(1);
            objectBufferInfo.buffer ( _frames[i].objectBuffer._buffer);
            objectBufferInfo.offset ( 0);
            objectBufferInfo.range ( GPUObjectData.sizeof() * MAX_OBJECTS);


            VkWriteDescriptorSet cameraWrite = VkInit.write_descriptor_buffer(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, _frames[i].globalDescriptor[0],cameraInfo,0);

            VkWriteDescriptorSet sceneWrite = VkInit.write_descriptor_buffer(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, _frames[i].globalDescriptor[0], sceneInfo, 1);

            VkWriteDescriptorSet objectWrite = VkInit.write_descriptor_buffer(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, _frames[i].objectDescriptor[0], objectBufferInfo, 0);

            VkWriteDescriptorSet.Buffer setWrites = VkWriteDescriptorSet.create(3);
            setWrites.put(0,cameraWrite);
            setWrites.put(1,sceneWrite);
            setWrites.put(2,objectWrite);

            vkUpdateDescriptorSets(_device, /*3,*/ setWrites, /*0,*/ null);
        }


        _mainDeletionQueue.push_function(() -> {

        vmaDestroyBuffer(_allocator, _sceneParameterBuffer._buffer, _sceneParameterBuffer._allocation);

        vkDestroyDescriptorSetLayout(_device, _objectSetLayout[0], null);
        vkDestroyDescriptorSetLayout(_device, _globalSetLayout[0], null);
        vkDestroyDescriptorSetLayout(_device, _singleTextureSetLayout[0], null);

        vkDestroyDescriptorPool(_device, _descriptorPool[0], null);

        for (int i = 0; i < FRAME_OVERLAP; i++)
        {
            vmaDestroyBuffer(_allocator, _frames[i].cameraBuffer._buffer, _frames[i].cameraBuffer._allocation);

            vmaDestroyBuffer(_allocator, _frames[i].objectBuffer._buffer, _frames[i].objectBuffer._allocation);
        }
    });
    }
}
