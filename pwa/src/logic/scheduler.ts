import { Chore, DAY_KEYS, Gender, Person, Priority } from "../data/types";

export function generateWeek(
  people: Person[],
  chores: Chore[],
  usePriorities: boolean = true
): Record<string, Record<string, string[]>> {
  const activePeople = people.filter((p) => p.active);
  const activeChores = chores.filter((c) => c.isActive);
  if (activePeople.length === 0 || activeChores.length === 0) return {};

  const newSchedule: Record<string, Record<string, string[]>> = {};
  const totalAssignments: Record<string, number> = {};
  const taskHistory: Record<string, Record<string, number>> = {};

  activePeople.forEach((p) => {
    totalAssignments[p.name] = 0;
    taskHistory[p.name] = {};
    activeChores.forEach((chore) => {
      taskHistory[p.name][chore.id] = 0;
    });
  });

  const activeDaysForLowPriority: Record<string, string[]> = {};
  activeChores.forEach((chore) => {
    if (usePriorities && chore.priority === Priority.LOW) {
      const numDays = Math.floor(Math.random() * 2) + 1;
      activeDaysForLowPriority[chore.id] = [...DAY_KEYS]
        .sort(() => Math.random() - 0.5)
        .slice(0, numDays);
    }
  });

  DAY_KEYS.forEach((day) => {
    const daySchedule: Record<string, string[]> = {};
    activeChores.forEach((it) => (daySchedule[it.id] = []));

    let available = activePeople
      .filter((p) => !p.unavailableDays.includes(day))
      .sort(() => Math.random() - 0.5);

    function pickPerson(pool: Person[], taskId: string): Person | null {
      if (pool.length === 0) return null;
      const selected = [...pool].sort((a, b) => {
        const diff = (totalAssignments[a.name] || 0) - (totalAssignments[b.name] || 0);
        if (diff !== 0) return diff;
        return (taskHistory[a.name][taskId] || 0) - (taskHistory[b.name][taskId] || 0);
      })[0];

      totalAssignments[selected.name] = (totalAssignments[selected.name] || 0) + 1;
      taskHistory[selected.name][taskId] = (taskHistory[selected.name][taskId] || 0) + 1;
      return selected;
    }

    const malePool = available.filter((it) => it.gender === Gender.MALE);
    if (activeChores.some((c) => c.id === "toilet_m")) {
      const selected = pickPerson(malePool, "toilet_m");
      if (selected) {
        daySchedule["toilet_m"].push(selected.name);
        available = available.filter((p) => p.id !== selected.id);
      }
    }

    const femalePool = available.filter((it) => it.gender === Gender.FEMALE);
    if (activeChores.some((c) => c.id === "toilet_f")) {
      const selected = pickPerson(femalePool, "toilet_f");
      if (selected) {
        daySchedule["toilet_f"].push(selected.name);
        available = available.filter((p) => p.id !== selected.id);
      }
    }

    activeChores
      .filter((c) => c.id !== "toilet_m" && c.id !== "toilet_f")
      .forEach((chore) => {
        if (
          usePriorities &&
          chore.priority === Priority.LOW &&
          !activeDaysForLowPriority[chore.id]?.includes(day)
        ) {
          return;
        }

        const selected = pickPerson(available, chore.id);
        if (selected) {
          daySchedule[chore.id].push(selected.name);
          available = available.filter((p) => p.id !== selected.id);
        }
      });

    while (available.length > 0) {
      let assignedInThisLoop = false;
      const sortedChores = usePriorities
        ? [...activeChores].sort((a, b) => b.priority - a.priority)
        : [...activeChores].sort(() => Math.random() - 0.5);

      for (const chore of sortedChores) {
        if (available.length === 0) break;

        if (
          usePriorities &&
          chore.priority === Priority.LOW &&
          !activeDaysForLowPriority[chore.id]?.includes(day)
        ) {
          continue;
        }

        const pool = chore.genderConstraint
          ? available.filter((p) => p.gender === chore.genderConstraint)
          : available;

        const selected = pickPerson(pool, chore.id);
        if (selected) {
          daySchedule[chore.id].push(selected.name);
          available = available.filter((p) => p.id !== selected.id);
          assignedInThisLoop = true;
        }
      }
      if (!assignedInThisLoop) break;
    }
    newSchedule[day] = daySchedule;
  });

  return newSchedule;
}
