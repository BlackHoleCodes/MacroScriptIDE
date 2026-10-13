package com.blackholecodes.macroscript.ide;

import com.blackholecodes.macroscript.ide.editor.Keywords;
import com.blackholecodes.macroscript.mapping.MappingLoader;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The editor's keywords come from the grammar. They must match the reserved words of the mapping. */
class KeywordsTest {

    @Test
    void grammarKeywordsAreTheReservedWordsOfTheLanguage() {
        assertEquals(new HashSet<>(MappingLoader.loadDefault().reservedWords()), new HashSet<>(Keywords.all()));
    }
}
