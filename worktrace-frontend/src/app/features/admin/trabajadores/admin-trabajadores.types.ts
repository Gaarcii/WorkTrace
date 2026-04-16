export interface EmployeeListItem {
  id: string;
  fullName: string;
  jobTitle: string | null;
  weeklyHours: number | null;
  isFirstLogin: boolean;
  isActive: boolean;
  createdAt: string;
  email?: string;
  phone?: string;
  avatarUrl?: string | null;
  dni?: string;
  positionId?: string | null;
}

export interface EmployeeTableHeader {
  key: string;
  title: string;
}

export interface SnackbarState {
  show: boolean;
  message: string;
  color: 'success' | 'error' | 'warning' | 'info';
}

export interface CreateEmployeeForm {
  fullName: string;
  employeeCode: string;
  email: string;
  phone: string;
  weeklyHours: number | null;
  positionId: string | null;
}

export interface EditProfileForm {
  positionId: string | null;
  weeklyHours: number | null;
}

export interface EditFichajeForm {
  id: string;
  date: string;
  startTime: string;
  endTime: string;
  modificationReason: string;
}

export interface TableHeader {
  key: string;
  title: string;
}