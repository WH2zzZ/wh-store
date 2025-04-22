package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.netflix.discovery.converters.Auto;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.crypto.tls.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {

    private static final List<String> CONTENT_TYPE = Arrays.asList("image/gif", "image/jpeg", "application/x-jpg");

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    public String uploadImg(MultipartFile file) {
        if(file != null) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();

        try {
            // 检验文件类型，后缀
            String contentType = file.getContentType();
            if (!CONTENT_TYPE.contains(contentType)) {
                LOGGER.info("文件上传失败：{}，文件类型错误", originalFilename);
                return null;
            }

            // 校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                LOGGER.info("文件上传失败：{}，文件内容不合法！", originalFilename);
            }

            // 校验成功保存到服务器
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath storePath = this.fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);

            // 返回url字符串
            return "http://image.leyou.com/" + storePath.getFullPath();
        } catch (IOException e) {
            LOGGER.info("服务器内部异常：" + originalFilename);
        }
        return null;
    }
}
