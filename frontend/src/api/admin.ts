import request from './request';
import type { ApiResult } from './auth';
import type { PageResult } from './wordBook';

export interface AdminDashboard {
  userCount: number;
  activeUserCount: number;
  wordBookCount: number;
  wordCount: number;
  todayStudyCount: number;
  todayCheckinCount: number;
  wrongWordCount: number;
  reviewPlanCount: number;
}

export interface AdminUser {
  id: number;
  username: string;
  nickname: string;
  email?: string;
  role: 'USER' | 'ADMIN';
  status: number;
  totalWords: number;
  masteredWords: number;
  continuousCheckinDays: number;
  createTime: string;
}

export interface OperationLog {
  id: number;
  userId: number;
  module: string;
  operation: string;
  requestUri: string;
  requestMethod: string;
  ip: string;
  createTime: string;
}

export interface Announcement {
  id: number;
  title: string;
  content: string;
  type: string;
  status: number;
  createBy?: number;
  createTime?: string;
}

export function getAdminDashboard() {
  return request.get<ApiResult<AdminDashboard>>('/admin/dashboard');
}

export function getAdminUsers(params: { current: number; size: number; keyword?: string; status?: number }) {
  return request.get<ApiResult<PageResult<AdminUser>>>('/admin/users/page', { params });
}

export function updateAdminUserStatus(id: number, status: number) {
  return request.post<ApiResult<null>>(`/admin/users/${id}/status`, null, { params: { status } });
}

export function getOperationLogs(params: { current: number; size: number }) {
  return request.get<ApiResult<PageResult<OperationLog>>>('/admin/logs/page', { params });
}

export function getAnnouncements() {
  return request.get<ApiResult<Announcement[]>>('/announcements');
}

export function getAnnouncementPage(params: { current: number; size: number; keyword?: string; type?: string; status?: number }) {
  return request.get<ApiResult<PageResult<Announcement>>>('/announcements/page', { params });
}

export function createAnnouncement(data: Omit<Announcement, 'id'>) {
  return request.post<ApiResult<Announcement>>('/announcements', data);
}

export function updateAnnouncement(id: number, data: Omit<Announcement, 'id'>) {
  return request.put<ApiResult<Announcement>>(`/announcements/${id}`, data);
}

export function deleteAnnouncement(id: number) {
  return request.delete<ApiResult<null>>(`/announcements/${id}`);
}
