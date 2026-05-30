import React, { useMemo, useState } from 'react';
import { View, Text, TextInput, FlatList, Pressable, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { repo } from '../src/db/repo';
import { Book, bookProgress } from '../src/data/types';
import { useTheme, type as typo, spacing } from '../src/theme/theme';
import { Cover } from '../src/components/Cover';
import { ProgressBar } from '../src/components/ProgressBar';
import { IconButton } from '../src/components/IconButton';

export default function Search() {
  const router = useRouter();
  const { colors } = useTheme();
  const [query, setQuery] = useState('');
  const all = useMemo(() => repo.allBooks(), []);

  const results = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return all;
    return all.filter(
      (b) => b.title.toLowerCase().includes(q) || (b.author ?? '').toLowerCase().includes(q),
    );
  }, [all, query]);

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: colors.background }]} edges={['top']}>
      <View style={styles.header}>
        <IconButton name="arrowLeft" onPress={() => router.back()} color={colors.onBackground} />
        <View style={[styles.field, { backgroundColor: colors.surfaceVariant }]}>
          <TextInput
            autoFocus
            value={query}
            onChangeText={setQuery}
            placeholder="Search title or author"
            placeholderTextColor={colors.onSurfaceVariant}
            style={[typo.body, { color: colors.onSurface, flex: 1 }]}
          />
          {query.length > 0 && (
            <IconButton name="multiply" size={18} color={colors.onSurfaceVariant} onPress={() => setQuery('')} />
          )}
        </View>
      </View>

      <FlatList
        data={results}
        keyExtractor={(b) => b.id}
        contentContainerStyle={{ padding: spacing.xl }}
        keyboardShouldPersistTaps="handled"
        ListEmptyComponent={
          <Text style={[typo.body, { color: colors.onSurfaceVariant }]}>No matching books.</Text>
        }
        renderItem={({ item }) => <Row book={item} onPress={() => router.push(`/book/${item.id}`)} />}
      />
    </SafeAreaView>
  );
}

function Row({ book, onPress }: { book: Book; onPress: () => void }) {
  const { colors } = useTheme();
  return (
    <Pressable onPress={onPress} style={styles.row}>
      <View style={styles.thumb}>
        <Cover uri={book.coverUri} size={56} />
      </View>
      <View style={{ flex: 1 }}>
        <Text numberOfLines={1} style={[typo.bodyLarge, { color: colors.onBackground }]}>{book.title}</Text>
        <Text numberOfLines={1} style={[typo.caption, { color: colors.onSurfaceVariant }]}>{book.author ?? ''}</Text>
        <View style={{ marginTop: spacing.xs }}>
          <ProgressBar progress={bookProgress(book)} height={3} />
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm, paddingHorizontal: spacing.lg, paddingTop: spacing.sm },
  field: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    flex: 1,
    paddingHorizontal: spacing.lg,
    height: 48,
    borderRadius: 999,
  },
  row: { flexDirection: 'row', alignItems: 'center', gap: spacing.md, paddingVertical: spacing.md },
  thumb: { width: 56, height: 56, borderRadius: 8, overflow: 'hidden' },
});
