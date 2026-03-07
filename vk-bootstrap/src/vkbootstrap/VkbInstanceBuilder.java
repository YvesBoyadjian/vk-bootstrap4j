package vkbootstrap;

import org.lwjgl.vulkan.*;
import port.Port;
import port.error_code;
import vkbootstrap.VkbVulkanFunctions.PFN_vkEnumerateInstanceVersion;
import vulkan.VkInstanceCreateFlagBits;
import vulkan.VulkanCore;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.EXTValidationFeatures.VK_STRUCTURE_TYPE_VALIDATION_FEATURES_EXT;
import static org.lwjgl.vulkan.EXTValidationFlags.VK_STRUCTURE_TYPE_VALIDATION_FLAGS_EXT;
import static org.lwjgl.vulkan.VK10.*;
import static vkbootstrap.VkBootstrap.make_error_code;
import static vkbootstrap.VkBootstrap.vulkan_functions;

public class VkbInstanceBuilder {

    private static class InstanceInfo {

        // VkApplicationInfo
		String app_name = null;
		String engine_name = null;
        int application_version = 0;
        int engine_version = 0;
        int minimum_instance_version = 0;
        int required_api_version = VK_MAKE_VERSION(1, 0, 0);

        // VkInstanceCreateInfo
        final List<String> layers = new ArrayList<>();
        final List<String> extensions = new ArrayList<>();
        /*VkInstanceCreateFlags*/int flags = 0;
        final List<VkLayerSettingEXT> layer_settings = new ArrayList<>();

        // debug callback
        /*PFN_*/ VkDebugUtilsMessengerCallbackEXT debug_callback = null;
        /*VkDebugUtilsMessageSeverityFlagsEXT*/int debug_message_severity =
                VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
        /*VkDebugUtilsMessageTypeFlagsEXT*/int debug_message_type =
                VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
                        VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
        long debug_user_data_pointer = 0;

        // validation features
        public final List</*VkValidationCheckEXT*/Integer> disabled_validation_checks = new ArrayList<>();
        public final List</*VkValidationFeatureEnableEXT*/Integer> enabled_validation_features = new ArrayList<>();
        public final List</*VkValidationFeatureDisableEXT*/Integer> disabled_validation_features = new ArrayList<>();

        // Custom allocator
        VkAllocationCallbacks allocation_callbacks = null;//VK_NULL_HANDLE;

        boolean request_validation_layers = false;
        boolean enable_validation_layers = false;
        boolean use_debug_messenger = false;
        boolean headless_context = false;

        VkbVulkanFunctions.PFN_vkGetInstanceProcAddr fp_vkGetInstanceProcAddr = null;
    }

    private final InstanceInfo info = new InstanceInfo();

    private interface PFN_check_add_window_ext {
        boolean invoke(String name);
    }

    private final static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isAndroid() {
        return false; //TODO
    }

    public static boolean isDirect2Display() {
        return false; //TODO
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0
                || OS.indexOf("nux") >= 0
                || OS.indexOf("aix") > 0);
    }

    /*573*/ public Result<VkbInstance> build() {

        var sys_info_ret = VkbSystemInfo.get_system_info(info.fp_vkGetInstanceProcAddr);
        if (sys_info_ret.not()) return new Result(sys_info_ret.error());
        var system = sys_info_ret.value();
        
        final int[] instance_version = new int[1]; instance_version[0] = Version.VKB_VK_API_VERSION_1_0;

        if (info.minimum_instance_version > Version.VKB_VK_API_VERSION_1_0 || info.required_api_version > Version.VKB_VK_API_VERSION_1_0) {
            PFN_vkEnumerateInstanceVersion pfn_vkEnumerateInstanceVersion = vulkan_functions().fp_vkEnumerateInstanceVersion;

            if (pfn_vkEnumerateInstanceVersion != null) {
                /*VkResult*/int res = pfn_vkEnumerateInstanceVersion.invoke(instance_version);
                // Should always return VK_SUCCESS
                if (res != VK_SUCCESS && (info.required_api_version > 0 || info.minimum_instance_version > 0)) {
                    return new Result(make_error_code(VkbInstanceError.vulkan_version_unavailable));
                }
            }
            if (pfn_vkEnumerateInstanceVersion == null ||
                (info.minimum_instance_version > 0 && instance_version[0] < info.minimum_instance_version) ||
                (info.minimum_instance_version == 0 && instance_version[0] < info.required_api_version)) {

                int version_error = info.minimum_instance_version == 0 ? info.required_api_version : info.minimum_instance_version;
                if (VK_VERSION_MINOR(version_error) == 4)
                    return new Result(make_error_code(VkbInstanceError.vulkan_version_1_4_unavailable));
                else if (VK_VERSION_MINOR(version_error) == 3)
                    return new Result(make_error_code(VkbInstanceError.vulkan_version_1_3_unavailable));
                else if (VK_VERSION_MINOR(version_error) == 2)
                    return new Result(make_error_code(VkbInstanceError.vulkan_version_1_2_unavailable));
                else if (VK_VERSION_MINOR(version_error) == 1)
                    return new Result(make_error_code(VkbInstanceError.vulkan_version_1_1_unavailable));
                else
                    return new Result(make_error_code(VkbInstanceError.vulkan_version_unavailable));
            }
        }

        // The API version to use is set by required_api_version, unless it isn't set, then it comes from minimum_instance_version
        int api_version = VK_MAKE_VERSION(1, 0, 0);
        if (info.required_api_version > Version.VKB_VK_API_VERSION_1_0) {
            api_version = info.required_api_version;
        } else if (info.minimum_instance_version > 0) {
            api_version = info.minimum_instance_version;
        }

        final VkApplicationInfo app_info = VkApplicationInfo.create();
        app_info.set(
        /*app_info.sType =*/ VK_STRUCTURE_TYPE_APPLICATION_INFO,
        /*app_info.pNext =*/ 0,
        /*app_info.pApplicationName =*/ memUTF8(info.app_name != null ? info.app_name : ""),
        /*app_info.applicationVersion =*/ info.application_version,
        /*app_info.pEngineName =*/ memUTF8(info.engine_name != null ? info.engine_name : ""),
        /*app_info.engineVersion =*/ info.engine_version,
        /*app_info.apiVersion =*/ api_version);

        final List<String> extensions = new ArrayList<>();
        for (var ext : info.extensions)
            extensions.add(ext);
        if (info.debug_callback != null && system.debug_utils_available) {
            extensions.add(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
        }
        
        boolean properties2_ext_enabled =
                api_version < Version.VKB_VK_API_VERSION_1_1 && VkBootstrap.check_extension_supported(system.available_extensions,
                		VulkanCore.VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME);
            if (properties2_ext_enabled) {
                extensions.add(VulkanCore.VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME);
            }

            if (info.layer_settings.size() > 0) {
                extensions.add(VulkanCore.VK_EXT_LAYER_SETTINGS_EXTENSION_NAME);
            }

//        #if defined(VK_KHR_portability_enumeration)
            boolean portability_enumeration_support =
            		VkBootstrap.check_extension_supported(system.available_extensions, VulkanCore.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME);
            if (portability_enumeration_support) {
                extensions.add(VulkanCore.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME);
            }
//        #else
//            bool portability_enumeration_support = false;
//        #endif
        
        

        if (!info.headless_context) {
            PFN_check_add_window_ext check_add_window_ext = (String name) -> {
                if (!VkBootstrap.check_extension_supported(system.available_extensions, name)) return false;
                extensions.add(name);
                return true;
            };
            boolean khr_surface_added = check_add_window_ext.invoke("VK_KHR_surface");
            boolean added_window_exts = false;
/*#if defined(_WIN32)*/if(isWindows()) {
                added_window_exts = check_add_window_ext.invoke("VK_KHR_win32_surface");
            }
/*#elif defined(__ANDROID__)*/else if(isAndroid()) {
                added_window_exts = check_add_window_ext.invoke("VK_KHR_android_surface");
            }
/*#elif defined(_DIRECT2DISPLAY)*/else if(isDirect2Display()) {
                added_window_exts = check_add_window_ext.invoke("VK_KHR_display");
            }
/*#elif defined(__linux__)*/ else if(isUnix()) {
                // make sure all three calls to check_add_window_ext, don't allow short circuiting
                added_window_exts = check_add_window_ext.invoke("VK_KHR_xcb_surface");
                added_window_exts = check_add_window_ext.invoke("VK_KHR_xlib_surface") || added_window_exts;
                added_window_exts = check_add_window_ext.invoke("VK_KHR_wayland_surface") || added_window_exts;
            }
/*#elif defined(__APPLE__)*/else if(isMac()) {
                added_window_exts = check_add_window_ext.invoke("VK_EXT_metal_surface");
            }
//#endif
            if (!khr_surface_added || !added_window_exts)
                return new Result(make_error_code(VkbInstanceError.windowing_extensions_not_present));
        }
        
        List<String> unsupported_extensions = VkBootstrap.check_extensions_supported(system.available_extensions, extensions);
        if (!unsupported_extensions.isEmpty()) {
            return new Result(make_error_code(VkbInstanceError.requested_extensions_not_present), unsupported_extensions);
        }
        
        /*656*/ final List<String> layers = new ArrayList<>();
        for (var layer : info.layers)
            layers.add(layer);

        if (info.enable_validation_layers || (info.request_validation_layers && system.validation_layers_available)) {
            layers.add(VkBootstrap.validation_layer_name);
        }
        List<String> unsupported_layers = VkBootstrap.check_layers_supported(system.available_layers, layers);
        if (!unsupported_layers.isEmpty()) {
            return new Result(make_error_code(VkbInstanceError.requested_layers_not_present), unsupported_layers);
        }
        
        final List<VkBaseOutStructure> pNext_chain = new ArrayList<>();

        final VkDebugUtilsMessengerCreateInfoEXT messengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.create();
        if (info.use_debug_messenger) {
            messengerCreateInfo.sType( VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
            messengerCreateInfo.pNext(0);
            messengerCreateInfo.messageSeverity( info.debug_message_severity);
            messengerCreateInfo.messageType( info.debug_message_type);
            messengerCreateInfo.pfnUserCallback( VkDebugUtilsMessengerCallbackEXT.create(info.debug_callback));
            pNext_chain.add(VkBaseOutStructure.createSafe(messengerCreateInfo.address()));
        }

        final VkValidationFeaturesEXT features = VkValidationFeaturesEXT.create();
        if (info.enabled_validation_features.size() != 0 || info.disabled_validation_features.size() != 0) {
            features.sType( VK_STRUCTURE_TYPE_VALIDATION_FEATURES_EXT);
            features.pNext( 0);
//            features.enabledValidationFeatureCount( java port
//                    (int)info.enabled_validation_features.size());
            features.pEnabledValidationFeatures( Port.datai(info.enabled_validation_features));
//            features.disabledValidationFeatureCount( java port
//                    (int)(info.disabled_validation_features.size());
            features.pDisabledValidationFeatures( Port.datai(info.disabled_validation_features));
            pNext_chain.add(VkBaseOutStructure.createSafe(features.address()));
        }

        final VkValidationFlagsEXT checks = VkValidationFlagsEXT.create();
        if (info.disabled_validation_checks.size() != 0) {
            checks.sType( VK_STRUCTURE_TYPE_VALIDATION_FLAGS_EXT);
            checks.pNext( 0);
            //checks.disabledValidationCheckCount( (int)info.disabled_validation_checks.size()); java port
            checks.pDisabledValidationChecks( Port.datai(info.disabled_validation_checks));
            pNext_chain.add(VkBaseOutStructure.createSafe(checks.address()));
        }

        final VkLayerSettingsCreateInfoEXT layer_settings_ci = VkLayerSettingsCreateInfoEXT.create();
        if (info.layer_settings.size() > 0) {
            layer_settings_ci.sType( EXTLayerSettings.VK_STRUCTURE_TYPE_LAYER_SETTINGS_CREATE_INFO_EXT);
            layer_settings_ci.pNext( 0);
            //layer_settings_ci.settingCount( (int)(info.layer_settings.size()));
            
            VkLayerSettingEXT.Buffer buf = VkLayerSettingEXT.create(info.layer_settings.size());
            for ( int i=0; i < info.layer_settings.size(); i++) {
            		buf.put(i, info.layer_settings.get(i));
            }
            
            layer_settings_ci.pSettings( buf/*info.layer_settings*/);
            pNext_chain.add(VkBaseOutStructure.createSafe(layer_settings_ci.address()));
        }

        final VkInstanceCreateInfo instance_create_info = VkInstanceCreateInfo.create();
        instance_create_info.sType( VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
        VkBootstrap.setup_pNext_chain(instance_create_info, pNext_chain);
        for (var node : pNext_chain) {
            assert(node.sType() != VK_STRUCTURE_TYPE_APPLICATION_INFO);
        }
        instance_create_info.flags(info.flags);
        instance_create_info.pApplicationInfo( app_info);
        //instance_create_info.enabledExtensionCount((int)(extensions.size())); java port
        instance_create_info.ppEnabledExtensionNames( Port.datastr(extensions));
        //instance_create_info.enabledLayerCount((int)(layers.size())); java port
        instance_create_info.ppEnabledLayerNames( Port.datastr(layers));
//        #if defined(VK_KHR_portability_enumeration)
        if (portability_enumeration_support) {
            instance_create_info.flags(instance_create_info.flags() | VkInstanceCreateFlagBits.VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR);
        }
//    #endif

        final VkbInstance instance = new VkbInstance();
        /*VkResult*/int res = VkBootstrap.vulkan_functions().fp_vkCreateInstance.invoke(
                instance_create_info, info.allocation_callbacks, instance.instance);
        if (res != VK_SUCCESS)
            return new Result<VkbInstance>(new error_code(VkbInstanceError.failed_create_instance.ordinal()), res);

        vulkan_functions().init_instance_funcs(instance.instance[0]);

        if (info.use_debug_messenger) {
            res = VkBootstrap.create_debug_utils_messenger(instance.instance[0],
                    info.debug_callback,
                    info.debug_message_severity,
                    info.debug_message_type,
                    info.debug_user_data_pointer,
                    instance.debug_messenger,
                    info.allocation_callbacks);
            if (res != VK_SUCCESS) {
                return new Result<VkbInstance>(new error_code(VkbInstanceError.failed_create_debug_messenger.ordinal()), res);
            }
        }

        instance.headless = info.headless_context;
        instance.properties2_ext_enabled = properties2_ext_enabled;
        instance.allocation_callbacks = info.allocation_callbacks;
        instance.instance_version = instance_version[0];
        instance.api_version = api_version;
        instance.fp_vkGetInstanceProcAddr = vulkan_functions().ptr_vkGetInstanceProcAddr;
        instance.fp_vkGetDeviceProcAddr = vulkan_functions().fp_vkGetDeviceProcAddr;
        return new Result(instance);
    }

    /*733*/ public VkbInstanceBuilder set_app_name (String app_name) {
        if (null == app_name) return this;
        info.app_name = app_name;
        return this;
    }

    /*751*/ public VkbInstanceBuilder require_api_version (int major, int minor, int patch) {
        info.required_api_version = VK_MAKE_VERSION (major, minor, patch);
        return this;
    }

    // Checks if the validation layers are available and loads them if they are.
    /*784*/ public VkbInstanceBuilder request_validation_layers() {
        return request_validation_layers(true);
    }
    public VkbInstanceBuilder request_validation_layers(boolean enable_validation) {
        info.request_validation_layers = enable_validation;
        return this;
    }

    // Use a default debug callback that prints to standard out.
    /*788*/ public VkbInstanceBuilder use_default_debug_messenger() {
        info.use_debug_messenger = true;
        info.debug_callback = VkBootstrap.default_debug_callback;
        return this;
    }
}
