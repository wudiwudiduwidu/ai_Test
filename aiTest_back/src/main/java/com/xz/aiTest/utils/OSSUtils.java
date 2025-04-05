package com.xz.aiTest.utils;


import com.aliyun.oss.*;
import com.xz.aiTest.common.ErrorCode;
import com.xz.aiTest.exception.BusinessException;
import com.xz.aiTest.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class OSSUtils {
    public static String doFileDispose(MultipartFile file, User loginUser){
        String userName = loginUser.getUserName();
        String originalFilename;
        String extension;
        String objectName;
        String filePath;
        try {
            //原始文件名
            originalFilename = file.getOriginalFilename();
            //截取原始文件名的后缀
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //构造新文件名称
            objectName = userName+ UUID.randomUUID().toString().substring(0, 6) + extension;

            //文件的请求路径
            filePath = OSSUtils.doGet(file.getBytes(), objectName);

        } catch (IOException e) {
            log.info(e.toString());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return filePath;
    }
    public static String doGet(byte[] bytes, String objectName)
    {
// 解除注释并反转变量名(因为下面变量是无法上传github 先反转变量名！)
String di = "";
String yek = "";
String endpoint = "";
String bucket = "";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, di, yek);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucket, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException | ClientException ce){
            log.info(ce.toString());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucket)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        return stringBuilder.toString();

    }

}

