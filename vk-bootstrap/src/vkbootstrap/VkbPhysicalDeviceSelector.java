package vkbootstrap;

import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.*;

import port.Port;
import port.StablePartition;
import port.error_code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.VK_API_VERSION_1_1;
import static org.lwjgl.vulkan.VK11.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2;
import static org.lwjgl.vulkan.VK12.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_1_FEATURES;
import static org.lwjgl.vulkan.VK12.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_2_FEATURES;
import static vkbootstrap.VkBootstrap.*;

public class VkbPhysicalDeviceSelector {

    private class InstanceInfo {
        public VkInstance instance = null;//VK_NULL_HANDLE;
        /*VkSurfaceKHR*/long surface = VK_NULL_HANDLE;
        public int version = VK_MAKE_VERSION(1, 0, 0);
        public boolean headless = false;
        public boolean properties2_ext_enabled = false;
    }

    private class PhysicalDeviceDesc {
        public VkPhysicalDevice phys_device = null;//VK_NULL_HANDLE;
        public final List<VkQueueFamilyProperties> queue_families = new ArrayList<>();

        public final VkPhysicalDeviceFeatures device_features = VkPhysicalDeviceFeatures.create();
        public final VkPhysicalDeviceProperties device_properties = VkPhysicalDeviceProperties.create();
        public final VkPhysicalDeviceMemoryProperties mem_properties = VkPhysicalDeviceMemoryProperties.create();
//#if defined(VK_API_VERSION_1_1)
        public final VkPhysicalDeviceFeatures2 device_features2 = VkPhysicalDeviceFeatures2.create();
        public final List<VkbGenericFeaturesPNextNode> extended_features_chain = new ArrayList<>();
//#endif
    }

    private class SelectionCriteria {
        public String name = "";
        public VkbPreferredDeviceType preferred_type = VkbPreferredDeviceType.discrete;
        public boolean allow_any_type = true;
        public boolean require_present = true;
        public boolean require_dedicated_transfer_queue = false;
        public boolean require_dedicated_compute_queue = false;
        public boolean require_separate_transfer_queue = false;
        public boolean require_separate_compute_queue = false;
        public /*VkDeviceSize*/long required_mem_size = 0;
        public /*VkDeviceSize*/long desired_mem_size = 0;

        public final List<String> required_extensions = new ArrayList<>();

        public int required_version = VK_MAKE_VERSION(1, 0, 0);

        public final VkPhysicalDeviceFeatures required_features = VkPhysicalDeviceFeatures.create();
//#if defined(VK_API_VERSION_1_1)
        public final VkPhysicalDeviceFeatures2 required_features2 = VkPhysicalDeviceFeatures2.create();
        
        public final /*List<VkbGenericFeaturesPNextNode>*/VkbFeaturesChain extended_features_chain = new VkbFeaturesChain();
//#endif

        public boolean defer_surface_initialization = false;
        public boolean use_first_gpu_unconditionally = false;
        public boolean enable_portability_subset = true;
    }

    private enum Suitable {
        yes, partial, no
    }

    private final InstanceInfo instance_info = new InstanceInfo();

    private final SelectionCriteria criteria = new SelectionCriteria();

    /*987*/ PhysicalDeviceDesc populate_device_details(int instance_version,
                                                                                               VkPhysicalDevice phys_device,
                                                                                               List<VkbGenericFeaturesPNextNode> src_extended_features_chain) {
        final VkbPhysicalDeviceSelector.PhysicalDeviceDesc desc = new PhysicalDeviceDesc();
        desc.phys_device = phys_device;
        var queue_families = VkBootstrap.get_vector_noerror/*<VkQueueFamilyProperties>*/(
                vulkan_functions().fp_vkGetPhysicalDeviceQueueFamilyProperties, phys_device);
        desc.queue_families.clear(); desc.queue_families.addAll(queue_families);

        vulkan_functions().fp_vkGetPhysicalDeviceProperties.invoke(phys_device, desc.device_properties);
        vulkan_functions().fp_vkGetPhysicalDeviceFeatures.invoke(phys_device, desc.device_features);
        vulkan_functions().fp_vkGetPhysicalDeviceMemoryProperties.invoke(phys_device, desc.mem_properties);

//#if defined(VK_API_VERSION_1_1)
        desc.device_features2.sType( VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2);

        var fill_chain = src_extended_features_chain;

        if (!fill_chain.isEmpty() && instance_version >= VK_API_VERSION_1_1) {

            VkbGenericFeaturesPNextNode prev = null;
            for (var extension : fill_chain) {
                if (prev != null) {
                    prev.pNext(extension);
                }
                prev = extension;
            }

            final VkPhysicalDeviceFeatures2 local_features = VkPhysicalDeviceFeatures2.create();
            local_features.sType( VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2);
            local_features.pNext( fill_chain.get(0).address());

            vulkan_functions().fp_vkGetPhysicalDeviceFeatures2.invoke(phys_device, local_features);
        }

        desc.extended_features_chain.clear(); desc.extended_features_chain.addAll(fill_chain);
//#endif
        return desc;
    }

    public VkbPhysicalDevice populate_device_details(
    VkPhysicalDevice vk_phys_device, VkbFeaturesChain src_extended_features_chain) {
    	
    final VkbPhysicalDevice physical_device = new VkbPhysicalDevice();
    physical_device.physical_device = vk_phys_device;
    physical_device.surface = instance_info.surface;
    physical_device.defer_surface_initialization = criteria.defer_surface_initialization;
    physical_device.instance_version = instance_info.version;
    var queue_families = VkBootstrap.get_vector_noerror/*<VkQueueFamilyProperties>*/(
        vulkan_functions().fp_vkGetPhysicalDeviceQueueFamilyProperties, vk_phys_device);
    physical_device.queue_families.clear(); physical_device.queue_families.addAll(queue_families);

    vulkan_functions().fp_vkGetPhysicalDeviceProperties.invoke(vk_phys_device, physical_device.properties);
    vulkan_functions().fp_vkGetPhysicalDeviceFeatures.invoke(vk_phys_device, physical_device.features);
    vulkan_functions().fp_vkGetPhysicalDeviceMemoryProperties.invoke(vk_phys_device, physical_device.memory_properties);

    physical_device.name = Port.toString(physical_device.properties.deviceName());

    final List<VkExtensionProperties> available_extensions = new ArrayList<>();
    var available_extensions_ret = VkBootstrap.get_vector/*<VkExtensionProperties>*/(
        available_extensions, vulkan_functions().fp_vkEnumerateDeviceExtensionProperties, vk_phys_device, null);
    if (available_extensions_ret != VK_SUCCESS) return physical_device;
    for (var ext : available_extensions) {
        physical_device.available_extensions.add(Port.toString(ext.extensionName()));
    }
    // Lets us quickly find extensions as this list can be 300+ elements long
    Collections.sort(physical_device.available_extensions);

    physical_device.properties2_ext_enabled = instance_info.properties2_ext_enabled;

    var fill_chain = src_extended_features_chain;

    boolean instance_is_1_1 = instance_info.version >= Version.VKB_VK_API_VERSION_1_1;
    if (!fill_chain.empty() && (instance_is_1_1 || instance_info.properties2_ext_enabled)) {
        final VkPhysicalDeviceFeatures2 local_features = VkPhysicalDeviceFeatures2.create();
        fill_chain.create_chained_features(local_features);
        // Use KHR function if not able to use the core function
        if (instance_is_1_1) {
            vulkan_functions().fp_vkGetPhysicalDeviceFeatures2.invoke(vk_phys_device, local_features);
        } else {
            vulkan_functions().fp_vkGetPhysicalDeviceFeatures2KHR.invoke(vk_phys_device, local_features);
        }
        physical_device.extended_features_chain.copyFrom(/*std::move(*/fill_chain)/*)*/;
    }

    return physical_device;
}

    /*1027*/ VkbPhysicalDevice.Suitable is_device_suitable(VkbPhysicalDevice pd, final List<String> unsuitability_reasons) {
    		VkbPhysicalDevice.Suitable suitable = VkbPhysicalDevice.Suitable.yes;

        if (criteria.name.length() > 0 && !Objects.equals(criteria.name, Port.toString(pd.properties.deviceName()))) {
            unsuitability_reasons.add(
                "VkPhysicalDeviceProperties::deviceName doesn't match requested name \"" + criteria.name + "\"");
            return VkbPhysicalDevice.Suitable.no;
        }

        if (criteria.required_version > pd.properties.apiVersion()) {
            unsuitability_reasons.add(
                    "VkPhysicalDeviceProperties::apiVersion " + Port.to_string(VK_API_VERSION_MAJOR(pd.properties.apiVersion())) +
                    "." + Port.to_string(VK_API_VERSION_MINOR(pd.properties.apiVersion())) + " lower than required version " +
                    Port.to_string(VK_API_VERSION_MAJOR(criteria.required_version)) + "." +
                    Port.to_string(VK_API_VERSION_MINOR(criteria.required_version)));
        		return VkbPhysicalDevice.Suitable.no;
        }

        boolean dedicated_compute = VkBootstrap.get_dedicated_queue_index(pd.queue_families,
                VK_QUEUE_COMPUTE_BIT,
                VK_QUEUE_TRANSFER_BIT) != QUEUE_INDEX_MAX_VALUE;
        boolean dedicated_transfer = VkBootstrap.get_dedicated_queue_index(pd.queue_families,
                VK_QUEUE_TRANSFER_BIT,
                VK_QUEUE_COMPUTE_BIT) != QUEUE_INDEX_MAX_VALUE;
        boolean separate_compute =
                VkBootstrap.get_separate_queue_index(pd.queue_families, VK_QUEUE_COMPUTE_BIT, VK_QUEUE_TRANSFER_BIT) !=
        QUEUE_INDEX_MAX_VALUE;
        boolean separate_transfer =
                VkBootstrap.get_separate_queue_index(pd.queue_families, VK_QUEUE_TRANSFER_BIT, VK_QUEUE_COMPUTE_BIT) !=
        QUEUE_INDEX_MAX_VALUE;

        boolean present_queue =
                VkBootstrap.get_present_queue_index(pd.physical_device, instance_info.surface, pd.queue_families) !=
        QUEUE_INDEX_MAX_VALUE;

        if (criteria.require_dedicated_compute_queue && !dedicated_compute) {
            unsuitability_reasons.add("No dedicated compute queue");
        		return VkbPhysicalDevice.Suitable.no;
        }
        if (criteria.require_dedicated_transfer_queue && !dedicated_transfer) { 
            unsuitability_reasons.add("No dedicated transfer queue");
        		return VkbPhysicalDevice.Suitable.no;
        }
        if (criteria.require_separate_compute_queue && !separate_compute) {
            unsuitability_reasons.add("No separate compute queue");
        		return VkbPhysicalDevice.Suitable.no;
        }
        if (criteria.require_separate_transfer_queue && !separate_transfer) {
            unsuitability_reasons.add("No separate transfer queue");
        		return VkbPhysicalDevice.Suitable.no;
        }
        if (criteria.require_present && !present_queue && !criteria.defer_surface_initialization) {
            unsuitability_reasons.add("No queue capable of present operations");
            return VkbPhysicalDevice.Suitable.no;
        }
        var unsupported_extensions =
                /*detail::*/find_unsupported_extensions_in_list(pd.available_extensions, criteria.required_extensions);
        if (unsupported_extensions.size() > 0) {
            for (var unsupported_ext : unsupported_extensions) {
                unsuitability_reasons.add("Device extension " + unsupported_ext + " not supported");
            }
            return VkbPhysicalDevice.Suitable.no;
        }
            
        if (!criteria.defer_surface_initialization && criteria.require_present) {
            final List<VkSurfaceFormatKHR> formats = new ArrayList<>();
            final List</*VkPresentModeKHR*/Integer> present_modes = new ArrayList<>();

            var formats_ret = /*detail::*/VkBootstrap.get_vector/*<VkSurfaceFormatKHR>*/(formats,
                /*detail::*/vulkan_functions().fp_vkGetPhysicalDeviceSurfaceFormatsKHR,
                pd.physical_device,
                instance_info.surface);
            var present_modes_ret = /*detail::*/VkBootstrap.get_vector/*<VkPresentModeKHR>*/(present_modes,
                /*detail::*/vulkan_functions().fp_vkGetPhysicalDeviceSurfacePresentModesKHR,
                pd.physical_device,
                instance_info.surface);

            if (formats_ret != VK_SUCCESS || present_modes_ret != VK_SUCCESS || formats.isEmpty() || present_modes.isEmpty()) {
                if (formats_ret != VK_SUCCESS) {
                    unsuitability_reasons.add(
                        "vkGetPhysicalDeviceSurfaceFormatsKHR returned error code " + Port.to_string(formats_ret));
                }
                if (present_modes_ret != VK_SUCCESS) {
                    unsuitability_reasons.add(
                        "vkGetPhysicalDeviceSurfacePresentModesKHR returned error code " + Port.to_string(present_modes_ret));
                }
                if (formats.isEmpty()) {
                    unsuitability_reasons.add("vkGetPhysicalDeviceSurfaceFormatsKHR returned zero surface formats");
                }
                if (present_modes.isEmpty()) {
                    unsuitability_reasons.add(
                        "vkGetPhysicalDeviceSurfacePresentModesKHR returned zero present modes");
                }
                return VkbPhysicalDevice.Suitable.no;
            }
        }                                  

//        boolean swapChainAdequate = false;
//        if (criteria.defer_surface_initialization) {
//            swapChainAdequate = true;
//        } else if (!instance_info.headless) {
//            final List<VkSurfaceFormatKHR> formats = new ArrayList<>();
//            final List</*VkPresentModeKHR*/Integer> present_modes = new ArrayList<>();
//
//            var formats_ret = VkBootstrap.get_vector/*<VkSurfaceFormatKHR>*/(formats,
//            vulkan_functions().fp_vkGetPhysicalDeviceSurfaceFormatsKHR,
//                    pd.phys_device,
//                    instance_info.surface);
//            var present_modes_ret = VkBootstrap.get_vector/*<VkPresentModeKHR>*/(present_modes,
//            vulkan_functions().fp_vkGetPhysicalDeviceSurfacePresentModesKHR,
//                    pd.phys_device,
//                    instance_info.surface);
//
//            if (formats_ret == VK_SUCCESS && present_modes_ret == VK_SUCCESS) {
//                swapChainAdequate = !formats.isEmpty() && !present_modes.isEmpty();
//            }
//        }
//        if (criteria.require_present && !swapChainAdequate) return Suitable.no;

        if (pd.properties.deviceType() != (/*VkPhysicalDeviceType*/int)(criteria.preferred_type.ordinal())) {
            if (criteria.allow_any_type) {
                suitable = VkbPhysicalDevice.Suitable.partial;
            }
            else {
                suitable = VkbPhysicalDevice.Suitable.no;
                unsuitability_reasons.add(
                    "VkPhysicalDeviceType \"" + VkBootstrap.to_string_device_type(pd.properties.deviceType()) + "\" does not match preferred type \"" +
                    		VkBootstrap.to_string_device_type(/*static_cast<VkPhysicalDeviceType>*/(criteria.preferred_type.ordinal())) + "\"");            	
            }
        }

        VkBootstrapFeatureChain.compare_VkPhysicalDeviceFeatures(unsuitability_reasons, pd.features, criteria.required_features);
        pd.extended_features_chain.match_all(unsuitability_reasons, criteria.extended_features_chain);
        if (!unsuitability_reasons.isEmpty()) {
            return VkbPhysicalDevice.Suitable.no;
        }
        
        for (int i = 0; i < pd.memory_properties.memoryHeapCount(); i++) {
            if ((pd.memory_properties.memoryHeaps(i).flags() & VK_MEMORY_HEAP_DEVICE_LOCAL_BIT) != 0) {
                if (pd.memory_properties.memoryHeaps(i).size() < criteria.required_mem_size) {
                    unsuitability_reasons.add("Did not contain a Device Local memory heap with enough size");
                    return VkbPhysicalDevice.Suitable.no;
                }
            }
        }
                        
//        boolean required_features_supported = VkBootstrap.supports_features(
//                pd.device_features, criteria.required_features, pd.extended_features_chain, criteria.extended_features_chain);
//        if (!required_features_supported) return Suitable.no;
//
//        boolean has_required_memory = false;
//        boolean has_preferred_memory = false;
//        final int memoryHeapCount = pd.mem_properties.memoryHeapCount();
//        for (int i = 0; i < memoryHeapCount; i++) {
//            if ((pd.mem_properties.memoryHeaps(i).flags() & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)!=0) {
//                final long memoryHeapSize = pd.mem_properties.memoryHeaps(i).size();
//                if (memoryHeapSize > criteria.required_mem_size) {
//                    has_required_memory = true;
//                }
//                if (memoryHeapSize > criteria.desired_mem_size) {
//                    has_preferred_memory = true;
//                }
//            }
//        }
//        if (!has_required_memory) return Suitable.no;
//        if (!has_preferred_memory) suitable = Suitable.partial;

        return suitable;
    }

    // Requires a vkb::Instance to construct, needed to pass instance creation info.
    /*1118*/public VkbPhysicalDeviceSelector(VkbInstance instance) {
    		this(instance, 0);
    }
    /*1118*/public VkbPhysicalDeviceSelector(VkbInstance instance, /*VkSurfaceKHR*/long surface) {
        instance_info.instance = instance.instance[0];
        //instance_info.headless = instance.headless; dont copy headless
        instance_info.version = instance.instance_version;
        instance_info.properties2_ext_enabled = instance.properties2_ext_enabled;
        instance_info.surface = surface;
        criteria.require_present = !instance.headless;
        criteria.required_version = instance.api_version;
    }

    /*1193*/ public VkbPhysicalDeviceSelector set_surface(/*VkSurfaceKHR*/long surface) {
        instance_info.surface = surface;
        //instance_info.headless = false; dont
        return this;
    }
    

 // Return all devices which are considered suitable - intended for applications which want to let the user pick the physical device
    public Result<List<VkbPhysicalDevice>> select_devices() {
     if (criteria.require_present && !criteria.defer_surface_initialization) {
         if (instance_info.surface == VK_NULL_HANDLE)
             return new Result<List<VkbPhysicalDevice>>(new error_code(VkbPhysicalDeviceError.no_surface_provided.ordinal()) );
     }

     // Get the VkPhysicalDevice handles on the system
     final List<VkPhysicalDevice> vk_physical_devices = new ArrayList<>();

     var vk_physical_devices_ret = VkBootstrap.get_vector/*<VkPhysicalDevice>*/(
         vk_physical_devices, vulkan_functions().fp_vkEnumeratePhysicalDevices, instance_info.instance);
     if (vk_physical_devices_ret != VK_SUCCESS) {
         return new Result<List<VkbPhysicalDevice>>(new error_code( VkbPhysicalDeviceError.failed_enumerate_physical_devices.ordinal()), vk_physical_devices_ret);
     }
     if (vk_physical_devices.isEmpty()) {
         return new Result<List<VkbPhysicalDevice>>(new error_code( VkbPhysicalDeviceError.no_physical_devices_found.ordinal()));
     }

     var fill_out_phys_dev_with_criteria = new Consumer<VkbPhysicalDevice>() {

		@Override
		public void accept(VkbPhysicalDevice phys_dev) {
	         phys_dev.features.set(criteria.required_features);
	         phys_dev.extended_features_chain.copyFrom(criteria.extended_features_chain);

	         boolean portability_ext_available =
	             criteria.enable_portability_subset &&
	             Collections.binarySearch(phys_dev.available_extensions, "VK_KHR_portability_subset") >= 0;
	             /*std::binary_search(phys_dev.available_extensions.begin(), phys_dev.available_extensions.end(), "VK_KHR_portability_subset")*/;

	         phys_dev.extensions_to_enable.clear();
	         phys_dev.extensions_to_enable.addAll( criteria.required_extensions);
	         if (portability_ext_available) {
	             phys_dev.extensions_to_enable.add("VK_KHR_portability_subset");
	         }
	         // Lets us quickly find extensions as this list can be 300+ elements long
	         Collections.sort(phys_dev.extensions_to_enable);
		}
     };

     // if this option is set, always return only the first physical device found
     if (criteria.use_first_gpu_unconditionally && vk_physical_devices.size() > 0) {
         VkbPhysicalDevice physical_device = populate_device_details(vk_physical_devices.get(0), criteria.extended_features_chain);
         fill_out_phys_dev_with_criteria.accept(physical_device);
         List<VkbPhysicalDevice> retVal = new ArrayList<VkbPhysicalDevice>();
         retVal.add(physical_device);
         
         return new Result<List<VkbPhysicalDevice>>(retVal);
     }

     // Populate their details and check their suitability
     final List<String> unsuitability_reasons = new ArrayList<>();
     final List<VkbPhysicalDevice> physical_devices = new ArrayList<>();
     for (var vk_physical_device : vk_physical_devices) {
         VkbPhysicalDevice phys_dev = populate_device_details(vk_physical_device, criteria.extended_features_chain);
         final List<String> gpu_unsuitability_reasons = new ArrayList<>();
         phys_dev.suitable = is_device_suitable(phys_dev, gpu_unsuitability_reasons);
         if (phys_dev.suitable != VkbPhysicalDevice.Suitable.no) {
             physical_devices.add(phys_dev);
         } else {
             for (var reason : gpu_unsuitability_reasons) {
                 unsuitability_reasons.add(
                     "Physical Device " + phys_dev.properties.deviceNameString() + " not selected due to: " + reason);
             }
         }
     }

     // No suitable devices found, return an error which contains the list of reason why it wasn't suitable
     if (physical_devices.isEmpty()) {
         return new Result<List<VkbPhysicalDevice>>(new error_code(VkbPhysicalDeviceError.no_suitable_device.ordinal()), unsuitability_reasons);
     }

     // sort the list into fully and partially suitable devices. use stable_partition to maintain relative order
     List<VkbPhysicalDevice> sp = StablePartition.stablePartition(physical_devices, new Predicate<VkbPhysicalDevice>() {

		@Override
		public boolean test(VkbPhysicalDevice pd) {
	         return pd.suitable == VkbPhysicalDevice.Suitable.yes;
		}
     });

     // Make the physical device ready to be used to create a Device from it
     for (var physical_device : physical_devices) {
         fill_out_phys_dev_with_criteria.accept(physical_device);
     }

     return new Result<>(physical_devices);
 }
    

    /*1127*/ public Result<VkbPhysicalDevice> select() {
    	
        var selected_devices = select_devices();

        if (selected_devices.not()) return new Result<VkbPhysicalDevice>(selected_devices.full_error());
        return new Result<>(selected_devices.value().get(0));
    	
//        if (!instance_info.headless && !criteria.defer_surface_initialization) {
//            if (instance_info.surface == VK_NULL_HANDLE)
//                return new Result<VkbPhysicalDevice>(new error_code(VkbPhysicalDeviceError.no_surface_provided.ordinal()));
//        }
//
//
//        final List<VkPhysicalDevice> physical_devices = new ArrayList<>();
//
//        int physical_devices_ret = VkBootstrap.get_vector/*<VkPhysicalDevice,VkbVulkanFunctions.PFN_vkEnumeratePhysicalDevices>*/(
//        physical_devices, vulkan_functions().fp_vkEnumeratePhysicalDevices, instance_info.instance);
//        if (physical_devices_ret != VK_SUCCESS) {
//            return new Result<VkbPhysicalDevice>( new error_code(VkbPhysicalDeviceError.failed_enumerate_physical_devices.ordinal()),
//                    physical_devices_ret );
//        }
//        if (physical_devices.size() == 0) {
//            return new Result<VkbPhysicalDevice>( new error_code(VkbPhysicalDeviceError.no_physical_devices_found.ordinal()));
//        }
//
//        final List<PhysicalDeviceDesc> phys_device_descriptions = new ArrayList<>();
//        for (var phys_device : physical_devices) {
//            phys_device_descriptions.add(populate_device_details(
//                    instance_info.version, phys_device, criteria.extended_features_chain));
//        }
//
//        PhysicalDeviceDesc selected_device = new PhysicalDeviceDesc();
//
//        if (criteria.use_first_gpu_unconditionally) {
//            selected_device = phys_device_descriptions.get(0);
//        } else {
//            for (var device : phys_device_descriptions) {
//                var suitable = is_device_suitable(device);
//                if (suitable == Suitable.yes) {
//                    selected_device = device;
//                    break;
//                } else if (suitable == Suitable.partial) {
//                    selected_device = device;
//                }
//            }
//        }
//
//        if (selected_device.phys_device == null || selected_device.phys_device.address() == VK_NULL_HANDLE) {
//            return new Result<VkbPhysicalDevice>(new error_code( VkbPhysicalDeviceError.no_suitable_device.ordinal() ));
//        }
//        final VkbPhysicalDevice out_device = new VkbPhysicalDevice();
//        out_device.physical_device = selected_device.phys_device;
//        out_device.surface = instance_info.surface;
//        out_device.features.set(criteria.required_features);
//        out_device.extended_features_chain.clear(); out_device.extended_features_chain.addAll(criteria.extended_features_chain);
//        out_device.properties = selected_device.device_properties;
//        out_device.memory_properties = selected_device.mem_properties;
//        out_device.queue_families.clear(); out_device.queue_families.addAll(selected_device.queue_families);
//        out_device.defer_surface_initialization = criteria.defer_surface_initialization;
//        out_device.instance_version = instance_info.version;
//
//        out_device.extensions_to_enable.addAll(criteria.required_extensions);
//        var desired_extensions_supported =
//                check_device_extension_support(out_device.physical_device, criteria.desired_extensions);
//        out_device.extensions_to_enable.addAll(desired_extensions_supported);
//        return new Result(out_device);
    }
    /*1212*/ public VkbPhysicalDeviceSelector set_minimum_version (int major, int minor) {
        criteria.required_version = VK_MAKE_VERSION (major, minor, 0);
        return this;
    }

    // Require a physical device which supports the features in VkPhysicalDeviceFeatures.
//#if defined(VKB_VK_API_VERSION_1_2)
// Just calls add_required_features
// Require a physical device which supports the features in VkPhysicalDeviceVulkan11Features.
// Must have vulkan version 1.2 - This is due to the VkPhysicalDeviceVulkan11Features struct being added in 1.2, not 1.1
 // The implementation of the set_required_features_1X functions sets the sType manually. This was a poor choice since
 // users of Vulkan should expect to fill out their structs properly. To make the functions take the struct parameter by
 // const reference, a local copy must be made in order to set the sType.
    public VkbPhysicalDeviceSelector set_required_features_11(VkPhysicalDeviceVulkan11Features features_11) {
        features_11.sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_1_FEATURES);
        add_required_extension_features(features_11);
        return this;
    }
    // Require a physical device which supports the features in VkPhysicalDeviceVulkan12Features.
    // Must have vulkan version 1.2
    public VkbPhysicalDeviceSelector set_required_features_12(VkPhysicalDeviceVulkan12Features features_12) {
        features_12.sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_2_FEATURES);
        add_required_extension_features(features_12);
        return this;
    }
//#endif

    // Require a physical device which supports a specific set of general/extension features.
    // If this function is used, the user should not put their own VkPhysicalDeviceFeatures2 in
    // the pNext chain of VkDeviceCreateInfo.
    public VkbPhysicalDeviceSelector add_required_extension_features(/*Struct<?>*/VkPhysicalDeviceVulkan11Features features) {
        criteria.extended_features_chain.add_structure(/*VkbGenericFeaturesPNextNode.from(*/features.sType(),features.sizeof(), features/*)*/);
        return this;
    }

    // Require a physical device which supports a specific set of general/extension features.
    // If this function is used, the user should not put their own VkPhysicalDeviceFeatures2 in
    // the pNext chain of VkDeviceCreateInfo.
    public VkbPhysicalDeviceSelector add_required_extension_features(/*Struct<?>*/VkPhysicalDeviceVulkan12Features features) {
        criteria.extended_features_chain.add_structure(/*VkbGenericFeaturesPNextNode.from(*/features.sType(), features.sizeof(), features/*)*/);
        return this;
    }


    public List<String> find_unsupported_extensions_in_list(
    final List<String> available_extensions, final List<String> required_extensions) {
    final List<String> unavailable_extensions = new ArrayList<>();

    for (var req_ext : required_extensions) {
        if (!Port.binary_search(available_extensions, req_ext)) {
            unavailable_extensions.add(req_ext);
        }
    }
    return unavailable_extensions;
}

}
