import React from 'react';
import { View, Text, Pressable, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';
import { usePlayer } from '../playback/playerStore';
import { useTheme, type as typo, spacing } from '../theme/theme';
import { Cover } from './Cover';
import { ProgressBar } from './ProgressBar';
import { IconButton } from './IconButton';
import { bookDurationMs, bookPositionMs } from '../data/types';

export function MiniPlayer() {
  const router = useRouter();
  const { colors } = useTheme();
  const book = usePlayer((s) => s.book);
  const playing = usePlayer((s) => s.playing);
  const chapterIndex = usePlayer((s) => s.chapterIndex);
  const positionMs = usePlayer((s) => s.positionMs);
  const togglePlay = usePlayer((s) => s.togglePlay);

  if (!book) return null;

  const prior = book.chapters.slice(0, chapterIndex).reduce((s, c) => s + c.durationMs, 0);
  const progress = bookDurationMs(book) > 0 ? (prior + positionMs) / bookDurationMs(book) : 0;

  return (
    <Pressable
      onPress={() => router.push('/player')}
      style={[styles.bar, { backgroundColor: colors.surfaceVariant }]}
    >
      <View style={styles.row}>
        <Cover uri={book.coverUri} size={44} />
        <View style={styles.meta}>
          <Text numberOfLines={1} style={[typo.label, { color: colors.onSurface }]}>
            {book.title}
          </Text>
          <Text numberOfLines={1} style={[typo.caption, { color: colors.onSurfaceVariant }]}>
            {book.author ?? ''}
          </Text>
        </View>
        <IconButton
          name={playing ? 'pause' : 'play'}
          color={colors.primary}
          size={28}
          onPress={togglePlay}
        />
      </View>
      <View style={{ marginTop: spacing.sm }}>
        <ProgressBar progress={progress} height={3} />
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  bar: { paddingHorizontal: spacing.lg, paddingVertical: spacing.md, borderRadius: 16 },
  row: { flexDirection: 'row', alignItems: 'center', gap: spacing.md },
  meta: { flex: 1 },
});
