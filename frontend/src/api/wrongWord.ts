import request from './request';
import type { ApiResult } from './auth';
import type { PageResult } from './wordBook';
import type { StudyWord } from './study';

export interface WrongWord {
  id: number;
  wordId: number;
  bookId: number;
  spelling: string;
  phonetic?: string;
  partOfSpeech?: string;
  meaning: string;
  exampleSentence?: string;
  exampleTranslation?: string;
  audioUrl?: string;
  difficulty: number;
  wrongCount: number;
  status: number;
  createTime: string;
  updateTime: string;
}

export function getWrongWordPage(params: {
  current: number;
  size: number;
  bookId?: number;
  keyword?: string;
}) {
  return request.get<ApiResult<PageResult<WrongWord>>>('/wrong-words/page', { params });
}

export function exportWrongWords(params: { bookId?: number; keyword?: string }) {
  return request.get<ApiResult<WrongWord[]>>('/wrong-words/export', { params });
}

export function getWrongWordReinforce(params: { bookId?: number; limit?: number }) {
  return request.get<ApiResult<StudyWord[]>>('/wrong-words/reinforce', { params });
}

export function markWrongWordMastered(id: number) {
  return request.post<ApiResult<null>>(`/wrong-words/${id}/mastered`);
}

export function removeWrongWord(id: number) {
  return request.delete<ApiResult<null>>(`/wrong-words/${id}`);
}
