import React, { useCallback, useState } from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect, useRouter } from 'expo-router';
import { repo } from '../src/db/repo';
import { Book, bookProgress, bookDurationMs, bookPositionMs } from '../src/data/types';
import { useTheme, type as typo, spacing } from '../src/theme/theme';
import { Cover } from '../src/components/Cover';
import { ProgressBar } from '../src/components/ProgressBar';
import { IconButton } from '../src/components/IconButton';
import { MiniPlayer } from '../src/components/MiniPlayer';
import { Icon } from '../src/icons/Icon';
import { formatRemaining } from '../src/lib/format';

const COMPLETE = 0.995;

export default function Library() {
  const { colors } = useTheme();
  const router = useRouter();
  const [books, setBooks] = useState<Book[]>([]);

  const refresh = useCallback(() => setBooks(repo.allBooks()), []);
  useFocusEffect(refresh);

  function confirmDelete(book: Book) {
    Alert.alert(book.title, 'Remove this book from your library?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Remove',
        style: 'destructive',
        onPress: () => {
          repo.deleteBook(book.id);
          refresh();
        },
      },
    ]);
  }

  const inProgress = books.filter((b) => {
    const p = bookProgress(b);
    return p > 0 && p < COMPLETE;
  });
  const notStarted = books.filter((b) => bookProgress(b) === 0);
  const completed = books.filter((b) => bookProgress(b) >= COMPLETE);

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: colors.background }]} edges={['top']}>
      <View style={styles.header}>
        <Pressable
          onPress={() => router.push('/search')}
          style={[styles.search, { backgroundColor: colors.surfaceVariant }]}
        >
          <Icon name="search" size={20} color={colors.onSurfaceVariant} />
          <Text style={[typo.body, { color: colors.onSurfaceVariant, flex: 1 }]}>Search</Text>
          <IconButton name="settings" size={22} color={colors.onSurfaceVariant} onPress={() => router.push('/settings')} />
        </Pressable>
      </View>

      {books.length === 0 ? (
        <View style={styles.empty}>
          <Icon name="folder" size={48} color={colors.onSurfaceVariant} />
          <Text style={[typo.headline, { color: colors.onBackground, marginTop: spacing.lg }]}>
            Add your first audiobook
          </Text>
          <Text style={[typo.body, { color: colors.onSurfaceVariant, textAlign: 'center', marginTop: spacing.sm }]}>
            Import a folder of audio files from your device.
          </Text>
          <Pressable onPress={() => router.push('/import')} style={[styles.cta, { backgroundColor: colors.primary }]}>
            <Icon name="folder" size={20} color={colors.onPrimary} />
            <Text style={[typo.bodyLarge, { color: colors.onPrimary, fontWeight: '600' }]}>Scan Folder</Text>
          </Pressable>
        </View>
      ) : (
        <ScrollView contentContainerStyle={{ paddingBottom: 120 }} showsVerticalScrollIndicator={false}>
          <Section title="In Progress" books={inProgress} showProgress onDelete={confirmDelete} />
          <Section title="Not started" books={notStarted} onDelete={confirmDelete} />
          <Section title="Completed" books={completed} onDelete={confirmDelete} />
        </ScrollView>
      )}

      <Pressable onPress={() => router.push('/import')} style={[styles.fab, { backgroundColor: colors.primary }]}>
        <Icon name="plus" size={28} color={colors.onPrimary} />
      </Pressable>

      <View style={styles.mini}>
        <MiniPlayer />
      </View>
    </SafeAreaView>
  );
}

function Section({
  title,
  books,
  showProgress,
  onDelete,
}: {
  title: string;
  books: Book[];
  showProgress?: boolean;
  onDelete: (b: Book) => void;
}) {
  const { colors } = useTheme();
  const router = useRouter();
  if (books.length === 0) return null;
  return (
    <View style={{ marginTop: spacing.xl }}>
      <Text style={[typo.headline, { color: colors.onBackground, paddingHorizontal: spacing.xl }]}>{title}</Text>
      <View style={styles.grid}>
        {books.map((b) => {
          const remaining = bookDurationMs(b) - bookPositionMs(b);
          return (
            <Pressable
              key={b.id}
              style={styles.card}
              onPress={() => router.push(`/book/${b.id}`)}
              onLongPress={() => onDelete(b)}
            >
              <View style={styles.cardCover}>
                <Cover uri={b.coverUri} />
              </View>
              <Text numberOfLines={2} style={[typo.label, { color: colors.onBackground, marginTop: spacing.sm }]}>
                {b.title}
              </Text>
              <Text numberOfLines={1} style={[typo.caption, { color: colors.onSurfaceVariant }]}>
                {b.author ?? ''}
              </Text>
              {showProgress && (
                <View style={styles.progressRow}>
                  <ProgressBar progress={bookProgress(b)} />
                  <Text style={[typo.caption, { color: colors.onSurfaceVariant }]}>
                    {formatRemaining(remaining)}
                  </Text>
                </View>
              )}
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  header: { paddingHorizontal: spacing.xl, paddingTop: spacing.sm },
  search: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    paddingHorizontal: spacing.lg,
    height: 48,
    borderRadius: 999,
  },
  grid: { flexDirection: 'row', flexWrap: 'wrap', paddingHorizontal: spacing.xl - 6, marginTop: spacing.md },
  card: { width: '33.333%', paddingHorizontal: 6, marginBottom: spacing.lg },
  cardCover: { aspectRatio: 1, borderRadius: 8, overflow: 'hidden' },
  progressRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, marginTop: spacing.xs },
  empty: { flex: 1, alignItems: 'center', justifyContent: 'center', paddingHorizontal: spacing.xxl },
  cta: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
    borderRadius: 999,
    marginTop: spacing.xl,
  },
  fab: {
    position: 'absolute',
    right: spacing.xl,
    bottom: 96,
    width: 56,
    height: 56,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 4,
  },
  mini: { position: 'absolute', left: spacing.lg, right: spacing.lg, bottom: spacing.lg },
});
