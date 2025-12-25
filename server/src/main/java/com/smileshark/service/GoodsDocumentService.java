package com.smileshark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smileshark.common.Result;
import com.smileshark.entity.GoodsDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GoodsDocumentService extends IService<GoodsDocument> {
    List<GoodsDocument> getListByGoodsId(Integer id);

    Result<?> uploadGoodsDocument(MultipartFile file, Integer goodsId);
}