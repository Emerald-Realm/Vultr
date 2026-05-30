// Brand palette ported from the Android app (primary #457CFA, Host Grotesk).
export const BrandBlue = '#457CFA';

export const lightColors = {
  primary: BrandBlue,
  onPrimary: '#FFFFFF',
  primaryContainer: '#F6F8FF',
  onPrimaryContainer: '#13234E',
  background: '#FFFFFF',
  onBackground: '#13234E',
  surface: '#FFFFFF',
  surfaceVariant: '#F3F5F6',
  onSurface: '#13234E',
  onSurfaceVariant: '#676B83',
  outline: '#C2CBD6',
  outlineVariant: '#E3E8EF',
  muted: '#8596AD',
};

export const darkColors: typeof lightColors = {
  primary: BrandBlue,
  onPrimary: '#FFFFFF',
  primaryContainer: '#1B2C57',
  onPrimaryContainer: '#D6E2FF',
  background: '#101216',
  onBackground: '#E4E6EE',
  surface: '#181B20',
  surfaceVariant: '#2A2E37',
  onSurface: '#E4E6EE',
  onSurfaceVariant: '#A7ACBF',
  outline: '#3C424E',
  outlineVariant: '#2A2E37',
  muted: '#8A93A6',
};

export type AppColors = typeof lightColors;
