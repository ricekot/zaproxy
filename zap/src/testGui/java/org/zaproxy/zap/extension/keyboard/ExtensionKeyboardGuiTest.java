/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.keyboard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.testutils.AbstractGuiTest;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.ZapMenuItem;

/** Regression tests for the keyboard operations used by core and add-ons. */
class ExtensionKeyboardGuiTest extends AbstractGuiTest {

    private ExtensionKeyboard extension;

    @BeforeEach
    void setUp() {
        extension = new ExtensionKeyboard();
        extension.getKeyboardParam().load(new ZapXmlConfiguration());
    }

    @Test
    void shouldKeepDefaultAcceleratorWithoutOverride() {
        executeInEdt(
                () -> {
                    // Given
                    KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    ZapMenuItem menu = new ZapMenuItem("menu.example", "Example", shortcut);
                    // When
                    extension.registerMenuItem(menu);
                    // Then
                    assertThat(menu.getAccelerator(), is(shortcut));
                    assertThat(extension.getShortcut("menu.example"), is(shortcut));
                });
    }

    @Test
    void shouldApplySavedOverrideWithoutChangingDefaultAccelerator() {
        executeInEdt(
                () -> {
                    // Given
                    KeyStroke defaultShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    KeyStroke customShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
                    extension.getKeyboardParam().setShortcut("menu.example", customShortcut);
                    ZapMenuItem menu = new ZapMenuItem("menu.example", "Example", defaultShortcut);
                    // When
                    extension.registerMenuItem(menu);
                    // Then
                    assertThat(menu.getAccelerator(), is(customShortcut));
                    assertThat(menu.getDefaultAccelerator(), is(defaultShortcut));
                    assertThat(extension.getShortcut("menu.example"), is(customShortcut));
                });
    }

    @Test
    void shouldApplyPersistedExplicitlyClearedShortcut() {
        executeInEdt(
                () -> {
                    // Given
                    extension.getKeyboardParam().setShortcut("menu.example", null);
                    extension.getKeyboardParam().setConfigs();
                    extension.getKeyboardParam().load(extension.getKeyboardParam().getConfig());
                    ZapMenuItem menu =
                            new ZapMenuItem(
                                    "menu.example",
                                    "Example",
                                    KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
                    // When
                    extension.registerMenuItem(menu);
                    // Then
                    assertThat(menu.getAccelerator(), is(nullValue()));
                    assertThat(extension.getShortcut("menu.example"), is(nullValue()));
                });
    }

    @Test
    void shouldConfigureMenuWithoutDefaultAccelerator() {
        executeInEdt(
                () -> {
                    // Given
                    ZapMenuItem menu = new ZapMenuItem("menu.example", "Example", null);
                    extension.registerMenuItem(menu);
                    KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    // When
                    extension.setShortcut("menu.example", shortcut);
                    // Then
                    assertThat(menu.getAccelerator(), is(shortcut));
                    assertThat(
                            extension.getKeyboardParam().getShortcut("menu.example"), is(shortcut));
                    assertThat(menu.getDefaultAccelerator(), is(nullValue()));
                });
    }

    @Test
    void shouldApplyBindingToRecreatedMenuAndUpdateOnlyReplacement() {
        executeInEdt(
                () -> {
                    // Given
                    ZapMenuItem original = new ZapMenuItem("panel.class.Name", "Original", null);
                    extension.registerMenuItem(original);
                    KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    extension.setShortcut("panel.class.Name", shortcut);
                    ZapMenuItem replacement = new ZapMenuItem("panel.class.Name", "Renamed", null);
                    // When
                    extension.registerMenuItem(replacement);
                    // Then
                    assertThat(replacement.getAccelerator(), is(shortcut));
                    // When
                    KeyStroke updatedShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
                    extension.setShortcut("panel.class.Name", updatedShortcut);
                    // Then
                    assertThat(replacement.getAccelerator(), is(updatedShortcut));
                    assertThat(original.getAccelerator(), is(shortcut));
                    assertThat(extension.getShortcut("panel.class.Name"), is(updatedShortcut));
                });
    }

    @Test
    void shouldTransferShortcutBetweenMenus() {
        executeInEdt(
                () -> {
                    // Given
                    KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    ZapMenuItem source = new ZapMenuItem("menu.source", "Source", shortcut);
                    ZapMenuItem target = new ZapMenuItem("menu.target", "Target", null);
                    extension.registerMenuItem(source);
                    extension.registerMenuItem(target);
                    // When (the sequence used by the encoder add-on)
                    KeyStroke existing = extension.getShortcut("menu.source");
                    extension.setShortcut("menu.source", null);
                    extension.setShortcut("menu.target", existing);
                    extension.getKeyboardParam().setConfigs();
                    // Then
                    assertThat(source.getAccelerator(), is(nullValue()));
                    assertThat(target.getAccelerator(), is(shortcut));
                    KeyboardParam reloaded = new KeyboardParam();
                    reloaded.load(extension.getKeyboardParam().getConfig());
                    assertThat(
                            reloaded.getShortcut("menu.source"), is(KeyStroke.getKeyStroke(0, 0)));
                    assertThat(reloaded.getShortcut("menu.target"), is(shortcut));
                });
    }
}
