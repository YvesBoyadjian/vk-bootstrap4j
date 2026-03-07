package vkbootstrap;

import port.error_code;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.util.ArrayList;
import java.util.List;

public class Error {
    public error_code type = new error_code();
    public /*VkResult*/int vk_result = VK_SUCCESS; // optional error value if a vulkan call failed
    public final List<String> detailed_failure_reasons = new ArrayList<>(); // optional list of reasons why the operation failed - mainly used to return why VkPhysicalDevices failed to be selected

    public Error() {
        // do nothing
    }

    public Error(error_code error_code) {
        type = error_code;
    }

    public Error(error_code error_code, int result) {
        type = error_code;
        vk_result = result;
    }

	public Error(error_code error_code, int result, List<String> detailed_failure_reasons) {
        type = error_code;
        vk_result = result;
        detailed_failure_reasons.addAll(detailed_failure_reasons);
	}
}
