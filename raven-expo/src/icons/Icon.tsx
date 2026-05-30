import React from 'react';
import Svg, { Path } from 'react-native-svg';
import { ICONS, IconName } from './paths';

type Props = {
  name: IconName;
  size?: number;
  color?: string;
  strokeWidth?: number;
};

// Renders a Mage (stroke) icon. The set is outline-based, so `color` maps to stroke.
export function Icon({ name, size = 24, color = '#13234E', strokeWidth = 1.5 }: Props) {
  const def = ICONS[name];
  return (
    <Svg width={size} height={size} viewBox={`0 0 ${def.vw} ${def.vh}`} fill="none">
      {def.paths.map((p, i) => (
        <Path
          key={i}
          d={p.d}
          stroke={p.fill ? 'none' : color}
          fill={p.fill ? color : 'none'}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      ))}
    </Svg>
  );
}
