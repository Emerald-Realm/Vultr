import AsyncStorage from '@react-native-async-storage/async-storage';
import { create } from 'zustand';

export const SKIP_OPTIONS = [10, 20, 30, 60];
export const AUTO_REWIND_OPTIONS = [0, 2, 5, 10];
export const SPEED_OPTIONS = [0.8, 1, 1.25, 1.5, 1.75, 2];

interface SettingsState {
  skipSeconds: number;
  autoRewindSeconds: number;
  defaultRate: number;
  loaded: boolean;
  load: () => Promise<void>;
  setSkipSeconds: (v: number) => void;
  setAutoRewindSeconds: (v: number) => void;
  setDefaultRate: (v: number) => void;
}

export const useSettings = create<SettingsState>((set) => ({
  skipSeconds: 20,
  autoRewindSeconds: 2,
  defaultRate: 1,
  loaded: false,
  async load() {
    const [skip, rewind, rate] = await Promise.all([
      AsyncStorage.getItem('skipSeconds'),
      AsyncStorage.getItem('autoRewindSeconds'),
      AsyncStorage.getItem('defaultRate'),
    ]);
    set({
      skipSeconds: skip ? Number(skip) : 20,
      autoRewindSeconds: rewind ? Number(rewind) : 2,
      defaultRate: rate ? Number(rate) : 1,
      loaded: true,
    });
  },
  setSkipSeconds(v) {
    set({ skipSeconds: v });
    AsyncStorage.setItem('skipSeconds', String(v));
  },
  setAutoRewindSeconds(v) {
    set({ autoRewindSeconds: v });
    AsyncStorage.setItem('autoRewindSeconds', String(v));
  },
  setDefaultRate(v) {
    set({ defaultRate: v });
    AsyncStorage.setItem('defaultRate', String(v));
  },
}));

// Non-reactive snapshot for use inside the player store.
export const settingsSnapshot = () => {
  const s = useSettings.getState();
  return { skipSeconds: s.skipSeconds, autoRewindSeconds: s.autoRewindSeconds, defaultRate: s.defaultRate };
};
