package com.opencode.teachingplatform.common.file;

import com.opencode.teachingplatform.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    private final Path storageRoot;

    public LocalFileStorageService(@Value("${app.file-storage-root}") String storageRoot) {
        this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
    }

    public String store(String folder, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40000, "上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String fileName = originalFilename == null || originalFilename.isBlank() ? "material.bin" : Path.of(originalFilename).getFileName().toString();
        String relativePath = folder + "/" + UUID.randomUUID() + "-" + fileName;
        Path targetPath = storageRoot.resolve(relativePath).normalize();
        if (!targetPath.startsWith(storageRoot)) {
            throw new BusinessException(40000, "文件路径不合法");
        }
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return relativePath.replace('\\', '/');
        } catch (IOException ex) {
            throw new BusinessException(50000, "文件保存失败");
        }
    }

    public byte[] read(String relativePath) {
        Path targetPath = storageRoot.resolve(relativePath).normalize();
        if (!targetPath.startsWith(storageRoot)) {
            throw new BusinessException(40000, "文件路径不合法");
        }
        try {
            return Files.readAllBytes(targetPath);
        } catch (IOException ex) {
            throw new BusinessException(40400, "资料文件不存在");
        }
    }

    public void delete(String relativePath) {
        Path targetPath = storageRoot.resolve(relativePath).normalize();
        if (!targetPath.startsWith(storageRoot)) {
            throw new BusinessException(40000, "文件路径不合法");
        }
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ex) {
            throw new BusinessException(50000, "文件删除失败");
        }
    }
}
