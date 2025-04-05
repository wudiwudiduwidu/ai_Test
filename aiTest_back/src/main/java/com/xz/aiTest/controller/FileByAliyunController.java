package com.xz.aiTest.controller;

import com.xz.aiTest.common.BaseResponse;
import com.xz.aiTest.common.ErrorCode;
import com.xz.aiTest.common.ResultUtils;
import com.xz.aiTest.exception.BusinessException;

import com.xz.aiTest.model.entity.App;
import com.xz.aiTest.model.entity.User;
import com.xz.aiTest.service.impl.AppServiceImpl;
import com.xz.aiTest.service.impl.FileByAliYunImpl;
import com.xz.aiTest.service.impl.UserServiceImpl;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.xz.aiTest.utils.OSSUtils.doFileDispose;

/**
 * 阿里云对象储存上传文件模板
 */
@RestController
@RequestMapping("/upload")
public class FileByAliyunController {
    // 上传头像,图片到阿里云对象储存oss
@Resource
private FileByAliYunImpl fileByAliYunService;
@Resource
private AppServiceImpl appService;
@Resource
private UserServiceImpl userService;
    @PostMapping("/uploadAvatar")
    public BaseResponse<String> handleFileUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request)
    {
        if(file.isEmpty())
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"上传头像为空");
        }
        //获取登录用户->userId
        User loginUser = userService.getLoginUser(request);
        if( fileByAliYunService.savaPicture(file,loginUser))
        {
            return ResultUtils.success("上传成功");
        }
        else
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }
    }
    @PostMapping("/appPicture")
    public BaseResponse<Long> handleAppPicture(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("id") Long id,
                                                 HttpServletRequest request)
    {
        if(file.isEmpty())
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"上传图片为空");
        }
        //获取登录用户->userId
        User loginUser = userService.getLoginUser(request);
      String filePath = doFileDispose(file,loginUser);
App app  =appService.getById(id);
app.setAppIcon(filePath);
//是有boolean返回值的 可以判断是否更新成功
appService.updateById(app);
        return ResultUtils.success(id);
    }
}
