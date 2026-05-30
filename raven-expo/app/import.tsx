import React, { useState } from 'react';
import { View, Text, Pressable, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { useTheme, type as typo, spacing, radius } from '../src/theme/theme';
import { IconButton } from '../src/components/IconButton';
import { Icon } from '../src/icons/Icon';
import { IconName } from '../src/icons/paths';
import { importFromFolder, FolderMode, ScanProgress } from '../src/scanner/scanner';

const MODES: { mode: FolderMode; icon: IconName; title: string; subtitle: string }[] = [
  { mode: 'topLevel', icon: 'folder', title: 'Top-level book mode', subtitle: 'Each subfolder is a book' },
  { mode: 'single', icon: 'file', title: 'Single book mode', subtitle: 'This folder is one book' },
  { mode: 'author', icon: 'server', title: 'Author–book mode', subtitle: 'Author folders contain book folders' },
];

export default function Import() {
  const router = useRouter();
  const { colors } = useTheme();
  const [scan, setScan] = useState<ScanProgress | null>(null);

  async function pick(mode: FolderMode) {
    try {
      const count = await importFromFolder(mode, setScan);
      setScan(null);
      if (count > 0) {
        router.replace('/');
      } else {
        Alert.alert('Nothing imported', 'No audio files were found in that folder.');
      }
    } catch (e) {
      setScan(null);
      Alert.alert('Import failed', String(e));
    }
  }

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: colors.background }]} edges={['top']}>
      <View style={styles.topBar}>
        <IconButton
          name="arrowLeft"
          onPress={() => (router.canGoBack() ? router.back() : router.replace('/'))}
          color={colors.onBackground}
        />
        <Pressable onPress={() => router.replace('/')} hitSlop={12}>
          <Text style={[typo.label, { color: colors.onSurfaceVariant }]}>Skip</Text>
        </Pressable>
      </View>

      <View style={[styles.badge, { backgroundColor: colors.primaryContainer }]}>
        <Icon name="folder" size={24} color={colors.primary} />
      </View>
      <Text style={[typo.title, { color: colors.onBackground, marginTop: spacing.lg }]}>
        How are your audiobooks structured?
      </Text>
      <Text style={[typo.body, { color: colors.onSurfaceVariant, marginTop: spacing.sm }]}>
        Pick the layout that matches your folder, then choose it on the next screen.
      </Text>

      <View style={{ marginTop: spacing.xl, gap: spacing.md }}>
        {MODES.map((m) => (
          <Pressable
            key={m.mode}
            onPress={() => pick(m.mode)}
            style={[styles.option, { borderColor: colors.outline }]}
          >
            <Icon name={m.icon} size={22} color={colors.primary} />
            <View style={{ flex: 1 }}>
              <Text style={[typo.bodyLarge, { color: colors.onBackground }]}>{m.title}</Text>
              <Text style={[typo.caption, { color: colors.onSurfaceVariant }]}>{m.subtitle}</Text>
            </View>
            <Icon name="chevronRight" size={20} color={colors.onSurfaceVariant} />
          </Pressable>
        ))}
      </View>

      {scan && (
        <View style={[styles.overlay, { backgroundColor: colors.background + 'EE' }]}>
          <ActivityIndicator color={colors.primary} size="large" />
          <Text style={[typo.bodyLarge, { color: colors.onBackground, marginTop: spacing.lg }]}>
            {scan.phase === 'probing' ? scan.message : 'Scanning…'}
          </Text>
          {scan.phase === 'probing' && scan.total ? (
            <Text style={[typo.caption, { color: colors.onSurfaceVariant, marginTop: spacing.xs }]}>
              Reading chapter {scan.current} of {scan.total}
            </Text>
          ) : null}
        </View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, paddingHorizontal: spacing.xl },
  topBar: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingTop: spacing.sm },
  badge: { width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center', marginTop: spacing.sm },
  option: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    borderWidth: 1,
    borderRadius: radius.pill,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.lg,
  },
  overlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
