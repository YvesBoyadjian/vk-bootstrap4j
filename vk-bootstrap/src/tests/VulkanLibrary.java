package tests;

import org.lwjgl.system.NativeType;
import org.lwjgl.vulkan.*;

public class VulkanLibrary {

    public interface PFN_vkCreateRenderPass {
        int invoke(VkDevice device, VkRenderPassCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pRenderPass);
    }

    /*135*/ public PFN_vkCreateRenderPass vkCreateRenderPass = /*VK_NULL_HANDLE*/null;

    public void init(VkInstance instance) {
        // Nothing to do
    }

    public void init(VkDevice device) {
        /*95*/ vkCreateRenderPass = //(PFN_vkCreateRenderPass)vkGetDeviceProcAddr(device, "vkCreateRenderPass");
        new PFN_vkCreateRenderPass() {
            @Override
            public int invoke(VkDevice device, VkRenderPassCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pRenderPass) {
                return VK10.vkCreateRenderPass(device,pCreateInfo,pAllocator,pRenderPass);
            }
        };
    }
}
