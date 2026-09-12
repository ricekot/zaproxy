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
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.zaproxy.testutils.AbstractGuiTest;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.ZapMenuItem;

class KeyboardShortcutProviderGuiTest extends AbstractGuiTest {

    private ExtensionKeyboard extension;
    private KeyboardShortcutProvider provider;
    private ViewDelegate view;
    private JMenu menu;
    private Model model;
    private I18N previousMessages;
    private ApiImplementor previousKeyboardApi;

    @BeforeEach
    void setUp() {
        previousMessages = Constant.messages;
        Constant.messages = mock(I18N.class);
        when(Constant.messages.getString(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        model = mock(Model.class);
        OptionsParam options = spy(new OptionsParam());
        when(options.getConfig()).thenReturn(new ZapXmlConfiguration());
        when(model.getOptionsParam()).thenReturn(options);
        view = mock(ViewDelegate.class, RETURNS_DEEP_STUBS);
        extension =
                new ExtensionKeyboard() {
                    @Override
                    public ViewDelegate getView() {
                        return view;
                    }

                    @Override
                    protected boolean hasView() {
                        return true;
                    }
                };
        extension.initModel(model);
        provider = mock(KeyboardShortcutProvider.class);
        executeInEdt(
                () -> {
                    previousKeyboardApi = API.getInstance().getImplementors().get("keyboard");
                    if (previousKeyboardApi != null) {
                        API.getInstance().removeApiImplementor(previousKeyboardApi);
                    }
                    menu = new JMenu("Test");
                    when(view.getMainFrame().getMainMenuBar().getMenuCount()).thenReturn(1);
                    when(view.getMainFrame().getMainMenuBar().getMenu(0)).thenReturn(menu);
                    ExtensionHook hook = spy(new ExtensionHook(model, view));
                    doAnswer(
                                    invocation -> {
                                        API.getInstance()
                                                .registerApiImplementor(invocation.getArgument(0));
                                        return null;
                                    })
                            .when(hook)
                            .addApiImplementor(any());
                    extension.hook(hook);
                    hook.getOptionsParamSetList().forEach(model.getOptionsParam()::addParamSet);
                });
    }

    @AfterEach
    void restoreGlobals() {
        try {
            executeInEdt(
                    () -> {
                        var api = API.getInstance().getImplementors().get("keyboard");
                        if (api != null) {
                            API.getInstance().removeApiImplementor(api);
                        }
                        if (previousKeyboardApi != null) {
                            API.getInstance().registerApiImplementor(previousKeyboardApi);
                        }
                    });
        } finally {
            Constant.messages = previousMessages;
        }
    }

    @Test
    void shouldSuspendLegacyResourcesBeforeStartingProvider() {
        doAnswer(
                        invocation -> {
                            assertThat(
                                    API.getInstance().getImplementors().get("keyboard"),
                                    is(nullValue()));
                            assertThat(
                                    model.getOptionsParam().getParamSet(KeyboardParam.class),
                                    is(nullValue()));
                            verify(view.getOptionsDialog())
                                    .removeParamPanel(any(OptionsKeyboardShortcutPanel.class));
                            return null;
                        })
                .when(provider)
                .start();

        executeInEdt(() -> extension.setShortcutProvider(provider));

        verify(provider).start();
    }

    @Test
    void shouldForwardLegacyOperationsToProvider() {
        executeInEdt(
                () -> {
                    extension.setShortcutProvider(provider);
                    KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    ZapMenuItem item = new ZapMenuItem("example", "Example", null);
                    List<KeyboardShortcut> shortcuts = new ArrayList<>();
                    when(provider.getShortcut("example")).thenReturn(shortcut);
                    when(provider.getShortcuts(false)).thenReturn(shortcuts);
                    when(provider.getShortcuts(true)).thenReturn(shortcuts);

                    extension.registerMenuItem(item);
                    extension.setShortcut("example", shortcut);

                    verify(provider).registerMenuItem(item);
                    verify(provider).setShortcut("example", shortcut);
                    assertThat(extension.getShortcut("example"), is(shortcut));
                    assertThat(extension.getShortcuts(), is(sameInstance(shortcuts)));
                    assertThat(extension.getShortcuts(true), is(sameInstance(shortcuts)));
                });
    }

    @Test
    void shouldNotStartSameProviderTwiceOrReplaceAnotherProvider() {
        executeInEdt(
                () -> {
                    extension.setShortcutProvider(provider);
                    extension.setShortcutProvider(provider);
                    assertThrows(
                            IllegalStateException.class,
                            () ->
                                    extension.setShortcutProvider(
                                            mock(KeyboardShortcutProvider.class)));
                });
        verify(provider, times(1)).start();
    }

    @Test
    void shouldRestoreLegacyUsingCurrentConfigurationAndMenus() {
        executeInEdt(
                () -> {
                    extension.setShortcutProvider(provider);
                    KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
                    doAnswer(
                                    invocation -> {
                                        var config = model.getOptionsParam().getConfig();
                                        config.setProperty(
                                                "keyboard.shortcuts(0).menu", "late.menu");
                                        config.setProperty(
                                                "keyboard.shortcuts(0).keycode",
                                                shortcut.getKeyCode());
                                        config.setProperty("keyboard.shortcuts(0).modifiers", 0);
                                        return null;
                                    })
                            .when(provider)
                            .stop();
                    ZapMenuItem item = new ZapMenuItem("late.menu", "Late", null);
                    menu.add(item);

                    extension.setShortcutProvider(null);
                    extension.setShortcutProvider(null);

                    verify(provider, times(1)).stop();
                    assertThat(item.getAccelerator(), is(shortcut));
                    assertThat(extension.getShortcut("late.menu"), is(shortcut));
                    assertThat(
                            model.getOptionsParam().getParamSet(KeyboardParam.class),
                            is(sameInstance(extension.getKeyboardParam())));
                    assertThat(
                            API.getInstance().getImplementors().get("keyboard")
                                    instanceof KeyboardAPI,
                            is(true));
                });
    }

    @Test
    void shouldPersistLegacyOverridesBeforeHandover() {
        executeInEdt(
                () -> {
                    KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
                    ZapMenuItem item = new ZapMenuItem("example", "Example", null);
                    extension.registerMenuItem(item);
                    extension.setShortcut("example", shortcut);

                    extension.setShortcutProvider(provider);

                    KeyboardParam saved = new KeyboardParam();
                    saved.load(model.getOptionsParam().getConfig());
                    assertThat(saved.getShortcut("example"), is(shortcut));
                });
    }

    @Test
    void shouldRestoreLegacyAndPropagateProviderStartupFailure() {
        RuntimeException failure = new IllegalStateException("Failed to start");
        doThrow(failure).when(provider).start();
        executeInEdt(
                () -> {
                    assertThat(
                            assertThrows(
                                    RuntimeException.class,
                                    () -> extension.setShortcutProvider(provider)),
                            is(sameInstance(failure)));
                    assertThat(
                            API.getInstance().getImplementors().get("keyboard")
                                    instanceof KeyboardAPI,
                            is(true));
                    assertThat(
                            model.getOptionsParam().getParamSet(KeyboardParam.class),
                            is(sameInstance(extension.getKeyboardParam())));
                });
        verify(provider).stop();
    }

    @Test
    void shouldRejectReplacementOffEdt() {
        assertThrows(IllegalStateException.class, () -> extension.setShortcutProvider(provider));
    }

    @Test
    void shouldRejectReplacementWithoutGui() {
        executeInEdt(
                () ->
                        assertThrows(
                                IllegalStateException.class,
                                () -> new ExtensionKeyboard().setShortcutProvider(provider)));
    }
}
