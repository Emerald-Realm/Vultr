import AsyncStorage from '@react-native-async-storage/async-storage';
import { create } from 'zustand';

export type ThemeMode = 'system' | 'light' | 'dark';

interface ThemeModeState {
  mode: ThemeMode;
  setMode: (m: ThemeMode) => void;
  load: () => Promise<void>;
}

export const useThemeMode = create<ThemeModeState>((set) => ({
  mode: 'system',
  setMode(m) {
    set({ mode: m });
    AsyncStorage.setItem('themeMode', m);
  },
  async load() {
    const v = (await AsyncStorage.getItem('themeMode')) as ThemeMode | null;
    if (v) set({ mode: v });
  },
}));
