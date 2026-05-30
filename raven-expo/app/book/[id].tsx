import React, { useCallback, useState } from 'react';
import { View, Text, ScrollView, Pressable, StyleSheet, Modal, TextInput, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useFocusEffect, useLocalSearchParams, useRouter } from 'expo-router';
import { repo } from '../../src/db/repo';
import { Book, bookProgress, bookDurationMs, bookPositionMs } from '../../src/data/types';
import { usePlayer } from '../../src/playback/playerStore';
import { useTheme, type as typo, spacing } from '../../src/theme/theme';
import { Cover } from '../../src/components/Cover';
import { ProgressBar } from '../../src/components/ProgressBar';
import { IconButton } from '../../src/components/IconButton';
import { Icon } from '../../src/icons/Icon';
import { MiniPlayer } from '../../src/components/MiniPlayer';
import { formatRemaining, formatTime } from '../../src/lib/format';

export default function BookDetails() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();
  const { colors } = useTheme();
  const loadBook = usePlayer((s) => s.loadBook);
  const [book, setBook] = useState<Book | null>(null);
  const [actions, setActions] = useState(false);
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState('');

  const refresh = useCallback(() => {
    if (id) setBook(repo.getBook(id));
  }, [id]);
  useFocusEffect(refresh);

  if (!book) return <SafeAreaView style={{ flex: 1, backgroundColor: colors.background }} />;

  function playFrom(chapterIndex: number, positionMs: number) {
    loadBook(book!, chapterIndex, positionMs, true);
    router.push('/player');
  }

  function saveTitle() {
    const t = draft.trim();
    if (t) {
      repo.updateTitle(book!.id, t);
      refresh();
    }
    setEditing(false);
  }

  function remove() {
    setActions(false);
    Alert.alert(book!.title, 'Remove this book from your library?', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Remove', style: 'destructive', onPress: () => { repo.deleteBook(book!.id); router.back(); } },
    ]);
  }

  const remaining = bookDurationMs(book) - bookPositionMs(book);

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: colors.background }]} edges={['top']}>
      <View style={styles.topBar}>
        <IconButton name="arrowLeft" onPress={() => router.back()} color={colors.onBackground} />
        <IconButton name="dotsMenu" onPress={() => setActions(true)} color={colors.onBackground} />
      </View>
      <ScrollView contentContainerStyle={{ padding: spacing.xl, paddingBottom: 120 }}>
        <View style={{ aspectRatio: 1, borderRadius: 12, overflow: 'hidden' }}>
          <Cover uri={book.coverUri} />
        </View>

        <View style={styles.titleRow}>
          <View style={{ flex: 1 }}>
            <Text style={[typo.title, { color: colors.onBackground }]}>{book.title}</Text>
            <Text style={[typo.body, { color: colors.onSurfaceVariant }]}>{book.author ?? ''}</Text>
          </View>
          <Pressable
            onPress={() => playFrom(book.currentChapterIndex, book.positionInChapterMs)}
            style={[styles.play, { backgroundColor: colors.primary }]}
          >
            <Icon name="play" size={26} color={colors.onPrimary} />
          </Pressable>
        </View>

        <View style={styles.progressRow}>
          <ProgressBar progress={bookProgress(book)} />
          <Text style={[typo.label, { color: colors.onSurfaceVariant }]}>{formatRemaining(remaining)}</Text>
        </View>

        {!!book.description && (
          <Text style={[typo.body, { color: colors.onBackground, marginTop: spacing.lg }]}>{book.description}</Text>
        )}

        <Text style={[typo.headline, { color: colors.onBackground, marginTop: spacing.xl }]}>Chapters</Text>
        {book.chapters.map((c, i) => {
          const completed = i < book.currentChapterIndex;
          return (
            <Pressable
              key={c.id}
              onPress={() => playFrom(i, 0)}
              style={[styles.chapter, { borderBottomColor: colors.outlineVariant, opacity: completed ? 0.6 : 1 }]}
            >
              <Text style={[typo.bodyLarge, { color: colors.onBackground, flex: 1 }]}>{c.name}</Text>
              <Text style={[typo.label, { color: colors.onSurfaceVariant }]}>{formatTime(c.durationMs)}</Text>
            </Pressable>
          );
        })}
      </ScrollView>

      <View style={styles.mini}>
        <MiniPlayer />
      </View>

      {/* actions sheet */}
      <Modal visible={actions} transparent animationType="fade" onRequestClose={() => setActions(false)}>
        <Pressable style={styles.backdrop} onPress={() => setActions(false)} />
        <View style={[styles.sheet, { backgroundColor: colors.surface }]}>
          <Pressable
            style={styles.actionRow}
            onPress={() => {
              setActions(false);
              setDraft(book.title);
              setEditing(true);
            }}
          >
            <Text style={[typo.bodyLarge, { color: colors.onSurface }]}>Edit title</Text>
          </Pressable>
          <Pressable style={styles.actionRow} onPress={remove}>
            <Text style={[typo.bodyLarge, { color: '#E5484D' }]}>Remove book</Text>
          </Pressable>
        </View>
      </Modal>

      {/* edit title */}
      <Modal visible={editing} transparent animationType="fade" onRequestClose={() => setEditing(false)}>
        <Pressable style={styles.backdrop} onPress={() => setEditing(false)} />
        <View style={[styles.dialog, { backgroundColor: colors.surface }]}>
          <Text style={[typo.headline, { color: colors.onSurface, marginBottom: spacing.md }]}>Edit title</Text>
          <TextInput
            value={draft}
            onChangeText={setDraft}
            autoFocus
            style={[typo.bodyLarge, styles.input, { color: colors.onSurface, borderColor: colors.outline }]}
          />
          <View style={styles.dialogActions}>
            <Pressable onPress={() => setEditing(false)}>
              <Text style={[typo.bodyLarge, { color: colors.onSurfaceVariant }]}>Cancel</Text>
            </Pressable>
            <Pressable onPress={saveTitle}>
              <Text style={[typo.bodyLarge, { color: colors.primary, fontWeight: '600' }]}>Save</Text>
            </Pressable>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  topBar: { flexDirection: 'row', justifyContent: 'space-between', paddingHorizontal: spacing.lg, paddingTop: spacing.sm },
  titleRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, marginTop: spacing.lg },
  play: { width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center' },
  progressRow: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, marginTop: spacing.lg },
  chapter: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    paddingVertical: spacing.lg,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  mini: { position: 'absolute', left: spacing.lg, right: spacing.lg, bottom: spacing.lg },
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
  actionRow: { paddingVertical: spacing.lg },
  dialog: {
    position: 'absolute',
    left: spacing.xl,
    right: spacing.xl,
    top: '35%',
    borderRadius: 16,
    padding: spacing.xl,
  },
  input: { borderWidth: 1, borderRadius: 10, paddingHorizontal: spacing.md, paddingVertical: spacing.sm },
  dialogActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: spacing.xl, marginTop: spacing.lg },
});
