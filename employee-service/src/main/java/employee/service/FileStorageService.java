package employee.service;
import employee.enums.AttachmentType;
import employee.exception.EmployeeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    private static final long PROFILE_IMAGE_MAX_SIZE = 5 * 1024 * 1024;
    private static final long DOCUMENT_MAX_SIZE = 20 * 1024 * 1024;
    private static final Set<String> PROFILE_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "docx", "xlsx", "png", "jpg");
    public String store(MultipartFile file, AttachmentType type) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new EmployeeException(400, "File cannot be empty", HttpStatus.BAD_REQUEST);
        }
        String extension = getExtension(file.getOriginalFilename());
        String subFolder = type == AttachmentType.PROFILE_IMAGE ? "profile-image" : "documents";
        if (type == AttachmentType.PROFILE_IMAGE) {
            validateFile(file, extension, PROFILE_IMAGE_MAX_SIZE, PROFILE_IMAGE_EXTENSIONS, "Profile image");
        } else {
            validateFile(file, extension, DOCUMENT_MAX_SIZE, DOCUMENT_EXTENSIONS, "Document");
        }

        try {
            Path uploadPath = Paths.get(uploadDir, subFolder);
            Files.createDirectories(uploadPath);
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            log.info("File stored successfully: {}", filePath);
            return subFolder + "/" + fileName;
        } catch (IOException e) {
            log.error("File storage failed", e);
            throw new EmployeeException(500, "Failed to store file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateFile(MultipartFile file, String extension, long maxSize, Set<String> allowedExtensions, String fileType) {
        if (!allowedExtensions.contains(extension)) {
            throw new EmployeeException(400, fileType + " format is not supported. Allowed formats: " + String.join(", ", allowedExtensions), HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > maxSize) {
            long maxSizeInMb = maxSize / (1024 * 1024);
            throw new EmployeeException(400, fileType + " size must not exceed " + maxSizeInMb + "MB", HttpStatus.BAD_REQUEST);
        }
    }

    private String getExtension(String originalFilename) {
        if (Objects.isNull(originalFilename) || !originalFilename.contains(".")) {
            throw new EmployeeException(400, "File extension is missing", HttpStatus.BAD_REQUEST);
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
    }

    public Resource loadFileAsResource(String relativePath) {
        if (Objects.isNull(relativePath) || relativePath.isBlank()) {
            throw new EmployeeException(400, "File path is required", HttpStatus.BAD_REQUEST);
        }
        try {
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(relativePath).normalize();
            if (!filePath.startsWith(basePath)) {
                throw new EmployeeException(400, "Invalid file path", HttpStatus.BAD_REQUEST);
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new EmployeeException(404, "File not found: " + relativePath, HttpStatus.NOT_FOUND);
            }
            return resource;
        } catch (InvalidPathException | MalformedURLException e) {
            log.error("Failed to resolve file path: {}", relativePath, e);
            throw new EmployeeException(400, "Invalid file path", HttpStatus.BAD_REQUEST);
        }
    }

    public String detectContentType(String relativePath) {
        try {
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(relativePath).normalize();
            String contentType = Files.probeContentType(filePath);
            return Objects.nonNull(contentType) ? contentType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    public String getOriginalFileName(String relativePath) {
        String storedFileName = Paths.get(relativePath).getFileName().toString();
        int separatorIndex = storedFileName.indexOf('_');
        if (separatorIndex > 0 && separatorIndex < storedFileName.length() - 1) {
            return storedFileName.substring(separatorIndex + 1);
        }
        return storedFileName;
    }
}