import React, { useCallback, useMemo, useState } from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect, useRouter } from 'expo-router';
import { repo } from '../src/db/repo';
import { usePlayer } from '../src/playback/playerStore';
import { HistoryEvent, HistoryAction } from '../src/data/types';
import { useTheme, type as typo, spacing } from '../src/theme/theme';
import { IconButton } from '../src/components/IconButton';
import { formatTime, relativeDay } from '../src/lib/format';

const LABEL: Record<HistoryAction, string> = {
  Played: 'Played',
  Paused: 'Paused',
  Jumped: 'Jumped',
  SkippedToChapter: 'Skipped to chapter',
  NewChapter: 'New chapter',
  SleepTimer: 'Sleep timer',
};

export default function History() {
  const router = useRouter();
  const { colors } = useTheme();
  const book = usePlayer((s) => s.book);
  const [events, setEvents] = useState<HistoryEvent[]>([]);

  useFocusEffect(
    useCallback(() => {
      if (book) setEvents(repo.historyFor(book.id));
    }, [book]),
  );

  const groups = useMemo(() => {
    const map = new Map<string, HistoryEvent[]>();
    for (const e of events) {
      const key = relativeDay(e.createdAt);
      const arr = map.get(key) ?? [];
      arr.push(e);
      map.set(key, arr);
    }
    return Array.from(map.entries());
  }, [events]);

  function chapterName(chapterId: string) {
    return book?.chapters.find((c) => c.id === chapterId)?.name ?? '';
  }

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: colors.background }]} edges={['top']}>
      <View style={styles.topBar}>
        <IconButton name="multiply" onPress={() => router.back()} color={colors.onBackground} />
        <Text style={[typo.title, { color: colors.onBackground }]}>History</Text>
        <View style={{ width: 24 }} />
      </View>

      <ScrollView contentContainerStyle={{ padding: spacing.xl }}>
        {groups.length === 0 && (
          <Text style={[typo.body, { color: colors.onSurfaceVariant }]}>No history yet.</Text>
        )}
        {groups.map(([day, items]) => {
          const listenedMin = Math.round(items.reduce((s, e) => s + e.listenedMs, 0) / 60000);
          return (
            <View key={day} style={{ marginBottom: spacing.lg }}>
              <View style={styles.dayHeader}>
                <Text style={[typo.label, { color: colors.onBackground }]}>{day}</Text>
                <Text style={[typo.caption, { color: colors.onSurfaceVariant }]}>
                  {listenedMin} min listened
                </Text>
              </View>
              {items.map((e) => (
                <View key={e.id} style={[styles.item, { backgroundColor: colors.surfaceVariant }]}>
                  <Text style={[typo.label, { color: colors.onSurface }]}>
                    {chapterName(e.chapterId)} — {formatTime(e.positionInChapterMs)}
                  </Text>
                  <Text style={[typo.caption, { color: colors.onSurfaceVariant }]}>{LABEL[e.action]}</Text>
                </View>
              ))}
            </View>
          );
        })}
      </ScrollView>
    </SafeAreaView>
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
  dayHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: spacing.sm },
  item: { borderRadius: 12, padding: spacing.lg, marginBottom: spacing.sm },
});
