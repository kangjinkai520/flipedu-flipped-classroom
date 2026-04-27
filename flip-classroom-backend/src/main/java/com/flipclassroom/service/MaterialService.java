package com.flipclassroom.service;

import com.flipclassroom.dto.material.CreateMaterialRequest;
import com.flipclassroom.dto.material.CreateMaterialResponse;
import com.flipclassroom.dto.material.MaterialDetailResponse;
import com.flipclassroom.dto.material.MaterialListItemResponse;
import com.flipclassroom.dto.material.UpdateMaterialRequest;
import com.flipclassroom.dto.material.UpdateMaterialResponse;

import java.util.List;

public interface MaterialService {

    List<MaterialListItemResponse> listMaterials(Long courseId); // 查询课程教学资料列表

    CreateMaterialResponse createMaterial(Long courseId, CreateMaterialRequest request); // 新增教学资料

    MaterialDetailResponse getMaterial(Long id); // 查询教学资料详情

    UpdateMaterialResponse updateMaterial(Long id, UpdateMaterialRequest request); // 修改教学资料基础信息

    void updateMaterialStatus(Long id, Integer status); // 修改教学资料状态
}
