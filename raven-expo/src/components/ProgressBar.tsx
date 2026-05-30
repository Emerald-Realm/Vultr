import React from 'react';
import { View } from 'react-native';
import { useTheme } from '../theme/theme';

export function ProgressBar({ progress, height = 4 }: { progress: number; height?: number }) {
  const { colors } = useTheme();
  const pct = Math.min(1, Math.max(0, progress));
  return (
    <View
      style={{
        height,
        flex: 1,
        borderRadius: height,
        backgroundColor: colors.outlineVariant,
        overflow: 'hidden',
      }}
    >
      <View
        style={{ width: `${pct * 100}%`, height, backgroundColor: colors.primary, borderRadius: height }}
      />
    </View>
  );
}
