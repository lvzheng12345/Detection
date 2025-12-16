<template>
  <div class="common-layout">
    <el-container class="layout-container">
      <!-- Sidebar -->
      <el-aside width="240px" class="aside-menu">
        <div class="logo-area">
          <h2 class="app-title">DeepAudit Admin</h2>
        </div>
        <el-menu
          :default-active="currentTab"
          class="el-menu-vertical"
          :border="false"
          @select="handleMenuSelect"
        >
          <el-menu-item index="1">
            <el-icon><Odometer /></el-icon>
            <span>全站概览</span>
          </el-menu-item>
          <el-menu-item index="2">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="3">
            <el-icon><List /></el-icon>
            <span>全站日志</span>
          </el-menu-item>
          <el-menu-item index="5">
            <el-icon><Lock /></el-icon>
            <span>审核规则</span>
          </el-menu-item>
          <el-menu-item index="4">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
        
        <div class="user-profile-area">
           <div class="user-info">
             <el-avatar :size="32" icon="UserFilled" class="user-avatar" style="background-color: #f56c6c" />
             <span class="username">Administrator</span>
           </div>
           <el-button link type="danger" size="small" @click="logout">
             <el-icon><SwitchButton /></el-icon>
           </el-button>
        </div>
      </el-aside>
      
      <!-- Main Content -->
      <el-main class="main-content">
        <div class="content-wrapper">
            <!-- Header -->
            <div class="page-header">
                <div class="header-left">
                    <h2>{{ getPageTitle() }}</h2>
                    <span class="subtitle">{{ getPageSubtitle() }}</span>
                </div>
                <div class="header-actions">
                     <el-button type="primary" @click="refreshData">刷新数据</el-button>
                </div>
            </div>
            
            <!-- Tab 1: Overview -->
            <div v-if="currentTab === '1'">
                <div class="stats-section">
                     <DashboardStats ref="statsRef" />
                </div>
                <div class="section-card" style="margin-top: 24px;">
                    <div class="section-header">
                        <h3><el-icon><List /></el-icon> 最新违规记录</h3>
                    </div>
                    <div class="card-body">
                        <AuditHistory ref="historyRef" :is-admin="true" />
                    </div>
                </div>
            </div>

            <!-- Tab 2: User Management -->
            <div v-if="currentTab === '2'">
                <el-card class="box-card">
                    <el-table :data="userList" style="width: 100%" v-loading="loadingUsers">
                        <el-table-column prop="id" label="ID" width="80" />
                        <el-table-column prop="username" label="用户名" />
                        <el-table-column prop="role" label="角色">
                            <template #default="scope">
                                <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'primary'">
                                    {{ scope.row.role }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="180">
                            <template #default="scope">
                                <el-button size="small" type="primary" link>编辑</el-button>
                                <el-button size="small" type="danger" link>禁用</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </div>

            <!-- Tab 3: All Logs -->
            <div v-if="currentTab === '3'">
                <el-card class="box-card">
                    <AuditHistory ref="historyRef" :is-admin="true" />
                </el-card>
            </div>

            <!-- Tab 5: Audit Rules -->
            <div v-if="currentTab === '5'">
                <el-card class="box-card">
                    <template #header>
                        <div class="card-header">
                            <span>敏感词库管理</span>
                        </div>
                    </template>
                    <AuditRules />
                </el-card>
            </div>

            <!-- Tab 4: Settings -->
            <div v-if="currentTab === '4'">
                <el-row :gutter="20">
                    <el-col :span="12">
                        <el-card class="box-card">
                            <template #header>
                                <div class="card-header">
                                    <span>平台开关</span>
                                </div>
                            </template>
                            <el-form label-width="120px">
                                <el-form-item label="系统维护模式">
                                    <el-switch v-model="adminSettings.maintenanceMode" active-text="开启" inactive-text="关闭" />
                                    <div class="form-tip">开启后，普通用户将无法登录</div>
                                </el-form-item>
                                <el-form-item label="开放注册">
                                    <el-switch v-model="adminSettings.registrationOpen" active-text="开启" inactive-text="关闭" />
                                </el-form-item>
                            </el-form>
                        </el-card>
                    </el-col>
                    <el-col :span="12">
                        <el-card class="box-card">
                            <template #header>
                                <div class="card-header">
                                    <span>审核策略</span>
                                </div>
                            </template>
                            <el-form label-width="120px">
                                <el-form-item label="审核严格度">
                                    <el-select v-model="adminSettings.auditLevel">
                                        <el-option label="宽松" value="loose" />
                                        <el-option label="标准" value="standard" />
                                        <el-option label="严格" value="strict" />
                                    </el-select>
                                </el-form-item>
                                <el-form-item label="管理员通知">
                                    <el-switch v-model="adminSettings.notifyAdmin" />
                                    <div class="form-tip">发现高风险内容时发送邮件通知</div>
                                </el-form-item>
                                <el-form-item>
                                    <el-button type="primary" @click="saveAdminSettings">保存配置</el-button>
                                </el-form-item>
                            </el-form>
                        </el-card>
                    </el-col>
                </el-row>
            </div>

        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Odometer, User, List, Setting, UserFilled, SwitchButton, ChatLineRound, Lock } from '@element-plus/icons-vue'
import AuditHistory from '../components/AuditHistory.vue'
import DashboardStats from '../components/DashboardStats.vue'
import AuditRules from '../components/AuditRules.vue'

const router = useRouter()
const historyRef = ref(null)
const statsRef = ref(null)
const currentTab = ref('1')
const userList = ref([])
const loadingUsers = ref(false)

// Admin Settings State
const adminSettings = reactive({
  maintenanceMode: false,
  registrationOpen: true,
  auditLevel: 'strict',
  notifyAdmin: true
})

onMounted(async () => {
    // Ensure Dark Mode is disabled for Admin
    document.documentElement.classList.remove('dark')

    // Load configs
    try {
        const res = await axios.get('/api/config/all')
        const configs = res.data
        configs.forEach(c => {
            if (c.configKey === 'maintenance_mode') adminSettings.maintenanceMode = c.configValue === 'true'
            if (c.configKey === 'registration_open') adminSettings.registrationOpen = c.configValue === 'true'
            if (c.configKey === 'audit_level') adminSettings.auditLevel = c.configValue
            if (c.configKey === 'notify_admin') adminSettings.notifyAdmin = c.configValue === 'true'
        })
    } catch (e) {
        console.error(e)
    }
})

const saveAdminSettings = async () => {
    try {
        await axios.post('/api/config/update', { key: 'maintenance_mode', value: String(adminSettings.maintenanceMode) })
        await axios.post('/api/config/update', { key: 'registration_open', value: String(adminSettings.registrationOpen) })
        await axios.post('/api/config/update', { key: 'audit_level', value: adminSettings.auditLevel })
        await axios.post('/api/config/update', { key: 'notify_admin', value: String(adminSettings.notifyAdmin) })
        ElMessage.success('系统设置已保存')
    } catch (e) {
        ElMessage.error('保存失败')
    }
}

const handleMenuSelect = (index) => {
    currentTab.value = index
    if (index === '2') {
        fetchUsers()
    }
}

const getPageTitle = () => {
    const titles = {
        '1': '全站概览',
        '2': '用户管理',
        '3': '全站日志',
        '4': '系统设置',
        '5': '审核规则管理'
    }
    return titles[currentTab.value]
}

const getPageSubtitle = () => {
    const subtitles = {
        '1': '系统整体运行状态监控',
        '2': '注册用户列表与权限管理',
        '3': '所有用户的检测历史记录',
        '4': '平台参数配置与维护',
        '5': '自定义敏感词库与拦截策略'
    }
    return subtitles[currentTab.value]
}

const fetchUsers = async () => {
    loadingUsers.value = true
    try {
        const response = await axios.get('/api/user/all')
        userList.value = response.data
    } catch (error) {
        console.error('Failed to fetch users', error)
    } finally {
        loadingUsers.value = false
    }
}

const refreshData = () => {
  if (currentTab.value === '1' || currentTab.value === '3') {
      if (historyRef.value) historyRef.value.fetchHistory()
      if (statsRef.value) statsRef.value.fetchStats()
  } else if (currentTab.value === '2') {
      fetchUsers()
  }
}

const logout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style scoped>
.common-layout {
  height: 100vh;
  background-color: var(--el-fill-color-light);
}

.layout-container {
  height: 100%;
}

.aside-menu {
  background-color: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-light);
  display: flex;
  flex-direction: column;
  position: relative;
}

.logo-area {
  padding: 20px 24px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.app-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.el-menu-vertical {
  border-right: none;
  padding-top: 10px;
}

.el-menu-item {
  font-size: 14px;
  margin: 4px 12px;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
}

.el-menu-item.is-active {
  background-color: var(--el-color-danger-light-9); /* Red tint for admin */
  color: var(--el-color-danger);
  font-weight: 500;
}

.user-profile-area {
  margin-top: auto;
  padding: 20px;
  border-top: 1px solid var(--el-border-color-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-size: 14px;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.main-content {
  padding: 0;
  background-color: var(--el-fill-color-light);
}

.content-wrapper {
  padding: 32px 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.header-left h2 {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.subtitle {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.stats-section {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.section-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  height: 100%;
  overflow: hidden;
}

.section-header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-body {
  padding: 24px;
}
</style>
