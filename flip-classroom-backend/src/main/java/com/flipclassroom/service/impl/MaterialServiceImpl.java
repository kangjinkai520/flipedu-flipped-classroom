package com.flipclassroom.service.impl;

import com.flipclassroom.dto.material.CreateMaterialRequest;
import com.flipclassroom.dto.material.CreateMaterialResponse;
import com.flipclassroom.dto.material.MaterialDetailResponse;
import com.flipclassroom.dto.material.MaterialListItemResponse;
import com.flipclassroom.dto.material.UpdateMaterialRequest;
import com.flipclassroom.dto.material.UpdateMaterialResponse;
import com.flipclassroom.entity.Course;
import com.flipclassroom.entity.TeachingMaterial;
import com.flipclassroom.mapper.CourseMapper;
import com.flipclassroom.mapper.MaterialMapper;
import com.flipclassroom.service.MaterialService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialMapper materialMapper;
    private final CourseMapper courseMapper;

    public MaterialServiceImpl(MaterialMapper materialMapper, CourseMapper courseMapper) {
        this.materialMapper = materialMapper;
        this.courseMapper = courseMapper;
    }

    @Override
    public List<MaterialListItemResponse> listMaterials(Long courseId) { // 查询课程教学资料列表
        ensureCourseExists(courseId);
        return materialMapper.findByCourseId(courseId)
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public CreateMaterialResponse createMaterial(Long courseId, CreateMaterialRequest request) { // 新增教学资料
        ensureCourseExists(courseId);
        validateCreateRequest(request);

        TeachingMaterial material = new TeachingMaterial();
        material.setCourseId(courseId);
        material.setTitle(request.getTitle().trim());
        material.setMaterialType(request.getMaterialType().trim().toLowerCase());
        material.setDescription(blankToNull(request.getDescription()));
        material.setMaterialUrl(request.getMaterialUrl().trim());
        material.setPublishTime(normalizePublishTime(request.getPublishTime()));
        material.setStatus(normalizeStatus(request.getStatus(), 1));

        Long id = materialMapper.insert(material);
        if (id == null) {
            throw new IllegalStateException("Failed to create material");
        }

        return new CreateMaterialResponse(
                id,
                material.getCourseId(),
                material.getTitle(),
                material.getMaterialType(),
                material.getMaterialUrl(),
                material.getPublishTime(),
                material.getStatus()
        );
    }

    @Override
    public MaterialDetailResponse getMaterial(Long id) { // 查询教学资料详情
        if (id == null) {
            throw new IllegalArgumentException("Material id cannot be empty");
        }

        TeachingMaterial material = materialMapper.findById(id);
        if (material == null) {
            throw new IllegalArgumentException("Material does not exist");
        }

        return toDetail(material);
    }

    @Override
    public UpdateMaterialResponse updateMaterial(Long id, UpdateMaterialRequest request) { // 修改教学资料基础信息
        if (id == null) {
            throw new IllegalArgumentException("Material id cannot be empty");
        }
        validateUpdateRequest(request);

        TeachingMaterial existingMaterial = materialMapper.findById(id);
        if (existingMaterial == null) {
            throw new IllegalArgumentException("Material does not exist");
        }

        ensureCourseExists(existingMaterial.getCourseId());

        existingMaterial.setTitle(request.getTitle().trim());
        existingMaterial.setMaterialType(request.getMaterialType().trim().toLowerCase());
        existingMaterial.setDescription(blankToNull(request.getDescription()));
        existingMaterial.setMaterialUrl(request.getMaterialUrl().trim());
        existingMaterial.setPublishTime(normalizePublishTime(request.getPublishTime()));
        existingMaterial.setStatus(normalizeStatus(request.getStatus(), existingMaterial.getStatus()));

        int affectedRows = materialMapper.updateBasicInfo(existingMaterial);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update material");
        }

        return new UpdateMaterialResponse(
                existingMaterial.getId(),
                existingMaterial.getCourseId(),
                existingMaterial.getTitle(),
                existingMaterial.getMaterialType(),
                existingMaterial.getDescription(),
                existingMaterial.getMaterialUrl(),
                existingMaterial.getPublishTime(),
                existingMaterial.getStatus()
        );
    }

    @Override
    public void updateMaterialStatus(Long id, Integer status) { // 修改教学资料状态
        if (id == null) {
            throw new IllegalArgumentException("Material id cannot be empty");
        }
        Integer normalizedStatus = normalizeStatus(status, null);
        TeachingMaterial material = materialMapper.findById(id);
        if (material == null) {
            throw new IllegalArgumentException("Material does not exist");
        }
        int affectedRows = materialMapper.updateStatus(id, normalizedStatus);
        if (affectedRows == 0) {
            throw new IllegalStateException("Failed to update material status");
        }
    }

    private MaterialListItemResponse toListItem(TeachingMaterial material) { // 把数据库实体转换成教学资料列表返回对象
        return new MaterialListItemResponse(
                material.getId(),
                material.getCourseId(),
                material.getTitle(),
                material.getMaterialType(),
                material.getDescription(),
                material.getMaterialUrl(),
                material.getPublishTime(),
                material.getStatus()
        );
    }

    private MaterialDetailResponse toDetail(TeachingMaterial material) { // 把数据库实体转换成教学资料详情返回对象
        return new MaterialDetailResponse(
                material.getId(),
                material.getCourseId(),
                material.getTitle(),
                material.getMaterialType(),
                material.getDescription(),
                material.getMaterialUrl(),
                material.getPublishTime(),
                material.getStatus()
        );
    }

    private Course ensureCourseExists(Long courseId) { // 确认课程存在
        if (courseId == null) {
            throw new IllegalArgumentException("Course id cannot be empty");
        }
        Course course = courseMapper.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course does not exist");
        }
        return course;
    }

    private void validateCreateRequest(CreateMaterialRequest request) { // 校验新增教学资料请求
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (isBlank(request.getTitle()) || isBlank(request.getMaterialType()) || isBlank(request.getMaterialUrl())) {
            throw new IllegalArgumentException("Title, materialType and materialUrl are required");
        }
        normalizeStatus(request.getStatus(), 1);
        normalizePublishTime(request.getPublishTime());
    }

    private void validateUpdateRequest(UpdateMaterialRequest request) { // 校验修改教学资料请求
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be empty");
        }
        if (isBlank(request.getTitle()) || isBlank(request.getMaterialType()) || isBlank(request.getMaterialUrl())) {
            throw new IllegalArgumentException("Title, materialType and materialUrl are required");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }
        normalizeStatus(request.getStatus(), 1);
        normalizePublishTime(request.getPublishTime());
    }

    private Integer normalizeStatus(Integer status, Integer defaultStatus) { // 统一并校验教学资料状态
        Integer finalStatus = status == null ? defaultStatus : status;
        if (finalStatus == null || (finalStatus != 0 && finalStatus != 1)) {
            throw new IllegalArgumentException("Status must be 0 or 1");
        }
        return finalStatus;
    }

    private String normalizePublishTime(String publishTime) { // 统一教学资料发布时间，未传则交给数据库默认值
        return isBlank(publishTime) ? null : publishTime.trim();
    }

    private boolean isBlank(String value) { // 判断字符串是否为空或全是空格
        return value == null || value.trim().isEmpty();
    }

    private String blankToNull(String value) { // 把空字符串转换成 null，方便入库
        return isBlank(value) ? null : value.trim();
    }
}
