import { useState, useEffect } from "react";
import { Chore, DEFAULT_CHORES, Person, UserRole } from "../data/types";
import { generateWeek } from "../logic/scheduler";

export function useStore() {
  const [people, setPeople] = useState<Person[]>(() => {
    const saved = localStorage.getItem("people");
    return saved ? JSON.parse(saved) : [];
  });

  const [chores, setChores] = useState<Chore[]>(() => {
    const saved = localStorage.getItem("chores");
    return saved ? JSON.parse(saved) : DEFAULT_CHORES;
  });

  const [schedule, setSchedule] = useState<Record<string, Record<string, string[]>> | null>(() => {
    const saved = localStorage.getItem("schedule");
    return saved ? JSON.parse(saved) : null;
  });

  const [priorityEnabled, setPriorityEnabled] = useState<boolean>(() => {
    const saved = localStorage.getItem("priority_enabled");
    return saved ? JSON.parse(saved) : true;
  });

  const [userRole, setUserRole] = useState<UserRole | null>(() => {
    const saved = localStorage.getItem("user_role");
    return (saved as UserRole) || null;
  });

  const [isDarkMode, setIsDarkMode] = useState<boolean>(() => {
    const saved = localStorage.getItem("dark_mode");
    return saved ? JSON.parse(saved) : true;
  });

  useEffect(() => localStorage.setItem("people", JSON.stringify(people)), [people]);
  useEffect(() => localStorage.setItem("chores", JSON.stringify(chores)), [chores]);
  useEffect(() => localStorage.setItem("schedule", JSON.stringify(schedule)), [schedule]);
  useEffect(() => localStorage.setItem("priority_enabled", JSON.stringify(priorityEnabled)), [priorityEnabled]);
  useEffect(() => {
    if (userRole) localStorage.setItem("user_role", userRole);
    else localStorage.removeItem("user_role");
  }, [userRole]);
  useEffect(() => localStorage.setItem("dark_mode", JSON.stringify(isDarkMode)), [isDarkMode]);

  const addPerson = (name: string, gender: any, unavailableDays: string[]) => {
    const newPerson: Person = {
      id: Date.now(),
      name,
      gender,
      active: true,
      unavailableDays,
    };
    setPeople([...people, newPerson]);
  };

  const updatePerson = (updated: Person) => {
    setPeople(people.map((p) => (p.id === updated.id ? updated : p)));
  };

  const deletePerson = (id: number) => {
    setPeople(people.filter((p) => p.id !== id));
  };

  const addCustomChore = (label: string, priority: number) => {
    const newChore: Chore = {
      id: `custom_${Date.now()}`,
      label,
      priority,
      isActive: true,
    };
    setChores([...chores, newChore]);
  };

  const updateChore = (updated: Chore) => {
    setChores(chores.map((c) => (c.id === updated.id ? updated : c)));
  };

  const deleteChore = (id: string) => {
    setChores(chores.filter((c) => c.id !== id));
  };

  const handleGenerateSchedule = () => {
    const newSchedule = generateWeek(people, chores, priorityEnabled);
    setSchedule(newSchedule);
  };

  return {
    people,
    chores,
    schedule,
    priorityEnabled,
    userRole,
    isDarkMode,
    setUserRole,
    setPriorityEnabled,
    setIsDarkMode,
    addPerson,
    updatePerson,
    deletePerson,
    addCustomChore,
    updateChore,
    deleteChore,
    setSchedule,
    generateSchedule: handleGenerateSchedule,
  };
}
