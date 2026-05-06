import os

base_dir = r"C:\Users\15030\Desktop\opencode\软件设计与体系结构作业\新系统项目文件夹\teaching-platform\frontend"

files = {
    "src/utils/request.ts": """import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
});

request.interceptors.request.use(
  config => {
    const authStore = useAuthStore();
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

request.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 200) {
      ElMessage.error(res.message || 'Error');
      if (res.code === 401) {
        const authStore = useAuthStore();
        authStore.logout();
        window.location.href = '/login';
      }
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return res.data;
  },
  error => {
    ElMessage.error(error.message || 'Request failed');
    return Promise.reject(error);
  }
);

export default request;
""",
    "src/stores/auth.ts": """import { defineStore } from 'pinia';
import { ref } from 'vue';
import { login as loginApi, getUserInfo } from '@/api/auth';
import type { LoginDto, User } from '@/types/user';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '');
  const user = ref<User | null>(null);

  const setToken = (newToken: string) => {
    token.value = newToken;
    localStorage.setItem('token', newToken);
  };

  const login = async (data: LoginDto) => {
    const res = await loginApi(data);
    setToken(res.token);
    await fetchUserInfo();
  };

  const fetchUserInfo = async () => {
    if (!token.value) return;
    const res = await getUserInfo();
    user.value = res;
  };

  const logout = () => {
    token.value = '';
    user.value = null;
    localStorage.removeItem('token');
  };

  return { token, user, login, logout, fetchUserInfo, setToken };
});
""",
    "src/types/user.ts": """export interface User {
  id: number;
  username: string;
  name: string;
  role: 'admin' | 'teacher' | 'student';
  createdAt: string;
}

export interface LoginDto {
  username: string;
  password?: string;
}
""",
    "src/types/common.ts": """export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}
""",
    "src/api/auth.ts": """import request from '@/utils/request';
import type { LoginDto, User } from '@/types/user';

export const login = (data: LoginDto) => {
  return request.post<{token: string}>('/auth/login', data);
};

export const getUserInfo = () => {
  return request.get<User>('/users/me');
};
""",
    "src/api/teacher.ts": """import request from '@/utils/request';
import type { PageResult } from '@/types/common';

export const getClasses = (params: any) => request.get<PageResult<any>>('/teacher/classes', { params });
export const getStudents = (params: any) => request.get<PageResult<any>>('/teacher/students', { params });
export const getMaterials = (params: any) => request.get<PageResult<any>>('/teacher/materials', { params });
export const createMaterial = (data: any) => request.post('/teacher/materials', data);
export const getQuestions = (params: any) => request.get<PageResult<any>>('/teacher/questions', { params });
export const getLabs = (params: any) => request.get<PageResult<any>>('/teacher/labs', { params });
export const getLabSteps = (id: string | number) => request.get<any>(`/teacher/labs/${id}/steps`);
export const getLabReports = (params: any) => request.get<PageResult<any>>('/teacher/lab-reports', { params });
export const getLabReportDetail = (id: string | number) => request.get<any>(`/teacher/lab-reports/${id}`);
export const getHomeworks = (params: any) => request.get<PageResult<any>>('/teacher/homeworks', { params });
export const getHomeworkSubmissions = (id: string | number, params: any) => request.get<PageResult<any>>(`/teacher/homeworks/${id}/submissions`, { params });
export const getExams = (params: any) => request.get<PageResult<any>>('/teacher/exams', { params });
export const getExamResults = (id: string | number, params: any) => request.get<PageResult<any>>(`/teacher/exams/${id}/results`, { params });
export const getAnalysis = () => request.get<any>('/teacher/analysis');
export const getPlagiarism = (params: any) => request.get<PageResult<any>>('/teacher/plagiarism', { params });
""",
    "src/api/student.ts": """import request from '@/utils/request';
import type { PageResult } from '@/types/common';

export const getMaterials = (params: any) => request.get<PageResult<any>>('/student/materials', { params });
export const getLabs = (params: any) => request.get<PageResult<any>>('/student/labs', { params });
export const getLabDetail = (id: string | number) => request.get<any>(`/student/labs/${id}`);
export const submitLabStep = (labId: string | number, stepId: string | number, data: any) => request.post(`/student/labs/${labId}/steps/${stepId}`, data);
export const getHomeworks = (params: any) => request.get<PageResult<any>>('/student/homeworks', { params });
export const getHomeworkDetail = (id: string | number) => request.get<any>(`/student/homeworks/${id}`);
export const getExams = (params: any) => request.get<PageResult<any>>('/student/exams', { params });
export const getExamDetail = (id: string | number) => request.get<any>(`/student/exams/${id}`);
export const submitExam = (id: string | number, data: any) => request.post(`/student/exams/${id}`, data);
export const getScores = (params: any) => request.get<PageResult<any>>('/student/scores', { params });
""",
    "src/router/index.ts": """import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import Layout from '@/layout/index.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/auth/Login.vue'), meta: { public: true } },
    {
      path: '/',
      component: Layout,
      redirect: '/profile',
      children: [
        { path: 'profile', component: () => import('@/views/common/Profile.vue') }
      ]
    },
    {
      path: '/teacher',
      component: Layout,
      meta: { role: 'teacher' },
      redirect: '/teacher/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/teacher/Dashboard.vue') },
        { path: 'classes', component: () => import('@/views/teacher/Classes.vue') },
        { path: 'students', component: () => import('@/views/teacher/Students.vue') },
        { path: 'materials', component: () => import('@/views/teacher/Materials.vue') },
        { path: 'questions', component: () => import('@/views/teacher/Questions.vue') },
        { path: 'labs', component: () => import('@/views/teacher/labs/List.vue') },
        { path: 'labs/:id/steps', component: () => import('@/views/teacher/labs/Steps.vue') },
        { path: 'lab-reports', component: () => import('@/views/teacher/lab-reports/List.vue') },
        { path: 'lab-reports/:id', component: () => import('@/views/teacher/lab-reports/Detail.vue') },
        { path: 'homeworks', component: () => import('@/views/teacher/homeworks/List.vue') },
        { path: 'homeworks/:id/submissions', component: () => import('@/views/teacher/homeworks/Submissions.vue') },
        { path: 'exams', component: () => import('@/views/teacher/exams/List.vue') },
        { path: 'exams/:id/results', component: () => import('@/views/teacher/exams/Results.vue') },
        { path: 'analysis', component: () => import('@/views/teacher/Analysis.vue') },
        { path: 'plagiarism', component: () => import('@/views/teacher/Plagiarism.vue') }
      ]
    },
    {
      path: '/student',
      component: Layout,
      meta: { role: 'student' },
      redirect: '/student/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/student/Dashboard.vue') },
        { path: 'materials', component: () => import('@/views/student/Materials.vue') },
        { path: 'labs', component: () => import('@/views/student/labs/List.vue') },
        { path: 'labs/:id', component: () => import('@/views/student/labs/Detail.vue') },
        { path: 'homeworks', component: () => import('@/views/student/homeworks/List.vue') },
        { path: 'homeworks/:id', component: () => import('@/views/student/homeworks/Detail.vue') },
        { path: 'exams', component: () => import('@/views/student/exams/List.vue') },
        { path: 'exams/:id', component: () => import('@/views/student/exams/Detail.vue') },
        { path: 'scores', component: () => import('@/views/student/Scores.vue') }
      ]
    }
  ]
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  
  if (to.meta.public) {
    return next();
  }

  if (!authStore.token) {
    return next('/login');
  }

  if (!authStore.user) {
    try {
      await authStore.fetchUserInfo();
    } catch {
      return next('/login');
    }
  }

  if (to.meta.role && authStore.user?.role !== to.meta.role) {
    return next('/');
  }

  next();
});

export default router;
""",
    "src/layout/index.vue": """<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu router :default-active="$route.path">
        <template v-if="user?.role === 'teacher'">
          <el-menu-item index="/teacher/dashboard">仪表盘</el-menu-item>
          <el-menu-item index="/teacher/classes">班级管理</el-menu-item>
          <el-menu-item index="/teacher/students">学生管理</el-menu-item>
          <el-menu-item index="/teacher/materials">教学资料</el-menu-item>
          <el-menu-item index="/teacher/questions">题库管理</el-menu-item>
          <el-menu-item index="/teacher/labs">实验管理</el-menu-item>
          <el-menu-item index="/teacher/lab-reports">实验报告</el-menu-item>
          <el-menu-item index="/teacher/homeworks">作业管理</el-menu-item>
          <el-menu-item index="/teacher/exams">考试管理</el-menu-item>
          <el-menu-item index="/teacher/analysis">学情分析</el-menu-item>
          <el-menu-item index="/teacher/plagiarism">查重分析</el-menu-item>
        </template>
        <template v-if="user?.role === 'student'">
          <el-menu-item index="/student/dashboard">仪表盘</el-menu-item>
          <el-menu-item index="/student/materials">教学资料</el-menu-item>
          <el-menu-item index="/student/labs">我的实验</el-menu-item>
          <el-menu-item index="/student/homeworks">我的作业</el-menu-item>
          <el-menu-item index="/student/exams">我的考试</el-menu-item>
          <el-menu-item index="/student/scores">我的成绩</el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-right">
          <span>{{ user?.name }}</span>
          <el-button link @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';

const authStore = useAuthStore();
const router = useRouter();
const { user } = storeToRefs(authStore);

const logout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.layout-header {
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>
""",
    "src/views/auth/Login.vue": """<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h3>登录系统</h3>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="form.password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import type { FormInstance } from 'element-plus';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  username: '',
  password: ''
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const handleLogin = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await authStore.login(form);
        ElMessage.success('登录成功');
        if (authStore.user?.role === 'teacher') {
          router.push('/teacher/dashboard');
        } else if (authStore.user?.role === 'student') {
          router.push('/student/dashboard');
        } else {
          router.push('/profile');
        }
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}
.login-card {
  width: 400px;
}
</style>
""",
    "src/views/common/Profile.vue": """<template>
  <div>
    <h2>个人信息</h2>
    <el-descriptions border>
      <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ user?.name }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ user?.role }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { storeToRefs } from 'pinia';

const authStore = useAuthStore();
const { user } = storeToRefs(authStore);
</script>
""",
    "src/views/teacher/Dashboard.vue": """<template>
  <div><h2>教师仪表盘</h2></div>
</template>
""",
    "src/views/teacher/Classes.vue": """<template>
  <div>
    <h2>班级管理</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="name" label="班级名称" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getClasses } from '@/api/teacher';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getClasses(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/Students.vue": """<template>
  <div>
    <h2>学生管理</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="studentNo" label="学号" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getStudents } from '@/api/teacher';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getStudents(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/Materials.vue": """<template>
  <div>
    <h2>教学资料</h2>
    <el-button type="primary" @click="dialogVisible = true">上传资料</el-button>
    <el-table :data="list" v-loading="loading" style="margin-top: 20px;">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="title" label="资料名称" />
      <el-table-column prop="url" label="下载链接" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />

    <el-dialog v-model="dialogVisible" title="上传资料">
      <el-form :model="form" ref="formRef">
        <el-form-item label="名称" prop="title" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="链接" prop="url" required>
          <el-input v-model="form.url" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { getMaterials, createMaterial } from '@/api/teacher';
import { ElMessage } from 'element-plus';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });
const dialogVisible = ref(false);
const formRef = ref();
const form = reactive({ title: '', url: '' });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getMaterials(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const submitForm = async () => {
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      await createMaterial(form);
      ElMessage.success('上传成功');
      dialogVisible.value = false;
      fetchData();
    }
  });
};

onMounted(fetchData);
</script>
""",
    "src/views/teacher/Questions.vue": """<template>
  <div>
    <h2>题库管理</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="content" label="题目内容" />
      <el-table-column prop="type" label="类型" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getQuestions } from '@/api/teacher';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getQuestions(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/labs/List.vue": """<template>
  <div>
    <h2>实验管理</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="title" label="实验名称" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/teacher/labs/${row.id}/steps`)">步骤管理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getLabs } from '@/api/teacher';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getLabs(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/labs/Steps.vue": """<template>
  <div>
    <h2>实验步骤管理 (实验ID: {{ $route.params.id }})</h2>
    <el-button @click="$router.back()">返回</el-button>
    <el-table :data="list" v-loading="loading" style="margin-top: 20px;">
      <el-table-column prop="stepOrder" label="步骤序号" />
      <el-table-column prop="description" label="描述" />
    </el-table>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getLabSteps } from '@/api/teacher';

const route = useRoute();
const list = ref([]);
const loading = ref(false);

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getLabSteps(route.params.id as string);
    list.value = res;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/lab-reports/List.vue": """<template>
  <div>
    <h2>实验报告</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="studentName" label="学生" />
      <el-table-column prop="labTitle" label="实验" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/teacher/lab-reports/${row.id}`)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getLabReports } from '@/api/teacher';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getLabReports(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/lab-reports/Detail.vue": """<template>
  <div>
    <h2>实验报告详情</h2>
    <el-button @click="$router.back()">返回</el-button>
    <div v-if="detail" style="margin-top: 20px;">
      <p>内容: {{ detail.content }}</p>
      <p>得分: {{ detail.score }}</p>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getLabReportDetail } from '@/api/teacher';

const route = useRoute();
const detail = ref<any>(null);

const fetchData = async () => {
  const res = await getLabReportDetail(route.params.id as string);
  detail.value = res;
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/homeworks/List.vue": """<template>
  <div>
    <h2>作业管理</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="title" label="作业标题" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/teacher/homeworks/${row.id}/submissions`)">查看提交</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getHomeworks } from '@/api/teacher';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getHomeworks(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/homeworks/Submissions.vue": """<template>
  <div>
    <h2>作业提交记录</h2>
    <el-button @click="$router.back()">返回</el-button>
    <el-table :data="list" v-loading="loading" style="margin-top: 20px;">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="studentName" label="学生" />
      <el-table-column prop="status" label="状态" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getHomeworkSubmissions } from '@/api/teacher';

const route = useRoute();
const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getHomeworkSubmissions(route.params.id as string, query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/exams/List.vue": """<template>
  <div>
    <h2>考试管理</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="title" label="考试标题" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/teacher/exams/${row.id}/results`)">查看结果</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getExams } from '@/api/teacher';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getExams(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/exams/Results.vue": """<template>
  <div>
    <h2>考试成绩</h2>
    <el-button @click="$router.back()">返回</el-button>
    <el-table :data="list" v-loading="loading" style="margin-top: 20px;">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="studentName" label="学生" />
      <el-table-column prop="score" label="分数" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getExamResults } from '@/api/teacher';

const route = useRoute();
const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getExamResults(route.params.id as string, query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/teacher/Analysis.vue": """<template>
  <div>
    <h2>学情分析</h2>
    <div v-if="data">
      <p>平均分: {{ data.averageScore }}</p>
      <p>及格率: {{ data.passRate }}%</p>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getAnalysis } from '@/api/teacher';

const data = ref<any>(null);

onMounted(async () => {
  data.value = await getAnalysis();
});
</script>
""",
    "src/views/teacher/Plagiarism.vue": """<template>
  <div>
    <h2>查重分析</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="report1Id" label="报告1" />
      <el-table-column prop="report2Id" label="报告2" />
      <el-table-column prop="similarity" label="相似度" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getPlagiarism } from '@/api/teacher';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getPlagiarism(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/student/Dashboard.vue": """<template>
  <div><h2>学生仪表盘</h2></div>
</template>
""",
    "src/views/student/Materials.vue": """<template>
  <div>
    <h2>教学资料</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="title" label="资料名称" />
      <el-table-column prop="url" label="下载链接" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getMaterials } from '@/api/student';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getMaterials(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/student/labs/List.vue": """<template>
  <div>
    <h2>我的实验</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="title" label="实验名称" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/student/labs/${row.id}`)">去实验</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getLabs } from '@/api/student';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getLabs(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/student/labs/Detail.vue": """<template>
  <div>
    <h2>实验详情</h2>
    <el-button @click="$router.back()">返回</el-button>
    <div v-if="detail" style="margin-top: 20px;">
      <h3>{{ detail.title }}</h3>
      <div v-if="detail.steps && detail.steps.length > 0">
        <p>步骤 {{ currentStepIndex + 1 }} / {{ detail.steps.length }}: {{ detail.steps[currentStepIndex].description }}</p>
        <el-input type="textarea" v-model="stepResult" placeholder="请输入结果" rows="4" style="margin-bottom: 20px;" />
        <el-button :disabled="currentStepIndex === 0" @click="currentStepIndex--">上一步</el-button>
        <el-button type="primary" @click="submitStep">提交本步</el-button>
        <el-button :disabled="currentStepIndex === detail.steps.length - 1" @click="currentStepIndex++">下一步</el-button>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getLabDetail, submitLabStep } from '@/api/student';
import { ElMessage } from 'element-plus';

const route = useRoute();
const detail = ref<any>(null);
const currentStepIndex = ref(0);
const stepResult = ref('');

const fetchData = async () => {
  const res = await getLabDetail(route.params.id as string);
  detail.value = res;
};

const submitStep = async () => {
  const stepId = detail.value.steps[currentStepIndex.value].id;
  await submitLabStep(route.params.id as string, stepId, { result: stepResult.value });
  ElMessage.success('提交成功');
  if (currentStepIndex.value < detail.value.steps.length - 1) {
    currentStepIndex.value++;
    stepResult.value = '';
  }
};

onMounted(fetchData);
</script>
""",
    "src/views/student/homeworks/List.vue": """<template>
  <div>
    <h2>我的作业</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="title" label="作业标题" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/student/homeworks/${row.id}`)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getHomeworks } from '@/api/student';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getHomeworks(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/student/homeworks/Detail.vue": """<template>
  <div>
    <h2>作业详情</h2>
    <el-button @click="$router.back()">返回</el-button>
    <div v-if="detail" style="margin-top: 20px;">
      <h3>{{ detail.title }}</h3>
      <p>{{ detail.content }}</p>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getHomeworkDetail } from '@/api/student';

const route = useRoute();
const detail = ref<any>(null);

const fetchData = async () => {
  const res = await getHomeworkDetail(route.params.id as string);
  detail.value = res;
};
onMounted(fetchData);
</script>
""",
    "src/views/student/exams/List.vue": """<template>
  <div>
    <h2>我的考试</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="title" label="考试标题" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/student/exams/${row.id}`)">参加考试</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getExams } from '@/api/student';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getExams(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/views/student/exams/Detail.vue": """<template>
  <div>
    <h2>参加考试</h2>
    <div v-if="detail">
      <p>剩余时间: {{ timeLeft }} 秒</p>
      <div v-for="(q, idx) in detail.questions" :key="q.id" style="margin-bottom: 20px;">
        <p>{{ idx + 1 }}. {{ q.content }}</p>
        <el-input v-model="answers[q.id]" placeholder="请输入答案" />
      </div>
      <el-button type="primary" @click="handleSubmit">交卷</el-button>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getExamDetail, submitExam } from '@/api/student';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const detail = ref<any>(null);
const answers = ref<Record<string, string>>({});
const timeLeft = ref(3600); // 1 hour for demo
let timer: any = null;

const fetchData = async () => {
  const res = await getExamDetail(route.params.id as string);
  detail.value = res;
  res.questions?.forEach((q: any) => {
    answers.value[q.id] = '';
  });
  startTimer();
};

const startTimer = () => {
  timer = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--;
    } else {
      clearInterval(timer);
      handleSubmit();
    }
  }, 1000);
};

const handleSubmit = async () => {
  clearInterval(timer);
  await submitExam(route.params.id as string, { answers: answers.value });
  ElMessage.success('交卷成功');
  router.back();
};

onMounted(fetchData);
onUnmounted(() => clearInterval(timer));
</script>
""",
    "src/views/student/Scores.vue": """<template>
  <div>
    <h2>我的成绩</h2>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="examTitle" label="考试" />
      <el-table-column prop="score" label="分数" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @change="fetchData" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getScores } from '@/api/student';

const list = ref([]);
const total = ref(0);
const loading = ref(false);
const query = ref({ pageNum: 1, pageSize: 10 });

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getScores(query.value);
    list.value = res.list;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};
onMounted(fetchData);
</script>
""",
    "src/main.ts": """import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(ElementPlus);

app.mount('#app');
""",
    "src/App.vue": """<template>
  <router-view />
</template>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
""",
}

for rel_path, content in files.items():
    full_path = os.path.join(base_dir, rel_path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content)

print("All files generated.")
