import { WorkerIncidenceResponseDto } from './incidence.model';

export interface DailySummaryResponse {
  accumulatedMinutes: number;
  targetMinutes: number;
  entryTime: string;
  lastTimeEntries: LastTimeEntriesResponseDto[];
}

export interface TimeEntryResponse {
  id: string;
  starAt: string;
  endAt: string;
  status: string;
}

export interface TimeEntryRequest {
  lat: number;
  lng: number;
  accuracyMeters: number;
}

export interface LastTimeEntriesResponseDto {
  timeEntryId: string;
  eventType: string;
  date: string;
}

export interface HistoryResponse {
  weeklyWorkedMinutes: string;
  weeklyTargetMinutes: string;
  dailyRecords: LastTimeEntriesResponseDto[];
  dailyWorkedMinutes: number;
  dailyTargetMinutes: number;
}

export interface DailyStatisticResponse {
  date: string;
  workedMinutes: number;
  plannedMinutes: number;
}

export interface StatisticsResponse {
  totalWorkedMinutes: number;
  minutesBalance: number;
  incompleteWorkdays: number;
  incidencesCount: number;
  dailySummary: DailyStatisticResponse[];
  incidenceList: WorkerIncidenceResponseDto[];
}

export interface ActiveWorkerDto {
  employeeId: string;
  fullName: string;
  jobPosition: string;
  avatarUrl: string;
  timeEntryTime: string;
  punctuality: number;
}

export interface TotalHoursTodayResponseDto {
  totalMinutes: number;
}

export interface DailyTimeEntryCountDto {
  date: string;
  timeEntryNumber: number;
}

export interface AdminTimeEntryByDateResponseDto {
  id: string;
  employeeId: string;
  workerName: string;
  jobPosition: string | null;
  avatarUrl: string | null;
  date: string;
  startAt: string;
  endAt: string | null;
  workedMinutes: number | null;
}

export interface TimeEntryTableResponseDto {
  id: string;
  date: string;
  startAt: string;
  endAt: string | null;
  latStartAt: number | null;
  lngStartAt: number | null;
  latEndAt: number | null;
  lngEndAt: number | null;
  workedHours: number | null;
  deletedAt?: string | null;
  work_date?: string;
  start_at?: string;
  end_at?: string | null;
  start_lat?: number | null;
  start_lng?: number | null;
  end_lat?: number | null;
  end_lng?: number | null;
  deleted_at?: string | null;
}

export interface EditTimeEntryRequestDto {
  startAt: string;
  endAt: string | null;
  justification: string;
}

export interface VoidTimeEntryRequestDto {
  justification: string;
}
