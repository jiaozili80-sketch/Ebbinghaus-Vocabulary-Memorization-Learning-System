import request from './request';
import type { ApiResult } from './auth';

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export interface WordBook {
  id: number;
  userId?: number;
  name: string;
  category: string;
  description: string;
  wordCount: number;
  type: number;
  status: number;
}

export interface WordBookForm {
  name: string;
  category: string;
  description?: string;
  type: number;
  status: number;
}

export function getWordBooks(category?: string) {
  return request.get<ApiResult<WordBook[]>>('/word-books', {
    params: { category },
  });
}

export function getWordBookPage(params: {
  current: number;
  size: number;
  keyword?: string;
  category?: string;
  status?: number;
}) {
  return request.get<ApiResult<PageResult<WordBook>>>('/word-books/page', { params });
}

export function createWordBook(data: WordBookForm) {
  return request.post<ApiResult<WordBook>>('/word-books', data);
}

export function updateWordBook(id: number, data: WordBookForm) {
  return request.put<ApiResult<WordBook>>(`/word-books/${id}`, data);
}

export function deleteWordBook(id: number) {
  return request.delete<ApiResult<null>>(`/word-books/${id}`);
}
