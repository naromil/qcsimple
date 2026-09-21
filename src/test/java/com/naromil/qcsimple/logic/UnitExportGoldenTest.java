package com.naromil.qcsimple.logic;

import com.naromil.qcsimple.data.BlockPalette;
import com.naromil.qcsimple.geom.Point3D;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Golden baseline for the NBT export path: layers -> block map -> root compound tag.
 *
 * <p>The test exports a fixed fixture through {@link UnitBlockConverter#convertLayersToBlockMap}
 * and {@link BlockNBTConverter#convertMapToTag}, renders the resulting blocks as sorted
 * {@code "x y z blockName"} text and compares it against {@code src/test/resources/golden/unit-export-baseline.txt}.
 * A mismatch means the export behavior changed, so the difference must be explained before the
 * baseline is touched. The test never overwrites an existing baseline; it only creates one when the
 * file is missing (and then fails, so the generated file gets reviewed and committed first).
 */
public class UnitExportGoldenTest {

    private static final String BASELINE_PATH = "src/test/resources/golden/unit-export-baseline.txt";
    private static final String MISSING_BASELINE_MESSAGE =
            "Baseline was missing and has been (re)generated from the current code — review it, commit it, and re-run to verify.";
    private static final int MAX_DIFF_LINES_PER_SIDE = 40;
    private static final int CONTEXT_LINES = 3;

    @Test
    void exportDumpMatchesGoldenBaseline() throws IOException {
        String dump = generateExportDump();

        // Cheap sanity checks: catch an export that silently produced nothing usable.
        long lineCount = dump.lines().count();
        assertTrue(lineCount > 1000, "Export dump is suspiciously small: " + lineCount + " line(s)");
        assertTrue(dump.contains("minecraft:polished_deepslate"), "Export dump is missing the framework blocks");
        assertTrue(dump.contains("minecraft:deepslate_bricks"), "Export dump is missing the inner/outer wall and gate blocks");

        Path baseline = baselinePath();
        if (!Files.exists(baseline)) {
            Files.createDirectories(baseline.getParent());
            Files.writeString(baseline, dump, StandardCharsets.UTF_8);
            fail(MISSING_BASELINE_MESSAGE);
        }

        String golden = Files.readString(baseline, StandardCharsets.UTF_8);
        if (!toLines(golden).equals(toLines(dump))) {
            fail(describeBehaviorChange(baseline, golden, dump));
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Export + dump
    // ---------------------------------------------------------------------------------------------

    private static String generateExportDump() {
        // Deterministic setup: the built-in defaults, no custom ids and no custom structures.
        BlockPalette.getInstance().applyDefaultConfig();

        Map<Point3D, CompoundTag> blockMap = UnitBlockConverter.convertLayersToBlockMap(TestFixtures.buildFixtureLayers());
        CompoundTag root = BlockNBTConverter.convertMapToTag(blockMap);

        ListTag<CompoundTag> palette = root.getListTag("palette").asCompoundTagList();
        ListTag<CompoundTag> blocks = root.getListTag("blocks").asCompoundTagList();

        List<ExportedBlock> exported = new ArrayList<>(blocks.size());
        for (CompoundTag block : blocks) {
            ListTag<IntTag> pos = block.getListTag("pos").asIntTagList();
            CompoundTag state = palette.get(block.getInt("state"));
            exported.add(new ExportedBlock(pos.get(0).asInt(), pos.get(1).asInt(), pos.get(2).asInt(), state.getString("Name")));
        }

        // Sort explicitly: the dump must not depend on hash map iteration order.
        exported.sort(Comparator.comparingInt(ExportedBlock::x)
                .thenComparingInt(ExportedBlock::y)
                .thenComparingInt(ExportedBlock::z)
                .thenComparing(ExportedBlock::name));

        StringBuilder dump = new StringBuilder();
        for (ExportedBlock block : exported) {
            dump.append(block.x()).append(' ')
                    .append(block.y()).append(' ')
                    .append(block.z()).append(' ')
                    .append(block.name()).append('\n');
        }
        return dump.toString();
    }

    private record ExportedBlock(int x, int y, int z, String name) {}

    // ---------------------------------------------------------------------------------------------
    // Baseline handling
    // ---------------------------------------------------------------------------------------------

    private static Path baselinePath() {
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null && !Files.exists(directory.resolve("settings.gradle"))) {
            directory = directory.getParent();
        }
        Path root = directory != null ? directory : Path.of("").toAbsolutePath();
        return root.resolve(BASELINE_PATH);
    }

    private static List<String> toLines(String text) {
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.endsWith("\n")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return List.of(normalized.split("\n", -1));
    }

    private static String describeBehaviorChange(Path baseline, String golden, String actual) {
        List<String> expected = toLines(golden);
        List<String> observed = toLines(actual);

        int prefix = 0;
        while (prefix < expected.size() && prefix < observed.size() && expected.get(prefix).equals(observed.get(prefix))) {
            prefix++;
        }
        int suffix = 0;
        while (suffix < expected.size() - prefix && suffix < observed.size() - prefix
                && expected.get(expected.size() - 1 - suffix).equals(observed.get(observed.size() - 1 - suffix))) {
            suffix++;
        }

        int expectedChangedEnd = expected.size() - suffix;
        int observedChangedEnd = observed.size() - suffix;
        int expectedChangedCount = expectedChangedEnd - prefix;
        int observedChangedCount = observedChangedEnd - prefix;

        List<String> goldenOnly = multisetDifference(expected, observed);
        List<String> actualOnly = multisetDifference(observed, expected);

        int contextBefore = Math.min(CONTEXT_LINES, prefix);
        int contextAfter = Math.min(CONTEXT_LINES, suffix);

        StringBuilder message = new StringBuilder();
        message.append("Behavior change detected: the NBT export no longer matches the golden baseline ")
                .append(baseline).append(".\n")
                .append("That baseline pins the export of the default block config through UnitBlockConverter.convertLayersToBlockMap(...) ")
                .append("and BlockNBTConverter.convertMapToTag(...). Do not rewrite the baseline to silence this failure: ")
                .append("either the production code changed behavior, or the baseline was edited by mistake.\n")
                .append("Golden lines: ").append(expected.size()).append(", actual lines: ").append(observed.size())
                .append(" (").append(goldenOnly.size()).append(" golden-only line(s), ")
                .append(actualOnly.size()).append(" actual-only line(s)).\n")
                .append("First difference at line ").append(prefix + 1).append(".\n")
                .append("Unified diff (first difference plus context, capped at ")
                .append(MAX_DIFF_LINES_PER_SIDE).append(" lines per side):\n\n")
                .append("--- ").append(baseline).append(" (golden)\n")
                .append("+++ export dump (actual)\n")
                .append("@@ -").append(prefix - contextBefore + 1).append(',')
                .append(contextBefore + expectedChangedCount + contextAfter)
                .append(" +").append(prefix - contextBefore + 1).append(',')
                .append(contextBefore + observedChangedCount + contextAfter)
                .append(" @@\n");

        for (int i = prefix - contextBefore; i < prefix; i++) {
            message.append(' ').append(expected.get(i)).append('\n');
        }

        int shownExpected = Math.min(expectedChangedCount, MAX_DIFF_LINES_PER_SIDE);
        for (int i = 0; i < shownExpected; i++) {
            message.append('-').append(expected.get(prefix + i)).append('\n');
        }
        int shownObserved = Math.min(observedChangedCount, MAX_DIFF_LINES_PER_SIDE);
        for (int i = 0; i < shownObserved; i++) {
            message.append('+').append(observed.get(prefix + i)).append('\n');
        }
        if (expectedChangedCount > shownExpected || observedChangedCount > shownObserved) {
            message.append("... diff truncated: ")
                    .append(expectedChangedCount - shownExpected).append(" more golden-only line(s), ")
                    .append(observedChangedCount - shownObserved).append(" more actual-only line(s)\n");
        }

        for (int i = observedChangedEnd; i < observedChangedEnd + contextAfter; i++) {
            message.append(' ').append(observed.get(i)).append('\n');
        }

        return message.toString();
    }

    private static List<String> multisetDifference(List<String> from, List<String> to) {
        Map<String, Integer> counts = new HashMap<>();
        for (String line : to) {
            counts.merge(line, 1, Integer::sum);
        }
        List<String> difference = new ArrayList<>();
        for (String line : from) {
            int remaining = counts.getOrDefault(line, 0);
            if (remaining == 0) {
                difference.add(line);
            } else {
                counts.put(line, remaining - 1);
            }
        }
        return difference;
    }
}
