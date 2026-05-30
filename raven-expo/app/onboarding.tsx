import React from 'react';
import { View, Text, Pressable, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Logo } from '../src/icons/Logo';
import { useTheme, type as typo, spacing, radius } from '../src/theme/theme';

export default function Onboarding() {
  const router = useRouter();
  const { colors } = useTheme();

  async function start() {
    await AsyncStorage.setItem('onboarded', 'true');
    router.replace('/import');
  }

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: colors.background }]}>
      <View style={styles.logoWrap}>
        <Logo size={150} color={colors.primary} />
      </View>

      <Text style={[typo.display, styles.center, { color: colors.onBackground }]}>
        Welcome to Raven
      </Text>
      <Text style={[typo.bodyLarge, styles.center, { color: colors.onSurfaceVariant, marginTop: spacing.md }]}>
        Your audiobook library. Listen to your books MP3, M4B, and more from local storage.
      </Text>

      <View style={styles.bottom}>
        <Pressable
          onPress={start}
          style={({ pressed }) => [
            styles.button,
            { backgroundColor: colors.primary, opacity: pressed ? 0.9 : 1 },
          ]}
        >
          <Text style={[typo.bodyLarge, { color: colors.onPrimary, fontWeight: '600' }]}>
            Get Started
          </Text>
        </Pressable>
        <Text style={[typo.body, styles.center, { color: colors.muted, marginTop: spacing.lg }]}>
          By continuing, you agree to our{'\n'}Terms & Conditions and Privacy policy
        </Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, paddingHorizontal: spacing.xl },
  logoWrap: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  center: { textAlign: 'center' },
  bottom: { flex: 1, justifyContent: 'flex-end', paddingBottom: spacing.lg },
  button: {
    height: 52,
    borderRadius: radius.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
