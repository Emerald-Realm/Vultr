import React, { useState } from 'react';
import { View, Text, Pressable, StyleSheet, Modal, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import Slider from '@react-native-community/slider';
import { usePlayer, SleepMode } from '../src/playback/playerStore';
import { SPEED_OPTIONS } from '../src/playback/settings';
import { useTheme, type as typo, spacing } from '../src/theme/theme';
import { Cover } from '../src/components/Cover';
import { IconButton } from '../src/components/IconButton';
import { Icon } from '../src/icons/Icon';
import { formatTime } from '../src/lib/format';

const SLEEP_OPTIONS: { label: string; value: SleepMode }[] = [
  { label: '15m', value: 15 },
  { label: '30m', value: 30 },
  { label: '45m', value: 45 },
  { label: 'Chapter', value: 'endOfChapter' },
];

export default function Player() {
  const router = useRouter();
  const { colors } = useTheme();
  const s = usePlayer();
  const [dragging, setDragging] = useState(false);
  const [dragValue, setDragValue] = useState(0);
  const [showChapters, setShowChapters] = useState(false);
  const [showSpeed, setShowSpeed] = useState(false);

  if (!s.book) {
    return (
      <SafeAreaView style={[styles.root, { backgroundColor: colors.background }]}>
        <Text style={[typo.body, { color: colors.onSurfaceVariant, textAlign: 'center' }]}>Nothing playing.</Text>
      </SafeAreaView>
    );
  }

  const chapter = s.book.chapters[s.chapterIndex];
  const sliderValue = dragging ? dragValue : s.positionMs;

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: colors.background }]}>
      <View style={styles.topBar}>
        <IconButton name="chevronDown" onPress={() => router.back()} color={colors.onBackground} />
        <View style={{ flexDirection: 'row', gap: spacing.lg }}>
          <IconButton name="soundWaves" color={colors.onBackground} onPress={() => router.push('/history')} />
          <IconButton name="dotsMenu" color={colors.onBackground} onPress={() => setShowChapters(true)} />
        </View>
      </View>

      <View style={styles.coverWrap}>
        <Cover uri={s.book.coverUri} size={260} rounded={16} />
      </View>

      <Text numberOfLines={1} style={[typo.title, { color: colors.onBackground }]}>{s.book.title}</Text>
      <Text numberOfLines={1} style={[typo.body, { color: colors.onSurfaceVariant, marginTop: 2 }]}>
        {chapter?.name ?? s.book.author}
      </Text>

      <View style={styles.progressBlock}>
        <Slider
          minimumValue={0}
          maximumValue={Math.max(1, s.durationMs)}
          value={sliderValue}
          onSlidingStart={() => {
            setDragValue(s.positionMs);
            setDragging(true);
          }}
          onValueChange={setDragValue}
          onSlidingComplete={(v) => {
            setDragging(false);
            s.seekToMs(v);
          }}
          minimumTrackTintColor={colors.primary}
          maximumTrackTintColor={colors.outlineVariant}
          thumbTintColor={colors.primary}
        />
        <View style={styles.times}>
          <Text style={[typo.caption, { color: colors.onSurfaceVariant }]}>{formatTime(sliderValue)}</Text>
          <Text style={[typo.caption, { color: colors.onSurfaceVariant }]}>{formatTime(s.durationMs)}</Text>
        </View>
      </View>

      <View style={styles.controls}>
        <IconButton name="previous" size={28} color={colors.onBackground} onPress={s.prevChapter} />
        <IconButton name="fastForwardBack" size={30} color={colors.onBackground} onPress={s.rewind} />
        <Pressable onPress={s.togglePlay} style={[styles.playBtn, { backgroundColor: colors.primary }]}>
          <Icon name={s.playing ? 'pause' : 'play'} size={34} color={colors.onPrimary} />
        </Pressable>
        <IconButton name="fastForward" size={30} color={colors.onBackground} onPress={s.fastForward} />
        <IconButton name="next" size={28} color={colors.onBackground} onPress={() => s.nextChapter(false)} />
      </View>

      {s.awaitingShake && (
        <Text style={[typo.caption, { color: colors.primary, textAlign: 'center', marginTop: spacing.md }]}>
          Sleep timer ended — shake to keep listening
        </Text>
      )}

      <View style={styles.footer}>
        <Pressable onPress={() => setShowSpeed(true)} style={[styles.chip, { borderColor: colors.outline }]}>
          <Text style={[typo.label, { color: colors.onBackground }]}>{s.rate}×</Text>
        </Pressable>
        <SleepControl />
      </View>

      <ChaptersSheet visible={showChapters} onClose={() => setShowChapters(false)} />
      <SpeedSheet visible={showSpeed} onClose={() => setShowSpeed(false)} />
    </SafeAreaView>
  );
}

function SleepControl() {
  const { colors } = useTheme();
  const s = usePlayer();
  if (s.sleepMode === 'endOfChapter') {
    return (
      <Pressable onPress={() => s.setSleep(null)} style={[styles.chip, { borderColor: colors.primary }]}>
        <Icon name="moon" size={16} color={colors.primary} />
        <Text style={[typo.label, { color: colors.primary }]}>End of chapter</Text>
      </Pressable>
    );
  }
  if (s.sleepRemainingMs != null) {
    return (
      <Pressable onPress={() => s.setSleep(null)} style={[styles.chip, { borderColor: colors.primary }]}>
        <Icon name="moon" size={16} color={colors.primary} />
        <Text style={[typo.label, { color: colors.primary }]}>{formatTime(s.sleepRemainingMs)}</Text>
      </Pressable>
    );
  }
  return (
    <View style={styles.sleepRow}>
      {SLEEP_OPTIONS.map((o) => (
        <Pressable key={o.label} onPress={() => s.setSleep(o.value)} style={[styles.chip, { borderColor: colors.outline }]}>
          <Text style={[typo.label, { color: colors.onSurfaceVariant }]}>{o.label}</Text>
        </Pressable>
      ))}
    </View>
  );
}

function Sheet({ visible, onClose, children }: { visible: boolean; onClose: () => void; children: React.ReactNode }) {
  const { colors } = useTheme();
  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <Pressable style={styles.backdrop} onPress={onClose} />
      <View style={[styles.sheet, { backgroundColor: colors.surface }]}>{children}</View>
    </Modal>
  );
}

function ChaptersSheet({ visible, onClose }: { visible: boolean; onClose: () => void }) {
  const { colors } = useTheme();
  const s = usePlayer();
  return (
    <Sheet visible={visible} onClose={onClose}>
      <Text style={[typo.headline, { color: colors.onSurface, marginBottom: spacing.md }]}>Chapters</Text>
      <ScrollView style={{ maxHeight: 380 }}>
        {s.book?.chapters.map((c, i) => {
          const active = i === s.chapterIndex;
          return (
            <Pressable
              key={c.id}
              onPress={() => {
                s.jumpToChapter(i);
                onClose();
              }}
              style={styles.chapterRow}
            >
              <Text numberOfLines={1} style={[typo.bodyLarge, { color: active ? colors.primary : colors.onSurface, flex: 1 }]}>
                {c.name}
              </Text>
              <Text style={[typo.caption, { color: colors.onSurfaceVariant }]}>{formatTime(c.durationMs)}</Text>
            </Pressable>
          );
        })}
      </ScrollView>
    </Sheet>
  );
}

function SpeedSheet({ visible, onClose }: { visible: boolean; onClose: () => void }) {
  const { colors } = useTheme();
  const s = usePlayer();
  return (
    <Sheet visible={visible} onClose={onClose}>
      <Text style={[typo.headline, { color: colors.onSurface, marginBottom: spacing.md }]}>Playback speed</Text>
      <View style={styles.speedGrid}>
        {SPEED_OPTIONS.map((r) => {
          const active = r === s.rate;
          return (
            <Pressable
              key={r}
              onPress={() => {
                s.setRate(r);
                onClose();
              }}
              style={[
                styles.speedChip,
                { borderColor: active ? colors.primary : colors.outline, backgroundColor: active ? colors.primaryContainer : 'transparent' },
              ]}
            >
              <Text style={[typo.bodyLarge, { color: active ? colors.primary : colors.onSurface }]}>{r}×</Text>
            </Pressable>
          );
        })}
      </View>
    </Sheet>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, paddingHorizontal: spacing.xl },
  topBar: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingTop: spacing.sm },
  coverWrap: { alignItems: 'center', marginVertical: spacing.xxl },
  progressBlock: { marginTop: spacing.xl },
  times: { flexDirection: 'row', justifyContent: 'space-between', marginTop: spacing.xs },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: spacing.xl,
    paddingHorizontal: spacing.sm,
  },
  playBtn: { width: 72, height: 72, borderRadius: 36, alignItems: 'center', justifyContent: 'center' },
  footer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 'auto',
    paddingBottom: spacing.xl,
    gap: spacing.md,
  },
  sleepRow: { flexDirection: 'row', gap: spacing.sm, flexWrap: 'wrap', flexShrink: 1, justifyContent: 'flex-end' },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    borderWidth: 1,
    borderRadius: 999,
    paddingHorizontal: 14,
    paddingVertical: 8,
  },
  backdrop: { flex: 1, backgroundColor: '#00000066' },
  sheet: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    padding: spacing.xl,
    paddingBottom: spacing.xxl,
  },
  chapterRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, paddingVertical: spacing.md },
  speedGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.md },
  speedChip: { borderWidth: 1, borderRadius: 12, paddingHorizontal: 20, paddingVertical: 12 },
});
