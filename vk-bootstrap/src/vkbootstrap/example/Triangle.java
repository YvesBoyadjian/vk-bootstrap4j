package vkbootstrap.example;

import tests.Common;
import vkbootstrap.VkbDeviceBuilder;
import vkbootstrap.VkbInstanceBuilder;
import vkbootstrap.VkbPhysicalDevice;
import vkbootstrap.VkbPhysicalDeviceSelector;

public class Triangle {
    public static void main(String[] args) {
        final Init init = new Init();
        final RenderData render_data = new RenderData();

        if (0 != device_initialization (init)) return;
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
}
