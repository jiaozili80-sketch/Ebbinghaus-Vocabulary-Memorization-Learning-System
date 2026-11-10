import { defineStore } from 'pinia';
import { getMeApi, loginApi, logoutApi, type UserInfo } from '@/api/auth';

interface UserState {
  token: string;
  userInfo: UserInfo | null;
}

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

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token') || '',
    userInfo: readStoredUserInfo(),
  }),
  actions: {
    async login(username: string, password: string) {
      const res = await loginApi({ username, password });
      this.token = res.data.data.token;
      this.userInfo = res.data.data.user;
      localStorage.setItem('token', this.token);
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
    },
    async loadUserInfo() {
      if (!this.token) return;
      const res = await getMeApi();
      this.userInfo = res.data.data;
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo));
    },
    async logout() {
      if (this.token) {
        await logoutApi().catch(() => undefined);
      }
      this.token = '';
      this.userInfo = null;
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
    },
  },
});
