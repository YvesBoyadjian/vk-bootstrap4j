package vkbootstrap;

import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.*;
import port.Port;
import port.error_code;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.VK_API_VERSION_1_1;
import static org.lwjgl.vulkan.VK11.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2;
import static vkbootstrap.VkBootstrap.vulkan_functions;

public class VkbDeviceBuilder {
    private static class DeviceInfo {
        /*VkDeviceCreateFlags*/int flags = 0;
        final List<VkBaseOutStructure> pNext_chain = new ArrayList<>();
        final List<VkbCustomQueueDescription> queue_descriptions = new ArrayList<>();
        VkAllocationCallbacks allocation_callbacks = null;//VK_NULL_HANDLE;
    }

    private VkbPhysicalDevice physical_device;// = new VkbPhysicalDevice();
    private final DeviceInfo info = new DeviceInfo();

    // Any features and extensions that are requested/required in PhysicalDeviceSelector are automatically enabled.
    /*1385*/ public VkbDeviceBuilder(VkbPhysicalDevice phys_device) {
        physical_device = phys_device;
    }

    /*1387*/ public Result<VkbDevice> build() {

        final List<VkbCustomQueueDescription> queue_descriptions = new ArrayList<>();
        queue_descriptions.addAll(
                info.queue_descriptions);

        if (queue_descriptions.size() == 0) {
            for (int i = 0; i < physical_device.queue_families.size(); i++) {
                List<Float> l = new ArrayList<>(); l.add(1.0f);
                queue_descriptions.add(new VkbCustomQueueDescription( i, 1, l ));
            }
        }

        //final List<VkDeviceQueueCreateInfo> queueCreateInfos = new ArrayList<>();
        final VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.create(queue_descriptions.size()); // java port
        int index = 0;
        for (var desc : queue_descriptions) {
            final VkDeviceQueueCreateInfo queue_create_info = queueCreateInfos.get(index);//VkDeviceQueueCreateInfo.create();
            queue_create_info.sType( VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
            queue_create_info.queueFamilyIndex( desc.index);
            //queue_create_info.queueCount( desc.count); java port
            queue_create_info.pQueuePriorities(Port.dataf(desc.priorities) );
            //queueCreateInfos.add(queue_create_info); java port
            index++;
        }

        final List<String> extensions_to_enable = new ArrayList<>(); extensions_to_enable.addAll(physical_device.extensions_to_enable);
        if (physical_device.surface != VK_NULL_HANDLE || physical_device.defer_surface_initialization)
            extensions_to_enable.add( VK_KHR_SWAPCHAIN_EXTENSION_NAME );

        final List<Struct<?>> final_pnext_chain = new ArrayList<>();
        VkDeviceCreateInfo device_create_info = VkDeviceCreateInfo.create();

        boolean user_defined_phys_dev_features_2 = false;
        for (var pnext : info.pNext_chain) {
            VkBaseOutStructure out_structure = VkBaseOutStructure.create();
            Port.memcpy(out_structure, pnext, VkBaseOutStructure.SIZEOF);
            if (out_structure.sType() == VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2) {
                user_defined_phys_dev_features_2 = true;
                break;
            }
        }

        if (user_defined_phys_dev_features_2 && !physical_device.extended_features_chain.empty()) {
            return new Result<>(new error_code(VkbDeviceError.VkPhysicalDeviceFeatures2_in_pNext_chain_while_using_add_required_extension_features.ordinal()) );
        }
        
        // These objects must be alive during the call to vkCreateDevice
        var physical_device_extension_features_copy = new VkbFeaturesChain(physical_device.extended_features_chain);
        final VkPhysicalDeviceFeatures2 local_features2 = VkPhysicalDeviceFeatures2.create();
        local_features2.sType( VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2);
        local_features2.features( physical_device.features);

        if (!user_defined_phys_dev_features_2) {
            if (physical_device.instance_version >= VK_MAKE_VERSION(1, 1, 0) || physical_device.properties2_ext_enabled) {
                final_pnext_chain.add(local_features2);

                var features_chain_members = physical_device_extension_features_copy.get_pNext_chain_members();
                for (var features_struct : features_chain_members) {
                    final_pnext_chain.add(features_struct);
                }
                
//                has_phys_dev_features_2 = true;
//                for (var features_node : physical_device_extension_features_copy) {
///*1436*/                    final_pnext_chain.add(VkBaseOutStructure.createSafe(features_node.address()));
//                }
            }
        } else {
            device_create_info.pEnabledFeatures( physical_device.features);
            System.out.print("User provided VkPhysicalDeviceFeatures2 instance found in pNext chain. All "+
                    "requirements added via 'add_required_extension_features' will be ignored.");
        }

//#endif

        for (var pnext : info.pNext_chain) {
            final_pnext_chain.add(pnext);
        }

        VkBootstrap.setup_pNext_chain(device_create_info, final_pnext_chain);

        device_create_info.sType( VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
        device_create_info.flags( info.flags);
        //device_create_info.queueCreateInfoCount( (int)(queueCreateInfos.size())); java port
        device_create_info.pQueueCreateInfos(queueCreateInfos);
        //device_create_info.enabledExtensionCount( (int)(extensions.size())); java port
        device_create_info.ppEnabledExtensionNames( Port.datastr(extensions_to_enable));

        final VkbDevice device = new VkbDevice();

        /*VkResult*/int res = vulkan_functions().fp_vkCreateDevice.invoke(
                physical_device.physical_device, device_create_info, info.allocation_callbacks, device.device);
        if (res != VK_SUCCESS) {
            return new Result( new error_code(VkbDeviceError.failed_create_device.ordinal()), res );
        }

        device.physical_device.copyFrom(physical_device);
        device.surface = physical_device.surface;
        device.queue_families.clear(); device.queue_families.addAll(physical_device.queue_families);
        device.allocation_callbacks[0] = info.allocation_callbacks;
        device.fp_vkGetDeviceProcAddr = VkBootstrap.vulkan_functions().fp_vkGetDeviceProcAddr;
        VkBootstrap.vulkan_functions().get_device_proc_addr(device.device[0], device.internal_table.fp_vkGetDeviceQueue, "vkGetDeviceQueue");
        VkBootstrap.vulkan_functions().get_device_proc_addr(device.device[0], device.internal_table.fp_vkDestroyDevice, "vkDestroyDevice");
        device.instance_version = physical_device.instance_version;
        return new Result(device);
    }
}
