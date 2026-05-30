import React, { useEffect, useState } from 'react';
import { useFonts } from 'expo-font';
import { Stack, useRouter, useSegments } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { StatusBar } from 'expo-status-bar';
import * as StoreReview from 'expo-store-review';
import { seedIfEmpty } from '../src/db/seed';
import { repo } from '../src/db/repo';
import { initAudioMode, usePlayer } from '../src/playback/playerStore';
import { useSettings } from '../src/playback/settings';
import { useThemeMode } from '../src/theme/themeMode';
import { useTheme } from '../src/theme/theme';

async function maybeRequestReview() {
  try {
    const count = Number((await AsyncStorage.getItem('openCount')) ?? '0') + 1;
    await AsyncStorage.setItem('openCount', String(count));
    if (count === 4 && (await StoreReview.hasAction())) {
      await StoreReview.requestReview();
    }
  } catch {}
}

SplashScreen.preventAutoHideAsync().catch(() => {});

export default function RootLayout() {
  const { colors, dark } = useTheme();
  const router = useRouter();
  const segments = useSegments();
  const [ready, setReady] = useState(false);
  const [onboarded, setOnboarded] = useState<boolean | null>(null);

  const [fontsLoaded] = useFonts({ HostGrotesk: require('../assets/fonts/HostGrotesk.ttf') });

  useEffect(() => {
    (async () => {
      seedIfEmpty();
      await initAudioMode();
      await Promise.all([useSettings.getState().load(), useThemeMode.getState().load()]);
      maybeRequestReview();
      // Restore the most recently played book so the mini-player + lock screen can resume.
      const recent = repo
        .allBooks()
        .filter((b) => b.lastPlayedAt != null)
        .sort((a, b) => (b.lastPlayedAt ?? 0) - (a.lastPlayedAt ?? 0))[0];
      if (recent) usePlayer.getState().loadBook(recent, undefined, undefined, false);
      const flag = await AsyncStorage.getItem('onboarded');
      setOnboarded(flag === 'true');
      setReady(true);
    })();
  }, []);

  useEffect(() => {
    if (!ready || !fontsLoaded || onboarded == null) return;
    SplashScreen.hideAsync().catch(() => {});
    const inOnboarding = segments[0] === 'onboarding';
    if (!onboarded && !inOnboarding) router.replace('/onboarding');
  }, [ready, fontsLoaded, onboarded, segments, router]);

  if (!fontsLoaded || !ready) return null;

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <StatusBar style={dark ? 'light' : 'dark'} />
        <Stack
          screenOptions={{
            headerShown: false,
            contentStyle: { backgroundColor: colors.background },
            animation: 'slide_from_right',
          }}
        >
          <Stack.Screen name="player" options={{ presentation: 'modal', animation: 'slide_from_bottom' }} />
        </Stack>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
