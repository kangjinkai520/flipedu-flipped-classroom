package com.flipclassroom.controller;

import com.flipclassroom.common.Result;
import com.flipclassroom.dto.material.CreateMaterialRequest;
import com.flipclassroom.dto.material.CreateMaterialResponse;
import com.flipclassroom.dto.material.MaterialDetailResponse;
import com.flipclassroom.dto.material.MaterialListItemResponse;
import com.flipclassroom.dto.material.UpdateMaterialRequest;
import com.flipclassroom.dto.material.UpdateMaterialResponse;
import com.flipclassroom.dto.material.UpdateMaterialStatusRequest;
import com.flipclassroom.service.MaterialService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping("/courses/{courseId}/materials")
    public Result<List<MaterialListItemResponse>> listMaterials(@PathVariable Long courseId) { // 查询课程教学资料列表
        try {
            return Result.success(materialService.listMaterials(courseId));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PostMapping("/courses/{courseId}/materials")
    public Result<CreateMaterialResponse> createMaterial(@PathVariable Long courseId,
                                                         @RequestBody CreateMaterialRequest request) { // 新增教学资料
        try {
            return Result.success(materialService.createMaterial(courseId, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @GetMapping("/materials/{id}")
    public Result<MaterialDetailResponse> getMaterial(@PathVariable Long id) { // 查询教学资料详情
        try {
            return Result.success(materialService.getMaterial(id));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PutMapping("/materials/{id}")
    public Result<UpdateMaterialResponse> updateMaterial(@PathVariable Long id,
                                                         @RequestBody UpdateMaterialRequest request) { // 修改教学资料基础信息
        try {
            return Result.success(materialService.updateMaterial(id, request));
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }

    @PatchMapping("/materials/{id}/status")
    public Result<String> updateMaterialStatus(@PathVariable Long id,
                                               @RequestBody UpdateMaterialStatusRequest request) { // 修改教学资料状态
        try {
            materialService.updateMaterialStatus(id, request.getStatus());
            return Result.success("material status updated");
        } catch (RuntimeException exception) {
            return Result.fail(exception.getMessage());
        }
    }
}
