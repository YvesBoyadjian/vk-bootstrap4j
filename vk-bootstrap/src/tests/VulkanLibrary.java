package tests;

import org.lwjgl.system.NativeType;
import org.lwjgl.vulkan.*;

public class VulkanLibrary {

    public interface PFN_vkCreateRenderPass {
        int invoke(VkDevice device, VkRenderPassCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pRenderPass);
    }

    public interface PFN_vkCreateShaderModule {
        int invoke(VkDevice device, VkShaderModuleCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pShaderModule);
    }

    public interface PFN_vkCreatePipelineLayout {
        int invoke(VkDevice device, VkPipelineLayoutCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pPipelineLayout);
    }

    public interface PFN_vkCreateGraphicsPipelines {
        int invoke(VkDevice device, long pipelineCache, VkGraphicsPipelineCreateInfo.Buffer pCreateInfos, VkAllocationCallbacks pAllocator, long[] pPipelines);
    }

    public interface PFN_vkDestroyShaderModule {
        void invoke(VkDevice device, long shaderModule, VkAllocationCallbacks pAllocator);
    }

    /*135*/ public PFN_vkCreateRenderPass vkCreateRenderPass = /*VK_NULL_HANDLE*/null;
    /*136*/ public PFN_vkCreateShaderModule vkCreateShaderModule = /*VK_NULL_HANDLE*/null;
    /*137*/ public PFN_vkCreatePipelineLayout vkCreatePipelineLayout = /*VK_NULL_HANDLE*/null;
    /*138*/ public PFN_vkCreateGraphicsPipelines vkCreateGraphicsPipelines = /*VK_NULL_HANDLE*/null;
    /*139*/ public PFN_vkDestroyShaderModule vkDestroyShaderModule = /*VK_NULL_HANDLE*/null;

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
        /*96*/ vkCreateShaderModule = //(PFN_vkCreateShaderModule)vkGetDeviceProcAddr(device, "vkCreateShaderModule");
        new PFN_vkCreateShaderModule() {
            @Override
            public int invoke(VkDevice device, VkShaderModuleCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pShaderModule) {
                return VK10.vkCreateShaderModule(device,pCreateInfo,pAllocator,pShaderModule);
            }
        };
        /*97*/ vkCreatePipelineLayout =
                //(PFN_vkCreatePipelineLayout)vkGetDeviceProcAddr(device, "vkCreatePipelineLayout");
        new PFN_vkCreatePipelineLayout() {
            @Override
            public int invoke(VkDevice device, VkPipelineLayoutCreateInfo pCreateInfo, VkAllocationCallbacks pAllocator, long[] pPipelineLayout) {
                return VK10.vkCreatePipelineLayout(device,pCreateInfo,pAllocator,pPipelineLayout);
            }
        };
        /*99*/ vkCreateGraphicsPipelines =
                //(PFN_vkCreateGraphicsPipelines)vkGetDeviceProcAddr(device, "vkCreateGraphicsPipelines");
        new PFN_vkCreateGraphicsPipelines() {
            @Override
            public int invoke(VkDevice device, long pipelineCache, VkGraphicsPipelineCreateInfo.Buffer pCreateInfos, VkAllocationCallbacks pAllocator, long[] pPipelines) {
                return VK10.vkCreateGraphicsPipelines(device,pipelineCache,pCreateInfos,pAllocator,pPipelines);
            }
        };
        /*101*/vkDestroyShaderModule = //(PFN_vkDestroyShaderModule)vkGetDeviceProcAddr(device, "vkDestroyShaderModule");
        new PFN_vkDestroyShaderModule() {
            @Override
            public void invoke(VkDevice device, long shaderModule, VkAllocationCallbacks pAllocator) {
                VK10.vkDestroyShaderModule(device,shaderModule,pAllocator);
            }
        };
    }
}
