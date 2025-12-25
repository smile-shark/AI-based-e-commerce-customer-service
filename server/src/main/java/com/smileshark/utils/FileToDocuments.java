package com.smileshark.utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class FileToDocuments {
    public List<Document> handle(MultipartFile file) {
        FileHandler handler = classifier(file);
        return handler.run(file);
    }

    private FileHandler classifier(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "md", "markdown" -> new MarkdownFileHandler();
            case "pdf" -> new PdfFileHandler();
            case "txt" -> new TextFileHandler();
            default -> throw new IllegalArgumentException("不支持的文件类型: " + extension);
        };
    }

    public interface FileHandler {
        List<Document> run(MultipartFile file);
    }

    private static class MarkdownFileHandler implements FileHandler {
        @Override

        public List<Document> run(MultipartFile file) {
            // 使用Spring AI的MarkdownDocumentReader设置分割的规则
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(false) // 不按照分割线创建新的document
                    .withIncludeCodeBlock(false) // 不按照代码块创建新的document
                    .build();
            // 将MultipartFile包装为Resource
            Resource resource;
            try {
                resource = new ByteArrayResource(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("读取文件失败", e);
            }
            MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
            List<Document> documents = reader.read();
            // 通过自定义的中文分割器对文档进行断句分割，保证语义完整
            ChineseTokenTextSplitter splitter = ChineseTokenTextSplitter.quicklyBuild();
            return splitter.apply(documents);
        }
    }

    private static class PdfFileHandler implements FileHandler {
        @Override
        public List<Document> run(MultipartFile file) {
            // 使用Spring AI的PdfDocumentReader --- 这里使用的是一页一个document
            // 将MultipartFile包装为Resource
            Resource resource;
            try {
                resource = new ByteArrayResource(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("读取文件失败", e);
            }
            PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
            List<Document> documents = reader.read();
            // 由于pdf读取到的有大量的空白，所以在分割器中优化了对于空白字符的过滤，减少向量模型token的消耗，并且提高检索效果
            // 通过自定义的中文分割器对文档进行断句分割，保证语义完整
            ChineseTokenTextSplitter splitter = ChineseTokenTextSplitter.quicklyBuild();
            return splitter.apply(documents);
        }
    }

    private static class TextFileHandler implements FileHandler {
        @Override
        public List<Document> run(MultipartFile file) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                List<Document> documents = new ArrayList<>(1);
                documents.add(new Document(content.toString()));
                // 通过自定义的中文分割器对文档进行断句分割，保证语义完整
                ChineseTokenTextSplitter splitter = ChineseTokenTextSplitter.quicklyBuild();
                return splitter.apply(documents);
            } catch (IOException e) {
                throw new RuntimeException("读取文本文件失败", e);
            }
        }
    }

}
