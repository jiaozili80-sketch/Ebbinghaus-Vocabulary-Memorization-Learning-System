import { createRouter, createWebHistory } from 'vue-router';
import type { UserInfo } from '@/api/auth';

const Login = () => import('@/views/Login.vue');
const Register = () => import('@/views/Register.vue');
const Dashboard = () => import('@/views/Dashboard.vue');
const WordBooks = () => import('@/views/WordBooks.vue');
const Study = () => import('@/views/Study.vue');
const WrongWords = () => import('@/views/WrongWords.vue');
const Schedule = () => import('@/views/Schedule.vue');
const Statistics = () => import('@/views/Statistics.vue');
const AdminDashboard = () => import('@/views/AdminDashboard.vue');
const UserManage = () => import('@/views/UserManage.vue');
const WordBookManage = () => import('@/views/WordBookManage.vue');
const WordManage = () => import('@/views/WordManage.vue');
const AnnouncementManage = () => import('@/views/AnnouncementManage.vue');
const OperationLogs = () => import('@/views/OperationLogs.vue');

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login, meta: { title: '登录', public: true } },
    { path: '/register', component: Register, meta: { title: '注册', public: true } },
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', component: Dashboard, meta: { title: '学习总览' } },
    { path: '/word-books', component: WordBooks, meta: { title: '词库中心' } },
    { path: '/manage/word-books', component: WordBookManage, meta: { title: '词库管理', admin: true } },
    { path: '/manage/words', component: WordManage, meta: { title: '单词管理', admin: true } },
    { path: '/study', component: Study, meta: { title: '艾宾浩斯背诵' } },
    { path: '/wrong-words', component: WrongWords, meta: { title: '生词本与错题本' } },
    { path: '/schedule', component: Schedule, meta: { title: '学习打卡与日程' } },
    { path: '/plan', redirect: '/schedule' },
    { path: '/statistics', component: Statistics, meta: { title: '学习统计' } },
    { path: '/manage/dashboard', component: AdminDashboard, meta: { title: '后台数据总览', admin: true } },
    { path: '/manage/users', component: UserManage, meta: { title: '用户管理', admin: true } },
    { path: '/manage/announcements', component: AnnouncementManage, meta: { title: '公告管理', admin: true } },
    { path: '/manage/logs', component: OperationLogs, meta: { title: '操作日志', admin: true } },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
});

function readStoredUserInfo(): UserInfo | null {
  const userInfoText = localStorage.getItem('userInfo');
  if (!userInfoText) return null;
  try {
    return JSON.parse(userInfoText) as UserInfo;
  } catch {
    localStorage.removeItem('userInfo');
    return null;
  }
}

router.beforeEach((to) => {
  const token = localStorage.getItem('token');
  const userInfo = readStoredUserInfo();
  if (!to.meta.public && !token) {
    return '/login';
  }
  if ((to.path === '/login' || to.path === '/register') && token) {
    return '/dashboard';
  }
  if (to.meta.admin && userInfo?.role !== 'ADMIN') {
    return '/dashboard';
  }
});

export default router;
