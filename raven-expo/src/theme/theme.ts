import { useColorScheme } from 'react-native';
import { AppColors, darkColors, lightColors } from './colors';
import { useThemeMode } from './themeMode';

export const FONT = 'HostGrotesk';

export const type = {
  display: { fontFamily: FONT, fontSize: 36, lineHeight: 40, fontWeight: '600' as const },
  title: { fontFamily: FONT, fontSize: 24, lineHeight: 29, fontWeight: '600' as const },
  headline: { fontFamily: FONT, fontSize: 20, lineHeight: 26, fontWeight: '600' as const },
  bodyLarge: { fontFamily: FONT, fontSize: 16, lineHeight: 24, fontWeight: '400' as const },
  body: { fontFamily: FONT, fontSize: 15, lineHeight: 22, fontWeight: '400' as const },
  label: { fontFamily: FONT, fontSize: 13, lineHeight: 18, fontWeight: '500' as const },
  caption: { fontFamily: FONT, fontSize: 12, lineHeight: 16, fontWeight: '400' as const },
};

export const spacing = { xs: 4, sm: 8, md: 12, lg: 16, xl: 20, xxl: 28 };
export const radius = { sm: 8, md: 12, pill: 999 };

export interface Theme {
  colors: AppColors;
  dark: boolean;
}

export function useTheme(): Theme {
  const scheme = useColorScheme();
  const mode = useThemeMode((s) => s.mode);
  const dark = mode === 'system' ? scheme === 'dark' : mode === 'dark';
  return { colors: dark ? darkColors : lightColors, dark };
}
