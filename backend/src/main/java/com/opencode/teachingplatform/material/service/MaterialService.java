package com.opencode.teachingplatform.material.service;

import com.opencode.teachingplatform.auth.security.CurrentUser;
import com.opencode.teachingplatform.common.enums.MaterialVisibility;
import com.opencode.teachingplatform.common.enums.UserRole;
import com.opencode.teachingplatform.common.exception.BusinessException;
import com.opencode.teachingplatform.common.file.LocalFileStorageService;
import com.opencode.teachingplatform.material.dto.MaterialRequests.UpdateMaterialRequest;
import com.opencode.teachingplatform.material.dto.MaterialRequests.UploadMaterialRequest;
import com.opencode.teachingplatform.material.entity.CourseMaterial;
import com.opencode.teachingplatform.material.repository.CourseMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class MaterialService {

    private final CourseMaterialRepository courseMaterialRepository;
    private final LocalFileStorageService localFileStorageService;

    public MaterialService(CourseMaterialRepository courseMaterialRepository, LocalFileStorageService localFileStorageService) {
        this.courseMaterialRepository = courseMaterialRepository;
        this.localFileStorageService = localFileStorageService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listMaterials(CurrentUser currentUser) {
        List<CourseMaterial> materials = currentUser.role() == UserRole.TEACHER
                ? courseMaterialRepository.findByUploaderUserIdOrderByIdDesc(currentUser.id())
                : courseMaterialRepository.findByVisibilityOrderByIdDesc(MaterialVisibility.ALL);
        return materials.stream().map(this::toView).toList();
    }

    @Transactional
    public Map<String, Object> uploadMaterial(CurrentUser currentUser, UploadMaterialRequest request) {
        if (currentUser.role() != UserRole.TEACHER) {
            throw new BusinessException(40300, "无权限上传资料");
        }
        String storedPath = localFileStorageService.store("materials", request.getFile());
        String originalFilename = request.getFile().getOriginalFilename();

        CourseMaterial material = new CourseMaterial();
        material.setTitle(defaultIfBlank(request.getTitle(), "未命名资料"));
        material.setCategory(defaultIfBlank(request.getCategory(), "COURSE"));
        material.setDescription(defaultIfBlank(request.getDescription(), ""));
        material.setFilePath(storedPath);
        material.setFileName(originalFilename == null || originalFilename.isBlank() ? "material.bin" : originalFilename);
        material.setUploaderUserId(currentUser.id());
        material.setVisibility(request.getVisibility() == null ? MaterialVisibility.ALL : request.getVisibility());
        CourseMaterial saved = courseMaterialRepository.save(material);
        return toView(saved);
    }

    @Transactional
    public Map<String, Object> updateMaterial(CurrentUser currentUser, Long id, UpdateMaterialRequest request) {
        CourseMaterial material = findOwnedMaterial(currentUser, id);
        if (request.getTitle() != null) {
            material.setTitle(request.getTitle());
        }
        if (request.getCategory() != null) {
            material.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            material.setDescription(request.getDescription());
        }
        if (request.getVisibility() != null) {
            material.setVisibility(request.getVisibility());
        }
        return toView(courseMaterialRepository.save(material));
    }

    @Transactional
    public void deleteMaterial(CurrentUser currentUser, Long id) {
        CourseMaterial material = findOwnedMaterial(currentUser, id);
        courseMaterialRepository.delete(material);
        localFileStorageService.delete(material.getFilePath());
    }

    @Transactional(readOnly = true)
    public DownloadedMaterial downloadMaterial(CurrentUser currentUser, Long id) {
        CourseMaterial material = courseMaterialRepository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "资料不存在"));
        if (currentUser.role() == UserRole.TEACHER) {
            if (!material.getUploaderUserId().equals(currentUser.id())) {
                throw new BusinessException(40300, "无权限访问该资料");
            }
        } else if (material.getVisibility() != MaterialVisibility.ALL) {
            throw new BusinessException(40300, "无权限访问该资料");
        }
        return new DownloadedMaterial(material.getFileName(), localFileStorageService.read(material.getFilePath()));
    }

    private CourseMaterial findOwnedMaterial(CurrentUser currentUser, Long id) {
        if (currentUser.role() != UserRole.TEACHER) {
            throw new BusinessException(40300, "无权限管理资料");
        }
        return courseMaterialRepository.findByIdAndUploaderUserId(id, currentUser.id())
                .orElseThrow(() -> new BusinessException(40300, "无权限访问该资料"));
    }

    private Map<String, Object> toView(CourseMaterial material) {
        return Map.of(
                "id", material.getId(),
                "title", material.getTitle(),
                "category", material.getCategory(),
                "description", material.getDescription(),
                "fileName", material.getFileName(),
                "visibility", material.getVisibility().name(),
                "uploaderUserId", material.getUploaderUserId(),
                "downloadUrl", "/api/v1/materials/" + material.getId() + "/download"
        );
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record DownloadedMaterial(String fileName, byte[] bytes) {
    }
}
