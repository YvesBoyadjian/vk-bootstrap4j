package vkbootstrap;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.NativeType;
import org.lwjgl.vulkan.*;
import port.error_code;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.*;

public class VkBootstrap {

    public static String to_string_message_severity(/*VkDebugUtilsMessageSeverityFlagBitsEXT*/int s) {
        switch (s) {
            case /*VkDebugUtilsMessageSeverityFlagBitsEXT::*/VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT:
                return "VERBOSE";
            case /*VkDebugUtilsMessageSeverityFlagBitsEXT::*/VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT:
                return "ERROR";
            case /*VkDebugUtilsMessageSeverityFlagBitsEXT::*/VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT:
                return "WARNING";
            case /*VkDebugUtilsMessageSeverityFlagBitsEXT::*/VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT:
                return "INFO";
            default:
                return "UNKNOWN";
        }
    }

    public static String to_string_message_type(/*VkDebugUtilsMessageTypeFlagsEXT*/int s) {
        if (s == 7) return "General | Validation | Performance";
        if (s == 6) return "Validation | Performance";
        if (s == 5) return "General | Performance";
        if (s == 4 /*VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT*/) return "Performance";
        if (s == 3) return "General | Validation";
        if (s == 2 /*VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT*/) return "Validation";
        if (s == 1 /*VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT*/) return "General";
        return "Unknown";
    }

    private static final VkbVulkanFunctions v = new VkbVulkanFunctions();
    /*211*/ public static VkbVulkanFunctions vulkan_functions() {
        return v;
    }

    // Helper for robustly executing the two-call pattern
    /*218*/ public static <T extends VkPhysicalDevice, F extends VkbVulkanFunctions.PFN_vkEnumeratePhysicalDevices> int get_vector(final List<T> out, F f, VkInstance ts) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke(ts, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            PointerBuffer pPhysicalDevices = memAllocPointer(count[0]);
            err = f.invoke(ts, count, /*out.data()*/pPhysicalDevices);
            out.clear();//out.resize(count);
            if( err == VK_SUCCESS) {
                for (int i = 0; i < count[0]; i++) {
                    long physicalDevice = pPhysicalDevices.get(i);
                    out.add((T) new VkPhysicalDevice(physicalDevice, ts));
                }
            }
            memFree(pPhysicalDevices);

        } while (err == VK_INCOMPLETE);
        return err;
    }

    /*218*/ public static <T extends VkExtensionProperties, F extends VkbVulkanFunctions.PFN_vkEnumerateDeviceExtensionProperties> int get_vector(final List<T> out, F f, VkPhysicalDevice ts, CharSequence o) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke(ts, o, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            org.lwjgl.vulkan.VkExtensionProperties.Buffer pExtensionsProperties = org.lwjgl.vulkan.VkExtensionProperties.create(count[0]);
            err = f.invoke(ts, o, count, /*out.data()*/pExtensionsProperties);
            out.clear();//out.resize(count);
            if( err == VK_SUCCESS) {
                for (int i = 0; i < count[0]; i++) {
                    out.add((T) pExtensionsProperties.get(i));
                }
            }

        } while (err == VK_INCOMPLETE);
        return err;
    }

    /*218*/ public static <T extends VkExtensionProperties, F extends VkbVulkanFunctions.PFN_vkEnumerateInstanceExtensionProperties> int get_vector(final List<T> out, F f, ByteBuffer o) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke( o, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            org.lwjgl.vulkan.VkExtensionProperties.Buffer pExtensionsProperties = org.lwjgl.vulkan.VkExtensionProperties.create(count[0]);
            err = f.invoke( o, count, /*out.data()*/pExtensionsProperties);
            out.clear();//out.resize(count);
            if( err == VK_SUCCESS) {
                for (int i = 0; i < count[0]; i++) {
                    out.add((T) pExtensionsProperties.get(i));
                }
            }

        } while (err == VK_INCOMPLETE);
        return err;
    }

    /*218*/ public static <T extends VkLayerProperties, F extends VkbVulkanFunctions.PFN_vkEnumerateInstanceLayerProperties> int get_vector(final List<T> out, F f) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke( count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            org.lwjgl.vulkan.VkLayerProperties.Buffer pExtensionsProperties = org.lwjgl.vulkan.VkLayerProperties.create(count[0]);
            err = f.invoke( count, /*out.data()*/pExtensionsProperties);
            out.clear();//out.resize(count);
            if( err == VK_SUCCESS) {
                for (int i = 0; i < count[0]; i++) {
                    out.add((T) pExtensionsProperties.get(i));
                }
            }

        } while (err == VK_INCOMPLETE);
        return err;
    }

    // Helper for robustly executing the two-call pattern
    /*218*/ public static int get_vector(final List<VkSurfaceFormatKHR> out, VkbVulkanFunctions.PFN_vkGetPhysicalDeviceSurfaceFormatsKHR f, VkPhysicalDevice ts1, long ts2 ) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke(ts1, ts2, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            VkSurfaceFormatKHR.Buffer buffer = VkSurfaceFormatKHR.create(count[0]);
            err = f.invoke(ts1,ts2, count, buffer);
            //out.resize(count);
            out.clear();
            for(int i=0; i<count[0];i++) {
                out.add(buffer.get(i));
            }
        } while (err == VK_INCOMPLETE);
        return err;
    }

    // Helper for robustly executing the two-call pattern
    /*218*/ public static int get_vector(final List<Integer> out, VkbVulkanFunctions.PFN_vkGetPhysicalDeviceSurfacePresentModesKHR f, VkPhysicalDevice ts1, long ts2) {
        final int[] count = new int[1];
        /*VkResult*/int err;
        do {
            err = f.invoke(ts1,ts2, count, null);
            if (err != 0) {
                return err;
            };
            //out.resize(count);
            final int[] pPresentModes = new int[count[0]];
            err = f.invoke(ts1,ts2, count, pPresentModes);
            //out.resize(count);
            out.clear();
            for( int i=0;i<count[0];i++) {
                out.add(pPresentModes[i]);
            }
        } while (err == VK_INCOMPLETE);
        return err;
    }

    /*234*/ public static List<VkQueueFamilyProperties> get_vector_noerror(VkbVulkanFunctions.PFN_vkGetPhysicalDeviceQueueFamilyProperties f, VkPhysicalDevice ts) {
        final int[] count = new int[1];
        f.invoke(ts, count, null);
        final VkQueueFamilyProperties.Buffer results = VkQueueFamilyProperties.create(count[0]);
        f.invoke(ts, count, results);
        List<VkQueueFamilyProperties> rl = new ArrayList<>();
        for(int i=0;i<count[0];i++) {
            rl.add(results.get(i));
        }
        return rl;
    }

    /*270*/ /*VkResult*/public static int create_debug_utils_messenger(VkInstance instance,
                                          VkDebugUtilsMessengerCallbackEXT debug_callback,
                                          /*VkDebugUtilsMessageSeverityFlagsEXT*/int severity,
                                          /*VkDebugUtilsMessageTypeFlagsEXT*/int type,
                                          /*VkDebugUtilsMessengerEXT*/final long[] pDebugMessenger,
                                          VkAllocationCallbacks allocation_callbacks) {

        if (debug_callback == null) debug_callback = default_debug_callback;
        final VkDebugUtilsMessengerCreateInfoEXT messengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.create();
        messengerCreateInfo.sType( VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
        messengerCreateInfo.pNext( 0);
        messengerCreateInfo.messageSeverity( severity);
        messengerCreateInfo.messageType( type);
        messengerCreateInfo.pfnUserCallback( debug_callback);

        VkbVulkanFunctions.PFN_vkCreateDebugUtilsMessengerEXT createMessengerFunc;
        //vulkan_functions().get_inst_proc_addr(createMessengerFunc, "vkCreateDebugUtilsMessengerEXT");
        createMessengerFunc = new VkbVulkanFunctions.PFN_vkCreateDebugUtilsMessengerEXT() {
            public int invoke(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT pCreateInfo, VkAllocationCallbacks pAllocator, final long[] pMessenger) {
                return EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance,pCreateInfo,pAllocator,pMessenger);
            }
        };

        if (createMessengerFunc != null) {
            return createMessengerFunc.invoke(instance, messengerCreateInfo, allocation_callbacks, pDebugMessenger);
        } else {
            return VK_ERROR_EXTENSION_NOT_PRESENT;
        }
    }

    /*306*/ public static final VkDebugUtilsMessengerCallbackEXT default_debug_callback = VkDebugUtilsMessengerCallbackEXT.create(new VkDebugUtilsMessengerCallbackEXTI() {
        @Override
        public int invoke(int messageSeverity, int messageType, long pCallbackData, long voide) {
            var ms = to_string_message_severity(messageSeverity);
            var mt = to_string_message_type(messageType);
            VkDebugUtilsMessengerCallbackDataEXT cbd = VkDebugUtilsMessengerCallbackDataEXT.createSafe(pCallbackData);
            String msg = "";
            if(null != cbd) {
                msg = cbd.pMessageString();
            }
            System.out.println("["+ms+": "+mt+"]\n"+msg+"\n");

            return VK_FALSE;
        }
    });

    /*318*/ public static boolean check_layer_supported(final List<VkLayerProperties> available_layers, String layer_name) {
        if (null==layer_name) return false;
        for (var layer_properties : available_layers) {
            if (Objects.equals(layer_name, layer_properties.layerNameString())) {
                return true;
            }
        }
        return false;
    }

    /*328*/ public static boolean check_layers_supported(List<VkLayerProperties> available_layers,
                                List<String> layer_names) {
        boolean all_found = true;
        for (var layer_name : layer_names) {
            boolean found = check_layer_supported(available_layers, layer_name);
            if (!found) all_found = false;
        }
        return all_found;
    }

    /*338*/ public static boolean check_extension_supported(
            final List<VkExtensionProperties> available_extensions, String extension_name) {
        if (extension_name == null) return false;
        for (var extension_properties : available_extensions) {
            if (Objects.equals(extension_name, extension_properties.extensionNameString())) {
                return true;
            }
        }
        return false;
    }

    /*349*/ public static boolean check_extensions_supported(final List<VkExtensionProperties> available_extensions,
                                    final List<String> extension_names) {
        boolean all_found = true;
        for (var extension_name : extension_names) {
            boolean found = check_extension_supported(available_extensions, extension_name);
            if (!found) all_found = false;
        }
        return all_found;
    }

    //template <typename T>
    /*360 */public static <T extends VkInstanceCreateInfo> void setup_pNext_chain(T structure, final List<VkBaseOutStructure> structs) {
        structure.pNext(0);
        if (structs.size() <= 0) return;
        for (int i = 0; i < structs.size() - 1; i++) {
            structs.get(i).pNext(structs.get(i + 1));
        }
        structure.pNext(structs.get(0).address());
    }

    //template <typename T>
    /*360 */public static <T extends VkDeviceCreateInfo> void setup_pNext_chain(T structure, final List<VkBaseOutStructure> structs) {
        structure.pNext(0);
        if (structs.size() <= 0) return;
        for (int i = 0; i < structs.size() - 1; i++) {
            structs.get(i).pNext(structs.get(i + 1));
        }
        structure.pNext(structs.get(0).address());
    }

    /*368*/ public static final String validation_layer_name = "VK_LAYER_KHRONOS_validation";

    static final InstanceErrorCategory instance_error_category = new InstanceErrorCategory();

    /*408*/ public static error_code make_error_code(VkbInstanceError instance_error) {
        return new error_code( instance_error.ordinal(), instance_error_category );
    }

    /*424*/ public static String to_string(VkbInstanceError err) {
        switch (err) {
            case vulkan_unavailable:
                return "vulkan_unavailable";
            case vulkan_version_unavailable:
                return "vulkan_version_unavailable";
            case vulkan_version_1_1_unavailable:
                return "vulkan_version_1_1_unavailable";
            case vulkan_version_1_2_unavailable:
                return "vulkan_version_1_2_unavailable";
            case failed_create_debug_messenger:
                return "failed_create_debug_messenger";
            case failed_create_instance:
                return "failed_create_instance";
            case requested_layers_not_present:
                return "requested_layers_not_present";
            case requested_extensions_not_present:
                return "requested_extensions_not_present";
            case windowing_extensions_not_present:
                return "windowing_extensions_not_present";
            default:
                return "";
        }
    }

    // Sentinel value, used in implementation only
    public static final int QUEUE_INDEX_MAX_VALUE = 65536;

    /*842*/
    static List<String> check_device_extension_support(
            VkPhysicalDevice device, List<String> desired_extensions) {
        final List<VkExtensionProperties> available_extensions = new ArrayList<>();
        var available_extensions_ret = get_vector/*<VkExtensionProperties>*/(
        available_extensions, vulkan_functions().fp_vkEnumerateDeviceExtensionProperties, device, null);
        if (available_extensions_ret != VK_SUCCESS) return new ArrayList<>();

        final List<String> extensions_to_enable = new ArrayList<>();
        for (var extension : available_extensions) {
            for (var req_ext : desired_extensions) {
                if (Objects.equals(req_ext, extension.extensionNameString())) {
                    extensions_to_enable.add(req_ext);
                    break;
                }
            }
        }
        return extensions_to_enable;
    }

    // clang-format off
    /*862*/ public static boolean supports_features(VkPhysicalDeviceFeatures supported,
                           VkPhysicalDeviceFeatures requested,
                           final List<VkbGenericFeaturesPNextNode> extension_supported,
                           final List<VkbGenericFeaturesPNextNode> extension_requested) {

        return true; // TODO
    }

    // Finds the queue which is separate from the graphics queue and has the desired flag and not the
    // undesired flag, but will select it if no better options are available compute support. Returns
    // QUEUE_INDEX_MAX_VALUE if none is found.
    /*940*/ public static int get_separate_queue_index(List<VkQueueFamilyProperties> families,
                                      /*VkQueueFlags*/int desired_flags,
                                      /*VkQueueFlags*/int undesired_flags) {
        int index = QUEUE_INDEX_MAX_VALUE;
        for (int i = 0; i < (int)(families.size()); i++) {
            if ((families.get(i).queueFlags() & desired_flags)!=0 && ((families.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) == 0)) {
                if ((families.get(i).queueFlags() & undesired_flags) == 0) {
                    return i;
                } else {
                    index = i;
                }
            }
        }
        return index;
    }

    // finds the first queue which supports only the desired flag (not graphics or transfer). Returns QUEUE_INDEX_MAX_VALUE if none is found.
    /*957*/
    static int get_dedicated_queue_index(List<VkQueueFamilyProperties> families,
            /*VkQueueFlags*/int desired_flags,
            /*VkQueueFlags*/int undesired_flags) {
        for (int i = 0; i < (int)(families.size()); i++) {
            if ((families.get(i).queueFlags() & desired_flags)!=0 && (families.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) == 0 &&
                    (families.get(i).queueFlags() & undesired_flags) == 0)
                return i;
        }
        return QUEUE_INDEX_MAX_VALUE;
    }

    // finds the first queue which supports presenting. returns QUEUE_INDEX_MAX_VALUE if none is found
    /*969*/ public static int get_present_queue_index(VkPhysicalDevice phys_device,
                                     /*VkSurfaceKHR*/long surface,
                                     final List<VkQueueFamilyProperties> families) {
        for (int i = 0; i < (int)(families.size()); i++) {
            /*VkBool32*/final int[] presentSupport = new int[1];
            if (surface != VK_NULL_HANDLE) {
                int res = vulkan_functions().fp_vkGetPhysicalDeviceSurfaceSupportKHR.invoke(
                        phys_device, i, surface, presentSupport);
                if (res != VK_SUCCESS)
                    return QUEUE_INDEX_MAX_VALUE; // TODO: determine if this should fail another way
            }
            if (presentSupport[0] == VK_TRUE) return i;
        }
        return QUEUE_INDEX_MAX_VALUE;
    }
}
