import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Image } from 'expo-image';
import { useTheme, radius } from '../theme/theme';
import { Icon } from '../icons/Icon';

export function Cover({
  uri,
  size,
  rounded = radius.sm,
}: {
  uri?: string | null;
  size?: number;
  rounded?: number;
}) {
  const { colors } = useTheme();
  const dim = size ? { width: size, height: size } : ({ width: '100%', height: '100%' } as const);
  if (uri) {
    return <Image source={{ uri }} style={[dim, { borderRadius: rounded }]} contentFit="cover" />;
  }
  return (
    <View
      style={[
        dim,
        styles.placeholder,
        { backgroundColor: colors.surfaceVariant, borderRadius: rounded },
      ]}
    >
      <Icon name="soundWaves" size={(size ?? 64) * 0.3} color={colors.onSurfaceVariant} />
    </View>
  );
}

const styles = StyleSheet.create({
  placeholder: { alignItems: 'center', justifyContent: 'center' },
});
