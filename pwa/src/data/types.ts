export enum Gender {
  MALE = "M",
  FEMALE = "F",
}

export enum UserRole {
  MANAGER = "MANAGER",
  WORKER = "WORKER",
}

export enum Priority {
  LOW = 1,
  MEDIUM = 2,
  HIGH = 3,
}

export interface Person {
  id: number;
  name: string;
  gender: Gender;
  active: boolean;
  unavailableDays: string[];
}

export interface Chore {
  id: string;
  label: string;
  priority: Priority;
  genderConstraint?: Gender;
  isActive: boolean;
}

export interface SyncData {
  people: Person[];
  chores: Chore[];
  schedule: Record<string, Record<string, string[]>> | null;
  priorityEnabled: boolean;
}

export const DEFAULT_CHORES: Chore[] = [
  { id: "toilet_m", label: "🚽 שירותים בנים", priority: Priority.HIGH, genderConstraint: Gender.MALE, isActive: true },
  { id: "toilet_f", label: "🚺 שירותים בנות", priority: Priority.HIGH, genderConstraint: Gender.FEMALE, isActive: true },
  { id: "office", label: "🧹 ניקיון משרד", priority: Priority.LOW, isActive: true },
  { id: "grass", label: "🌿 דשא", priority: Priority.LOW, isActive: true },
  { id: "kitchen", label: "☕ מטבחון", priority: Priority.MEDIUM, isActive: true },
  { id: "trash", label: "🗑️ פחים", priority: Priority.MEDIUM, isActive: true },
];

export const DAYS_HE: Record<string, string> = {
  sun: "ראשון",
  mon: "שני",
  tue: "שלישי",
  wed: "רביעי",
  thu: "חמישי",
};

export const DAY_KEYS = ["sun", "mon", "tue", "wed", "thu"];
