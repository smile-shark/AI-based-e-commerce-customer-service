CREATE TABLE `commercial_tenant` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '商户id',
  `name` varchar(30) COMMENT '商户名称',
  `account` char(20) unique ,
  `password` char(30)
);

CREATE TABLE `user` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '客户id',
  `name` varchar(30) COMMENT '客户名称',
  `account` char(20) unique ,
  `password` char(30)
);

CREATE TABLE `goods` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '商品id自增列',
  `name` varchar(200) COMMENT '商品名称',
  `ct_id` int COMMENT '关联的商户'
);

CREATE TABLE `goods_document` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '文档id，用户向量检索时筛选',
  `name` varchar(100) COMMENT '文档的名称，用于查看对应商品下面有哪些文档',
  `goods_id` int COMMENT '关联的商品'
);

CREATE TABLE `role` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '角色的id',
  `role_name` varchar(255) COMMENT '角色名称 <roleName>',
  `role_description` text COMMENT '角色描述 <roleDescription>',
  `greeting_message` text COMMENT '问候语 <greetingMessage>',
  `problem_solving_approach` text COMMENT '解决问题的方法 <problemSolvingApproach>',
  `communication_style` varchar(255) COMMENT '沟通风格 <communicationStyle>',
  `response_tone` varchar(255) COMMENT '回复语调 <responseTone>',
  `product_knowledge_level` varchar(255) COMMENT '产品知识水平 <productKnowledgeLevel>',
  `emotional_intelligence` varchar(255) COMMENT '情商表现 <emotionalIntelligence>',
  `escalation_criteria` text COMMENT '升级处理标准 <escalationCriteria>',
  `closing_message` text COMMENT '结束语 <closingMessage>',
  `created_at` datetime COMMENT '创建时间',
  `updated_at` datetime COMMENT '更新时间',
  `ct_id` int COMMENT '关联的商户，每个商户有一个客户的角色'
);

drop table `session`;
CREATE TABLE `session` (
   `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '会话id',
   `ct_id` int COMMENT '关联的商户',
   `user_id` int COMMENT '关联的客户',
   `goods_id` int COMMENT '关联的商品，RAG会根据这个取查询对应商品的知识',
   `conversation_status` enum('AI', 'HUMAN') DEFAULT 'AI' COMMENT '会话状态，AI表示AI对话，HUMAN表示人工对话'
);

CREATE TABLE `session_log` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '记录的id',
  `content` text COMMENT '对话的内容',
  `type` enum('USER','ASSISTANT','SYSTEM','TOOL','COMMERCIAL_TENANT') COMMENT '对话人身份',
  `timestamp` datetime DEFAULT (now()) COMMENT '记录创建的时间',
  `read_status` enum('READ', 'UNREAD') DEFAULT 'UNREAD' COMMENT '消息已读状态，READ表示已读，UNREAD表示未读',
  `session_id` int COMMENT '关联的会话'
);

CREATE TABLE `sensitive_words` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `content` char(40)
);

ALTER TABLE `commercial_tenant` COMMENT = '商户表';

ALTER TABLE `user` COMMENT = '客户表';

ALTER TABLE `goods` COMMENT = '商品表';

ALTER TABLE `goods_document` COMMENT = '商品对应的文档';

ALTER TABLE `role` COMMENT = '角色设定表，用于动态控制客户角色的细微调整';

ALTER TABLE `session` COMMENT = '会话表，一个商家和客户的会话id';

ALTER TABLE `session_log` COMMENT = '对话记录表';

ALTER TABLE `sensitive_words` COMMENT = '敏感词表';

ALTER TABLE `goods` ADD FOREIGN KEY (`ct_id`) REFERENCES `commercial_tenant` (`id`);

ALTER TABLE `goods_document` ADD FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`);

ALTER TABLE `role` ADD FOREIGN KEY (`ct_id`) REFERENCES `commercial_tenant` (`id`);

ALTER TABLE `session` ADD FOREIGN KEY (`ct_id`) REFERENCES `commercial_tenant` (`id`);

ALTER TABLE `session` ADD FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `session_log` ADD FOREIGN KEY (`session_id`) REFERENCES `session` (`id`);

ALTER TABLE `session` ADD FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`);
