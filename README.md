# 企业级电商AI智能客服系统

## AI电商客服流程图
![AI电商客服流程图](./images/AI电商客服流程图_成本优化版.jpg)
### 1. 商户管理端：知识资产与配置
这是系统的初始化阶段，决定了 AI 客服的“智商”和“性格”。

- **商品与策略录入**：商户在管理后台添加商品信息，并上传相关的服务政策（如退换货规则、运费标准等）。
- **RAG 预处理**：系统自动对上传的非结构化文档进行拆分、向量化处理，并存储至向量数据库，为后续的检索增强生成（RAG）做准备。
- **个性化配置**：商户在 MySQL 中配置 AI 的角色定义（如“温柔的导购”）、回复语调以及特定的欢迎语。

### 2. 前置分流：安全检测与成本控制 (核心优化)
该模块旨在通过“最小代价判断”原则，在消耗高额 Token 前过滤无效或特定请求。

- **会话初始化**：后端根据用户 ID 和商品 ID 创建或恢复 Session。
- **商户配置校验**：首先查询 MySQL，确认该商户是否开启了 AI 客服功能。若未开启，直接进入人工模式。
- **敏感词与情绪预检**：利用轻量级算法或正则表达式进行过滤。
- **异常触发**：若检测到违禁词或极端投诉情绪，系统自动将 Session 状态修改为 HUMAN，跳过所有 AI 逻辑。
- **状态分流**：检查 Session 中的 Status 标记。若为 HUMAN，证明该会话已被接管或判定为需人工处理，直接流向人工模块。

### 3. AI 核心链路：深度推理与知识检索
仅在确定由 AI 处理且环境安全时触发，实现精准回复。

- **记忆拉取**：从 Redis 中检索该用户近期（如 Top 10）的聊天记录，构建上下文感知能力。
- **RAG 检索增强**：根据用户当前提问，在向量数据库中检索最相关的商品详情或政策片段。
- **LLM 启发式生成**：将“角色设定 + 历史记忆 + 检索到的知识 + 用户当前提问”组合成 Prompt，由大语言模型生成回答。
- **二次逻辑校验**：
    - **转人工判定**：若 LLM 生成过程中发现用户意图为“投诉”、“要求人工”或知识库无法覆盖，则标记转人工。
    - **更新记忆**：若正常回答，同步更新 Redis（短期记忆）和 MySQL（长期消息记录）。

### 4. 人工兜底：人工服务闭环
确保在 AI 无法解决问题时，用户体验不中断。

- **实时性判断**：系统查询商户当前是否在线。
    - **在线**：通过 WebSocket 或 App 推送实时告知商户，开启双向实时对话。
    - **离线**：引导用户填写留言，系统自动生成工单归档。
- **同步策略**：人工对话的所有记录同步写入 MySQL，以便 AI 在后续会话中能通过“长期记忆”了解人工处理的进度。

## 数据库设计
![数据库设计图](./images/ai_customer_service.png)
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
  conversation_status enum('AI', 'HUMAN') [default: 'AI', note:'会话状态，AI表示AI对话，HUMAN表示人工对话']
}
Table session_log [note:'对话记录表']{
  id int [pk, increment, note:'记录的id']
  content text [note:'对话的内容']
  type enum('USER', 'ASSISTANT', 'SYSTEM', 'TOOL','COMMERCIAL_TENANT') [note:'对话人身份']
  timestamp datetime [default: `now()`,note:'记录创建的时间']
  session_id int [ref:> session.id,note:'关联的会话']
  read_status enum('READ', 'UNREAD') [default: 'UNREAD', note:'消息已读状态，READ表示已读，UNREAD表示未读']
}

Table sensitive_words [note:'敏感词表']{
  id int [pk, increment]
  content char(40) 
}
```