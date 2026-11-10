import request from './request';
import type { ApiResult } from './auth';
import type { PageResult } from './wordBook';

export interface Word {
  id: number;
  bookId: number;
  spelling: string;
  phonetic?: string;
  partOfSpeech?: string;
  meaning: string;
  exampleSentence?: string;
  exampleTranslation?: string;
  audioUrl?: string;
  difficulty: number;
  status: number;
}

export interface WordForm {
  bookId?: number;
  spelling: string;
  phonetic?: string;
  partOfSpeech?: string;
  meaning: string;
  exampleSentence?: string;
  exampleTranslation?: string;
  audioUrl?: string;
  difficulty: number;
  status: number;
}

export function getWordPage(params: {
  current: number;
  size: number;
  bookId?: number;
  keyword?: string;
  status?: number;
}) {
  return request.get<ApiResult<PageResult<Word>>>('/words/page', { params });
}

export function createWord(data: WordForm) {
  return request.post<ApiResult<Word>>('/words', data);
}

export function updateWord(id: number, data: WordForm) {
  return request.put<ApiResult<Word>>(`/words/${id}`, data);
}

export function deleteWord(id: number) {
  return request.delete<ApiResult<null>>(`/words/${id}`);
}
