# 企业级电商AI智能客服系统
## 数据库设计
```dbml
Table commercial_tenant [note:'商户表']{
  id int [pk, increment, note:'商户id']
  name varchar(30) [note:'商户名称']
  account char(20) [unique]
  password char(30)
}

Table user [note:'客户表']{
  id int [pk, increment, note:'客户id']
  name varchar(30) [note:'客户名称']
  account char(20) [unique]
  password char(30)
}

Table goods [note:'商品表']{
  id int [pk, increment, note:'商品id自增列']
  name varchar(30) [note:'商品名称']
  ct_id int [ref:> commercial_tenant.id,note:'关联的商户']
}

Table goods_document [note:'商品对应的文档']{
  id int [pk, increment, note:'文档id，用户向量检索时筛选']
  name varchar(100) [note:'文档的名称，用于查看对应商品下面有哪些文档']
  goods_id int [ref:> goods.id,note:'关联的商品']
}

Table role [note:'角色设定表，用于动态控制客户角色的细微调整']{
  id int [pk, increment, note:'角色的id']
  role_name varchar [note:'角色名称 <roleName>']
  role_description text [note:'角色描述 <roleDescription>']
  greeting_message text [note:'问候语 <greetingMessage>']
  problem_solving_approach text [note:'解决问题的方法 <problemSolvingApproach>']
  communication_style varchar [note:'沟通风格 <communicationStyle>']
  response_tone varchar [note:'回复语调 <responseTone>']
  product_knowledge_level varchar [note:'产品知识水平 <productKnowledgeLevel>']
  emotional_intelligence varchar [note:'情商表现 <emotionalIntelligence>']
  escalation_criteria text [note:'升级处理标准 <escalationCriteria>']
  closing_message text [note:'结束语 <closingMessage>']
  created_at datetime [note:'创建时间']
  updated_at datetime [note:'更新时间']
  
  ct_id int [ref:> commercial_tenant.id,note:'关联的商户，每个商户有一个客户的角色']
}

Table session [note:'会话表，一个商家和客户的会话id']{
  id int [pk, increment, note:'会话id']
  ct_id int [ref:> commercial_tenant.id,note:'关联的商户']
  user_id int [ref:>user.id,note:'关联的客户']
  goods_id int [ref:> goods.id,note:'关联的商品，RAG会根据这个取查询对应商品的知识']
}

Table session_log [note:'对话记录表']{
  id int [pk, increment, note:'记录的id']
  content text [note:'对话的内容']
  type enum('USER', 'ASSISTANT', 'SYSTEM', 'TOOL','COMMERCIAL_TENANT') [note:'对话人身份']
  timestamp datetime [default: `now()`,note:'记录创建的时间']
  session_id int [ref:> session.id,note:'关联的会话']
}

Table sensitive_words [note:'敏感词表']{
  id int [pk, increment]
  content char(40) 
}
```