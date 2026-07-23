package com.ourgram.kubex.compiler;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KubeXCompilerTest {
    private final KubeXCompiler compiler = new KubeXCompiler();

    @Test
    void rewritesBundledPackageRequires() {
        String source = """
            (function () {
              var __require = function (x) {
                if (typeof require !== "undefined") return require.apply(this, arguments);
                throw Error('Dynamic require of "' + x + '" is not supported');
              };
              var import_item = __require("@package/net/minecraft/world/item");
              var import_util = __require("@package/com/example/util");
              import_item.$Items.APPLE.getDefaultInstance();
              import_util.$PlayerExtensionsKt.party(player);
            })();
        """;
        String output = compiler.compile("server_scripts.js", source).outputSource();

        assertTrue(output.contains("Java.loadClass(\"net.minecraft.world.item.Items\").APPLE.getDefaultInstance();"));
        assertTrue(output.contains("Java.loadClass(\"com.example.util.PlayerExtensionsKt\").party(player);"));
        assertFalse(output.contains("__require"));
        assertFalse(output.contains("typeof require"));
    }

    @Test
    void rewritesPackageRequiresInsideArrayLiterals() {
        String source = """
            (function () {
              var import_stats = __require("@package/com/cobblemon/mod/common/api/pokemon/stats");
              var STATS = [import_stats.$Stats.HP, import_stats.$Stats.ATTACK];
            })();
        """;
        String output = compiler.compile("server_scripts.js", source).outputSource();

        assertTrue(output.contains("Java.loadClass(\"com.cobblemon.mod.common.api.pokemon.stats.Stats\").HP"));
        assertTrue(output.contains("Java.loadClass(\"com.cobblemon.mod.common.api.pokemon.stats.Stats\").ATTACK"));
        assertFalse(output.contains("import_stats.$Stats"));
    }

    @Test
    void rewritesPackageRequiresInsideNestedMemberChains() {
        String source = """
            (function () {
              var import_util = __require("@package/com/cobblemon/mod/common/util");
              var party = import_util.$PlayerExtensionsKt.party(player);
            })();
        """;
        String output = compiler.compile("server_scripts.js", source).outputSource();

        assertTrue(output.contains("Java.loadClass(\"com.cobblemon.mod.common.util.PlayerExtensionsKt\").party(player);"));
        assertFalse(output.contains("import_util.$PlayerExtensionsKt"));
    }

    @Test
    void doesNotRewritePackageMarkersInsideStrings() {
        String source = """
            (function () {
              var import_util = __require("@package/com/cobblemon/mod/common/util");
              var message = "import_util.$PlayerExtensionsKt should stay text";
              return message;
            })();
        """;
        String output = compiler.compile("server_scripts.js", source).outputSource();

        assertTrue(output.contains("\"import_util.$PlayerExtensionsKt should stay text\""));
        assertFalse(output.contains("Java.loadClass(\"com.cobblemon.mod.common.util.PlayerExtensionsKt\").should"));
    }

    @Test
    void leavesNonPackageRequiresUntouched() {
        String source = """
            (function () {
              var __require = function (x) {
                if (typeof require !== "undefined") return require.apply(this, arguments);
                throw Error('Dynamic require of "' + x + '" is not supported');
              };
              var fs = __require("node:fs");
              return fs.readFileSync(path);
            })();
        """;
        String output = compiler.compile("server_scripts.js", source).outputSource();

        assertTrue(output.contains("var fs = __require(\"node:fs\");"));
    }

    @Test
    void failsLoudlyWhenRhinoSyntaxCannotBeParsed() {
        String source = """
            (function () {
              var broken = ;
            })();
        """;

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> compiler.compile("server_scripts.js", source)
        );

        assertTrue(exception.getMessage().contains("Failed to lower Rhino syntax"));
        assertTrue(exception.getMessage().contains("at "));
    }

    @Test
    void rewritesBabelLoopHelpersIntoPerIterationIifes() {
        String source = """
            (function () {
              var _loop = function _loop() {
                var pokemon = partyPokemon[slot];
                gui.button(slot + 1, 1, pokemon, function (click) {
                  return pokemon.getDisplayName(true);
                });
              };
              for (var slot = 0; slot < partyPokemon.length; slot++) {
                _loop();
              }
            })();
        """;
        String output = compiler.compile("server_scripts.js", source).outputSource();

        assertFalse(output.contains("var _loop = function _loop()"));
        assertTrue(output.contains("for (var slot = 0; slot < partyPokemon.length; slot++) {"));
        assertTrue(output.contains("(function(slot) {"));
        assertTrue(output.contains("var pokemon = partyPokemon[slot];"));
        assertTrue(output.contains("})(slot);"));
    }

    @Test
    void wrapsCallbackFunctionsInDebugMode() {
        String source = """
            (function () {
              gui.button(1, 1, item, function (click) {
                player.tell("ok");
              });
            })();
        """;
        String output = compiler.compile("server_scripts.js", source, new CompileOptions(true, null)).outputSource();

        assertTrue(output.contains("var __kubexReport"));
        assertTrue(output.contains("try {"));
        assertTrue(output.contains("__kubexReport(e, { scriptGroup: \"server_scripts\", file: \"server_scripts.js\""));
        assertTrue(output.contains("throw e;"));
    }

    @Test
    void skipsWrappingCallbacksThatAlreadyHaveTryCatch() {
        String source = """
            (function () {
              gui.button(1, 1, item, function (click) {
                try {
                  player.tell("ok");
                } catch (e) {
                  player.tell(String(e));
                }
              });
            })();
        """;
        String output = compiler.compile("server_scripts.js", source, new CompileOptions(true, null)).outputSource();

        assertFalse(output.contains("var __kubexReport"));
        assertFalse(output.contains("throw e;"));
    }
}
