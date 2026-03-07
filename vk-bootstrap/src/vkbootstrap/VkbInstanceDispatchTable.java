/*
 * Copyright © 2021 Cody Goodson (contact@vibimanx.com)
 * Copyright © 2022-2025 Charles Giessen (charles@lunarg.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
// This file is a part of VkBootstrap
// https://github.com/charles-lunarg/vk-bootstrap
package vkbootstrap;

import org.lwjgl.vulkan.VkInstance;

import vkbootstrap.VkbVulkanFunctions.PFN_vkGetInstanceProcAddr;

/**
 * 
 */
public class VkbInstanceDispatchTable {

	public VkbInstanceDispatchTable() {
	}
	
	public VkbInstanceDispatchTable(VkInstance instance, PFN_vkGetInstanceProcAddr procAddr) {
		this.instance = instance;
		populated = true;
		
		//TODO
	}

	public boolean is_populated() {
		return populated;
	}
	
	public VkInstance instance;	
	
	private boolean populated;

	public void copyFrom(VkbInstanceDispatchTable other) {
		instance = other.instance;
		populated = other.populated;
	}
}
