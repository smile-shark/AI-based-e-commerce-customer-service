package com.smileshark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smileshark.code.ResultCode;
import com.smileshark.common.Result;
import com.smileshark.entity.GoodsDocument;
import com.smileshark.mapper.GoodsDocumentMapper;
import com.smileshark.service.GoodsDocumentService;
import com.smileshark.utils.FileToDocuments;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.smileshark.code.DocumentCode.FILE_ID;
import static com.smileshark.code.DocumentCode.GOODS_ID;

@Service
@RequiredArgsConstructor
public class GoodsDocumentServiceImpl extends ServiceImpl<GoodsDocumentMapper, GoodsDocument> implements GoodsDocumentService {
    private final FileToDocuments fileToDocuments;
    private final MilvusVectorStore vectorStore;

    @Override
    public List<GoodsDocument> getListByGoodsId(Integer id) {
        return lambdaQuery().eq(GoodsDocument::getGoodsId, id).list();
    }

    @Override
    @Transactional
    public Result<?> uploadGoodsDocument(MultipartFile file, Integer goodsId) {
        /*
         * 1. 判断文件的类型，选择合适的处理器进行处理
         * 2. 构建文档对象传入数据库，接收对应的唯一文件id
         * 3. 对每个文档设置唯标识和统一的商品标识
         * 4. 将处理后的文档通过向量模型处理后存入向量数据库中
         */
        List<Document> documents = fileToDocuments.handle(file);
        // 将文件信息进行存储
        GoodsDocument fileInfo = GoodsDocument.builder()
                .name(file.getOriginalFilename())
                .goodsId(goodsId)
                .build();
        if(!save(fileInfo)){
            throw new RuntimeException("文件保存失败");
        }
        // 为每个document设置对应的文件id和对用的商品id
        documents.forEach(document -> {
            document.getMetadata().put(FILE_ID, fileInfo.getId());
            document.getMetadata().put(GOODS_ID, goodsId);
        });
        // 使用 Milvus 进行向量存储
        vectorStore.add(documents);
        return Result.success(ResultCode.ADD_SUCCESS);
    }

    @Override
    @Transactional
    public Result<?> delete(Integer id) {
        // 查询要删除的文件信息，用于从向量数据库中删除对应的向量
        GoodsDocument fileInfo = getById(id);
        if (fileInfo == null) {
            return Result.error(ResultCode.DELETE_ERROR);
        }
        
        if (!removeById(id)) {
            return Result.error(ResultCode.DELETE_ERROR);
        }
        // 根据文件id删除向量数据库中的数据
        vectorStore.delete(String.format("\"%s\" = %d", FILE_ID, id));
        return Result.success(ResultCode.DELETE_SUCCESS);
    }
}
