package com.smileshark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.entity.GoodsDocument;
import com.smileshark.mapper.GoodsDocumentMapper;
import com.smileshark.service.GoodsDocumentService;
import org.springframework.stereotype.Service;

@Service
public class GoodsDocumentServiceImpl extends ServiceImpl<GoodsDocumentMapper, GoodsDocument> implements GoodsDocumentService {
}