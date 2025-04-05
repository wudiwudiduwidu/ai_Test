package com.xz.aiTest.service.impl;

import com.xz.aiTest.model.entity.User;
import com.xz.aiTest.service.FileByAliYunService;
import com.xz.aiTest.utils.OSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

import static com.xz.aiTest.utils.OSSUtils.doFileDispose;

@Service
@Slf4j
public class FileByAliYunImpl implements FileByAliYunService {
@Resource
private UserServiceImpl userService;
    @Override
    public boolean savaPicture(MultipartFile file, User loginUser) {
       String filePath = doFileDispose(file,loginUser);
       loginUser.setUserAvatar(filePath);
        userService.updateById(loginUser);
        return true;
    }
}
