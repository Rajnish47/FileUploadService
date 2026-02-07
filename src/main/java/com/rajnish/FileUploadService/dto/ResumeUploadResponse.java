package com.rajnish.FileUploadService.dto;

import java.util.List;

public record ResumeUploadResponse(List<Integer> missingChunkNos) {
}
