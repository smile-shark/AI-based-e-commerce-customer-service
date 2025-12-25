package com.smileshark.controller;

import com.smileshark.common.Result;
import com.smileshark.service.GoodsDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/goodsDocument")
@RequiredArgsConstructor
public class GoodsDocumentController {
    private final GoodsDocumentService goodsDocumentService;

    /**
     * 文档上传
     * @param file 文档
     * @param goodsId 商品id
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Result<?> upload(@RequestBody MultipartFile file, Integer goodsId) {
        return goodsDocumentService.uploadGoodsDocument(file, goodsId);
    }

    /**
     * 删除 文档
     * @param id 文档id
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam Integer id) {
        return goodsDocumentService.delete(id);
    }
}
