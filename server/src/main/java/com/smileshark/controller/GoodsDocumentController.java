package com.smileshark.controller;

import com.smileshark.common.Result;
import com.smileshark.service.GoodsDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/goodsDocument")
@RequiredArgsConstructor
public class GoodsDocumentController {
    private final GoodsDocumentService goodsDocumentService;
    @PostMapping("/upload")
    public Result<?> upload(@RequestBody MultipartFile file, Integer goodsId) {
        // 这里调用service方法处理上传逻辑，具体实现由用户完成
        return goodsDocumentService.uploadGoodsDocument(file, goodsId);
    }
}
