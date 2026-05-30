import React from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useTheme, type as typo, spacing } from '../src/theme/theme';
import { useThemeMode, ThemeMode } from '../src/theme/themeMode';
import { IconButton } from '../src/components/IconButton';
import { useSettings, SKIP_OPTIONS, AUTO_REWIND_OPTIONS, SPEED_OPTIONS } from '../src/playback/settings';

const THEME_MODES: ThemeMode[] = ['system', 'light', 'dark'];

export default function Settings() {
  const router = useRouter();
  const { colors } = useTheme();
  const mode = useThemeMode((s) => s.mode);
  const setMode = useThemeMode((s) => s.setMode);
  const skipSeconds = useSettings((s) => s.skipSeconds);
  const autoRewindSeconds = useSettings((s) => s.autoRewindSeconds);
  const defaultRate = useSettings((s) => s.defaultRate);
  const setSkipSeconds = useSettings((s) => s.setSkipSeconds);
  const setAutoRewindSeconds = useSettings((s) => s.setAutoRewindSeconds);
  const setDefaultRate = useSettings((s) => s.setDefaultRate);

  async function replayOnboarding() {
    await AsyncStorage.removeItem('onboarded');
    router.replace('/onboarding');
  }

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: colors.background }]} edges={['top']}>
      <View style={styles.topBar}>
        <IconButton name="arrowLeft" onPress={() => router.back()} color={colors.onBackground} />
        <Text style={[typo.title, { color: colors.onBackground }]}>Settings</Text>
        <View style={{ width: 24 }} />
      </View>

      <ScrollView contentContainerStyle={{ padding: spacing.xl }}>
        <Choices
          label="Theme"
          options={THEME_MODES}
          value={mode}
          format={(m) => m[0].toUpperCase() + m.slice(1)}
          onChange={setMode}
        />
        <Choices
          label="Default speed"
          options={SPEED_OPTIONS}
          value={defaultRate}
          format={(v) => `${v}×`}
          onChange={setDefaultRate}
        />
        <Choices
          label="Skip duration"
          options={SKIP_OPTIONS}
          value={skipSeconds}
          format={(v) => `${v}s`}
          onChange={setSkipSeconds}
        />
        <Choices
          label="Auto-rewind"
          options={AUTO_REWIND_OPTIONS}
          value={autoRewindSeconds}
          format={(v) => (v === 0 ? 'Off' : `${v}s`)}
          onChange={setAutoRewindSeconds}
        />

        <Pressable onPress={replayOnboarding} style={[styles.action, { borderColor: colors.outline }]}>
          <Text style={[typo.bodyLarge, { color: colors.primary }]}>Replay onboarding</Text>
        </Pressable>
        <Text style={[typo.caption, { color: colors.onSurfaceVariant, marginTop: spacing.xl }]}>
          Raven · React Native (Expo) edition · v1
        </Text>
      </ScrollView>
    </SafeAreaView>
  );
}

function Choices<T extends string | number>({
  label,
  options,
  value,
  format,
  onChange,
}: {
  label: string;
  options: readonly T[];
  value: T;
  format: (v: T) => string;
  onChange: (v: T) => void;
}) {
  const { colors } = useTheme();
  return (
    <View style={{ marginBottom: spacing.xl }}>
      <Text style={[typo.bodyLarge, { color: colors.onBackground, marginBottom: spacing.sm }]}>{label}</Text>
      <View style={styles.chips}>
        {options.map((o) => {
          const active = o === value;
          return (
            <Pressable
              key={String(o)}
              onPress={() => onChange(o)}
              style={[
                styles.chip,
                {
                  borderColor: active ? colors.primary : colors.outline,
                  backgroundColor: active ? colors.primaryContainer : 'transparent',
                },
              ]}
            >
              <Text style={[typo.label, { color: active ? colors.primary : colors.onSurfaceVariant }]}>
                {format(o)}
              </Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.sm,
  },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  chip: { borderWidth: 1, borderRadius: 999, paddingHorizontal: 16, paddingVertical: 8 },
  action: {
    marginTop: spacing.sm,
    borderWidth: 1,
    borderRadius: 12,
    paddingVertical: spacing.lg,
    alignItems: 'center',
  },
});
