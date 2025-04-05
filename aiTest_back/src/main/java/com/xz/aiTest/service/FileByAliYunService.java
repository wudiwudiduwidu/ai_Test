package com.xz.aiTest.service;

import com.xz.aiTest.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface FileByAliYunService {
    boolean savaPicture(MultipartFile file, User loginUser);
}
