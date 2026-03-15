/*
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
 * Copyright © 2020 Charles Giessen (charles@lunarg.com)
 */
package vkbootstrap;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkBaseOutStructure;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;

import port.Port;

/**
 * 
 */
public class VkbFeaturesChain {
	
	static class StructInfo {
		/*VkStructureType*/public int sType;
        /*size_t*/public int starting_location;
        /*size_t*/public int struct_size;

        public StructInfo(int sType, int starting_location, int struct_size) {
			this.sType = sType;
			this.starting_location = starting_location;
			this.struct_size = struct_size;
		}
	}
    final List<StructInfo> structure_infos = new ArrayList<>();
    final List<Struct<?>> structures = new ArrayList<>();

	public VkbFeaturesChain(VkbFeaturesChain other) {
		copyFrom(other);
	}

	public VkbFeaturesChain() {
	}

	public boolean empty() { return structure_infos.isEmpty(); }

	public void copyFrom(VkbFeaturesChain other) {
		structure_infos.clear(); structure_infos.addAll(other.structure_infos);
		structures.clear(); structures.addAll(other.structures);
	}


	public void add_structure(/*VkStructureType*/int sType, int struct_size, Struct<?> structure) {
//#if !defined(NDEBUG)
//    // Validation
//    assert(sType != static_cast<VkStructureType>(0) && "Features struct sType must be filled with the struct's "
//                                                       "corresponding VkStructureType enum");
//    assert(sType != VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2 &&
//           "Do not pass VkPhysicalDeviceFeatures2 as a required extension feature structure. An "
//           "instance of this is managed internally for selection criteria and device creation.");
//#endif

    var found = find_sType(sType);
    if (found != /*structure_infos.end()*/null) {
        // Merge structure into the current structure
//#if !defined(NDEBUG)
//        assert(found->starting_location + found->struct_size <= structures.size() &&
//               "Internal Consistency Error: FeatureChain::add_structure tyring to merge structures into memory that is "
//               "past the end of the structures array");
//#endif
    	VkBootstrapFeatureChain.merge_feature_struct(sType, structures, found.starting_location, structure);
    } else {
        // Add a structure into the chain
        structure_infos.add(new StructInfo( sType, structures.size(), struct_size ));
        var/*auto&*/ new_structure_info = structure_infos.get(structure_infos.size() - 1)/* back()*/;
        structures.add(Port.copyStruct(structure));
//        for (int i=0;i<struct_size;i++) {structures.add((byte)0);}//structures.insert(structures.end(), struct_size, std::byte(0));
//        Port.memcpy(structures,new_structure_info.starting_location, structure, struct_size);
    }
}
	

	private void merge_feature_struct(int sType, List<Byte> structures2, int starting_location, Struct<?> structure) {
		// TODO Auto-generated method stub
		
	}

	StructInfo/*std::vector<vkb::detail::FeaturesChain::StructInfo>::const_iterator*/ find_sType(/*VkStructureType*/int sType) {
		int numStructureInfos = structure_infos.size();
		for (int i=0; i < numStructureInfos; i++) {
			if (structure_infos.get(i).sType == sType) {
				return structure_infos.get(i);
			}
		}
		return null;
	}


	public void match_all(final List<String> error_list, final VkbFeaturesChain requested_features_chain) {
    if (structure_infos.size() != requested_features_chain.structure_infos.size()) {
        return;
    }
    for (int i = 0; i < structure_infos.size(); ++i) {
    	VkBootstrapFeatureChain.compare_feature_struct(structure_infos.get(i).sType,
            error_list,
            (structures.get(structure_infos.get(i).starting_location)),
            (requested_features_chain.structures.get(requested_features_chain.structure_infos.get(i).starting_location)));
    }
}
	
	public void create_chained_features(VkPhysicalDeviceFeatures2 features2) {
		
    features2.sType( VK11.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2);
    Struct<?> s0 = structures.get(0);
    if (s0 instanceof VkPhysicalDeviceVulkan11Features) {
    		features2.pNext((VkPhysicalDeviceVulkan11Features)s0);
    }
    else {
    		throw new RuntimeException();
    }
    
    // Write the address of structure N+1 to the pNext member of structure N
    for (int i = 0; i < structure_infos.size() - 1; i++) {
        VkBaseOutStructure structure = VkBaseOutStructure.create();// structure{};
        Port.memcpy(structure, structures.get(structure_infos.get(i).starting_location), VkBaseOutStructure.SIZEOF);                
        structure.pNext(VkBaseOutStructure.create(structures.get(structure_infos.get(i + 1).starting_location).address()));        
        Port.memcpy(structures.get(structure_infos.get(i).starting_location), structure, VkBaseOutStructure.SIZEOF);
    }
    // Write nullptr to the last structures pNext member
    VkBaseOutStructure structure = VkBaseOutStructure.create();
    Port.memcpy(structure, structures.get(structure_infos.get(structure_infos.size()-1).starting_location), VkBaseOutStructure.SIZEOF);        
    structure.pNext(null);    
    Port.memcpy(structures.get(structure_infos.get(structure_infos.size()-1).starting_location), structure, VkBaseOutStructure.SIZEOF);
}

	public List<Struct<?>> get_pNext_chain_members() {
    final List<Struct<?>> members = new ArrayList<>();
    for (var structure_info : structure_infos) {
        members.add((structures.get(structure_info.starting_location)));
    }
    return members;
}
	
}
