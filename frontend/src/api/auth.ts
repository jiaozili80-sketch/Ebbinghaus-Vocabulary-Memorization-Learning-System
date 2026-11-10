import request from './request';

export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  avatar?: string;
  email?: string;
  role: 'USER' | 'ADMIN';
  totalWords: number;
  masteredWords: number;
  continuousCheckinDays: number;
}

export interface LoginResult {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: UserInfo;
}

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export function loginApi(data: { username: string; password: string }) {
  return request.post<ApiResult<LoginResult>>('/auth/login', data);
}

export function registerApi(data: { username: string; password: string; nickname?: string; email?: string }) {
  return request.post<ApiResult<UserInfo>>('/auth/register', data);
}

export function getMeApi() {
  return request.get<ApiResult<UserInfo>>('/auth/me');
}

export function logoutApi() {
  return request.post<ApiResult<null>>('/auth/logout');
}
