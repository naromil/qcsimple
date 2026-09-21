package com.naromil.qcsimple.logic;

import com.naromil.qcsimple.data.BlockPalette;
import com.naromil.qcsimple.data.QCComponent;
import com.naromil.qcsimple.geom.Point2D;
import com.naromil.qcsimple.geom.Point3D;
import net.querz.nbt.tag.CompoundTag;
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
 * Golden baseline for the NBT import path: layers -> block map -> root compound tag -> block map -> layers.
 *
 * <p>The test exports the shared fixture, feeds the resulting root compound tag back through
 * {@link UnitBlockConverter#convertBlockMapToLayers}, and compares a sorted dump of the layers it
 * gets back plus the block configuration the import detected against
 * {@code src/test/resources/golden/unit-import-baseline.txt}. A mismatch means the import behavior
 * changed, so the difference must be explained before the baseline is touched. The test never
 * overwrites an existing baseline; it only creates one when the file is missing (and then fails, so
 * the generated file gets reviewed and committed first).
 *
 * <p>Extracted structures are dumped as their size triple and path only: the palette ordering
 * inside an extracted tag is not deterministic and must not be pinned.
 */
public class UnitImportGoldenTest {

    private static final String BASELINE_PATH = "src/test/resources/golden/unit-import-baseline.txt";
    private static final String MISSING_BASELINE_MESSAGE =
            "Baseline was missing and has been (re)generated from the current code — review it, commit it, and re-run to verify.";
    private static final int MAX_DIFF_LINES_PER_SIDE = 40;
    private static final int CONTEXT_LINES = 3;

    @Test
    void importDumpMatchesGoldenBaseline() throws IOException {
        String dump = generateImportDump();

        // Cheap sanity checks: catch an import that silently produced nothing usable.
        long lineCount = dump.lines().count();
        assertTrue(lineCount > 10, "Import dump is suspiciously small: " + lineCount + " line(s)");
        assertTrue(dump.contains("walls=1") && dump.contains("gates=1"), "Import dump is missing the wall and gate flags");
        assertTrue(dump.contains("innerWall=7x7x3") && dump.contains("outerWall=7x7x3") && dump.contains("innerColumn=3x7x3"),
                "Import dump is missing the extracted structures");

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
    // Import + dump
    // ---------------------------------------------------------------------------------------------

    private static String generateImportDump() {
        // Deterministic setup: the built-in defaults, no custom ids and no custom structures.
        BlockPalette palette = BlockPalette.getInstance();
        palette.applyDefaultConfig();

        Map<Point3D, CompoundTag> blockMap = UnitBlockConverter.convertLayersToBlockMap(TestFixtures.buildFixtureLayers(), palette);
        CompoundTag root = BlockNBTConverter.convertMapToTag(blockMap);
        Map<Point3D, CompoundTag> readBack = BlockNBTConverter.convertTagToMap(root);
        Map<Integer, Map<Point2D, QCUnit>> layers = UnitBlockConverter.convertBlockMapToLayers(readBack, palette);

        StringBuilder dump = new StringBuilder();

        // One line per unit, layers ascending and units sorted by (x, z).
        List<Integer> layerKeys = new ArrayList<>(layers.keySet());
        layerKeys.sort(Comparator.naturalOrder());
        for (int layerKey : layerKeys) {
            Map<Point2D, QCUnit> layer = layers.get(layerKey);
            List<Point2D> positions = new ArrayList<>(layer.keySet());
            positions.sort(Comparator.comparingInt(Point2D::x).thenComparingInt(Point2D::z));
            for (Point2D pos : positions) {
                QCUnit unit = layer.get(pos);
                dump.append("L").append(layerKey).append(" U").append(pos.x()).append(',').append(pos.z())
                        .append(" walls=")
                        .append(flag(unit.hasWallN())).append(flag(unit.hasWallW()))
                        .append(flag(unit.hasWallS())).append(flag(unit.hasWallE()))
                        .append(" gates=")
                        .append(flag(unit.isGateN())).append(flag(unit.isGateW()))
                        .append(flag(unit.isGateS())).append(flag(unit.isGateE()))
                        .append('\n');
            }
        }

        // One line per component, in palette order.
        dump.append("framework=").append(palette.frameworkBlock().getBlockId()).append('\n');
        dump.append("row=").append(palette.rowBlock().getBlockId()).append('\n');
        dump.append("column=").append(palette.columnBlock().getBlockId()).append('\n');
        dump.append("floor=").append(palette.floorBlock().getBlockId()).append('\n');
        dump.append("wall=").append(palette.wallBlock().getBlockId()).append('\n');
        dump.append("innerWall=").append(describeStructure(palette.innerWall())).append('\n');
        dump.append("gate=").append(describeStructure(palette.gate())).append('\n');
        dump.append("outerWall=").append(describeStructure(palette.outerWall())).append('\n');
        dump.append("innerColumn=").append(describeStructure(palette.innerColumn())).append('\n');

        return dump.toString();
    }

    private static String describeStructure(QCComponent component) {
        if (component.getTag() == null) {
            return "EMPTY";
        }
        return component.getX() + "x" + component.getY() + "x" + component.getZ() + " " + component.getPath();
    }

    private static String flag(boolean value) {
        return value ? "1" : "0";
    }

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
        message.append("Behavior change detected: the NBT import no longer matches the golden baseline ")
                .append(baseline).append(".\n")
                .append("That baseline pins the layers and block config produced by UnitBlockConverter.convertBlockMapToLayers(...) ")
                .append("for the exported fixture. Do not rewrite the baseline to silence this failure: ")
                .append("either the production code changed behavior, or the baseline was edited by mistake.\n")
                .append("Golden lines: ").append(expected.size()).append(", actual lines: ").append(observed.size())
                .append(" (").append(goldenOnly.size()).append(" golden-only line(s), ")
                .append(actualOnly.size()).append(" actual-only line(s)).\n")
                .append("First difference at line ").append(prefix + 1).append(".\n")
                .append("Unified diff (first difference plus context, capped at ")
                .append(MAX_DIFF_LINES_PER_SIDE).append(" lines per side):\n\n")
                .append("--- ").append(baseline).append(" (golden)\n")
                .append("+++ import dump (actual)\n")
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
