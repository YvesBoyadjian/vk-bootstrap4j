package vulkanguide;

import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.vulkan.VK10.*;

public class VkInit {

    public static VkCommandPoolCreateInfo command_pool_create_info(int queueFamilyIndex) {
        return command_pool_create_info(queueFamilyIndex,0);
    }
    /*3*/ public static VkCommandPoolCreateInfo command_pool_create_info(int queueFamilyIndex, /*VkCommandPoolCreateFlags*/int flags /*= 0*/)
    {
        final VkCommandPoolCreateInfo info = VkCommandPoolCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
        info.pNext ( 0);

        info.flags ( flags);
        return info;
    }

    public static VkCommandBufferAllocateInfo command_buffer_allocate_info(/*VkCommandPool*/long pool, int count /*= 1*/) {
        return command_buffer_allocate_info(pool,count,VK_COMMAND_BUFFER_LEVEL_PRIMARY);
    }
    /*13*/ public static VkCommandBufferAllocateInfo command_buffer_allocate_info(/*VkCommandPool*/long pool, int count /*= 1*/, /*VkCommandBufferLevel*/int level /*= VK_COMMAND_BUFFER_LEVEL_PRIMARY*/)
    {
        final VkCommandBufferAllocateInfo info = VkCommandBufferAllocateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
        info.pNext ( 0);

        info.commandPool ( pool);
        info.commandBufferCount ( count);
        info.level ( level);
        return info;
    }

    /*36*/ public static VkFramebufferCreateInfo framebuffer_create_info(/*VkRenderPass*/long renderPass, VkExtent2D extent)
    {
        final VkFramebufferCreateInfo info = VkFramebufferCreateInfo.create();
        info.sType( VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
        info.pNext ( 0);

        info.renderPass ( renderPass);
        //info.attachmentCount ( 1); java port
        LongBuffer dummy = memAllocLong(1);
        info.pAttachments(dummy);
        info.width( extent.width());
        info.height( extent.height());
        info.layers ( 1);

        return info;
    }

    public static VkFenceCreateInfo fence_create_info() {
        return fence_create_info(0);
    }
    /*51*/ public static VkFenceCreateInfo fence_create_info(/*VkFenceCreateFlags*/int flags /*= 0*/)
    {
        final VkFenceCreateInfo info = VkFenceCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
        info.pNext ( 0);

        info.flags ( flags);

        return info;
    }

    public static VkSemaphoreCreateInfo semaphore_create_info() {
        return semaphore_create_info(0);
    }
    /*62*/ public static VkSemaphoreCreateInfo semaphore_create_info(/*VkSemaphoreCreateFlags*/int flags /*= 0*/)
    {
        final VkSemaphoreCreateInfo info = VkSemaphoreCreateInfo.create();
        info.sType ( VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
        info.pNext ( 0);
        info.flags ( flags);
        return info;
    }

    /*214*/ public static VkImageCreateInfo image_create_info(/*VkFormat*/long format, /*VkImageUsageFlags*/int usageFlags, VkExtent3D extent)
    {
        final VkImageCreateInfo info = VkImageCreateInfo.create();
        info.sType( VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
        info.pNext( 0);

        info.imageType( VK_IMAGE_TYPE_2D);

        info.format( (int)format);
        info.extent( extent);

        info.mipLevels( 1);
        info.arrayLayers( 1);
        info.samples( VK_SAMPLE_COUNT_1_BIT);
        info.tiling( VK_IMAGE_TILING_OPTIMAL);
        info.usage( usageFlags);

        return info;
    }

    /*234*/ public static VkImageViewCreateInfo imageview_create_info(/*VkFormat*/long format, /*VkImage*/long image, /*VkImageAspectFlags*/int aspectFlags)
    {
        //build a image-view for the depth image to use for rendering
        final VkImageViewCreateInfo info = VkImageViewCreateInfo.create();
        info.sType( VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
        info.pNext( 0);

        info.viewType( VK_IMAGE_VIEW_TYPE_2D);
        info.image( image);
        info.format( (int)format);
        info.subresourceRange().baseMipLevel( 0);
        info.subresourceRange().levelCount( 1);
        info.subresourceRange().baseArrayLayer( 0);
        info.subresourceRange().layerCount( 1);
        info.subresourceRange().aspectMask( aspectFlags);

        return info;
    }

    /*270*/ public static VkDescriptorSetLayoutBinding descriptorset_layout_binding(/*VkDescriptorType*/long type, /*VkShaderStageFlags*/int stageFlags, int binding)
    {
        final VkDescriptorSetLayoutBinding setbind = VkDescriptorSetLayoutBinding.create();
        setbind.binding ( binding);
        setbind.descriptorCount ( 1);
        setbind.descriptorType ( (int)type);
        setbind.pImmutableSamplers ( null);
        setbind.stageFlags ( stageFlags);

        return setbind;
    }

    /*281*/ public static VkWriteDescriptorSet write_descriptor_buffer(/*VkDescriptorType*/long type, /*VkDescriptorSet*/long dstSet, VkDescriptorBufferInfo.Buffer bufferInfo , int binding)
    {
        final VkWriteDescriptorSet write = VkWriteDescriptorSet.create();
        write.sType ( VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        write.pNext ( 0);

        write.dstBinding ( binding);
        write.dstSet ( dstSet);
        write.descriptorCount ( 1);
        write.descriptorType ( (int)type);
        write.pBufferInfo ( bufferInfo);

        return write;
    }

}
