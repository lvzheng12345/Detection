<template>
  <div class="api-docs-container">
    <el-tabs v-model="activeTab" class="docs-tabs">
      <el-tab-pane label="接入指南" name="guide">
        <div class="doc-content">
          <h2>如何接入自动审核？</h2>
          <p class="intro-text">
            通过集成 DeepAudit API，您可以实现用户评论、发帖的实时自动审核，无需人工干预。
            当用户点击“提交”按钮时，先调用审核接口，只有审核通过的内容才允许提交到您的数据库。
          </p>

          <el-steps direction="vertical" :active="3" finish-status="success" class="integration-steps">
            <el-step title="第一步：准备数据" description="获取用户输入的文本内容。" />
            <el-step title="第二步：调用审核 API" description="将文本发送至 /api/audit/check 接口。" />
            <el-step title="第三步：处理响应" description="根据返回的 isSafe 字段判断是否允许提交。如果为 false，则提示用户修改。" />
          </el-steps>

          <h3>场景示例：评论区自动拦截</h3>
          <p>以下是一个标准的前端集成示例（使用 Axios）：</p>
          
          <div class="code-block">
            <pre><code>
// 1. 定义审核函数
async function checkContentSafety(text, userId) {
  try {
    const response = await axios.post('http://localhost:8080/api/audit/check', {
      content: text,
      userId: userId,
      username: "User_" + userId
    });
    return response.data; // 返回审核结果
  } catch (error) {
    console.error("Audit API Error", error);
    return { safe: false, reason: "审核服务暂时不可用" }; // 默认阻断或放行
  }
}

// 2. 在提交按钮的点击事件中使用
async function handleSubmitComment() {
  const commentText = document.getElementById('commentInput').value;
  
  // --- 核心逻辑开始 ---
  const auditResult = await checkContentSafety(commentText, 1001);
  
  if (!auditResult.safe) {
    // 审核未通过，拦截提交并提示
    alert(`发布失败：内容包含违规信息（${auditResult.reason}）。\n建议修改：${auditResult.suggestion}`);
    return; 
  }
  // --- 核心逻辑结束 ---

  // 审核通过，继续执行正常的提交逻辑
  saveCommentToBackend(commentText);
}
            </code></pre>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="API 参考" name="reference">
        <div class="doc-content">
          <h3>文本审核接口</h3>
          <div class="endpoint-badge">POST /api/audit/check</div>
          
          <h4>请求参数 (Request Body)</h4>
          <el-table :data="requestParams" border style="width: 100%; margin-bottom: 20px;">
            <el-table-column prop="name" label="字段名" width="150" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="required" label="必填" width="80" />
            <el-table-column prop="desc" label="说明" />
          </el-table>

          <h4>响应参数 (Response)</h4>
          <el-table :data="responseParams" border style="width: 100%">
            <el-table-column prop="name" label="字段名" width="150" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="desc" label="说明" />
          </el-table>

          <h4>响应示例</h4>
          <div class="code-block json-block">
<pre>{
  "safe": false,
  "category": "violence",
  "reason": "包含暴力威胁内容",
  "suggestion": "建议删除威胁性语言",
  "sentiment": "negative"
}</pre>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('guide')

const requestParams = [
  { name: 'content', type: 'String', required: '是', desc: '需要审核的文本内容' },
  { name: 'userId', type: 'Long', required: '否', desc: '用户ID，用于统计和历史记录' },
  { name: 'username', type: 'String', required: '否', desc: '用户名，用于日志记录' }
]

const responseParams = [
  { name: 'safe', type: 'Boolean', desc: '是否安全（true: 通过, false: 违规）' },
  { name: 'category', type: 'String', desc: '违规类别（如：politics, violence, insult）' },
  { name: 'reason', type: 'String', desc: '具体的违规原因描述' },
  { name: 'suggestion', type: 'String', desc: 'AI 给出的修改建议' },
  { name: 'sentiment', type: 'String', desc: '情感倾向（positive, neutral, negative）' }
]
</script>

<style scoped>
.api-docs-container {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  min-height: 600px;
}

.doc-content {
  max-width: 900px;
}

.intro-text {
  color: var(--el-text-color-regular);
  line-height: 1.6;
  margin-bottom: 30px;
}

.integration-steps {
  margin: 30px 0;
  padding: 20px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.code-block {
  background: #282c34;
  color: #abb2bf;
  padding: 20px;
  border-radius: 8px;
  font-family: 'Fira Code', monospace;
  font-size: 14px;
  line-height: 1.5;
  overflow-x: auto;
  margin: 20px 0;
}

.endpoint-badge {
  display: inline-block;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border: 1px solid var(--el-color-primary-light-8);
  padding: 8px 16px;
  border-radius: 4px;
  font-family: monospace;
  font-weight: bold;
  margin: 10px 0 20px;
}

h2 { margin-bottom: 20px; }
h3 { margin-top: 30px; margin-bottom: 15px; }
h4 { margin-top: 20px; margin-bottom: 10px; color: var(--el-text-color-primary); }
</style>
