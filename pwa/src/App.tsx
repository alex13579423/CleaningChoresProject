import React, { useState, useEffect, useRef } from "react";
import { useStore } from "./store/useStore";
import { UserRole, DAYS_HE, DAY_KEYS, Priority, Gender, Person } from "./data/types";
import { useDarkMode } from './hooks/useDarkMode';
import { Html5Qrcode } from 'html5-qrcode';
import { compressString, encryptBytes, decryptBytes, decompressGzip, bytesToBase64, base64ToBytes } from './utils/qrCrypto';
import { QrShare } from './components/QrShare';

const App = () => {
  const store = useStore();
  const { isDark, toggleDarkMode } = useDarkMode();
  const [activeTab, setActiveTab] = useState(0);
  const [modalType, setModalType] = useState<"person" | "chore" | "scanQr" | "shareQr" | null>(null);
  const [editingItem, setEditingItem] = useState<any>(null);

  if (!store.userRole) {
    return (
      <div className="min-h-screen w-full flex items-center justify-center p-6 bg-background">
        <div className="w-full max-w-md p-10 rounded-[40px] bg-surface border border-outline/10 shadow-2xl text-center flex flex-col items-center">
          <div className="w-24 h-24 bg-primary-container rounded-[32px] mb-8 flex items-center justify-center text-5xl shadow-inner border border-primary/5 animate-bounce-slow">
            🧹
          </div>
          <h1 className="text-3xl font-black mb-3 text-on-surface tracking-tight">לוח תורנויות 👋</h1>
          <p className="text-on-surface-variant text-sm mb-12 leading-relaxed opacity-80 max-w-[240px]">נהלו את תורנויות הניקיון שלכם בקלות ובסטייל</p>
          <div className="space-y-4 w-full">
            <button
              onClick={() => store.setUserRole(UserRole.MANAGER)}
              className="m3-button-filled w-full h-16 !text-lg !rounded-[24px] shadow-primary/20"
            >
              👨‍💼 מנהל (גישה מלאה)
            </button>
            <button
              onClick={() => store.setUserRole(UserRole.WORKER)}
              className="m3-button-tonal w-full h-16 !text-lg !rounded-[24px] !bg-surface-variant/40"
            >
              👷 עובד (צפייה בלבד)
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen w-full bg-background flex flex-col items-center font-sans text-on-background" dir="rtl">
      <header className="w-full h-20 sticky top-0 z-30 flex justify-center items-center bg-background/80 backdrop-blur-xl border-b border-outline/5 px-6">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center text-xl shadow-lg text-on-primary">🧹</div>
          <h1 className="text-xl font-black tracking-tight text-on-surface">לוח תורנויות</h1>
        </div>
      </header>

      <main className="w-full max-w-2xl px-4 py-8 pb-48">
        {activeTab === 0 && <ScheduleScreen store={store} />}
        {activeTab === 1 && <StatsScreen store={store} />}
        {activeTab === 2 && (
            <SettingsScreen
                store={store}
                isDark={isDark}
                toggleDarkMode={toggleDarkMode}
                onAddPerson={() => { setEditingItem(null); setModalType("person"); }}
                onEditPerson={(p) => { setEditingItem(p); setModalType("person"); }}
                onAddChore={() => { setEditingItem(null); setModalType("chore"); }}
                onScanQr={() => setModalType("scanQr")}
                onShareQr={() => setModalType("shareQr")}
            />
        )}
      </main>

      <nav className="fixed bottom-6 left-6 right-6 h-20 bg-surface/90 backdrop-blur-2xl flex justify-around items-center px-4 z-40 max-w-2xl mx-auto rounded-[32px] border border-outline/20 shadow-[0_12px_48px_rgba(0,0,0,0.2)] dark:shadow-[0_12px_64px_rgba(0,0,0,0.6)] overflow-hidden">
        {[
          { icon: activeTab === 0 ? "📅" : "🗓️", label: "לוח" },
          { icon: activeTab === 1 ? "📊" : "📈", label: "סטטיסטיקה" },
          { icon: activeTab === 2 ? "⚙️" : "🛠️", label: "הגדרות" }
        ].map((item, idx) => (
          <button
            key={idx}
            onClick={() => setActiveTab(idx)}
            className="flex flex-col items-center group relative flex-1 h-full justify-center transition-transform active:scale-90"
          >
            <div className={`w-16 h-10 rounded-m3-full flex items-center justify-center mb-1 transition-all duration-300 ${activeTab === idx ? 'bg-primary-container text-on-primary-container scale-110 shadow-sm' : 'group-hover:bg-surface-variant/50 text-on-surface-variant opacity-70'}`}>
              <span className={`text-xl transition-transform ${activeTab === idx ? 'scale-110' : ''}`}>{item.icon}</span>
            </div>
            <span className={`text-[10px] font-black tracking-widest transition-colors ${activeTab === idx ? 'text-on-surface' : 'text-on-surface-variant opacity-40'}`}>{item.label}</span>
          </button>
        ))}
      </nav>

      {modalType === "person" && (
        <PersonModal
            person={editingItem}
            onClose={() => setModalType(null)}
            onSave={(name, gender, days) => {
                if (editingItem) {
                    store.updatePerson({ ...editingItem, name, gender, unavailableDays: days });
                } else {
                    store.addPerson(name, gender, days);
                }
                setModalType(null);
            }}
        />
      )}

      {modalType === "chore" && (
        <ChoreModal
            onClose={() => setModalType(null)}
            onSave={(label, priority) => {
                store.addCustomChore(label, priority);
                setModalType(null);
            }}
        />
      )}

      {modalType === "shareQr" && (
        <QrShareModal
            data={JSON.stringify({
                people: store.people,
                chores: store.chores,
                schedule: store.schedule,
                priorityEnabled: store.priorityEnabled
            })}
            onClose={() => setModalType(null)}
        />
      )}

      {modalType === "scanQr" && (
        <QrScanModal
            onClose={() => setModalType(null)}
            onResult={(data) => {
                try {
                    const schedule = JSON.parse(data);
                    store.setSchedule(schedule);
                    setModalType(null);
                    setActiveTab(0);
                } catch (e) {
                    alert("QR Code Invalid");
                }
            }}
        />
      )}
    </div>
  );
};

const QrShareModal = ({ data, onClose }: { data: string, onClose: () => void }) => {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-300">
            <div className="w-full max-w-sm bg-surface rounded-[32px] border border-outline/10 shadow-2xl p-8 flex flex-col items-center gap-6 animate-in slide-in-from-bottom-10" dir="rtl">
                <h2 className="text-xl font-black text-on-surface">שיתוף סידור</h2>
                <QrShare jsonData={data} />
                <button onClick={onClose} className="m3-button-tonal w-full h-14 !rounded-2xl">
                    סגור
                </button>
            </div>
        </div>
    );
};

const QrScanModal = ({ onClose, onResult }: { onClose: () => void, onResult: (data: string) => void }) => {
    const [progress, setProgress] = useState(0);
    const collectedChunks = useRef<Map<number, string>>(new Map());
    const totalExpected = useRef<number>(0);

    useEffect(() => {
        let html5QrCode: Html5Qrcode | null = null;
        let isMounted = true;

        const timer = setTimeout(() => {
            if (!isMounted) return;
            html5QrCode = new Html5Qrcode("qr-reader");

            html5QrCode.start(
                { facingMode: "environment" },
                { fps: 15, qrbox: { width: 250, height: 250 } },
                async (decodedText) => {
                    try {
                        const parts = decodedText.split('|');
                        if (parts.length !== 2) return;

                        const header = parts[0].split('/');
                        if (header.length !== 2) return;

                        const index = parseInt(header[0], 10);
                        const total = parseInt(header[1], 10);
                        const chunkData = parts[1];

                        if (isNaN(index) || isNaN(total)) return;

                        if (totalExpected.current === 0) {
                            totalExpected.current = total;
                        }

                        if (!collectedChunks.current.has(index)) {
                            collectedChunks.current.set(index, chunkData);
                            const currentProgress = Math.round((collectedChunks.current.size / total) * 100);
                            setProgress(currentProgress);

                            if (collectedChunks.current.size === total) {
                                let fullBase64 = "";
                                for (let i = 0; i < total; i++) {
                                    const part = collectedChunks.current.get(i);
                                    if (!part) throw new Error("Missing chunk");
                                    fullBase64 += part;
                                }

                                const bytes = base64ToBytes(fullBase64);
                                const decrypted = await decryptBytes(bytes);
                                const json = decompressGzip(decrypted);

                                if (html5QrCode && html5QrCode.isScanning) {
                                    await html5QrCode.stop();
                                }
                                onResult(json);
                            }
                        }
                    } catch (err) {
                        // Reset cache safely on any decryption mismatch
                        collectedChunks.current.clear();
                        totalExpected.current = 0;
                        setProgress(0);
                    }
                },
                () => {}
            ).catch(() => {});
        }, 100);

        return () => {
            isMounted = false;
            clearTimeout(timer);
            if (html5QrCode) {
                try {
                    if (html5QrCode.isScanning) {
                        html5QrCode.stop().catch(() => {});
                    }
                } catch (e) {}
            }
        };
    }, [onResult]);

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-300">
            <div className="w-full max-w-sm bg-surface rounded-[32px] border border-outline/10 shadow-2xl p-6 flex flex-col items-center gap-6 animate-in slide-in-from-bottom-10" dir="rtl">
                <h2 className="text-xl font-black text-on-surface">סריקת סידור</h2>

                <div className="w-full aspect-square rounded-2xl overflow-hidden shadow-inner border-2 border-primary/20 relative bg-black">
                    <div id="qr-reader" className="w-full h-full object-cover" />
                </div>

                {progress > 0 && (
                    <div className="w-full space-y-2">
                        <div className="w-full h-2 bg-surface-variant rounded-full overflow-hidden">
                            <div className="h-full bg-primary transition-all duration-300" style={{ width: `${progress}%` }} />
                        </div>
                        <p className="text-xs text-center text-on-surface-variant font-bold">{progress}%</p>
                    </div>
                )}

                <button onClick={onClose} className="m3-button-tonal w-full h-14 !rounded-2xl">
                    סגור
                </button>
            </div>
        </div>
    );
};
const PersonModal = ({ person, onClose, onSave }: { person?: Person, onClose: () => void, onSave: (n: string, g: Gender, d: string[]) => void }) => {
    const [name, setName] = useState(person?.name || "");
    const [gender, setGender] = useState<Gender>(person?.gender || Gender.MALE);
    const [unavailableDays, setUnavailableDays] = useState<string[]>(person?.unavailableDays || []);

    return (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-300">
            <div className="w-full max-w-lg bg-surface rounded-t-[40px] sm:rounded-[40px] border-t sm:border border-outline/10 shadow-2xl p-10 flex flex-col gap-8 animate-in slide-in-from-bottom-20 duration-500" dir="rtl">
                <div className="flex justify-between items-center">
                    <h2 className="text-2xl font-black text-on-surface">{person ? 'עריכת משתתף' : 'הוספת משתתף חדש 👤'}</h2>
                    <button onClick={onClose} className="w-12 h-12 rounded-full bg-surface-variant/40 flex items-center justify-center text-xl hover:bg-surface-variant/60 transition-colors">✕</button>
                </div>

                <div className="space-y-3">
                    <label className="text-[11px] font-black text-on-surface-variant/60 uppercase tracking-widest mr-1">שם המשתתף</label>
                    <input
                        autoFocus
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder="הכנס שם מלא..."
                        className="m3-input"
                    />
                </div>

                <div className="space-y-4">
                    <label className="text-[11px] font-black text-on-surface-variant/60 uppercase tracking-widest mr-1">מגדר</label>
                    <div className="flex gap-4">
                        {[
                            { id: Gender.MALE, label: 'בן', color: '#90CAF9', icon: '👦' },
                            { id: Gender.FEMALE, label: 'בת', color: '#673AB7', icon: '👧' }
                        ].map((g) => (
                            <button
                                key={g.id}
                                onClick={() => setGender(g.id)}
                                className={`flex-1 h-20 rounded-3xl border-2 transition-all flex flex-col items-center justify-center gap-1.5 ${gender === g.id ? 'border-primary bg-primary/10 shadow-lg scale-105' : 'border-outline/10 bg-surface-variant/10 opacity-50 hover:opacity-100'}`}
                            >
                                <span className="text-2xl">{g.icon}</span>
                                <span className="text-sm font-black">{g.label}</span>
                            </button>
                        ))}
                    </div>
                </div>

                <div className="space-y-4">
                    <label className="text-[11px] font-black text-on-surface-variant/60 uppercase tracking-widest mr-1">ימי היעדרות (לא זמין)</label>
                    <div className="flex justify-between gap-2">
                        {DAY_KEYS.map((day) => {
                            const isSelected = unavailableDays.includes(day);
                            return (
                                <button
                                    key={day}
                                    onClick={() => setUnavailableDays(isSelected ? unavailableDays.filter(d => d !== day) : [...unavailableDays, day])}
                                    className={`flex-1 py-4 rounded-2xl text-sm font-black border transition-all ${isSelected ? 'bg-error text-on-error border-transparent shadow-lg scale-110' : 'bg-surface-variant/20 border-outline/10 text-on-surface-variant opacity-40 hover:opacity-100'}`}
                                >
                                    {DAYS_HE[day].charAt(0)}
                                </button>
                            );
                        })}
                    </div>
                </div>

                <button
                    disabled={!name.trim()}
                    onClick={() => onSave(name, gender, unavailableDays)}
                    className="m3-button-filled w-full h-16 !rounded-3xl !text-lg mt-4 disabled:opacity-30 shadow-xl shadow-primary/20"
                >
                    שמור משתתף
                </button>
            </div>
        </div>
    );
};

const ChoreModal = ({ onClose, onSave }: { onClose: () => void, onSave: (l: string, p: Priority) => void }) => {
    const [label, setLabel] = useState("");
    const [priority, setPriority] = useState<Priority>(Priority.MEDIUM);

    return (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-300">
            <div className="w-full max-w-lg bg-surface rounded-t-[40px] sm:rounded-[40px] border-t sm:border border-outline/10 shadow-2xl p-10 flex flex-col gap-8 animate-in slide-in-from-bottom-20 duration-500" dir="rtl">
                <div className="flex justify-between items-center">
                    <h2 className="text-2xl font-black text-on-surface">הוספת משימה חדשה 🧹</h2>
                    <button onClick={onClose} className="w-12 h-12 rounded-full bg-surface-variant/40 flex items-center justify-center text-xl hover:bg-surface-variant/60 transition-colors">✕</button>
                </div>

                <div className="space-y-3">
                    <label className="text-[11px] font-black text-on-surface-variant/60 uppercase tracking-widest mr-1">שם המשימה</label>
                    <input
                        autoFocus
                        value={label}
                        onChange={(e) => setLabel(e.target.value)}
                        placeholder="לדוגמה: ניקוי שולחנות..."
                        className="m3-input"
                    />
                </div>

                <div className="space-y-4">
                    <label className="text-[11px] font-black text-on-surface-variant/60 uppercase tracking-widest mr-1">רמת קושי</label>
                    <div className="flex gap-3">
                        {[Priority.LOW, Priority.MEDIUM, Priority.HIGH].map((p) => {
                            const isSelected = priority === p;
                            const pLabel = p === Priority.LOW ? 'קל' : p === Priority.MEDIUM ? 'בינוני' : 'קשה';
                            const pColor = p === Priority.LOW ? '#A5D6A7' : p === Priority.MEDIUM ? '#FFD180' : '#FF8A80';
                            return (
                                <button
                                    key={p}
                                    onClick={() => setPriority(p)}
                                    className={`flex-1 h-16 rounded-2xl border-2 transition-all font-black text-sm ${isSelected ? 'shadow-lg scale-105 border-transparent' : 'border-outline/10 bg-surface-variant/10 opacity-40 hover:opacity-100'}`}
                                    style={{
                                        backgroundColor: isSelected ? pColor : '',
                                        color: isSelected ? (p === Priority.LOW ? '#003300' : p === Priority.MEDIUM ? '#5D4037' : '#FFFFFF') : ''
                                    }}
                                >
                                    {pLabel}
                                </button>
                            );
                        })}
                    </div>
                </div>

                <button
                    disabled={!label.trim()}
                    onClick={() => onSave(label, priority)}
                    className="m3-button-filled w-full h-16 !rounded-3xl !text-lg mt-4 disabled:opacity-30 shadow-xl shadow-primary/20"
                >
                    שמור משימה
                </button>
            </div>
        </div>
    );
};

const DaySelector = ({ selectedDay, onSelect }: { selectedDay: string, onSelect: (day: string) => void }) => {
  return (
    <div className="m3-card-variant p-2 mb-10 flex items-center shadow-inner !rounded-[32px] border-outline/10 bg-surface-variant/20">
      {DAY_KEYS.map((key, idx) => {
        const isSelected = selectedDay === key;
        return (
          <React.Fragment key={key}>
            {idx > 0 && <div className="w-[1px] h-8 bg-outline/10" />}
            <button
              onClick={() => onSelect(key)}
              className={`flex-1 py-4 rounded-[24px] text-sm font-black transition-all ${
                isSelected ? 'bg-primary text-on-primary shadow-xl scale-110 z-10' : 'text-on-surface-variant hover:bg-surface-variant/50'
              }`}
            >
              {DAYS_HE[key]}
            </button>
          </React.Fragment>
        );
      })}
    </div>
  );
};

const ScheduleScreen = ({ store }: { store: any }) => {
  const [selectedDay, setSelectedDay] = useState("sun");
  const [isEditing, setIsEditing] = useState(false);
  const daySchedule = store.schedule?.[selectedDay] || {};

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-10 duration-500">
      <DaySelector selectedDay={selectedDay} onSelect={(d) => { setSelectedDay(d); setIsEditing(false); }} />

      <div className="flex items-center justify-between px-3">
        <div className="flex items-center gap-3">
            <h2 className="text-3xl font-black text-on-surface">יום {DAYS_HE[selectedDay]}</h2>
            <div className="w-2 h-2 bg-primary rounded-full animate-pulse shadow-[0_0_8px_rgba(var(--md-sys-color-primary),0.5)]" />
        </div>
        {store.userRole === UserRole.MANAGER && (
          <button
            onClick={() => setIsEditing(!isEditing)}
            className={`m3-button-tonal !h-10 !px-6 !text-xs !rounded-full border border-outline/10 shadow-sm ${isEditing ? '!bg-primary !text-on-primary' : ''}`}
          >
            {isEditing ? '💾 שמור שינויים' : '✏️ עריכת תורנים'}
          </button>
        )}
      </div>

      {!store.schedule ? (
        <div className="m3-card p-20 text-center border-dashed border-2 bg-surface/50 backdrop-blur-sm flex flex-col items-center rounded-[48px]">
          <div className="w-24 h-24 bg-surface-variant/30 rounded-[32px] flex items-center justify-center text-5xl mb-8 shadow-inner border border-outline/5">🗓️</div>
          <h3 className="text-2xl font-black mb-3 text-on-surface">אין סידור לשבוע זה</h3>
          <p className="text-on-surface-variant text-sm mb-10 max-w-[240px] leading-relaxed">המנהל טרם הגריל את תורנויות השבוע. ניתן להגריל בהגדרות או כאן:</p>
          {store.userRole === UserRole.MANAGER && (
            <button
              onClick={store.generateSchedule}
              className="m3-button-filled h-16 px-14 !rounded-[24px] shadow-2xl shadow-primary/20 scale-105 active:scale-95"
            >
              🎲 הגרל סידור חדש
            </button>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {store.chores.filter((c: any) => c.isActive).map((chore: any) => (
            <div key={chore.id} className="m3-card !rounded-[32px] border-none bg-surface shadow-lg hover:shadow-xl transition-all">
              <div className="p-6">
                <div className="flex items-start justify-between">
                    <div className="flex-1">
                    <h3 className="font-black text-xl text-on-surface leading-tight mb-1">{chore.label}</h3>
                    {store.priorityEnabled && (
                        <div className={`inline-block px-3 py-1 rounded-xl mt-2 text-[10px] font-black uppercase tracking-widest ${
                            chore.priority === 3 ? 'bg-error/10 text-error' :
                            chore.priority === 2 ? 'bg-[#FFA000]/10 text-[#FFA000]' :
                            'bg-secondary/10 text-secondary'
                        }`}>
                            {chore.priority === 3 ? 'קשה מאוד' : chore.priority === 2 ? 'בינוני' : 'קל'}
                        </div>
                    )}
                    </div>

                    {!isEditing && (
                        <div className="flex flex-wrap gap-2 justify-end max-w-[65%] mt-1">
                            {(daySchedule[chore.id] || []).length > 0 ? (
                                daySchedule[chore.id].map((name: string) => (
                                    <div key={name} className="px-5 py-2.5 bg-primary-container text-on-primary-container text-sm font-black rounded-2xl shadow-md border border-primary/10">
                                        {name}
                                    </div>
                                ))
                            ) : (
                                <div className={`flex items-center gap-2 px-4 py-2 rounded-2xl border-2 ${chore.priority === 1 ? 'border-outline/10 opacity-30 bg-surface-variant/10' : 'border-error/20 bg-error/5 animate-pulse'}`}>
                                    <span className={`text-xs font-black ${chore.priority === 1 ? 'text-on-surface-variant' : 'text-error'}`}>
                                        {chore.id === 'toilet_m' ? '⚠️ אין בן פנוי' : chore.id === 'toilet_f' ? '⚠️ אין בת פנויה' : chore.priority === 1 ? '🏖️ לא נדרש היום' : '⚠️ חסר כוח אדם'}
                                    </span>
                                </div>
                            )}
                        </div>
                    )}
                </div>

                {isEditing && (
                    <div className="mt-6 pt-6 border-t border-outline/10">
                    <p className="text-[11px] font-black text-on-surface-variant/50 uppercase mb-4 px-1 tracking-widest">שינוי תורנים ידני:</p>
                    <div className="flex flex-wrap gap-2.5">
                        {store.people
                            .filter((p: any) => p.active)
                            .filter((p: any) => !chore.genderConstraint || p.gender === chore.genderConstraint)
                            .map((person: any) => {
                            const isSelected = (daySchedule[chore.id] || []).includes(person.name);
                            return (
                                <button
                                key={person.id}
                                onClick={() => {
                                    const current = daySchedule[chore.id] || [];
                                    const next = current.includes(person.name)
                                        ? current.filter((n:string) => n !== person.name)
                                        : [...current, person.name];

                                    const newSchedule = { ...store.schedule };
                                    newSchedule[selectedDay] = { ...newSchedule[selectedDay], [chore.id]: next };
                                    store.setSchedule(newSchedule);
                                }}
                                className={`px-5 py-3 rounded-2xl text-xs font-black border-2 transition-all ${
                                    isSelected ? 'bg-primary text-on-primary border-transparent shadow-lg scale-105 z-10' : 'bg-surface-variant/10 text-on-surface-variant border-outline/10 hover:border-primary/20'
                                }`}
                                >
                                {person.name}
                                </button>
                            );
                            })}
                    </div>
                    </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <button
        onClick={() => {
            let text = "📋 *סידור תורנויות שבועי* 📋\n\n";
            DAY_KEYS.forEach(day => {
                text += `*יום ${DAYS_HE[day]}*\n`;
                const sched = store.schedule?.[day] || {};
                store.chores.filter((c:any) => c.isActive).forEach((c:any) => {
                    const assigned = sched[c.id] || [];
                    if (assigned.length > 0) {
                        text += `▫️ ${c.label}: ${assigned.join(", ")}\n`;
                    } else if (c.priority !== 1) {
                        text += `▫️ ${c.label}: _חסר_ ⚠️\n`;
                    }
                });
                text += "\n";
            });
            text += "בהצלחה לכולם! 💪 ~ צוות מובייל";
            navigator.share ? navigator.share({ text }) : alert("הסידור הועתק! ניתן להדביק ב-WhatsApp.");
        }}
        className="m3-button-filled w-full h-20 !rounded-[32px] mt-12 shadow-2xl shadow-primary/30 active:scale-95 !text-xl"
      >
        <span>📱</span>
        <span>שתף סידור ל-WhatsApp</span>
      </button>
    </div>
  );
};

const StatsScreen = ({ store }: { store: any }) => {
    const stats: Record<string, number> = {};
    if (store.schedule) {
        Object.values(store.schedule).forEach((day: any) => {
            Object.entries(day).forEach(([choreId, names]: [string, any]) => {
                const chore = store.chores.find((c:any) => c.id === choreId);
                const score = store.priorityEnabled ? (chore?.priority || 2) : 1;
                names.forEach((name: string) => {
                    stats[name] = (stats[name] || 0) + score;
                });
            });
        });
    }

    const maxScore = Math.max(...Object.values(stats), 1);

    const totalScore = Object.values(stats).reduce((sum, current) => sum + current, 0) || 1;

    return (
        <div className="space-y-10 animate-in fade-in slide-in-from-bottom-10 duration-500">
            <div className="px-2">
                <h2 className="text-4xl font-black text-on-surface tracking-tight">חלוקת עומסים</h2>
                <p className="text-on-surface-variant text-base mt-2 opacity-60">מי עובד הכי קשה השבוע? 🧐</p>
            </div>

            {Object.entries(stats).length === 0 ? (
                <div className="m3-card p-24 text-center border-dashed border-2 bg-surface/50 rounded-[48px] flex flex-col items-center">
                    <div className="w-20 h-20 bg-surface-variant/30 rounded-[32px] flex items-center justify-center text-4xl mb-6">📈</div>
                    <p className="text-on-surface-variant font-black text-lg">אין מספיק נתונים להצגת גרף</p>
                </div>
            ) : (
                <div className="space-y-5">
                    {Object.entries(stats).sort((a, b) => b[1] - a[1]).map(([name, score]) => {
                        const percentage = Math.round((score / totalScore) * 100);

                        return (
                            <div key={name} className="p-6 rounded-[32px] border border-outline/5 bg-surface shadow-xl relative overflow-hidden group">
                                <div className="absolute left-0 top-0 bottom-0 bg-primary/5 transition-all duration-1000 group-hover:bg-primary/10" style={{ width: `${(score / maxScore) * 100}%` }} />
                                <div className="relative flex items-center justify-between">
                                    <div className="flex items-center gap-4">
                                        <div className="w-12 h-12 rounded-2xl bg-primary-container flex items-center justify-center text-on-primary-container text-lg font-black shadow-inner">{name.charAt(0)}</div>
                                        <span className="font-black text-xl text-on-surface">{name}</span>
                                    </div>
                                    <div className="flex items-center gap-6">
                                        <div className="w-40 h-3 bg-surface-variant rounded-full overflow-hidden shadow-inner hidden md:block">
                                            <div className="h-full bg-primary transition-all duration-1000 ease-out rounded-full shadow-[0_0_15px_rgba(var(--md-sys-color-primary),0.6)]" style={{ width: `${(score / maxScore) * 100}%` }} />
                                        </div>
                                        <div className="px-5 py-2 bg-primary-container rounded-2xl shadow-md border border-primary/10">
                                            <span className="font-mono font-black text-xl text-on-primary-container">{percentage}%</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            <div className="p-8 rounded-[40px] border border-outline/10 bg-primary/5 relative overflow-hidden shadow-inner">
                <div className="absolute -top-4 -right-4 p-8 opacity-5 text-9xl">💡</div>
                <h3 className="font-black text-xl mb-4 text-on-surface flex items-center gap-2">
                    <span>💡</span>
                    <span>איך מחושב האחוז?</span>
                </h3>
                <p className="text-base text-on-surface-variant leading-relaxed font-medium opacity-90">
                    כל משימה מקבלת ניקוד לפי רמת הקושי שלה: <span className="font-black text-error">קשה (3)</span>, <span className="font-black text-[#FFA000]">בינוני (2)</span>, <span className="font-black text-secondary">קל (1)</span>. אנו סוכמים את כל המשימות, והאחוז מציג את החלק היחסי של העובד מתוך סך העומס השבועי של כולם.
                </p>
            </div>
        </div>
    );
};

const SettingsScreen = ({ store, isDark, toggleDarkMode, onAddPerson, onEditPerson, onAddChore, onScanQr, onShareQr }: { store: any, isDark: boolean, toggleDarkMode: () => void, onAddPerson: () => void, onEditPerson: (p: any) => void, onAddChore: () => void, onScanQr: () => void, onShareQr: () => void }) => {
    return (
        <div className="space-y-12 pb-24 animate-in fade-in slide-in-from-bottom-10 duration-500">
            <h2 className="text-4xl font-black px-2 text-on-surface tracking-tight">הגדרות</h2>

            <div className="m3-card !rounded-[40px] border-none shadow-2xl overflow-hidden bg-primary-container/50">
                <div className="p-6">
                    <h3 className="font-black text-lg text-on-surface mb-4">סנכרון תורנויות</h3>
                    <div className="flex gap-4">
                        <button
                            onClick={store.userRole === UserRole.MANAGER ? onShareQr : onScanQr}
                            className="flex-1 m3-button-tonal !h-20 !rounded-[24px] !bg-surface flex flex-col gap-1 items-center justify-center hover:scale-105 active:scale-95 transition-all shadow-md"
                        >
                            <span className="text-3xl">📷</span>
                        </button>
                    </div>
                </div>
            </div>

            <div className="m3-card !rounded-[40px] border-none shadow-2xl overflow-hidden bg-surface">
                <div className="p-8 flex items-center justify-between hover:bg-surface-variant/10 transition-colors group">
                    <div className="flex items-center gap-5">
                        <div className="w-14 h-14 rounded-[20px] bg-primary-container flex items-center justify-center text-3xl shadow-md group-hover:scale-110 transition-transform">
                            {isDark ? '🌙' : '☀️'}
                        </div>
                        <div>
                            <p className="font-black text-xl text-on-surface">מצב כהה</p>
                            <p className="text-[11px] text-on-surface-variant font-black uppercase tracking-widest opacity-60">מראה כהה או בהיר</p>
                        </div>
                    </div>
                    <Switch
                        checked={isDark}
                        onChange={toggleDarkMode}
                    />
                </div>
                <div className="h-[1px] w-full bg-outline/10" />
                <button
                    onClick={() => store.setUserRole(null)}
                    className="w-full py-6 text-error font-black text-lg active:bg-error/10 hover:bg-error/5 transition-colors flex items-center justify-center gap-3"
                >
                    <span>🚪</span> החלף סוג משתמש
                </button>
            </div>

            {store.userRole === UserRole.MANAGER && (
                <>
                    <section className="space-y-8">
                        <div className="flex justify-between items-center px-4">
                            <div>
                                <h3 className="text-2xl font-black text-on-surface">משתתפים</h3>
                                <p className="text-sm text-on-surface-variant font-bold opacity-60">ניהול צוות והיעדרויות</p>
                            </div>
                            <button onClick={store.generateSchedule} className="m3-button-tonal !h-12 !px-8 !text-sm !rounded-2xl shadow-lg border border-primary/10 hover:scale-105 active:scale-95">
                                🎲 הגרל שבוע
                            </button>
                        </div>

                        <div className="grid grid-cols-1 gap-4">
                            {store.people.map((p: any) => (
                                <div key={p.id} className="p-5 rounded-[32px] border border-outline/5 flex justify-between items-center bg-surface shadow-lg group hover:shadow-xl transition-all">
                                    <div className="flex items-center gap-5 cursor-pointer flex-1" onClick={() => onEditPerson(p)}>
                                        <div className={`w-14 h-14 rounded-2xl flex items-center justify-center text-3xl shadow-inner transition-transform group-hover:scale-110 ${p.gender === Gender.MALE ? 'bg-[#90CAF9]/20 text-[#90CAF9]' : 'bg-[#673AB7]/20 text-[#673AB7]'}`}>
                                            👤
                                        </div>
                                        <div>
                                            <span className={`font-black text-xl ${p.active ? 'text-on-surface' : 'line-through opacity-30 text-on-surface-variant'}`}>{p.name}</span>
                                            {p.unavailableDays.length > 0 && (
                                                <div className="flex items-center gap-1.5 mt-1">
                                                    <span className="w-1.5 h-1.5 rounded-full bg-error animate-pulse" />
                                                    <p className="text-[11px] text-error font-black uppercase tracking-tight">
                                                        לא זמין: {p.unavailableDays.map((d:string) => DAYS_HE[d]).join(", ")}
                                                    </p>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-3">
                                        <Switch
                                            checked={p.active}
                                            onChange={() => store.updatePerson({ ...p, active: !p.active })}
                                        />
                                        <button onClick={(e) => { e.stopPropagation(); store.deletePerson(p.id); }} className="w-12 h-12 rounded-full flex items-center justify-center hover:bg-error/10 text-error/30 hover:text-error transition-all active:scale-90">
                                            <span className="text-2xl">🗑️</span>
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>

                        <button
                            onClick={onAddPerson}
                            className="m3-button-tonal w-full h-20 !rounded-[32px] shadow-md border-2 border-primary/20 border-dashed hover:border-solid transition-all !bg-primary/5 hover:bg-primary/10"
                        >
                            <span className="text-2xl">➕</span>
                            <span className="text-lg">הוסף משתתף חדש</span>
                        </button>
                    </section>

                    <section className="space-y-8">
                        <div className="px-4">
                            <h3 className="text-2xl font-black text-on-surface">משימות</h3>
                            <p className="text-sm text-on-surface-variant font-bold opacity-60 uppercase tracking-widest">הגדרת רמות קושי ומשימות פעילות</p>
                        </div>

                        <div className="p-8 rounded-[40px] border-2 border-primary/10 bg-primary/5 flex items-center justify-between shadow-inner">
                            <div>
                                <p className="font-black text-lg text-on-surface leading-tight">שימוש במערכת עדיפויות</p>
                                <p className="text-xs text-on-surface-variant mt-2 font-bold opacity-70 max-w-[200px]">חלוקת עומסים חכמה לפי רמות קושי (1-3)</p>
                            </div>
                            <Switch
                                checked={store.priorityEnabled}
                                onChange={() => store.setPriorityEnabled(!store.priorityEnabled)}
                            />
                        </div>

                        <div className="grid grid-cols-1 gap-4">
                            {store.chores.map((c: any) => (
                                <div key={c.id} className="p-6 rounded-[32px] border border-outline/5 bg-surface shadow-lg group hover:shadow-xl transition-all">
                                    <div className="flex justify-between items-center mb-5">
                                        <div className="flex items-center gap-4">
                                            <div className="w-12 h-12 bg-surface-variant/30 rounded-2xl flex items-center justify-center text-2xl shadow-inner group-hover:rotate-12 transition-transform">🧹</div>
                                            <span className={`font-black text-xl ${c.isActive ? 'text-on-surface' : 'opacity-30 text-on-surface-variant'}`}>{c.label}</span>
                                        </div>
                                        <div className="flex items-center gap-3">
                                            {c.id.startsWith("custom_") && (
                                                <button onClick={() => store.deleteChore(c.id)} className="w-10 h-10 rounded-full flex items-center justify-center hover:bg-error/10 text-error/30 hover:text-error transition-all">🗑️</button>
                                            )}
                                            <Switch
                                                checked={c.isActive}
                                                onChange={() => store.updateChore({ ...c, isActive: !c.isActive })}
                                            />
                                        </div>
                                    </div>
                                    {c.isActive && (
                                        <div className="flex flex-col gap-4 pt-5 border-t border-outline/10">
                                            <span className="text-[11px] font-black text-on-surface-variant/50 uppercase tracking-[0.2em] text-center">בחר רמת קושי של המשימה</span>
                                            <div className="flex gap-3">
                                                {[Priority.LOW, Priority.MEDIUM, Priority.HIGH].map((p) => {
                                                    const isSelected = c.priority === p;
                                                    const pLabel = p === Priority.LOW ? 'קל' : p === Priority.MEDIUM ? 'בינוני' : 'קשה';
                                                    const pColor = p === Priority.LOW ? '#A5D6A7' : p === Priority.MEDIUM ? '#FFD180' : '#FF8A80';
                                                    return (
                                                        <button
                                                            key={p}
                                                            onClick={() => store.updateChore({ ...c, priority: p })}
                                                            className={`flex-1 py-4 rounded-2xl text-[11px] font-black border-2 transition-all ${
                                                                isSelected
                                                                    ? `scale-110 shadow-lg border-transparent`
                                                                    : 'opacity-20 border-outline/10 grayscale hover:opacity-50 hover:grayscale-0'
                                                            }`}
                                                            style={{
                                                                backgroundColor: isSelected ? pColor : '',
                                                                color: isSelected ? (p === Priority.LOW ? '#003300' : p === Priority.MEDIUM ? '#5D4037' : '#FFFFFF') : ''
                                                            }}
                                                        >
                                                            {pLabel}
                                                        </button>
                                                    );
                                                })}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>

                        <button
                            onClick={onAddChore}
                            className="m3-button-tonal w-full h-20 !rounded-[32px] shadow-md border-2 border-primary/20 border-dashed hover:border-solid transition-all !bg-primary/5"
                        >
                            <span className="text-2xl">📋</span>
                            <span>הוסף משימה חדשה</span>
                        </button>
                    </section>
                </>
            )}
        </div>
    );
};

const Switch = ({ checked, onChange }: { checked: boolean, onChange: () => void }) => {
    return (
        <button
            onClick={(e) => { e.stopPropagation(); onChange(); }}
            className={`w-[64px] h-[36px] rounded-m3-full p-[6px] transition-all duration-500 relative flex items-center shadow-inner ${checked ? 'bg-primary/40 dark:bg-primary/30' : 'bg-outline/30 dark:bg-outline/10'}`}
        >
            <div
                className={`w-[24px] h-[24px] rounded-m3-full shadow-2xl transition-all duration-500 flex items-center justify-center absolute ${checked ? 'right-[34px] bg-primary dark:bg-primary scale-110' : 'right-[6px] bg-white dark:bg-[#72796F] scale-90 opacity-70'}`}
            >
                {checked && <span className="text-[14px] text-on-primary font-black">✓</span>}
            </div>
        </button>
    );
}

export default App;