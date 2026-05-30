import React from 'react';
import { Pressable, ViewStyle } from 'react-native';
import { Icon } from '../icons/Icon';
import { IconName } from '../icons/paths';

export function IconButton({
  name,
  onPress,
  size = 24,
  color,
  hitSlop = 12,
  style,
}: {
  name: IconName;
  onPress?: () => void;
  size?: number;
  color?: string;
  hitSlop?: number;
  style?: ViewStyle;
}) {
  return (
    <Pressable
      onPress={onPress}
      hitSlop={hitSlop}
      style={({ pressed }) => [{ opacity: pressed ? 0.55 : 1 }, style]}
    >
      <Icon name={name} size={size} color={color} />
    </Pressable>
  );
}
